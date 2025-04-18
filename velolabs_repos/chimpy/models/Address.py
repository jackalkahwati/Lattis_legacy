from models.Model import Model


class Address(Model):
    def __init__(self, model_dictionary):
        self.address_id = self._get_property('address_id', model_dictionary)
        self.city = self._get_property('city', model_dictionary)
        self.country = self._get_property('country', model_dictionary)
        self.address1 = self._get_property('address1', model_dictionary)
        self.address2 = self._get_property('address2', model_dictionary)
        self.state = self._get_property('state', model_dictionary)
        self.zip = self._get_property('zip', model_dictionary)
        self.phone = self._get_property('phone', model_dictionary)
        self.shopify_customer_id = self._get_property('shopify_customer_id', model_dictionary)
        self.stripe_customer_id = self._get_property('stripe_customer_id', model_dictionary)
        self.stripe_card_id = self._get_property('stripe_card_id', model_dictionary)
        self.indiegogo_customer_id = self._get_property('indiegogo_customer_id', model_dictionary)
        if not self.address1:
            self.address1 = self._get_property('address_line1', model_dictionary)
        if not self.address2:
            self.address2 = self._get_property('address_line2', model_dictionary)
        if not self.state:
            self.state = self._get_property('address_state', model_dictionary)
        if not self.state:
            self.state = self._get_property('province', model_dictionary)
        if not self.city:
            self.city = self._get_property('address_city', model_dictionary)
        if not self.country:
            self.country = self._get_property('address_country', model_dictionary)
        if not self.address_id:
            self.address_id = self._get_property('id', model_dictionary)

    def as_dict(self):
        return {
            'city': self.city,
            'country': self.country,
            'address1': self.address1,
            'address2': self.address2,
            'state': self.state,
            'zip': self.zip,
            'phone': self.phone,
            'shopify_customer_id': self.shopify_customer_id,
            'stripe_customer_id': self.stripe_customer_id,
            'stripe_card_id': self.stripe_card_id,
            'indiegogo_customer_id': self.indiegogo_customer_id
        }
