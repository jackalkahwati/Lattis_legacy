from shapely import geometry
import random
from collections import defaultdict
import geohash
from polygon_geohasher.polygon_geohasher import polygon_to_geohashes   # geohashes_to_polygon
from hashlib import pbkdf2_hmac
import binascii

def hash_fleet_id(fleet_id):
    """
    Hashes password using PKDBF2 method:
    """
    salt = "LATTISISAWESOME"

    hash = pbkdf2_hmac(
        hash_name="sha256", password=str(fleet_id).encode(), salt=salt.encode(), iterations=10000
    )
    return binascii.hexlify(hash).decode() 


def generate_random(number, polygon):
    points = []
    minx, miny, maxx, maxy = polygon.bounds
    while len(points) < number:
        pnt = geometry.Point(random.uniform(minx, maxx), random.uniform(miny, maxy))
        if polygon.contains(pnt):
            points.append(pnt)
    return points


def get_random_lat_lng_in_geojson_dict(json_dict, n=1):
    poly = json_dict['features'][0]['geometry']['coordinates'][0]
    poly = geometry.Polygon(poly)
    points = generate_random(n, poly)
    return [(p.y, p.x) for p in points]


def aggregate_lat_lng_by_geohash(lat_lng_list, precision=7):
    """ convert a list of lat lng ( could be scooter location) and count them by geohash
    return a dict: geohash -> count """

    count = defaultdict(int)
    for lat, lng in lat_lng_list:
        count[geohash.encode(lat, lng, precision=precision)] += 1
    return dict(count)


def convert_polygon_to_list_of_geohashes(json_dict, precision=7, inner=True):
    poly = json_dict['features'][0]['geometry']['coordinates'][0]
    return polygon_to_geohashes(geometry.Polygon(poly), precision=precision, inner=inner)
