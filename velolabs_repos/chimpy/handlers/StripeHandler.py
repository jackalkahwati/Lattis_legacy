import stripe
import sys
from config import ChimpyConfig
from models.StripeCustomer import StripeCustomer
from models.StripeCard import StripeCard
from models.StripeCharge import StripeCharge
from handlers.DatabaseHandler import DatabaseHandler


class StripeHandler:
    def __init__(self):
        self.config = ChimpyConfig()
        self.fetch_counter = 0
        self.db_handler = DatabaseHandler()
        self._previous_customer_ids = set()

    def setup(self):
        stripe.api_key = self.config.CHIMPY_STRIPE_API_KEY
        self._previous_customer_ids = set()
        previous_customer_ids = self.db_handler.select(table='stripe_customer', columns=['stripe_customer_id'])
        [self._previous_customer_ids.add(customer_id['stripe_customer_id'])
         for customer_id in previous_customer_ids]
        return None

    def fetch_customers(self, last_customer_id=None):
        if last_customer_id:
            customers_dict = stripe.Customer.list(starting_after=last_customer_id)
        else:
            customers_dict = stripe.Customer.list()
        customers_list = customers_dict['data']
        self._save_customers(customers_list)
        self.fetch_counter += len(customers_list)
        if customers_dict['has_more']:
            last_customer_id = customers_list[len(customers_list) - 1]['id']
            print('Fetched', self.fetch_counter, 'stripe customers')
            self.fetch_customers(last_customer_id=last_customer_id)
        else:
            print('Finished fetching', self.fetch_counter, 'stripe customers')
        self.db_handler.close_connection()
        return None

    def _save_customers(self, customers):
        customer_table = 'stripe_customer'
        card_table = 'stripe_card'
        address_table = 'address'
        stripe_customer = [StripeCustomer(customer_dict) for customer_dict in customers]
        for customer in stripe_customer:
            for i in range(0, len(customer.cards)):
                customer.cards[i].stripe_customer_id = customer.stripe_customer_id
            if customer.stripe_customer_id in self._previous_customer_ids:
                self.db_handler.update(
                    table=customer_table,
                    update_dict=customer.as_dict(),
                    where_params=['stripe_customer_id'],
                    where_values=[customer.stripe_customer_id]
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
                    where_params=['stripe_customer_id'],
                    where_values=[customer.stripe_customer_id]
                )[0]['customer_id']
            customer_cards_in_db = self.db_handler.select(
                table=card_table,
                columns=['stripe_customer_id'],
                where_params=['stripe_customer_id'],
                where_values=[customer.stripe_customer_id]
            )
            customer_card_ids = set([card['stripe_customer_id'] for card in customer_cards_in_db])
            for card in customer.cards:
                if card.stripe_customer_id in customer_card_ids:
                    self.db_handler.update(
                        table=card_table,
                        update_dict=card.as_dict(),
                        where_params=['stripe_customer_id'],
                        where_values=[customer.stripe_customer_id]
                    )
                else:
                    columns_and_values = self.db_handler.convert_list_of_dictionaries_to_columns_and_values(
                        list_of_dictionaries=[card.as_dict()]
                    )
                    self.db_handler.insert(
                        table=card_table,
                        columns=columns_and_values['columns'],
                        values=columns_and_values['values']
                    )
                card.address.stripe_customer_id = customer.stripe_customer_id
                card.address.stripe_card_id = card.stripe_card_id
                db_address_card_id = self.db_handler.select(
                    table=address_table,
                    columns=['stripe_card_id'],
                    where_params=['stripe_card_id'],
                    where_values=[card.stripe_card_id]
                )
                if len(db_address_card_id) > 0:
                    self.db_handler.update(
                        table=address_table,
                        update_dict=card.address.as_dict(),
                        where_params=['address_id'],
                        where_values=[card.address.address_id]
                    )
                else:
                    columns_and_values = self.db_handler.convert_list_of_dictionaries_to_columns_and_values(
                        list_of_dictionaries=[card.address.as_dict()]
                    )
                    self.db_handler.insert(
                        table=address_table,
                        columns=columns_and_values['columns'],
                        values=columns_and_values['values']
                    )
        return None

    def get_charges(self):
        self.fetch_counter = 0
        self._get_charges_helper()
        return None

    def _get_charges_helper(self, last_charge_id=None):
        charges_dict = (stripe.Charge.list(starting_after=last_charge_id)
                        if last_charge_id else stripe.Charge.list())
        charges = [StripeCharge(charge_dict) for charge_dict in charges_dict['data']]
        columns_and_values = self.db_handler.convert_list_of_dictionaries_to_columns_and_values(
            [charge.as_dict() for charge in charges]
        )
        self.db_handler.insert(
            'stripe_charge',
            columns=columns_and_values['columns'],
            values=columns_and_values['values']
        )
        if charges_dict['has_more']:
            print('Fetched:', self.fetch_counter, 'charges')
            self.fetch_counter += len(charges_dict)
            self._get_charges_helper(last_charge_id=charges[len(charges) - 1].stripe_charge_id)
        else:
            print('Completed fetching:', self.fetch_counter, 'charges')
        return None
