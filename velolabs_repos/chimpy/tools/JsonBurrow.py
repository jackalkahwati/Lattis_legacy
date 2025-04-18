class JsonBurrow:
    def __init__(self, json_data, case_sensitive=False):
        self._json_data = json_data
        self._values = []
        self._case_sensitive = case_sensitive
        self._allKeys = []

    def burrow(self, search_param):
        self._burrow_helper(search_param, self._json_data)
        return None

    def _burrow_helper(self, search_param, json):
        if type(json) is dict:
            if search_param in json:
                self._values.append(json[search_param])
            [self._burrow_helper(value, search_param) for value in json.values()]
        elif type(json) is list:
            [self._burrow_helper(search_param, item) for item in json]
        elif search_param == json:
            self._values.append(search_param)
        return None

    def discovered_values(self):
        return self._values

    def get_keys(self):
        self.allKeys = []
        self._get_keys_helper(self._json_data)
        return self.allKeys

    def _get_keys_helper(self, json, path=''):
        if type(json) is dict:
            for key, value in json.items():
                new_path = path + '/' + key
                self.allKeys.append(new_path)
                self._get_keys_helper(value, new_path)
        elif type(json) is list:
            [self._get_keys_helper(value, path) for value in json]
        return
