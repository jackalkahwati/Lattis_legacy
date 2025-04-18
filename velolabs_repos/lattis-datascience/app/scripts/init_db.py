from lattis_ds.db import crud, schemas, database
import json

def create_test_user():
    myuser = schemas.UserCreate(id=1, username='clement', email='clement@unsupervised.ai', is_active=True, password='safe')
    crud.create_user(db, myuser)

geojson = json.dumps({
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "properties": {},
      "geometry": {
        "type": "Polygon",
        "coordinates": [
          [
            [
              2.3017215728759766,
              48.838848121684194
            ],
            [
              2.3052406311035156,
              48.83076911859322
            ],
            [
              2.3284149169921875,
              48.829582581850715
            ],
            [
              2.3234367370605464,
              48.84444051446046
            ],
            [
              2.3017215728759766,
              48.838848121684194
            ]
          ]
        ]
      }
    }
  ]
})

def create_test_region():
    myregion = schemas.RegionCreate(name='paris', geojson=geojson, owner_id=1, parameters='{}')
    r = crud.create_region(db, myregion)
    return r

db = database.SessionLocal()
create_test_user()
r = create_test_region()
crud.create_geohash_for_region(db, crud.get_regions(db, to_pydantic=False)[0])
