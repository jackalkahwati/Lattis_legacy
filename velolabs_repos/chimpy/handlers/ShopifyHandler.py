from config import ChimpyConfig
from handlers.RestHandler import RestHandler
from handlers.DatabaseHandler import DatabaseHandler
from models.ShopifyCustomer import ShopifyCustomer
from models.ShopifyOrder import ShopifyOrder
import os


class ShopifyHandler:
    def __init__(self):
        self._config = ChimpyConfig()
        self._customer_fetch_size = 250
        self._order_fetch_size = 100
        self.rest_handler = RestHandler()
        self.page_fetch_number = 0
        self.fetch_counter = 0
        self.db_handler = DatabaseHandler()

    def shop_url(self):
        return "https://%s.myshopify.com/admin" % self._config.CHIMPY_SHOPIFY_APP_NAME

    def customers_url(self):
        return os.path.join(self.shop_url(), 'customers.json')

    def orders_url(self):
        return os.path.join(self.shop_url(), 'orders.json') #+ self.rest_handler.construct_query_string({'status': 'all'})

    def order_count_url(self):
        return os.path.join(self.shop_url(), 'orders/count.json')

    def urls_to_register(self):
        return [
            self.customers_url(),
            self.orders_url()
        ]

    def setup(self):
        for url in self.urls_to_register():
            self.rest_handler.register_basic_auth(
                base_url=self.shop_url(),
                target_url=url,
                username=self._config.CHIMPY_SHOPIFY_API_KEY,
                password=self._config.CHIMPY_SHOPIFY_PASSWORD
            )
        print('registered:', self.urls_to_register())
        return None

    def fetch_customers(self):
        self.page_fetch_number = 0
        self.fetch_counter = 0
        self._fetch_customers_helper()
        self.db_handler.close_connection()
        return None

    def _fetch_customers_helper(self):
        url = os.path.join(
            self.customers_url(),
            self.rest_handler.construct_query_string(
                {'limit': self._customer_fetch_size, 'page': self.page_fetch_number}
            )
        )
        rest_data = self.rest_handler.get_request(url=url)
        #path = os.path.join(os.path.dirname(os.path.abspath(__file__)), '../files/shopify_customers.json')
        # with open(path, 'r') as shopify_cusomers:
        #     rest_data = json.load(shopify_cusomers)
        # shopify_cusomers.close()
        try:
            customers = rest_data['customers']
        except KeyError:
            print('Error: no key `customers` in rest data from server.')
            return None
        if len(customers) > 0:
            self._save_customers(customers)
            self.page_fetch_number += 1
            self._fetch_customers_helper()
        return None

    def _update_customers(self, customers):
        shopify_customers = [ShopifyCustomer(customer_dict) for customer_dict in customers]
        customer_dicts = [customer.as_dict() for customer in shopify_customers]
        for customer_dict in customer_dicts:
            self.db_handler.update(
                table='shopify_customer',
                update_dict=customer_dict,
                where_params=['shopify_customer_id'],
                where_values=[customer_dict['shopify_customer_id']]
            )
        return None


    def _save_customers(self, customers):
        customer_table = 'shopify_customer'
        address_table = 'address'
        shopify_customers = [ShopifyCustomer(customer_dict) for customer_dict in customers]
        for customer in shopify_customers:
            for i in range(0, len(customer.addresses)):
                print (customer.shopify_customer_id)
                customer.addresses[i].shopify_customer_id = customer.shopify_customer_id
            db_customer_id = self.db_handler.select(
                table=customer_table,
                columns=['shopify_customer_id'],
                where_params=['shopify_customer_id'],
                where_values=[customer.shopify_customer_id]
            )
            if len(db_customer_id) > 0:
                self.db_handler.update(
                    table=customer_table,
                    update_dict=customer.as_dict(),
                    where_params=['shopify_customer_id'],
                    where_values=[customer.shopify_customer_id]
                )
            else:
                columns_and_values = self.db_handler.convert_list_of_dictionaries_to_columns_and_values(
                    list_of_dictionaries=[customer.as_dict()]
                )
                self.db_handler.insert(
                    table=customer_table,
                    columns=columns_and_values['columns'],
                    values=columns_and_values['values']
                )
                customer.customer_id = self.db_handler.select(
                    table=customer_table,
                    columns=['customer_id'],
                    where_params=['shopify_customer_id'],
                    where_values=[customer.shopify_customer_id]
                )[0]['customer_id']
            customer_address_in_db = self.db_handler.select(
                table=address_table,
                columns=['shopify_customer_id'],
                where_params=['shopify_customer_id'],
                where_values=[customer.shopify_customer_id]
            )
            customer_address_ids = set([address['shopify_customer_id'] for address in customer_address_in_db])
            for address in customer.addresses:
                if address.shopify_customer_id in customer_address_ids:
                    self.db_handler.update(
                        table=address_table,
                        update_dict=address.as_dict(),
                        where_params=['shopify_customer_id'],
                        where_values=[customer.shopify_customer_id]
                    )
                else:
                    columns_and_values = self.db_handler.convert_list_of_dictionaries_to_columns_and_values(
                        list_of_dictionaries=[address.as_dict()]
                    )
                    self.db_handler.insert(
                        table=address_table,
                        columns=columns_and_values['columns'],
                        values=columns_and_values['values']
                    )
        self.fetch_counter += len(customers)
        print('Customers saved:', self.fetch_counter, 'customers for page number:', self.page_fetch_number)
        return None
        
    def fetch_orders(self):
        self.page_fetch_number = 0
        self.fetch_counter = 0
        self._fetch_orders_helper()
        return None

    def _fetch_orders_helper(self):
        order_table = 'shopify_order'
        url = self.orders_url() + self.rest_handler.construct_query_string(
            {'limit': self._order_fetch_size, 'page': self.page_fetch_number}
        )
        orders = self.rest_handler.get_request(url=url)['orders']
        # path = os.path.join(os.path.dirname(__file__), '../files/shopify_orders.json')
        # with open(path, 'r') as order_file:
        #     orders = json.load(order_file)
        # order_file.close()
        for order in orders:
            financial_status = order['financial_status']
            line_items = order['line_items']
            
            for line_item in line_items:
                line_item['shopify_customer_id'] = order['customer']['id']
                line_item['financial_status'] = financial_status
                shopify_order = ShopifyOrder(line_item)
                db_order_id = self.db_handler.select(
                    table=order_table,
                    columns=['shopify_order_id'],
                    where_params=['shopify_order_id'],
                    where_values=[shopify_order.shopify_order_id]
                )
                if len(db_order_id) > 0:
                    self.db_handler.update(
                        table=order_table,
                        update_dict=shopify_order.as_dict(),
                        where_params=['shopify_order_id'],
                        where_values=[shopify_order.shopify_order_id]
                    )
                else:
                    columns_and_values = self.db_handler.convert_list_of_dictionaries_to_columns_and_values(
                        list_of_dictionaries=[shopify_order.as_dict()]
                    )
                    self.db_handler.insert(
                        table=order_table,
                        columns=columns_and_values['columns'],
                        values=columns_and_values['values']
                    )
        self.fetch_counter += len(orders)
        print('Fetched', self.fetch_counter, 'orders from:', url)
        if len(orders) == self._order_fetch_size:
            self.page_fetch_number += 1
            self._fetch_orders_helper()
        return None

    def get_order_count(self):
        count_dict = self.rest_handler.get_request(self.order_count_url())
        try:
            return count_dict['count']
        except KeyError:
            return None
