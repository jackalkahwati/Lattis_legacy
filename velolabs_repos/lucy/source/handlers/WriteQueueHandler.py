from source.handlers.SqsHandler import SQSHandler
from source.utils.logger import logger
from source.queues.FifoQueue import FifoQueue
from source.models.SqsWriteMessage import SqsWriteMessage
from threading import Timer


class WriteQueueHandler:
    def __init__(self, queue_url, message_type, interval=1):
        self._queue_url = queue_url
        self._sqsHandler = SQSHandler()
        self._queue = FifoQueue()
        self._batch_size = 10
        self._message_type = message_type
        self._enqueued_messages = set()
        self._messages_in_flight = {}
        self._interval = interval

    def start(self):
        self.write_to_queue()
        return None

    def add_message(self, message):
        try:
            sqs_message = SqsWriteMessage(
                message=message.__dict__,
                message_type=self._message_type,
                id_number=getattr(message, self._message_type + '_id')
            )
        except AttributeError:
            logger('Error: could not add message:', message, 'no property:', self._message_type + '_id')
            return None
        identifier = sqs_message.identifier()
        if identifier not in self._enqueued_messages:
            self._queue.enqueue(sqs_message)
            self._enqueued_messages.add(identifier)
        return None

    def add_messages(self, messages):
        before_count = self._queue.count()
        [self.add_message(message) for message in messages]
        logger('Added:', self._queue.count() - before_count, 'unique messages to queue')
        return None

    def write_to_queue(self):
        timer = Timer(self._interval, self._write)
        timer.start()
        return None

    def _write(self):
        if self._queue.count() == 0:
            logger('No messages in queue to write')
            self.write_to_queue()
            return None
        messages = self._queue.dequeue_multiple(self._batch_size)
        for message in messages:
            self._enqueued_messages.remove(message.identifier())
            self._messages_in_flight[message.identifier()] = message
        sqs_messages = [message.for_sqs() for message in messages]
        logger('write queue handler writing', len(sqs_messages), 'messages')
        response = self._sqsHandler.send_batch_messages(queue_url=self._queue_url, messages=sqs_messages)
        if response:
            if 'Successful' in response:
                for messages_response in response['Successful']:
                    if messages_response['Id'] in self._messages_in_flight:
                        del self._messages_in_flight[messages_response['Id']]
            if 'Failed' in response:
                for messages_response in response['Failed']:
                    if messages_response['Id'] in self._messages_in_flight:
                        message = self._messages_in_flight[messages_response['Id']]
                        self.add_message(message.for_sqs())
        logger('There are:', self._queue.count(), 'messages in the local queue.')
        self.write_to_queue()
        return None
