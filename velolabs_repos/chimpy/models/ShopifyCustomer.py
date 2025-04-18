from models.Model import Model
from models.Address import Address
import datetime


class ShopifyCustomer(Model):
    def __init__(self, model_dictionary):
        self.customer_id = self._get_property('customer_id', model_dictionary)
        self.email = self._get_property('email', model_dictionary)
        self.shopify_customer_id = self._get_property('id', model_dictionary)
        self.default_address_id = self._get_default_address_id(model_dictionary)
        self.first_name = self._get_property('first_name', model_dictionary)
        self.last_name = self._get_property('last_name', model_dictionary)
        self.accepts_marketing = self._get_property('accepts_marketing', model_dictionary)
        self.created_at = self._get_property('created_at', model_dictionary)
        self.update_at = self._get_property('update_at', model_dictionary)
        self.orders_count = self._get_property('orders_count', model_dictionary)
        self.last_order_id = self._get_property('last_order_id', model_dictionary)
        self.last_order_name = self._get_property('last_order_name', model_dictionary)
        self.addresses = self._get_addresses(model_dictionary)
        self.total_spent = self._get_total_spent(model_dictionary)
        if not self.shopify_customer_id:
            self.shopify_customer_id = self._get_property('shopify_customer_id', model_dictionary)
        if not self.default_address_id:
            self.default_address_id = self._get_property('default_address_id', model_dictionary)

    def _get_addresses(self, model_dictionary):
        try:
            addresses = model_dictionary['addresses']
        except KeyError:
            return None
        return [Address(address) for address in addresses]

    def _get_total_spent(self, model_dictionary):
        if 'total_spent' in model_dictionary:
            total_spent = model_dictionary['total_spent']
            if type(total_spent) is str:
                total_spent = float(total_spent)
            return total_spent
        return None

    def _get_default_address_id(self, model_dictionary):
        try:
            default_address = model_dictionary['default_address']
        except KeyError:
            return None
        if type(default_address) is dict:
            try:
                default_address_id = default_address['id']
            except KeyError:
                return None
        else:
            default_address_id = default_address
        return default_address_id

    def as_dict(self):
         return {
            'shopify_customer_id': self.shopify_customer_id,
            'email': self.email,
            'default_address_id': self.default_address_id,
            'first_name': self.first_name,
            'last_name': self.last_name,
            'accepts_marketing': self.accepts_marketing,
            'total_spent': self.total_spent,
            'created_at': self.created_at,
            'update_at': self.update_at,
            'orders_count': self.orders_count,
            'last_order_id': self.last_order_id,
            'last_order_name': self.last_order_name
        }
