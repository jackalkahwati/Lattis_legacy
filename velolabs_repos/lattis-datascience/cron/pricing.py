from lattis_ds.db import crud, get_db, database
from lattis_ds import pricing
from lattis_ds import api_integration
from collections import defaultdict
import json


class RegionPricingWorker(object):
    def __init__(self, region):
        """ init with a region ORM object """
        self.enable = False
        self.region = region
        self.pricing_model = None
        self.api_integration = None

    def init_pricing_model(self):
        params = json.loads(self.region.parameters)
        json_dict = json.loads(self.region.geojson)
        self.pricing_model = pricing.get_pricing_model(params.get('pricing_model', 'v0'))(params, json_dict)
        self.api_integration = api_integration.get_api_integration_class(params.get('api_integration', 'testing'))(params)

    def format_supply_and_prices(self, supply, prices):
        ghs_data = defaultdict(dict)
        for gh, n_scooter in supply.items():
            ghs_data[gh]['supply'] = n_scooter
        for gh, price in prices.items():
            ghs_data[gh]['price'] = price
        return json.dumps(ghs_data)

    def get_supply_from_third_party(self):
        supply = self.api_integration.get_supply()
        print(supply)
        return supply

    def run_pricing_model(self, supply):
        prices = self.pricing_model.run(supply)
        self.region.ghs_data = self.format_supply_and_prices(supply, prices)

    def run(self):
        self.init_pricing_model()
        supply = self.get_supply_from_third_party()
        self.run_pricing_model(supply)


class PricingWorker(object):
    def __init__(self, db):
        self.db = db

    def run(self):
        regions = crud.get_all_regions(self.db)
        for r in regions:
            worker = RegionPricingWorker(r)
            worker.run()
        self.db.commit()


if __name__ == "__main__":
    db = database.SessionLocal()
    p = PricingWorker(db)
    p.run()
