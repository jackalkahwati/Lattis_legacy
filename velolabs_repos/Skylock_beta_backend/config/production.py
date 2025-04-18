import os

basedir = os.path.abspath(os.path.dirname(__file__))
#db_path = os.path.join(basedir, '../data.postgresql')

DEBUG = False
SECRET_KEY = 'top-secret!'
#SQLALCHEMY_DATABASE_URI = os.environ.get('DATABASE_URL') or 'postgresql:///' + db_path

SQLALCHEMY_DATABASE_URI = os.environ['DATABASE_URL']
account_sid = os.environ['ACCOUNT_SID'] #"ACcfa5178285192224b6376b124eff168d"
auth_token  = os.environ['AUTH_TOKEN'] #"35ea4b5b8b40ec6c17fe7a1cbbbf2b4e"
api_key = os.environ['API_KEY']