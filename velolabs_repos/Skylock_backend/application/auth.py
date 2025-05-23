from flask import jsonify, g, current_app
from flask.ext.httpauth import HTTPBasicAuth
from .models import User

auth = HTTPBasicAuth()
auth1_token = HTTPBasicAuth()



@auth.verify_password
def verify_password(username, password):
    g.user = User.query.filter_by(username=username).first()
    if g.user is None:
        return False
    return g.user.verify_password(password)


@auth.error_handler
def unauthorized():
    response = jsonify({'status': 401, 'error': 'unauthorized',
                        'payload': 'please authenticate'})
    response.status_code = 401
    return response


@auth1_token.verify_password
def verify_auth_token(token, unused):
    g.user = User.verify_auth_token(token)
    return g.user is not None


@auth1_token.error_handler
def unauthorized_token():
    response = jsonify({'status': 401, 'error': 'unauthorized',
                        'payload': 'please send your authentication token'})
    response.status_code = 401
    return response


