import os


#default config
class BaseConfig(object):
	DEBUG = False
	CSRF_ENABLED = True
	SECRET_KEY = '*0\xa8\x10\x13\xceb\xfd\x08\xde\x168\xefr/\xcc|\xda\xc6*Z\xa7hx'
	SQLALCHEMY_DATABASE_URI = "mysql://@localhost/skylock_mysql_test"
	account_sid = os.environ['ACCOUNT_SID'] #"ACcfa5178285192224b6376b124eff168d"
	auth_token  = os.environ['AUTH_TOKEN'] #"35ea4b5b8b40ec6c17fe7a1cbbbf2b4e"
	api_key = os.environ['API_KEY']
	otp_secret = os.environ['OTP_SECRET'] #"G6X5YS7VN6PLOGBV"
	admin = os.environ['ADMIN']
	print (SQLALCHEMY_DATABASE_URI)
	#"mysql://velolabs:toomanyflies@skylocktest.ca7kx2vitaft.us-west-2.rds.amazonaws.com:3306/Skylockdb"

	#print SQLALCHEMY_DATABASE_URI
	


class DevelopmentConfig(BaseConfig):
	DEBUG = True
	threaded=True

class ProductionConfig(BaseConfig):
	DEBUG = False
	threaded=True