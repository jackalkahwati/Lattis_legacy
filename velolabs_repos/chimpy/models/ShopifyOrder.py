from models.Model import Model


class ShopifyOrder(Model):
    def __init__(self, model_dictionary):
        self.order_id = self._get_property('customer_id', model_dictionary)
        self.shopify_order_id = self._get_property('id', model_dictionary)
        self.shopify_customer_id = self._get_property('shopify_customer_id', model_dictionary)
        self.name = self._get_property('name', model_dictionary)
        self.variant_title = self._get_property('variant_title', model_dictionary)
        self.quantity = self._get_property('quantity', model_dictionary)
        self.sku = self._get_property('sku', model_dictionary)
        self.shopify_address_id = self._get_shopify_address_id(model_dictionary)
        self.product_id = self._get_property('product_id', model_dictionary)
        self.gift_card = self._get_property('gift_card', model_dictionary)
        self.taxable = self._get_property('taxable', model_dictionary)
        self.vendor = self._get_property('vendor', model_dictionary)
        self.total_discount = self._get_property('total_discount', model_dictionary)
        self.financial_status = self._get_property('financial_status', model_dictionary)
        self.price = self._get_property('price', model_dictionary)
        self.title = self._get_property('title', model_dictionary)
        self.requires_shipping = self._get_property('requires_shipping', model_dictionary)
        if not self.shopify_order_id:
            self.shopify_order_id = self._get_property('shopify_order_id', model_dictionary)

    def _get_shopify_address_id(self, model_dictionary):
        if 'shopify_address_id' in model_dictionary:
            return model_dictionary['shopify_address_id']
        try:
            address_dict = model_dictionary['destination_location']
        except KeyError:
            return None
        return address_dict['id']

    def get_color(self):
        if self.variant_title == 'Charcoal Grey':
            return 'grey'
        elif self.variant_title == 'Midnight Blue' or self.variant_title == 'Special Edition Blue':
            return 'blue'
        elif self.variant_title == 'Pearl White':
            return 'white'
        return None

    def as_dict(self):
        return {
            'shopify_order_id': self.shopify_order_id,
            'shopify_customer_id': self.shopify_customer_id,
            'name': self.name,
            'variant_title': self.variant_title,
            'quantity': self.quantity,
            'sku': self.sku,
            'shopify_address_id': self.shopify_address_id,
            'product_id': self.product_id,
            'gift_card': self.gift_card,
            'taxable': self.taxable,
            'vendor': self.vendor,
            'total_discount': self.total_discount,
            'financial_status': self.financial_status,
            'price': self.price,
            'title': self.title,
            'requires_shipping': self.requires_shipping
        }
