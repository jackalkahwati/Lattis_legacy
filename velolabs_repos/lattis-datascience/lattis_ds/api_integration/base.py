# File to manage third party api integration. One of them is the Lattis Node API
# to get the supply data. We also use a testing api hosted on the same node.
import requests
import json
from lattis_ds import utils


class BaseAPIIntegration(object):
    def __init__(self, params):
        pass

    def get_supply(self):
        raise NotImplementedError("Implement in child classes")


class TestAPI(BaseAPIIntegration):
    base = 'http://127.0.0.1:8000/'

    def get_supply(self):
        rep = requests.get(TestAPI.base + 'testing/random_supply')
        lat_lng_list = json.loads(rep.content)
        return utils.aggregate_lat_lng_by_geohash(lat_lng_list, precision=7)


class LattisNodeAPIIntegration(BaseAPIIntegration):
    def get_supply(self):
        pass
