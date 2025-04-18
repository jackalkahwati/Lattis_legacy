import urllib
import json
import base64
from errors.Errors import JsonEncodeError


class RestHandler:
    def __init__(self):
        self.TIMEOUT = 60

    def get_request(self, url, headers=None):
        request = urllib.request.Request(url)
        return self._handle_request(request, headers)

    def json_post_request(self, url, post_object, headers=None):
        return self._handle_put_or_post(url, post_object, headers, 'POST')

    #def post_request(self, url, post_body, headers=None):

    def put_request(self, url, put_object, headers=None):
        return self._handle_put_or_post(url, put_object, headers, 'PUT')

    def _handle_put_or_post(self, url, object, headers, put_or_post):
        json_data = self._object_to_json(object)
        if not json_data:
            raise JsonEncodeError('Json Data for object ' + str(object) + ' cannot be encoded')
        if not headers:
            headers = {}
        headers['content-type'] = 'application/json'
        request = urllib.request.Request(url, data=json_data, method=put_or_post)
        return self._handle_request(request, headers)

    def register_basic_auth(self, base_url, target_url, username, password):
        password_manager = urllib.request.HTTPPasswordMgrWithDefaultRealm()
        password_manager.add_password(None, base_url, username, password)
        handler = urllib.request.HTTPBasicAuthHandler(password_manager)
        opener = urllib.request.build_opener(handler)
        opener.open(target_url)
        urllib.request.install_opener(opener)
        return None

    def _handle_request(self, request, headers):
        if headers:
            [request.add_header(key, value) for key, value in headers.items()]
        try:
            with urllib.request.urlopen(request) as response:
                data = response.read()
                returned_json = json.loads(data.decode('utf8'))
        except urllib.error.HTTPError as e:
            print('Error making request:', e)
            raise
        return returned_json

    def construct_query_string(self, params):
        query_string = '?'
        counter = 0
        for key, value in params.items():
            query_string += key + '=' + str(value)
            if type(value) == list:
                list_counter = 0
                for item in value:
                    query_string += str(item)
                    if list_counter != len(value) - 1:
                        query_string += ','
                    list_counter += 1
            elif counter != len(params.keys()) - 1:
                query_string += '&'
            counter += 1
        return query_string

    def basicAuthHeader(self, username, password):
        return 'Basic ' + base64.b64encode((username + ':' + password).encode('utf-8')).decode('utf-8')

    def _object_to_json(self, object):
        try:
            json_data = json.dumps(object).encode('utf8')
        except TypeError as e:
            print('Error encoding json object. Invalid object.', e)
            return None
        except Exception as e:
            print('Unknown Error encoding json object:', e)
            return None
        return json_data
