from models.Model import Model


class StripeCharge(Model):
    def __init__(self, model_dictionary):
        self.charge_id = self._get_property('card_id', model_dictionary)
        self.stripe_charge_id = self._get_property('stripe_charge_id', model_dictionary)
        self.amount = self._get_property('amount', model_dictionary)
        self.amount_refunded = self._get_property('amount_refunded', model_dictionary)
        self.balance_transaction = self._get_property('balance_transaction', model_dictionary)
        self.created = self._get_property('created', model_dictionary)
        self.currency = self._get_property('currency', model_dictionary)
        self.stripe_customer_id = self._get_property('stripe_customer_id', model_dictionary)
        self.description = self._get_property('description', model_dictionary)
        self.object = self._get_property('object', model_dictionary)
        self.paid = self._get_property('paid', model_dictionary)
        self.receipt_email = self._get_property('receipt_email', model_dictionary)
        self.receipt_number = self._get_property('receipt_number', model_dictionary)
        self.refunded = self._get_property('refunded', model_dictionary)
        self.stripe_card_id = self._get_property('stripe_card_id', model_dictionary)
        self.status = self._get_property('status', model_dictionary)
        if not self.stripe_card_id:
            self.stripe_charge_id = self._get_property('id', model_dictionary)
        if not self.stripe_card_id and 'source' in model_dictionary:
            self.stripe_card_id = self._get_property('id', model_dictionary['source'])
        if not self.stripe_customer_id:
            self.stripe_customer_id = self._get_property('customer', model_dictionary)

    def as_dict(self):
        return {
            'stripe_charge_id': self.stripe_charge_id,
            'amount': self.amount,
            'amount_refunded': self.amount_refunded,
            'balance_transaction': self.balance_transaction,
            'created': self.created,
            'currency': self.currency,
            'stripe_customer_id': self.stripe_customer_id,
            'description': self.description,
            'object': self.object,
            'paid': self.paid,
            'receipt_email': self.receipt_email,
            'receipt_number': self.receipt_number,
            'refunded': self.refunded,
            'stripe_card_id': self.stripe_card_id,
            'status': self.status
        }
