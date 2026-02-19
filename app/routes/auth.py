from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.tables.user_table import User
from app.schemas import LoginRequest, SignupRequest, AuthResponse

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/signup", response_model=AuthResponse)
def signup(data: SignupRequest, db: Session = Depends(get_db)):
    """
    Register a new user account.
    Email must be unique. Password minimum 6 characters.
    ⚠️  Use bcrypt in production — plain text here for clarity.
    """
    existing = db.query(User).filter(User.email == data.email).first()
    if existing:
        raise HTTPException(status_code=409, detail="Email already registered")

    user = User(email=data.email, password=data.password)
    db.add(user)
    db.commit()
    db.refresh(user)

    return AuthResponse(user_id=str(user.id), email=user.email, role=user.role.value)


@router.post("/login", response_model=AuthResponse)
def login(data: LoginRequest, db: Session = Depends(get_db)):
    """Authenticate with email + password."""
    user = db.query(User).filter(User.email == data.email.strip().lower()).first()
    if not user or user.password != data.password:
        raise HTTPException(status_code=401, detail="Invalid credentials")
    return AuthResponse(user_id=str(user.id), email=user.email, role=user.role.value)
