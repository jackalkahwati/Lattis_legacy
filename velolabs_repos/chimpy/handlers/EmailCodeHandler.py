from handlers.DatabaseHandler import DatabaseHandler
from models.ShopifyCustomer import ShopifyCustomer
from models.IndiegogoCustomer import IndiegogoCustomer
from models.StripeCustomer import StripeCustomer
from models.StripeCharge import StripeCharge
from models.ShopifyOrder import ShopifyOrder
import hashlib
import random
import json


class EmailCodeHandler:
    def __init__(self):
        self.db_handler = DatabaseHandler()
        self.shopify_customers = []
        self.indiegogo_customers = []
        self.stripe_customers = []
        self.LOCK_VALUE = 150.0

    def get_shopify_customers(self):
        customers = self.db_handler.select(table='shopify_customer')
        self.shopify_customers = [ShopifyCustomer(customer) for customer in customers if customer['orders_count'] > 0]
        return None

    def get_indiegogo_customers(self):
        customers = self.db_handler.select(table='indiegogo_customer')
        self.indiegogo_customers = [IndiegogoCustomer(customer) for customer in customers if customer['amount'] > 90.0]
        return None

    def get_stripe_customers(self):
        customers = self.db_handler.select(table='stripe_customer')
        self.stripe_customers = [StripeCustomer(customer) for customer in customers]
        return None

    def set_shopify_customer_codes(self):
        counter = 0
        for customer in self.shopify_customers:
            if self._updateCustomerCount(customer):
                counter += 1
                continue
            db_email_code_id = self.db_handler.select(
                table='email_code',
                columns=['email_code_id'],
                where_params=['shopify_customer_id'],
                where_values=[customer.shopify_customer_id]
            )
            db_customer_orders = self.db_handler.select(
                table='shopify_order',
                where_params=['shopify_customer_id'],
                where_values=[customer.shopify_customer_id]
            )
            if len(db_email_code_id) == 0 and len(db_customer_orders) > 0:
                colors = []
                quantity = 0
                for db_order in db_customer_orders:
                    shopify_order = ShopifyOrder(db_order)
                    colors += [shopify_order.get_color()]*shopify_order.quantity
                    quantity += shopify_order.quantity
                code_dict = {
                    'email': customer.email,
                    'first_name': customer.first_name,
                    'last_name': customer.last_name,
                    'email_code': self.create_code(str(customer.shopify_customer_id) + customer.email),
                    'code': random.randint(1000, 9999),
                    'shopify_customer_id': customer.shopify_customer_id,
                    'quantity': quantity,
                    'colors': json.dumps(colors)
                }
                columns_and_values = self.db_handler.convert_list_of_dictionaries_to_columns_and_values(
                    list_of_dictionaries=[code_dict]
                )
                self.db_handler.insert(
                    table='email_code',
                    columns=columns_and_values['columns'],
                    values=columns_and_values['values']
                )
                counter += 1
        print('Added codes for', counter, 'Shopify customers')
        return None

    def set_stripe_codes(self):
        counter = 0
        for customer in self.stripe_customers:
            if self._updateCustomerCount(customer):
                counter += 1
                continue
            db_charges = self.db_handler.select(
                'stripe_charge',
                where_params=['stripe_customer_id'],
                where_values=[customer.stripe_customer_id]
            )
            db_email_code_id = self.db_handler.select(
                table='email_code',
                columns=['email_code_id'],
                where_params=['stripe_customer_id'],
                where_values=[customer.stripe_customer_id]
            )
            charges = [StripeCharge(charge_dict) for charge_dict in db_charges]
            quantity = 0
            for charge in charges:
                if charge.paid and charge.amount > 0.0 and charge.status == 'succeeded':
                    # These charges are in cents, so we'll convert them to dollars
                    quantity += self.number_of_locks_from_price((charge.amount - charge.amount_refunded)/100.0)
            if len(db_email_code_id) == 0 and quantity > 0:
                code_dict = {
                    'email': customer.email,
                    'email_code': self.create_code(customer.stripe_customer_id + customer.email),
                    'code': random.randint(1000, 9999),
                    'quantity': quantity,
                    'stripe_customer_id': customer.stripe_customer_id,
                    'colors': json.dumps([])
                }
                columns_and_values = self.db_handler.convert_list_of_dictionaries_to_columns_and_values(
                    list_of_dictionaries=[code_dict]
                )
                self.db_handler.insert(
                    table='email_code',
                    columns=columns_and_values['columns'],
                    values=columns_and_values['values']
                )
                counter += 1
        print('Added codes for', counter, 'Stripe customers')
        return None

    def set_indiegogo_codes(self):
        counter = 0
        for customer in self.indiegogo_customers:
            if self._updateCustomerCount(customer):
                counter += 1
                continue
            db_email_code_id = self.db_handler.select(
                table='email_code',
                columns=['email_code_id'],
                where_params=['indiegogo_order_number'],
                where_values=[customer.order_number]
            )
            quantity = self.number_of_locks_from_price(customer.amount)
            if len(db_email_code_id) == 0 and quantity > 0:
                code_dict = {
                    'email': customer.email,
                    'first_name': customer.first_name,
                    'last_name': customer.last_name,
                    'email_code': self.create_code(str(customer.order_number) + customer.email),
                    'code': random.randint(1000, 9999),
                    'quantity': quantity,
                    'indiegogo_order_number': customer.order_number,
                    'colors': json.dumps([])
                }
                columns_and_values = self.db_handler.convert_list_of_dictionaries_to_columns_and_values(
                    list_of_dictionaries=[code_dict]
                )
                self.db_handler.insert(
                    table='email_code',
                    columns=columns_and_values['columns'],
                    values=columns_and_values['values']
                )
                counter += 1
        print('Added codes for', counter, 'Indiegogo customers')
        return None

    def number_of_locks_from_price(self, price):
        count = price/self.LOCK_VALUE
        bottom = int(count)
        if count - bottom >= 0.5:
            return bottom + 1
        return bottom

    def create_code(self, to_hash):
        return hashlib.md5(to_hash.encode()).hexdigest()

    def create_codes(self):
        self.get_shopify_customers()
        self.get_indiegogo_customers()
        self.get_stripe_customers()
        print(
            'Attempting to create codes for:',
            len(self.shopify_customers) + len(self.stripe_customers) + len(self.indiegogo_customers),
            'customers'
        )
        self.set_shopify_customer_codes()
        self.set_indiegogo_codes()
        self.set_stripe_codes()
        self.db_handler.close_connection()
        return None

    def _updateCustomerCount(self, customer):
        db_users = self.db_handler.select(
            table='email_code',
            columns=['quantity'],
            where_params=['email'],
            where_values=[customer.email]
        )
        if len(db_users) == 0:
            return False
        quantity = db_users[0]['quantity'] if db_users[0]['quantity'] else 0
        quantity_before = quantity
        if type(customer) is IndiegogoCustomer:
            quantity = self.number_of_locks_from_price(customer.amount)
            update_dict = {
                'indiegogo_order_number': customer.order_number,
                'quantity': quantity
            }
        elif type(customer) is ShopifyCustomer:
            db_customer_orders = self.db_handler.select(
                table='shopify_order',
                where_params=['shopify_customer_id'],
                where_values=[customer.shopify_customer_id]
            )
            update_dict = {
                'shopify_customer_id': customer.shopify_customer_id,
                'quantity': quantity + len(db_customer_orders)
            }
        else:
            db_stripe_charges = self.db_handler.select(
                table='stripe_charge',
                where_params=['stripe_customer_id'],
                where_values=[customer.stripe_customer_id]
            )
            for stripe_charge in db_stripe_charges:
                if (stripe_charge['paid'] and not
                        stripe_charge['amount'] > 0.0 and
                            stripe_charge['status'] == 'succeeded'):
                    quantity += self.number_of_locks_from_price(
                        (stripe_charge['amount'] - stripe_charge['amount_refunded'])/100.0
                    )
            update_dict = {
                'stripe_customer_id': customer.stripe_customer_id,
                'quantity': quantity
            }
        if quantity_before != quantity:
            print('Duplicate email:', customer.email, 'quantity before update:', quantity_before, 'after:', quantity)
        self.db_handler.update(
            table='email_code',
            update_dict=update_dict,
            where_params=['email'],
            where_values=[customer.email]
        )
        return True


