from sqlalchemy import Column, String, DateTime, ForeignKey
from sqlalchemy.dialects.postgresql import UUID as PG_UUID
from sqlalchemy.sql import func
from app.db.base import Base


class Device(Base):
    __tablename__ = "devices"

    id           = Column(PG_UUID(as_uuid=True), primary_key=True, server_default="uuid_generate_v4()")
    user_id      = Column(PG_UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    manufacturer = Column(String(100), nullable=True)
    model        = Column(String(100), nullable=True)
    os_version   = Column(String(50),  nullable=True)
    app_version  = Column(String(50),  nullable=True)
    created_at   = Column(DateTime(timezone=True), server_default=func.now())
