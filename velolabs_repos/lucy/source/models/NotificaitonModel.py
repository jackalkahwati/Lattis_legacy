class NotificationModel:
    def __init__(self, params):
        self.date = self._get_property(params, 'date')
        self.type = self._get_property(params, 'type')
        self.type_id = None
        self.rank = 0.0

    def _get_property(self, params, prop):
        try:
            return params[prop]
        except KeyError:
            return None

