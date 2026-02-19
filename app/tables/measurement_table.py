from sqlalchemy import Column, String, Float, Boolean, DateTime, Integer, BigInteger, ForeignKey
from sqlalchemy.dialects.postgresql import UUID as PG_UUID
from sqlalchemy.sql import func
from geoalchemy2 import Geometry
from app.db.base import Base


class NetworkMeasurement(Base):
    __tablename__ = "network_measurements"

    id                = Column(BigInteger, primary_key=True, index=True)
    session_id        = Column(PG_UUID(as_uuid=True), ForeignKey("measurement_sessions.id"), nullable=False)
    device_id         = Column(PG_UUID(as_uuid=True), ForeignKey("devices.id"),              nullable=False)
    user_id           = Column(PG_UUID(as_uuid=True), ForeignKey("users.id"),                nullable=False)
    timestamp         = Column(DateTime, nullable=False)

    network_type      = Column(String(20),  nullable=True)
    operator_name     = Column(String(50),  nullable=True)
    mcc               = Column(Integer,     nullable=True)
    mnc               = Column(Integer,     nullable=True)
    cell_id           = Column(BigInteger,  nullable=True)
    pci               = Column(Integer,     nullable=True)
    earfcn            = Column(Integer,     nullable=True)
    bandwidth_mhz     = Column(Integer,     nullable=True)

    # LTE / 5G
    rsrp              = Column(Float, nullable=True)
    rsrq              = Column(Float, nullable=True)
    sinr              = Column(Float, nullable=True)

    # 2G / 3G
    rssi              = Column(Float, nullable=True)
    rscp              = Column(Float, nullable=True)
    ecno              = Column(Float, nullable=True)

    cqi               = Column(Integer, nullable=True)
    ta                = Column(Integer, nullable=True)

    latitude          = Column(Float, nullable=True)
    longitude         = Column(Float, nullable=True)
    altitude          = Column(Float, nullable=True)
    speed             = Column(Float, nullable=True)
    heading           = Column(Float, nullable=True)
    location_accuracy = Column(Float, nullable=True)

    # PostGIS geometry — populated automatically by DB trigger
    geom              = Column(Geometry("POINT", srid=4326), nullable=True)

    is_roaming        = Column(Boolean, nullable=True)
    is_data_active    = Column(Boolean, nullable=True)
    created_at        = Column(DateTime(timezone=True), server_default=func.now())
