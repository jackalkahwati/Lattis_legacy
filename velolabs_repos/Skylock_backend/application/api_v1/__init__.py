from flask import Blueprint
from ..auth import auth1_token
from ..decorators import rate_limit, etag



api = Blueprint('api', __name__)

@api.before_request
@rate_limit(limit=5, period=30)
@auth1_token.login_required
def before_request():
    # All routes in this blueprint require authentication.
    pass

application = Blueprint('application', __name__)


from . import users, locks, mobiles, shared, crash, firmware, metadata, errors