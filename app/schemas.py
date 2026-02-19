from pydantic import BaseModel, EmailStr, field_validator
from typing import List, Optional
from datetime import datetime
from uuid import UUID


# ── Auth ──────────────────────────────────────────────
class SignupRequest(BaseModel):
    email:    str
    password: str

    @field_validator("email")
    @classmethod
    def email_lower(cls, v):
        return v.strip().lower()

    @field_validator("password")
    @classmethod
    def password_length(cls, v):
        if len(v) < 6:
            raise ValueError("Password must be at least 6 characters")
        return v

class LoginRequest(BaseModel):
    email:    str
    password: str

class AuthResponse(BaseModel):
    user_id: str
    email:   str
    role:    str


# ── Device ────────────────────────────────────────────
class DeviceRegisterRequest(BaseModel):
    user_id:      UUID
    manufacturer: Optional[str] = None
    model:        Optional[str] = None
    os_version:   Optional[str] = None
    app_version:  Optional[str] = None

class DeviceRegisterResponse(BaseModel):
    device_id: str


# ── Session ───────────────────────────────────────────
class SessionStartRequest(BaseModel):
    user_id:       UUID
    device_id:     UUID
    mobility_type: Optional[str] = None

class SessionStartResponse(BaseModel):
    session_id: str

class SessionEndRequest(BaseModel):
    session_id: UUID


# ── Measurements ──────────────────────────────────────
class NetworkMeasurementSchema(BaseModel):
    session_id:  UUID
    device_id:   UUID
    user_id:     UUID
    timestamp:   datetime

    network_type:   Optional[str]   = None
    operator_name:  Optional[str]   = None
    mcc:            Optional[int]   = None
    mnc:            Optional[int]   = None
    cell_id:        Optional[int]   = None
    pci:            Optional[int]   = None
    earfcn:         Optional[int]   = None
    bandwidth_mhz:  Optional[int]   = None

    # LTE / 5G
    rsrp:           Optional[float] = None
    rsrq:           Optional[float] = None
    sinr:           Optional[float] = None

    # 2G / 3G
    rssi:           Optional[float] = None
    rscp:           Optional[float] = None   # 3G WCDMA
    ecno:           Optional[float] = None   # 3G Ec/No

    cqi:            Optional[int]   = None
    ta:             Optional[int]   = None

    latitude:          Optional[float] = None
    longitude:         Optional[float] = None
    altitude:          Optional[float] = None
    speed:             Optional[float] = None
    heading:           Optional[float] = None
    location_accuracy: Optional[float] = None

    is_roaming:     Optional[bool] = None
    is_data_active: Optional[bool] = None
