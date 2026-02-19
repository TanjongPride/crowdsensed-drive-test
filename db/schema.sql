-- =====================================================
-- CrowdSenseDDT  –  Full Schema v3 with PostGIS
-- Run once:  psql -U postgres -d test_db -f schema.sql
-- Requires:  CREATE EXTENSION postgis; (see below)
-- =====================================================

-- PostGIS extension (must be installed on your PostgreSQL)
-- On Render/Railway it is pre-installed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;

-- ── ENUM types ────────────────────────────────────────
DO $$ BEGIN CREATE TYPE user_role       AS ENUM ('admin','contributor','tester');  EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE mobility_type   AS ENUM ('walking','driving','static');    EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE upload_log_status AS ENUM ('success','retry','fail');      EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE hole_status     AS ENUM ('candidate','confirmed','fixed'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ── 1. users ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       user_role NOT NULL DEFAULT 'contributor',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── 2. devices ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS devices (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    manufacturer VARCHAR(100),
    model        VARCHAR(100),
    os_version   VARCHAR(50),
    app_version  VARCHAR(50),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── 3. measurement_sessions ──────────────────────────
CREATE TABLE IF NOT EXISTS measurement_sessions (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id       UUID NOT NULL REFERENCES users(id),
    device_id     UUID NOT NULL REFERENCES devices(id),
    start_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time      TIMESTAMP,
    mobility_type mobility_type,
    total_samples INT DEFAULT 0,
    uploaded      BOOLEAN DEFAULT FALSE
);

-- ── 4. network_measurements (with PostGIS geometry) ──
CREATE TABLE IF NOT EXISTS network_measurements (
    id                BIGSERIAL PRIMARY KEY,
    session_id        UUID NOT NULL REFERENCES measurement_sessions(id),
    device_id         UUID NOT NULL REFERENCES devices(id),
    user_id           UUID NOT NULL REFERENCES users(id),
    timestamp         TIMESTAMP NOT NULL,

    -- Signal
    network_type      VARCHAR(20),
    operator_name     VARCHAR(50),
    mcc               INT,
    mnc               INT,
    cell_id           BIGINT,
    pci               INT,
    earfcn            INT,
    bandwidth_mhz     INT,

    -- LTE/5G metrics
    rsrp              FLOAT,
    rsrq              FLOAT,
    sinr              FLOAT,

    -- 2G/3G metrics
    rssi              FLOAT,   -- 2G RSSI and general RSSI
    rscp              FLOAT,   -- 3G RSCP (WCDMA)
    ecno              FLOAT,   -- 3G Ec/No

    cqi               INT,
    ta                INT,

    -- Location
    latitude          DOUBLE PRECISION,
    longitude         DOUBLE PRECISION,
    altitude          FLOAT,
    speed             FLOAT,
    heading           FLOAT,
    location_accuracy FLOAT,

    -- PostGIS geometry point (auto-populated by trigger)
    geom              GEOMETRY(Point, 4326),

    is_roaming        BOOLEAN,
    is_data_active    BOOLEAN,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Spatial index for fast geographic queries
CREATE INDEX IF NOT EXISTS idx_measurements_geom
    ON network_measurements USING GIST(geom);

-- Regular indexes
CREATE INDEX IF NOT EXISTS idx_measurements_timestamp ON network_measurements(timestamp);
CREATE INDEX IF NOT EXISTS idx_measurements_rsrp      ON network_measurements(rsrp);
CREATE INDEX IF NOT EXISTS idx_measurements_session   ON network_measurements(session_id);

-- Trigger to auto-populate geometry from lat/lon
CREATE OR REPLACE FUNCTION set_measurement_geom()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.latitude IS NOT NULL AND NEW.longitude IS NOT NULL THEN
        NEW.geom = ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_measurement_geom ON network_measurements;
CREATE TRIGGER trg_measurement_geom
    BEFORE INSERT OR UPDATE ON network_measurements
    FOR EACH ROW EXECUTE FUNCTION set_measurement_geom();

-- ── 5. coverage_holes (PostGIS polygons) ─────────────
CREATE TABLE IF NOT EXISTS coverage_holes (
    id              BIGSERIAL PRIMARY KEY,
    geom            GEOMETRY(Polygon, 4326),   -- the hole area
    centroid        GEOMETRY(Point, 4326),
    avg_rsrp        FLOAT,
    min_rsrp        FLOAT,
    sample_count    INT,
    operator_name   VARCHAR(50),
    network_type    VARCHAR(20),
    first_detected  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status          hole_status DEFAULT 'candidate'
);

CREATE INDEX IF NOT EXISTS idx_holes_geom ON coverage_holes USING GIST(geom);

-- ── 6. upload_logs ───────────────────────────────────
CREATE TABLE IF NOT EXISTS upload_logs (
    id            BIGSERIAL PRIMARY KEY,
    session_id    UUID REFERENCES measurement_sessions(id) ON DELETE CASCADE,
    upload_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status        upload_log_status NOT NULL,
    rows_sent     INT DEFAULT 0,
    error_message TEXT
);

-- ── Seed admin user ───────────────────────────────────
-- (additional users are created via /auth/signup)
INSERT INTO users (email, password, role)
VALUES ('admin@crowdsenseddt.com', 'admin1234', 'admin')
ON CONFLICT (email) DO NOTHING;
