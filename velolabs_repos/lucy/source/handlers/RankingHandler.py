from source.utils.logger import logger
from source.models.Crash import Crash
from source.models.Theft import Theft
from source.models.Maintenance import Maintenance
from source.constants.NotificationTypes import NotificationTypes
from source.utils.utils import asymptotic_score
from source.exceptions.LucyExceptions import NotificationModelAttributeError
import json


class RankingHandler:
    def __init__(self):
        self._notification_types = NotificationTypes()

    def rank(self, message):
        try:
            message=json.loads(message.body)
        except TypeError:
            logger('Error: could not rank message:', message, 'could not decode json')
            return None
        try:
            message_type = message['type']
        except KeyError:
            logger('Error: could not rank message. No type in:', message)
            return
        try:
            if message_type == self._notification_types.crash:
                return self._rank_crash(message)
            elif message_type == self._notification_types.theft:
                return self._rank_theft(message)
            elif message_type == self._notification_types.maintenance:
                return self._rank_maintenance(message,)
            else:
                logger('Could not rank message:', message, message_type, 'is unknown.')
                return None
        except NotificationModelAttributeError:
            logger('Error failed to rank message:', message, 'missing necessary attribute.')
        return None

    def _rank_crash(self, crash_message):
        crash = Crash(crash_message)
        crash.type_id = crash.crash_id
        crash.rank = self._rank(crash)
        return crash

    def _rank_theft(self, theft_message):
        theft = Theft(theft_message)
        theft.type_id = theft.theft_id
        theft.rank = self._rank(theft)
        return theft

    def _rank_maintenance(self, maintenance_message):
        maintenance = Maintenance(maintenance_message)
        maintenance.type_id = maintenance.bike_id
        maintenance.rank = self._rank(maintenance)
        return maintenance

    def _rank(self, rank_model):
        try:
            return asymptotic_score(self._issue_score(rank_model.type), rank_model.date)
        except AttributeError:
            logger('Error: failed to rank model:', rank_model, 'It is missing a property.')
            raise NotificationModelAttributeError

    def _issue_score(self, notification_type):
        score = 0.0
        if notification_type == self._notification_types.crash:
            score = 0.9
        elif notification_type == self._notification_types.damage:
            score = 0.1
        elif notification_type == self._notification_types.locking_issue:
            score = 0.85
        elif notification_type == self._notification_types.maintenance:
            score = 0.3
        elif notification_type == self._notification_types.out_of_parking:
            score = 0.5
        elif notification_type == self._notification_types.request_for_help:
            score = 0.95,
        elif notification_type == self._notification_types.theft:
            score = 0.6
        return score



