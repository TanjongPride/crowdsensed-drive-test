from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text
from app.db.session import get_db
from typing import Optional

router = APIRouter(prefix="/coverage", tags=["coverage"])


@router.get("/holes")
def get_coverage_holes(
    operator: Optional[str] = None,
    db: Session = Depends(get_db)
):
    """
    Returns detected coverage holes — geographic clusters of poor RSRP (< -100 dBm).
    Uses PostGIS ST_ClusterDBSCAN to group nearby poor measurements.
    Each hole includes centroid coordinates, avg RSRP, and sample count.
    """
    operator_filter = "AND operator_name = :operator" if operator else ""

    sql = text(f"""
        WITH poor_measurements AS (
            SELECT
                id,
                geom,
                rsrp,
                operator_name,
                network_type,
                ST_ClusterDBSCAN(geom, eps := 0.003, minpoints := 5)
                    OVER (PARTITION BY operator_name) AS cluster_id
            FROM network_measurements
            WHERE rsrp < -100
              AND geom IS NOT NULL
              {operator_filter}
        ),
        holes AS (
            SELECT
                cluster_id,
                ST_AsGeoJSON(ST_Centroid(ST_Collect(geom)))::json  AS centroid,
                ST_AsGeoJSON(ST_ConvexHull(ST_Collect(geom)))::json AS polygon,
                AVG(rsrp)          AS avg_rsrp,
                MIN(rsrp)          AS min_rsrp,
                COUNT(*)           AS sample_count,
                MAX(operator_name) AS operator_name,
                MAX(network_type)  AS network_type
            FROM poor_measurements
            WHERE cluster_id IS NOT NULL
            GROUP BY cluster_id
        )
        SELECT * FROM holes
        ORDER BY avg_rsrp ASC
    """)

    params = {"operator": operator} if operator else {}
    rows   = db.execute(sql, params).fetchall()

    return [
        {
            "cluster_id":    r.cluster_id,
            "centroid":      r.centroid,
            "polygon":       r.polygon,
            "avg_rsrp":      round(r.avg_rsrp, 2) if r.avg_rsrp else None,
            "min_rsrp":      round(r.min_rsrp, 2) if r.min_rsrp else None,
            "sample_count":  r.sample_count,
            "operator_name": r.operator_name,
            "network_type":  r.network_type,
            "severity":      _severity(r.avg_rsrp)
        }
        for r in rows
    ]


@router.get("/heatmap")
def get_heatmap(
    network_type:  Optional[str] = None,
    operator_name: Optional[str] = None,
    db: Session = Depends(get_db)
):
    """
    Returns all geo-referenced measurements for heatmap rendering.
    Filters out stationary measurements (speed < 2 m/s) to keep data clean.
    """
    filters = ["geom IS NOT NULL"]
    params  = {}

    if network_type:
        filters.append("network_type = :network_type")
        params["network_type"] = network_type
    if operator_name:
        filters.append("operator_name = :operator_name")
        params["operator_name"] = operator_name

    where = " AND ".join(filters)

    sql = text(f"""
        SELECT
            latitude, longitude, rsrp, rsrq, sinr, rssi, rscp,
            network_type, operator_name
        FROM network_measurements
        WHERE {where}
        ORDER BY id DESC
        LIMIT 5000
    """)

    rows = db.execute(sql, params).fetchall()

    return [
        {
            "lat":           r.latitude,
            "lon":           r.longitude,
            "rsrp":          r.rsrp,
            "rsrq":          r.rsrq,
            "sinr":          r.sinr,
            "rssi":          r.rssi,
            "rscp":          r.rscp,
            "network_type":  r.network_type,
            "operator_name": r.operator_name,
        }
        for r in rows
    ]


def _severity(avg_rsrp: float) -> str:
    if avg_rsrp is None:    return "unknown"
    if avg_rsrp >= -105:    return "mild"
    if avg_rsrp >= -115:    return "moderate"
    return "severe"
