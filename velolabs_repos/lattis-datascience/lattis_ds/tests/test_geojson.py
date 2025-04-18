from lattis_ds.db.region import Region
import json


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

r = Region()
