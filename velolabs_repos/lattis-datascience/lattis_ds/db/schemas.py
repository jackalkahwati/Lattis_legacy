from typing import List, Optional
from pydantic import BaseModel


class RegionBase(BaseModel):
    name: str
    owner_id: int
    geojson: Optional[str] = None
    parameters: Optional[str] = None
    ghs_data: Optional[str] = None


class RegionCreate(RegionBase):
    pass


class Region(RegionBase):
    id: str

    class Config:
        orm_mode = True


class UserBase(BaseModel):
    username: str
    email: str


class UserCreate(UserBase):
    password: str


class User(UserBase):
    id: int
    is_active: bool
    Regions: List[Region] = []

    class Config:
        orm_mode = True


class RegionView(RegionBase):
    owner: User

    class Config:
        orm_mode = True
