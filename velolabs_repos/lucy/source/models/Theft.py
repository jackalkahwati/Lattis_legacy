from source.models.NotificaitonModel import NotificationModel


class Theft(NotificationModel):
    def __init__(self, params):
        NotificationModel.__init__(self, params)
        self.theft_id = self._get_property(params, 'theft')
