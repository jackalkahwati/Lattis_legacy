import os

basedir = os.path.abspath(os.path.dirname(__file__))


DEBUG = True
#IGNORE_AUTH = True


SECRET_KEY = "shiva"
SECRET_KEY_AUTH = "shiva1"
APP_SETTINGS="config.DevelopmentConfig"
#SQLALCHEMY_DATABASE_URI="mysql://root:@localhost/skylock_mysql_test"
SQLALCHEMY_DATABASE_URI="mysql://velolabs:toomanyflies@skylocktest.ca7kx2vitaft.us-west-2.rds.amazonaws.com:3306/Skylockdb"
ACCOUNT_SID="ACcfa5178285192224b6376b124eff168d"
AUTH_TOKEN="35ea4b5b8b40ec6c17fe7a1cbbbf2b4e"
OTP_SECRET="G6X5YS7VN6PLOGBV"
TEXT_MESSAGE="+14155992671"
API_KEY="shiva"
GCM_TOKEN="AIzaSyD1UGnXcTUV7huxZV4D3vR6O6ZYmJlmL7U"
ADMIN="4156767921, 4156767922"


"""
#SECRET_KEY = os.environ['SECRET_KEY']
SQLALCHEMY_DATABASE_URI = os.environ['DATABASE_URL']
account_sid = os.environ['ACCOUNT_SID'] #"ACcfa5178285192224b6376b124eff168d"
auth_token  = os.environ['AUTH_TOKEN'] #"35ea4b5b8b40ec6c17fe7a1cbbbf2b4e"
api_key = os.environ['API_KEY']
otp_secret = os.environ['OTP_SECRET']
secret_key = os.environ['SECRET_KEY']
"""

print SQLALCHEMY_DATABASE_URI


"""
export APP_SETTINGS="config.DevelopmentConfig"
export DATABASE_URL="postgresql://localhost/skylock_test"
export ACCOUNT_SID="ACcfa5178285192224b6376b124eff168d"
export AUTH_TOKEN="35ea4b5b8b40ec6c17fe7a1cbbbf2b4e"
export TEXT_MESSAGE="+14155992671"
export API_KEY="shiva"
export SECRET_KEY="shiva"
"""
	