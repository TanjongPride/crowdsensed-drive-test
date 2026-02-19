from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func
from app.db.session import get_db
from app.tables.measurement_table import NetworkMeasurement
from app.tables.session_table import MeasurementSession

router = APIRouter(prefix="/stats", tags=["stats"])


@router.get("/summary")
def summary(db: Session = Depends(get_db)):
    total    = db.query(func.count(NetworkMeasurement.id)).scalar() or 0
    sessions = db.query(func.count(MeasurementSession.id)).scalar() or 0

    def metric_stats(col):
        row = db.query(
            func.avg(col).label("avg"),
            func.min(col).label("min"),
            func.max(col).label("max"),
        ).filter(col.isnot(None)).first()
        if not row or row.avg is None:
            return None
        return {"avg": round(row.avg, 2), "min": row.min, "max": row.max}

    # Coverage quality buckets (3GPP thresholds)
    excellent = db.query(func.count()).filter(NetworkMeasurement.rsrp >= -80).scalar()  or 0
    good      = db.query(func.count()).filter(NetworkMeasurement.rsrp.between(-90, -80)).scalar() or 0
    fair      = db.query(func.count()).filter(NetworkMeasurement.rsrp.between(-100, -90)).scalar() or 0
    poor      = db.query(func.count()).filter(NetworkMeasurement.rsrp < -100).scalar()  or 0

    type_counts = db.query(
        NetworkMeasurement.network_type,
        func.count(NetworkMeasurement.id).label("count")
    ).group_by(NetworkMeasurement.network_type).all()

    return {
        "total_measurements": total,
        "total_sessions":     sessions,
        "rsrp":  metric_stats(NetworkMeasurement.rsrp),
        "rsrq":  metric_stats(NetworkMeasurement.rsrq),
        "sinr":  metric_stats(NetworkMeasurement.sinr),
        "rssi":  metric_stats(NetworkMeasurement.rssi),
        "rscp":  metric_stats(NetworkMeasurement.rscp),  # 3G
        "ecno":  metric_stats(NetworkMeasurement.ecno),  # 3G
        "coverage_quality": {
            "excellent": excellent, "good": good,
            "fair": fair,          "poor": poor,
        },
        "network_types": {
            (r.network_type or "Unknown"): r.count for r in type_counts
        }
    }


@router.get("/timeseries")
def timeseries(
    metric: str = "rsrp",
    limit:  int = 100,
    db: Session = Depends(get_db)
):
    col = {
        "rsrp": NetworkMeasurement.rsrp,
        "rsrq": NetworkMeasurement.rsrq,
        "sinr": NetworkMeasurement.sinr,
        "rssi": NetworkMeasurement.rssi,
        "rscp": NetworkMeasurement.rscp,
        "ecno": NetworkMeasurement.ecno,
    }.get(metric, NetworkMeasurement.rsrp)

    rows = (
        db.query(NetworkMeasurement.timestamp, col)
        .filter(col.isnot(None))
        .order_by(NetworkMeasurement.timestamp.desc())
        .limit(limit)
        .all()
    )

    return [
        {"timestamp": r[0].isoformat(), "value": r[1]}
        for r in reversed(rows)
    ]
