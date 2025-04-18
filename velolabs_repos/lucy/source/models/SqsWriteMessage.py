import json

class SqsWriteMessage:
    def __init__(self, message, message_type, id_number):
        self._message = message
        self._message_type = message_type
        self._id = id_number

    def identifier(self):
        return '{0}-{1}'.format(self._message_type, self._id)

    def for_sqs(self):
        return {'Id': self.identifier(), 'MessageBody': json.dumps(self._message)}
