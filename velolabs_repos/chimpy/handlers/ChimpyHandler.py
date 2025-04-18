from handlers.MailChimpHandler import MailChimpHandler
from handlers.ShopifyHandler import ShopifyHandler
from handlers.StripeHandler import StripeHandler
from handlers.IndiegogoHandler import IndiegogoHandler
from handlers.DatabaseHandler import DatabaseHandler
from handlers.EmailCodeHandler import EmailCodeHandler
from tools.InfoMerger import InfoMerger
from tools.InfoMergerCSV import InfoMergerCSV


class ChimpyHandler:
    def __init__(self):
        self.db_handler = DatabaseHandler()
        self.number_of_errors = 0

    def run_chimpy(self):
        #self.sync()
        #self.set_email_codes()
        #self.update_mailchimp_lists()
        self.parse_db_as_csv()
        #self.write_csv_to_db()
        #self.sync_stripe()
        return None

    def update_mailchimp_lists(self):
        chimp_handler = MailChimpHandler()
        chimp_handler.update_list()
        #chimp_handler.get_batch_errors()
        #print(chimp_handler.get_batch_error_info())
        #print(chimp_handler.get_batch_status('d172fb8906'))
        return None

    def set_email_codes(self):
        code_handler = EmailCodeHandler()
        code_handler.create_codes()
        return None

    def sync(self):
        self.sync_indiegogo()
        #self.sync_stripe()
        #self.sync_shopify()
        return None

    def sync_shopify(self):
        shopify_handler = ShopifyHandler()
        shopify_handler.setup()
        shopify_handler.fetch_customers()
        shopify_handler.fetch_orders()
        #print('order count', shopify_handler.get_order_count())
        shopify_handler.db_handler.close_connection()
        return None

    def sync_stripe(self):
        stripe_handler = StripeHandler()
        stripe_handler.setup()
        stripe_handler.fetch_customers()
        stripe_handler.get_charges()
        stripe_handler.db_handler.close_connection()
        return None

    def sync_indiegogo(self):
        indiegogo_handler = IndiegogoHandler()
        indiegogo_handler.parse_contributions_csv()
        return None

    def parse_db_as_csv(self):
        merger = InfoMerger()
        merger.create_csv()
        return None

    def write_csv_to_db(self):
        merger = InfoMergerCSV()
        merger.run()
        return None
