from lattis_ds.pricing.base_model import DummyPricingModel

models = {DummyPricingModel.version: DummyPricingModel}


def get_pricing_model(model):
    return models.get(model, DummyPricingModel)
