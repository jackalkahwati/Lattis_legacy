import json
from lattis_ds import utils


class PricingModel(object):
    def __init__(self, params, json_dict):
        self.params = params
        self.json_dict = json_dict
        self.geohashes = utils.convert_polygon_to_list_of_geohashes(self.json_dict)

    def run(self, supply):
        raise NotImplementedError("Implement in Children classes")


class DummyPricingModel(PricingModel):
    version = 'v0'
    thresholds = [0, 3, 9, 15]
    price_val = [2, 1.5, 1.25, 1]

    def get_geohash_price(self, n_scooter):
        for i, t in enumerate(self.thresholds):
            if t > n_scooter:
                return self.price_val[i - 1]
        return self.price_val[-1]

    def run(self, supply):
        prices = {}
        for gh, n_scooter in supply.items():
            prices[gh] = self.get_geohash_price(n_scooter)
        return prices
