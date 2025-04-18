import os
from handlers.DatabaseHandler import DatabaseHandler


class InfoMergerCSV:
    def __init__(self):
        self.db_handler = DatabaseHandler()
        self.updated_customers = []
        self.customers = []
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
            'manual_update'
        ]
        self.email_code_update_columns = [
            'new_email',
            'first_name',
            'last_name',
            'has_updated',
            'quantity',
            'colors'
        ]
        self.address_update_columns = [
            'city',
            'country',
            'address1',
            'address2',
            'state',
            'zip',
            'phone'
        ]

    def run(self):
        self.read_csv()
        for customer in self.updated_customers:
            self.save_customer_to_db(customer)
        return None

    def read_csv(self):
        self.customers_from_csv = []
        path = os.path.join(os.path.dirname(os.path.abspath(__file__)), '../files/merged_customers_updated.csv')
        with open(path, 'r') as customers_file:
            skip_first = True
            for line in customers_file:
                if skip_first:
                    skip_first = False
                    continue
                customer = self.create_customer_from_csv(line.strip())
                if customer['manual_update']:
                    self.updated_customers.append(customer)
        customers_file.close()
        return None

    def create_customer_from_csv(self, line):
        csv_columns = line.split(',')
        customer = {}
        for i in range(len(self.column_order)):
            column = self.column_order[i]
            csv_column = csv_columns[i]
            if column == 'colors':
                csv_column = self.format_colors_column(csv_column)
            elif column == 'manual_update':
                csv_column = csv_column == '1'
            elif column == 'has_updated':
                test = csv_column
                csv_column = csv_column == '1'
                if 'michael@michaelfargo.com' in line:
                    print('before', test)
                    print('after', csv_column)
            elif column == 'quantity' and csv_column != '':
                csv_column = int(csv_column)
            elif column == 'shopify_customer_id' and csv_column != '':
                csv_column = int(csv_column)
            customer[column] = csv_column
        return customer

    def format_colors_column(self, colors):
        if colors == '[]':
            return colors
        colors = colors.replace('"', '').replace('[', '').replace(']', '')
        parts = colors.split(' ')
        colors = '['
        counter = 0
        for color in parts:
            colors += '"' + color + '"'
            if counter != len(parts) - 1:
                colors += ','
            counter += 1
        colors += ']'
        return colors

    def save_customer_to_db(self, customer):
        self.update_email_code_table(customer)
        self.update_address_table(customer)
        return None

    def update_email_code_table(self, customer):
        db_customers = self.db_handler.select(
            table='email_code',
            where_params=['email'],
            where_values=[customer['email']]
        )
        if not db_customers or len(db_customers) == 0:
            print(
                'Could not update customer:',
                customer['email'],
                'There in no matching email number present in the db.'
            )
            return None
        db_customer = db_customers[0]
        update_dict = {}
        for column in self.email_code_update_columns:
            if db_customer[column] != customer[column]:
                if customer[column] == '' and db_customer[column] != None:
                    update_dict[column] = customer[column]
                elif customer[column] != '':
                    update_dict[column] = customer[column]
        if len(update_dict.keys()) > 0:
            print('Updating customer:', customer['email'], 'properties:', update_dict, '\n')
            self.db_handler.update(
                table='email_code',
                update_dict=update_dict,
                where_params=['email_code_id'],
                where_values=[db_customer['email_code_id']]
            )
        return None

    def update_address_table(self, customer):
        db_customers = self.db_handler.select(
            table='email_code',
            where_params=['email'],
            where_values=[customer['email']]
        )
        if not db_customers or len(db_customers) == 0:
            print(
                'Could not update address for customer:',
                customer['email'],
                'There in no matching email number present in the db.'
            )
            return None
        db_customer = db_customers[0]
        where_param = None
        where_value = None
        if db_customer['shopify_customer_id']:
            where_param = 'shopify_customer_id'
            where_value = db_customer['shopify_customer_id']
        elif db_customer['indiegogo_order_number']:
            where_param = 'indiegogo_customer_id'
            where_value = db_customer['indiegogo_order_number']
        elif db_customer['stripe_customer_id']:
            where_param = 'stripe_customer_id'
            where_value = db_customer['stripe_customer_id']
        if not where_param:
            print(
                'Could not update address for customer:',
                db_customer,
                'There in no customer id number present in the db.'
            )
            return None
        db_addresses = self.db_handler.select(
            table='address',
            where_params=[where_param],
            where_values=[where_value]
        )
        if not db_addresses or len(db_addresses) == 0:
            print(
                'Could not update address for customer:',
                customer['email'],
                'There in no matching address number present in the db for:',
                where_param,
                '=',
                where_value
            )
            return None
        db_address = db_addresses[0]
        update_dict = {}
        for column in self.address_update_columns:
            if db_address[column] != customer[column]:
                if customer[column] == '' and db_address[column] != None:
                    update_dict[column] = customer[column]
                elif customer[column] != '':
                    update_dict[column] = customer[column]
        if len(update_dict.keys()) > 0:
            print('Updating customer:', customer['email'], 'address properties:', update_dict, '\n')
            self.db_handler.update(
                table='address',
                update_dict=update_dict,
                where_params=[where_param],
                where_values=[where_value]
            )
        return None

