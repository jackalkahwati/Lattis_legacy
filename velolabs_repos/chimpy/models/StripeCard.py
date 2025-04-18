from models.Model import Model
from models.Address import Address


class StripeCard(Model):
    def __init__(self, model_dictionary):
        super().__init__()
        self.card_id = self._get_property('card_id', model_dictionary)
        self.stripe_card_id = self._get_property('id', model_dictionary)
        self.country = self._get_property('country', model_dictionary)
        self.brand = self._get_property('brand', model_dictionary)
        self.stripe_customer_id = self._get_property('customer', model_dictionary)
        self.exp_month = self._get_property('exp_month', model_dictionary)
        self.exp_year = self._get_property('exp_year', model_dictionary)
        self.fingerprint = self._get_property('fingerprint', model_dictionary)
        self.funding = self._get_property('funding', model_dictionary)
        self.last4 = self._get_property('last4', model_dictionary)
        self.name = self._get_property('name', model_dictionary)
        self.address = Address(model_dictionary)
        if not self.stripe_card_id:
            self.stripe_card_id = self._get_property('stripe_card_id', model_dictionary)
        if not self.stripe_customer_id:
            self.stripe_customer_id = self._get_property('stripe_customer_id', model_dictionary)

    def as_dict(self):
        return {
            'stripe_card_id': self.stripe_card_id,
            'brand': self.brand,
            'country': self.country,
            'stripe_customer_id': self.stripe_customer_id,
            'exp_month': self.exp_month,
            'exp_year': self.exp_year,
            'fingerprint': self.fingerprint,
            'funding': self.funding,
            'last4': self.last4,
            'name': self.name
        }
