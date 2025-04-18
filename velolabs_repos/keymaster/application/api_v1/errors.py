from flask import jsonify
from ..exceptions import ValidationError
from . import api


@api.app_errorhandler(ValidationError)
def bad_request(e):
    response = jsonify({'status': 400, 'error': 'bad request',
                        'payload': e.args[0]})
    response.status_code = 400
    return response

@api.app_errorhandler(403)  # this is app-wide handler
def not_found(e):
    response = jsonify({'status': 403, 'error': 'not suffiecnt permission',
                        'payload': 'You dont have permission to access the requested route.'})
    response.status_code = 403
    return response

@api.app_errorhandler(404) 
def not_found(e):
    response = jsonify({'status': 404, 'error': 'not found',
                        'payload': 'invalid resource URI'})
    response.status_code = 404
    return response


@api.app_errorhandler(405)
def method_not_supported(e):
    response = jsonify({'status': 405, 'error': 'method not supported',
                        'payload': 'the method is not supported'})
    response.status_code = 405
    return response


@api.app_errorhandler(500)  # this is app-wide handler
def internal_server_error(e):
    response = jsonify({'status': 500, 'error': 'internal server error',
                        'payload': e.args[0]})
    response.status_code = 500
    return response


#401, 'error': 'Invalid User or Wrong Password'
#404, 'message': 'User Dosent Exist'