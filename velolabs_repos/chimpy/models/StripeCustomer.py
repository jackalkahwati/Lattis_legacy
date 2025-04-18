from models.Model import Model
from models.StripeCard import StripeCard


class StripeCustomer(Model):
    def __init__(self, model_dictionary):
        super().__init__()
        self.customer_id = self._get_property('customer_id', model_dictionary)
        self.stripe_customer_id = self._get_property('id', model_dictionary)
        self.account_balance = self._get_property('account_balance', model_dictionary)
        self.created = self._get_property('created', model_dictionary)
        self.default_source = self._get_property('default_source', model_dictionary)
        self.description = self._get_property('description', model_dictionary)
        self.email = self._get_property('email', model_dictionary)
        self.cards = self._get_cards(model_dictionary)
        if not self.stripe_customer_id:
            self.stripe_customer_id = self._get_property('stripe_customer_id', model_dictionary)

    def _get_cards(self, model_dictionary):
        if 'sources' in model_dictionary and 'data' in model_dictionary['sources']:
            return [StripeCard(card_dict) for card_dict in model_dictionary['sources']['data']]
        return None

    def as_dict(self):
        return {
            'stripe_customer_id': self.stripe_customer_id,
            'account_balance': self.account_balance,
            'created': self.created,
            'default_source': self.default_source,
            'description': self.description,
            'email': self.email
        }
