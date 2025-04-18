import os


class ChimpyConfig():
    def __init__(self):
        self.CHIMPY_APP_NAME = os.environ['CHIMPY_APP_NAME']
        self.CHIMPY_SHOPIFY_APP_NAME = os.environ['CHIMPY_SHOPIFY_APP_NAME']
        self.CHIMPY_SHOPIFY_API_KEY = os.environ['CHIMPY_SHOPIFY_API_KEY']
        self.CHIMPY_SHOPIFY_PASSWORD = os.environ['CHIMPY_SHOPIFY_PASSWORD']
        self.CHIMPY_SHOPIFY_SECRET_TOKEN = os.environ['CHIMPY_SHOPIFY_SECRET_TOKEN']
        self.CHIMPY_MAILCHIMP_API_KEY = os.environ['CHIMPY_MAILCHIMP_API_KEY']
        self.CHIMPY_STRIPE_API_KEY = os.environ['CHIMPY_STRIPE_API_KEY']
        self.CHIMPY_STRIPE_API_BASE_URL = os.environ['CHIMPY_STRIPE_API_BASE_URL']
        self.CHIMPY_INDIEGOGO_ACCESS_TOKEN = os.environ['CHIMPY_INDIEGOGO_ACCESS_TOKEN']
        self.CHIMPY_INDIEGOGO_API_TOKEN = os.environ['CHIMPY_INDIEGOGO_API_TOKEN']
        self.CHIMPY_INDIEGOGO_REFRESH_TOKEN = os.environ['CHIMPY_INDIEGOGO_REFRESH_TOKEN']
        self.mailchimp_list_ids = MailChimpListIds()
        self.INDIEGOGO_BASE_URL = 'https://api.indiegogo.com/1.1/'
        self.EMAIL_UPDATE_URL = 'https://ellipselock.com/order_update'

    def mail_chimp_data_center(self):
        data_center = ''
        try:
            data_center = self.CHIMPY_MAILCHIMP_API_KEY.split('-')[1]
        except IndexError as e:
            print('Mail chimp data center not in api key', e)
        except Exception as e:
            print('Unknown error getting mail chimp data center', e)
        return data_center

    def mail_chimp_api_base_url(self):
        return 'https://' + self.mail_chimp_data_center() + '.api.mailchimp.com/3.0/'

    def mail_chimp_authorization_key(self):
        return 'Basic ' + self.CHIMPY_MAILCHIMP_API_KEY


class MailChimpListIds:
    def __init__(self):
        self.PRE_ORDER_CUSTOMERS = '07dccdba6b'
        self.EMAIL_UPDATE = 'd8def8148c'
        self.TEST = '8cf517b56e'


class DatabaseConfig:
    def __init__(self):
        self.host = os.environ['CHIMPY_DB_HOST']
        self.user = os.environ['CHIMPY_DB_USER']
        self.password = os.environ['CHIMPY_DB_PASSWORD']
        self.database = os.environ['CHIMPY_DB_DATABASE']
        self.charset = os.environ['CHIMPY_DB_CHARSET']
