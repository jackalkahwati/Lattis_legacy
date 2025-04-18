from config import ChimpyConfig
from handlers.RestHandler import RestHandler
from handlers.DatabaseHandler import DatabaseHandler
from models.IndiegogoCustomer import IndiegogoCustomer
import os


class IndiegogoHandler:
    def __init__(self):
        self._config = ChimpyConfig()
        self.rest_handler = RestHandler()
        self.db_handler = DatabaseHandler()
        self._customer_keys = [
            'perk_id',
            'order_number',
            'pledge_id',
            'fulfillment_status',
            'funding_date',
            'payment_method',
            'appearance',
            'name',
            'email',
            'amount',
            'perk',
            'name',
            'address1',
            'address2',
            'city',
            'state',
            'zip',
            'country'
        ]

    def _campaign_url(self):
        return os.path.join(
            self._config.INDIEGOGO_BASE_URL,
            'accounts/5687580/contributions.json?access_token=%s' % self._config.CHIMPY_INDIEGOGO_ACCESS_TOKEN
        )

    def fetch_campaigns(self):
        return self.rest_handler.get_request(url=self._campaign_url())

    def parse_contributions_csv(self):
        file_path = os.path.join(os.path.dirname(__file__), '../files/indiegogo_contributions.csv')
        with open(file_path, 'r') as contributions:
            data = contributions.read()
        contributions.close()
        contribution_lines = data.split('\n')
        del contribution_lines[0]
        customers = []
        for line in contribution_lines:
            parts = line.split(',')
            if len(parts) <= 1:
                continue
            customers.append(IndiegogoCustomer(self._parse_line(parts)))
        self._save_customers(customers)
        return customers

    def _parse_line(self, parts):
        customer_dict = {}
        for index in range(len(self._customer_keys)):
            value = parts[index].replace('=', '').replace('"', '')
            if self._customer_keys[index] == 'name':
                names = value.split(' ')
                customer_dict['first_name'] = self._assign_value(names[0])
                try:
                    customer_dict['last_name'] = self._assign_value(names[1])
                except IndexError:
                    customer_dict['last_name'] = None
            elif self._customer_keys[index] == 'amount':
                customer_dict[self._customer_keys[index]] = float(value.replace('$', ''))
            else:
                customer_dict[self._customer_keys[index]] = self._assign_value(value)
        return customer_dict

    def _assign_value(self, value):
        if value == '':
            return None
        return value

    def _save_customers(self, customers):
        customer_table = 'indiegogo_customer'
        address_table = 'address'
        for customer in customers:
            if not customer.order_number:
                continue
            db_customer = self.db_handler.select(
                table=customer_table,
                columns=['order_number'],
                where_params=['order_number'],
                where_values=[customer.order_number]
            )
            if len(db_customer) > 0:
                self.db_handler.update(
                    table=customer_table,
                    update_dict=customer.as_dict(),
                    where_params=['order_number'],
                    where_values=[customer.order_number]
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
            customer.address.indiegogo_customer_id = customer.order_number
            db_address = self.db_handler.select(
                table=address_table,
                columns=['indiegogo_customer_id'],
                where_params=['indiegogo_customer_id'],
                where_values=[customer.address.indiegogo_customer_id]
            )
            if len(db_address) > 0:
                self.db_handler.update(
                    table=address_table,
                    update_dict=customer.address.as_dict(),
                    where_params=['indiegogo_customer_id'],
                    where_values=[customer.address.indiegogo_customer_id]
                )
            else:
                columns_and_values = self.db_handler.convert_list_of_dictionaries_to_columns_and_values(
                    list_of_dictionaries=[customer.address.as_dict()]
                )
                self.db_handler.insert(
                    table=address_table,
                    columns=columns_and_values['columns'],
                    values=columns_and_values['values']
                )
        return None
