from models.Model import Model
from models.Address import Address


class IndiegogoCustomer(Model):
    def __init__(self, model_dictionary):
        self.customer_id = self._get_property('customer_id', model_dictionary)
        self.perk_id = self._get_property('perk_id', model_dictionary)
        self.order_number = self._get_property('order_number', model_dictionary)
        self.pledge_id = self._get_property('pledge_id', model_dictionary)
        self.fulfillment_status = self._get_property('fulfillment_status', model_dictionary)
        self.funding_date = self._get_property('funding_date', model_dictionary)
        self.payment_method = self._get_property('payment_method', model_dictionary)
        self.appearance = self._get_property('appearance', model_dictionary)
        self.name = self._get_property('name', model_dictionary)
        self.email = self._get_property('email', model_dictionary)
        self.amount = self._get_property('amount', model_dictionary)
        self.perk = self._get_property('perk', model_dictionary)
        self.first_name = self._get_property('first_name', model_dictionary)
        self.last_name = self._get_property('last_name', model_dictionary)
        self.address = Address(model_dictionary)

    def as_dict(self):
        return {
            'perk_id': self.perk_id,
            'order_number': self.order_number,
            'pledge_id': self.pledge_id,
            'fulfillment_status': self.fulfillment_status,
            'funding_date': self.funding_date,
            'payment_method': self.payment_method,
            'appearance': self.appearance,
            'name': self.name,
            'email': self.email,
            'amount': self.amount,
            'perk': self.perk,
            'first_name': self.first_name,
            'last_name': self.last_name
        }
