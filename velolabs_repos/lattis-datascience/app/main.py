from typing import Optional, List

from fastapi import Depends, FastAPI, Request
from passlib.context import CryptContext
#from lattis_ds.db import crud, schemas, database
from lattis_ds import utils

from sqlalchemy.orm import Session

from fastapi.responses import HTMLResponse, JSONResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
import json
import random

#from lattis_ds.db.get_db import get_db
from lattis_ds.db.database import MYSQL, main_mysql_credentials
from lattis_ds.db.shortcuts import Shortcuts
import datetime


from fastapi.middleware.wsgi import WSGIMiddleware
from flask import Flask, escape, request
from lattis_ds.utils import hash_fleet_id

from dashboards import get_dash_app
from dashboards.main import get_dashboard_layout


templates = Jinja2Templates(directory="templates")


pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

#  oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

#database.Base.metadata.create_all(bind=database.engine)

app = FastAPI()
app.mount("/static", StaticFiles(directory="static"), name="static")

mysql = MYSQL(credentials=main_mysql_credentials)
sh = Shortcuts(mysql)


flask_app = Flask(__name__)
dash_app = get_dash_app(server=flask_app, sh=sh)

app.mount("/", WSGIMiddleware(flask_app))

FLEET_HASH_DICT = {hash_fleet_id(id): id for id in sh.get_fleet_id_names().keys()}
print(FLEET_HASH_DICT)
def get_current_active_user():
    pass

@flask_app.route("/dashboard/<fleet_hash>")
def my_dash_app(fleet_hash):
    if fleet_hash in FLEET_HASH_DICT.keys():
        fleet_id = FLEET_HASH_DICT[fleet_hash] 
        dash_app.fleet_id = fleet_id   
        dash_app.layout = get_dashboard_layout(sh, fleet_id=fleet_id)
        return dash_app.index()
    else:
        return "Enter valid fleet hash"

@flask_app.route("/")
def flask_main():
    name = request.args.get("name", "World")
    return f"Hello, {escape(name)} from Flask!"


# @app.get("/users/me/", response_model=schemas.User)
# async def read_users_me(db: Session = Depends(get_db)):
#     return crud.get_users(db)[0]


# @app.get("/users/all", response_model=List[schemas.User])
# async def get_all_users(db: Session = Depends(get_db)):
#     return crud.get_users(db)


# @app.get("/regions/all", response_model=List[schemas.RegionView])
# async def get_all_region(db: Session = Depends(get_db)):
#     return crud.get_regions(db)


# @app.get("/regions/test", response_model=schemas.RegionView)
# async def get_test_region(db: Session = Depends(get_db)):
#     return crud.get_regions(db)[0]


# @app.get("/supply")
# async def get_supply(region: Optional[str] = None, db: Session = Depends(get_db)):
#     crud.get_region_data_key(db, region=region, key='supply')


# @app.get("/testing/random_supply")
# async def get_random_supply(db: Session=Depends(get_db)):
#     region = crud.get_regions(db)[0]
#     return utils.get_random_lat_lng_in_geojson_dict(json.loads(region.geojson), n=random.randint(50, 1000))


# @app.get("/pricing/test", response_class=HTMLResponse)
# async def read_item(request: Request, db: Session = Depends(get_db)):
#     r = crud.get_regions(db)[0]
#     return templates.TemplateResponse("pricing_view.html", {'request': request, 'region': r, 'geohashes': [{'geohash': gh, 'data': data['price']} for gh, data in json.loads(r.ghs_data).items()]})


# @app.get("/graph/test", response_class=HTMLResponse)
# async def graphql(request: Request, db: Session = Depends(get_db)):
#     return templates.TemplateResponse("graph_view_test.html", {'request': request})

@app.get("/data/start_end_trip", response_class=JSONResponse)
async def get_start_end_trip(request: Request, y=2017, m=3, d=26):
    """ return the start and end trip data for a specific day 
    -> goal use this for slider Heatmap data. 
    example : http://127.0.0.1:8000/data/start_end_trip?y=2018&m=2&d=12"""
    return sh.get_start_end_trips(int(y), int(m), int(d)).to_dict('records')

@app.get("/data/active_history", response_class=JSONResponse)
async def get_active_history(request: Request , y1=2017, m1=3, d1=26, y2=2017, m2=3, d2=30):
    """ return the history of active vehicles
    example : http://127.0.0.1:8000/data/active_history?y1=2017&m1=3&d1=25&y2=2017&m2=3&d2=29"""
    dstart = datetime.datetime(year=int(y1), month=int(m1), day=int(d1))
    dend = datetime.datetime(year=int(y2), month=int(m2), day=int(d2))
    return sh.get_active_vehicles_per_day(dstart, dend).to_dict('records')

@app.get("/data/daily_data_history", response_class=JSONResponse)
async def get_daily_history(request: Request, y1=2017, m1=7, d1=10, y2=2017, m2=7, d2=20):
    """ return the history of active vehicles
    example : http://127.0.0.1:8000/data/daily_data_history?y1=2017&m1=7&d1=10&y2=2017&m2=7&d2=20"""
    dstart = datetime.datetime(year=int(y1), month=int(m1), day=int(d1))
    dend = datetime.datetime(year=int(y2), month=int(m2), day=int(d2))
    df = sh.get_daily_data(dstart, dend)
    print(df)
    return df.to_dict('records')
