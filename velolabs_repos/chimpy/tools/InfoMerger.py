from handlers.DatabaseHandler import DatabaseHandler
import os


class InfoMerger:
    def __init__(self):
        self.email_code_customers = []
        self.db_handler = DatabaseHandler()
        self.complete_customers = []
        self.addresses = []
        self.column_order = [
            'email',
            'new_email',
            'first_name',
            'last_name',
            'has_updated',
            'quantity',
            'colors',
            'city',
            'country',
            'address1',
            'address2',
            'state',
            'zip',
            'phone',
            'indiegogo_order_number',
            'shopify_customer_id',
            'stripe_customer_id',
            'financial_status'
        ]

    def create_csv(self):
        self.email_code_customers = self.db_handler.select('email_code')
        self.addresses = self.db_handler.select('address')
        self.shopify_orders = self.db_handler.select('shopify_order')
        self.complete_customers = [self.make_customer(customer) for customer in self.email_code_customers]
        path = os.path.join(os.path.dirname(os.path.abspath(__file__)), '../files/merged_customers.csv')
        with open(path, 'w') as merged_customers_file:
            columns = ','.join(self.column_order) + '\n'
            merged_customers_file.write(columns)
            for customer in self.complete_customers:
                if customer:
                    merged_customers_file.write(self.customer_as_string(customer) + '\n')
        merged_customers_file.close()
        print('Wrote', len(self.complete_customers), 'to', path)
        return None

    def make_customer(self, email_code_customer):
        customer_address = None
        for address in self.addresses:
            if ((email_code_customer['indiegogo_order_number'] and address['indiegogo_customer_id'] and
                        email_code_customer['indiegogo_order_number'] == address['indiegogo_customer_id']) or
                (email_code_customer['stripe_customer_id'] and address['stripe_customer_id'] and
                        email_code_customer['stripe_customer_id'] == address['stripe_customer_id']) or
                (email_code_customer['shopify_customer_id'] and address['shopify_customer_id'] and 
                        email_code_customer['shopify_customer_id'] == address['shopify_customer_id'])):
                
                    
                for shopify_order in self.shopify_orders:
                    if (email_code_customer['shopify_customer_id'] == shopify_order['shopify_customer_id']):
                        address['financial_status'] = shopify_order['financial_status']
                if 'financial_status' not in address:
                    address['financial_status']= 'No Status'
  
                customer_address = address
                break
        if customer_address:
            print('Found address for', email_code_customer['email'])
            return {
                'email': self.clean(email_code_customer['email']),
                'new_email': self.clean(email_code_customer['new_email']),
                'first_name': self.clean(email_code_customer['first_name']),
                'last_name': self.clean(email_code_customer['last_name']),
                'has_updated': self.clean((str(email_code_customer['has_updated']))),
                'quantity': self.clean(str(email_code_customer['quantity'])),
                'colors': self.clean(email_code_customer['colors']),
                'city': self.clean(customer_address['city']),
                'country': self.clean(customer_address['country']),
                'address1': self.clean(customer_address['address1']),
                'address2': self.clean(customer_address['address2']),
                'state': self.clean(customer_address['state']),
                'zip': self.clean(customer_address['zip']),
                'phone': self.clean(customer_address['phone']),
                'indiegogo_order_number': self.clean(str(email_code_customer['indiegogo_order_number'])),
                'shopify_customer_id': self.clean(str(email_code_customer['shopify_customer_id'])),
                'stripe_customer_id': self.clean(email_code_customer['stripe_customer_id']),
                'financial_status': customer_address['financial_status']
            }
        print('Could not retrieve address for email:', email_code_customer['email'])
        return None




    def customer_as_string(self, customer):
        customer_list = []
        for column in self.column_order:
            value = customer[column]
            customer_list.append(value if value else '')
        return ','.join(customer_list)

    def clean(self, value):
        if not value or value.lower() == 'none':
            return ''
        return value.replace(',', ' ')
