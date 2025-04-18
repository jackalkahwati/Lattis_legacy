from source.models.NotificaitonModel import NotificationModel


class Maintenance(NotificationModel):
    def __init__(self, params):
        NotificationModel.__init__(self, params)
        self.bike_id = self._get_property(params, 'bike_id')
