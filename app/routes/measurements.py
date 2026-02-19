from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime
from uuid import UUID
from pydantic import BaseModel

from app.db.session import get_db
from app.tables.measurement_table import NetworkMeasurement
from app.tables.session_table import MeasurementSession
from app.tables.upload_log_table import UploadLog, UploadLogStatus
from app.schemas import NetworkMeasurementSchema

router = APIRouter(tags=["measurements"])


# ── POST /upload ──────────────────────────────────────────────────────────────
@router.post("/upload")
def upload_measurements(
    measurements: List[NetworkMeasurementSchema],
    db: Session = Depends(get_db)
):
    """
    Receives a JSON array of measurements from Android.
    All measurements in a batch must share the same session_id.
    Writes an upload_log entry for audit trail.
    """
    if not measurements:
        raise HTTPException(status_code=400, detail="Empty payload")

    session_id = measurements[0].session_id

    # Verify the session exists
    session = db.query(MeasurementSession).filter(
        MeasurementSession.id == session_id
    ).first()
    if not session:
        raise HTTPException(status_code=404, detail=f"Session {session_id} not found. Call /sessions/start first.")

    inserted_ids = []
    try:
        for m in measurements:
            row = NetworkMeasurement(
                session_id        = m.session_id,
                device_id         = m.device_id,
                user_id           = m.user_id,
                timestamp         = m.timestamp,
                network_type      = m.network_type,
                operator_name     = m.operator_name,
                mcc               = m.mcc,
                mnc               = m.mnc,
                cell_id           = m.cell_id,
                pci               = m.pci,
                earfcn            = m.earfcn,
                bandwidth_mhz     = m.bandwidth_mhz,
                rsrp              = m.rsrp,
                rsrq              = m.rsrq,
                sinr              = m.sinr,
                rssi              = m.rssi,
                cqi               = m.cqi,
                ta                = m.ta,
                latitude          = m.latitude,
                longitude         = m.longitude,
                altitude          = m.altitude,
                speed             = m.speed,
                heading           = m.heading,
                location_accuracy = m.location_accuracy,
                is_roaming        = m.is_roaming,
                is_data_active    = m.is_data_active,
            )
            db.add(row)
            db.flush()
            inserted_ids.append(row.id)

        # Update session sample count
        session.total_samples = (session.total_samples or 0) + len(measurements)
        session.uploaded = True

        # Write upload log
        log = UploadLog(
            session_id = session_id,
            status     = UploadLogStatus.success,
            rows_sent  = len(measurements),
        )
        db.add(log)

        db.commit()
        return {"status": "success", "count": len(measurements), "ids": inserted_ids}

    except Exception as e:
        db.rollback()
        # Log the failure
        try:
            fail_log = UploadLog(
                session_id    = session_id,
                status        = UploadLogStatus.fail,
                rows_sent     = 0,
                error_message = str(e),
            )
            db.add(fail_log)
            db.commit()
        except Exception:
            pass
        raise HTTPException(status_code=500, detail=str(e))


# ── GET /measurements ─────────────────────────────────────────────────────────
class MeasurementResponse(BaseModel):
    id:                int
    session_id:        UUID
    device_id:         UUID
    user_id:           UUID
    timestamp:         datetime
    network_type:      Optional[str]   = None
    operator_name:     Optional[str]   = None
    mcc:               Optional[int]   = None
    mnc:               Optional[int]   = None
    cell_id:           Optional[int]   = None
    pci:               Optional[int]   = None
    earfcn:            Optional[int]   = None
    bandwidth_mhz:     Optional[int]   = None
    rsrp:              Optional[float] = None
    rsrq:              Optional[float] = None
    sinr:              Optional[float] = None
    rssi:              Optional[float] = None
    cqi:               Optional[int]   = None
    ta:                Optional[int]   = None
    latitude:          Optional[float] = None
    longitude:         Optional[float] = None
    altitude:          Optional[float] = None
    speed:             Optional[float] = None
    heading:           Optional[float] = None
    location_accuracy: Optional[float] = None
    is_roaming:        Optional[bool]  = None
    is_data_active:    Optional[bool]  = None

    class Config:
        from_attributes = True


@router.get("/measurements", response_model=List[MeasurementResponse])
def get_measurements(limit: int = 50, db: Session = Depends(get_db)):
    return (
        db.query(NetworkMeasurement)
        .order_by(NetworkMeasurement.id.desc())
        .limit(limit)
        .all()
    )


# ── GET /measurements/heatmap ─────────────────────────────────────────────────
@router.get("/measurements/heatmap")
def heatmap_data(
    network_type:  Optional[str]      = None,
    operator_name: Optional[str]      = None,
    start_time:    Optional[datetime] = None,
    end_time:      Optional[datetime] = None,
    db: Session = Depends(get_db)
):
    query = db.query(
        NetworkMeasurement.latitude,
        NetworkMeasurement.longitude,
        NetworkMeasurement.rsrp,
        NetworkMeasurement.network_type,
        NetworkMeasurement.operator_name,
    )
    if network_type:
        query = query.filter(NetworkMeasurement.network_type == network_type)
    if operator_name:
        query = query.filter(NetworkMeasurement.operator_name == operator_name)
    if start_time:
        query = query.filter(NetworkMeasurement.timestamp >= start_time)
    if end_time:
        query = query.filter(NetworkMeasurement.timestamp <= end_time)

    return [
        {
            "lat":           r.latitude,
            "lon":           r.longitude,
            "rsrp":          r.rsrp,
            "network_type":  r.network_type,
            "operator_name": r.operator_name,
        }
        for r in query.all()
    ]
