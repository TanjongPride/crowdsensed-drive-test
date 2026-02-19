from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy.sql import func
from app.db.session import get_db
from app.tables.session_table import MeasurementSession, MobilityType
from app.tables.device_table import Device
from app.schemas import SessionStartRequest, SessionStartResponse, SessionEndRequest

router = APIRouter(prefix="/sessions", tags=["sessions"])


@router.post("/start", response_model=SessionStartResponse)
def start_session(data: SessionStartRequest, db: Session = Depends(get_db)):
    """
    Called when the user taps START in the DriveTestScreen.
    Creates a session record and returns the session_id.
    """
    device = db.query(Device).filter(
        Device.id == data.device_id,
        Device.user_id == data.user_id
    ).first()
    if not device:
        raise HTTPException(status_code=404, detail="Device not found or does not belong to user")

    mobility = None
    if data.mobility_type:
        try:
            mobility = MobilityType(data.mobility_type)
        except ValueError:
            raise HTTPException(status_code=400, detail=f"Invalid mobility_type: {data.mobility_type}")

    session = MeasurementSession(
        user_id       = data.user_id,
        device_id     = data.device_id,
        mobility_type = mobility,
    )
    db.add(session)
    db.commit()
    db.refresh(session)

    return SessionStartResponse(session_id=str(session.id))


@router.post("/end")
def end_session(data: SessionEndRequest, db: Session = Depends(get_db)):
    """
    Called when the user taps STOP.
    Stamps end_time and updates total_samples.
    """
    from app.tables.measurement_table import NetworkMeasurement

    session = db.query(MeasurementSession).filter(MeasurementSession.id == data.session_id).first()
    if not session:
        raise HTTPException(status_code=404, detail="Session not found")

    count = db.query(NetworkMeasurement).filter(
        NetworkMeasurement.session_id == data.session_id
    ).count()

    session.end_time      = func.now()
    session.total_samples = count
    db.commit()

    return {"status": "closed", "total_samples": count}
