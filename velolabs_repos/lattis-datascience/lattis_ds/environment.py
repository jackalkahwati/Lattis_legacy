# dev credentials
import os 

default_val = {
    # Main DB
    'MAIN_DB': os.environ.get('MAIN_DB', 'lattis_main'),
    'MAIN_DB_HOST': os.environ.get('MAIN_DB_HOST', 'lattis-app-main.c1a64u1onfsf.us-west-1.rds.amazonaws.com'),
    'MAIN_DB_USERNAME': os.environ.get('MAIN_DB_USERNAME', 'lattis_user'),
    'MAIN_DB_PASS': os.environ.get('MAIN_DB_PASS', '2015lattis_india2015'),
    #USER DB
    'USER_DB': os.environ.get('USER_DB', 'lattis_users'),
    'USER_DB_HOST': os.environ.get('USER_DB_HOST', 'lattis-app-users.c1a64u1onfsf.us-west-1.rds.amazonaws.com'),
    'USER_DB_USERNAME': os.environ.get('USER_DB_USERNAME', 'lattis_user'),
    'USER_DB_PASS': os.environ.get('USER_DB_PASS', '2015lattis_india2015') 
}

env_var = {}

for k, v in default_val.items():
    env_var[k] = os.environ.get(k, v)