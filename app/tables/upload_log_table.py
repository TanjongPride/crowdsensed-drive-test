import enum
from sqlalchemy import Column, BigInteger, Integer, Text, DateTime, ForeignKey, Enum as PgEnum
from sqlalchemy.dialects.postgresql import UUID as PG_UUID
from sqlalchemy.sql import func
from app.db.base import Base


class UploadLogStatus(str, enum.Enum):
    success = "success"
    retry   = "retry"
    fail    = "fail"


class UploadLog(Base):
    __tablename__ = "upload_logs"

    id            = Column(BigInteger, primary_key=True)
    session_id    = Column(PG_UUID(as_uuid=True), ForeignKey("measurement_sessions.id", ondelete="CASCADE"), nullable=True)
    upload_time   = Column(DateTime(timezone=True), server_default=func.now())
    status        = Column(PgEnum(UploadLogStatus), nullable=False)
    rows_sent     = Column(Integer, default=0)
    error_message = Column(Text, nullable=True)
