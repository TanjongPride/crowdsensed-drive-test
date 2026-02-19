import os
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# Reads DATABASE_URL from environment (set automatically on Render.com)
# Falls back to local PostgreSQL for development
DATABASE_URL = os.environ.get(
    "DATABASE_URL",
    "postgresql://postgres:loop@localhost:5432/test_db"
)

# Render provides postgres:// but SQLAlchemy needs postgresql://
if DATABASE_URL.startswith("postgres://"):
    DATABASE_URL = DATABASE_URL.replace("postgres://", "postgresql://", 1)

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
