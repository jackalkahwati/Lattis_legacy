from source.models.NotificaitonModel import NotificationModel


class Crash(NotificationModel):
    def __init__(self, params):
        NotificationModel.__init__(self, params)
        self.crash_id = self._get_property(params, 'crash_id')
        self.message_sent = self._get_property(params, 'message_sent')
        self.lock_id = self._get_property(params, 'lock_id')
        self.user_id = self._get_property(params, 'user_id')
        self.operator_id = self._get_property(params, 'operator_id')
        self.customer_id = self._get_property(params, 'customer_id')
