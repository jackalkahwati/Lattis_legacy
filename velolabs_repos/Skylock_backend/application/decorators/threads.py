import threading
#from threading import Thread
from functools import wraps
from flask import abort, g
from flask.ext.login import current_user
from ..models import Permission, User

#print threading.__file__

def async(f):
    def wrapper(*args, **kwargs):
        thr = threading.Thread(target=f, args=args, kwargs=kwargs)
        thr.start()
    return wrapper

def permission_required(permission):
	def decorator(f):
		@wraps(f)
		def decorated_function(*args, **kwargs):
			if not User.can(kwargs['user_id'], permission):
				abort(403)
			return f(*args, **kwargs)
		return decorated_function
	return decorator


def admin_required(f):
	return permission_required(Permission.ADMINISTER)(f)