"""Create, Read, Update, and Delete."""
from sqlalchemy.orm import Session
from . import region, user, schemas
from typing import Optional
import uuid
import json
import random


def get_user(db: Session, user_id: int):
    return db.query(user.User).filter(user.User.id == user_id).first()


def get_user_by_email(db: Session, email: str):
    return db.query(user.User).filter(user.User.email == email).first()


def get_users(db: Session, skip: int = 0, limit: int = 100, to_pydantic=True):
    res = db.query(user.User).offset(skip).limit(limit).all()
    if to_pydantic:
        return [schemas.User.from_orm(u) for u in res]
    else:
        return res


def get_regions(db: Session, skip: int = 0, limit: int = 100, to_pydantic=True):
    res = db.query(region.Region).offset(skip).limit(limit).all()
    if to_pydantic:
        return [schemas.RegionView.from_orm(r) for r in res]
    else:
        return res


def create_user(db: Session, u: schemas.UserCreate):
    fake_hashed_password = u.password + "notreallyhashed"
    db_user = user.User(email=u.email, username=u.username, hashed_password=fake_hashed_password)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user


def create_region(db: Session, r: schemas.RegionCreate):
    db_region = region.Region(id=str(uuid.uuid1()), owner_id=r.owner_id, name=r.name, geojson=r.geojson, parameters=r.parameters)
    db.add(db_region)
    db.commit()
    db.refresh(db_region)
    return db_region


def create_geohash_for_region(db: Session, r):
    """ From region r we get the geohash list within the geojson and add them to the DB """
    ghs = r.get_geohashes_from_geojson_polygon()
    ghs_data = {}
    for gh in ghs:
        ghs_data[gh] = {'price': random.random() + 1.0}
    r.ghs_data = json.dumps(ghs_data)
    db.commit()


def get_region(db: Session, name: Optional[str] = None):
    return db.query(region.Region).filter(region.Region.name == name).first()


def get_region_params(db: Session, region: Optional[str] = None):
    return db.query(region.Region).filter(region.Region.name == region).first().parameters


def get_all_regions(db: Session):
    return db.query(region.Region)


def get_region_data_key(db: Session, region_name: Optional[str] = None, key: Optional[str] = 'supply'):
    """ return the stored value in the geohash store for a region, for instance: price, supply ..."""
    ghs_data = json.loads(get_region(db, region_name).ghs_data)
    res = {}
    for gh, val in ghs_data.items():
        res[gh] = val[key]
    return res


def get_geohash_data(db: Session, region_id: Optional[str] = None, geohash: Optional[str] = None):
    return db.query(geohash.Geohash).filter(region.Geohash.region_id == region_id, geohash.Geohash.geohash == geohash).first().data


# TODO function
# -create user region
# create geohash from region geojson
