""" File with shortcuts to talk to the API """
from requests import get


class API(object):
    def __init__(self, base_url='127.0.0.1:8000'):
        self.base_url = base_url

    def get_supply(self, region):
        data = get(self.base_url + '/supply', params={'region': region})
        return data
