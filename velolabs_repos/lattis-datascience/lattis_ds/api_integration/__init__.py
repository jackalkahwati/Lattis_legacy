from lattis_ds.api_integration import base

models = {'test': base.TestAPI}


def get_api_integration_class(name):
    return models.get(name, base.TestAPI)
