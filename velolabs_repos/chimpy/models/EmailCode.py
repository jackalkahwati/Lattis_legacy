from models.Model import Model


class EmailCode(Model):
    def __init__(self,  model_dictionary):
        self.email_code_id = self._get_property('email_code_id', model_dictionary)
        self.email_code = self._get_property('email_code', model_dictionary)
        self.code = self._get_property('code', model_dictionary)
        self.email = self._get_property('email', model_dictionary)
        self.new_email = self._get_property('new_email', model_dictionary)
        self.has_updated = self._get_property('has_updated', model_dictionary)
        self.first_name = self._get_property('first_name', model_dictionary)
        self.last_name = self._get_property('last_name', model_dictionary)
        self.quantity = self._get_property('quantity', model_dictionary)
        self.colors = self._get_property('colors', model_dictionary)
        self.indiegogo_order_number = self._get_property('indiegogo_order_number', model_dictionary)
        self.shopify_customer_id = self._get_property('shopify_customer_id', model_dictionary)
        self.stripe_customer_id = self._get_property('stripe_customer_id', model_dictionary)

    def as_dict(self):
        return {
            'email_code': self.email_code,
            'email': self.email,
            'new_email': self.new_email,
            'code': self.code,
            'has_updated': self.has_updated,
            'first_name': self.first_name,
            'last_name': self.last_name,
            'quantity': self.quantity,
            'colors': self.colors,
            'indiegogo_order_number': self.indiegogo_order_number,
            'shopify_customer_id': self.shopify_customer_id,
            'stripe_customer_id': self.stripe_customer_id
        }
