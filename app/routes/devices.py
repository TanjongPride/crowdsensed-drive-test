from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.tables.device_table import Device
from app.tables.user_table import User
from app.schemas import DeviceRegisterRequest, DeviceRegisterResponse

router = APIRouter(prefix="/devices", tags=["devices"])


@router.post("/register", response_model=DeviceRegisterResponse)
def register_device(data: DeviceRegisterRequest, db: Session = Depends(get_db)):
    """
    Called once on first app launch (or after re-install).
    Creates a device record linked to the user and returns a server-assigned device_id.
    """
    user = db.query(User).filter(User.id == data.user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    device = Device(
        user_id      = data.user_id,
        manufacturer = data.manufacturer,
        model        = data.model,
        os_version   = data.os_version,
        app_version  = data.app_version,
    )
    db.add(device)
    db.commit()
    db.refresh(device)

    return DeviceRegisterResponse(device_id=str(device.id))
