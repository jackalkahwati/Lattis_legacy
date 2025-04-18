import boto3
import json
from source.notificaitons.LucyNotification import LucyNotificaiton
from source.queues.LucyWriteQueue import LucyWriteQueue
from source.exceptions.LucyExceptions import QueueDoesNotExistException
from source.utils.logger import logger


class SQSHandler:
    def __init__(self):
        self._sqs = boto3.resource('sqs')
        self._max_messages_to_retrieve = 10
        self._queues = {}

    def get_queue(self, queue_name):
        # queues = self.sqs.queues.all()
        # print('printing queues')
        # for queue in queues:
        #     print('queue:', queue)
        queue = self._sqs.get_queue_by_name(QueueName=queue_name)
        return None

    def _get_queue_from_url(self, queue_url):
        if queue_url in self._queues:
            return self._queues[queue_url]
        queue = self._sqs.Queue(queue_url)
        if not queue:
            raise QueueDoesNotExistException('No Queue with url:', queue_url)
        self._add_to_saved_queues(queue)
        return queue

    def create_queue(self, queue_name):
        queue = self._sqs.create_queue(QueueName=queue_name, Attributes={})
        # You can now access identifiers and attributes
        print(queue.url)
        print(queue.attributes.get('DelaySeconds'))
        return None

    def _send_message(self, queue, message_object):
        message_json = json.dumps(message_object)
        logger('sending message', message_json)
        print('queue url:', queue.attributes)
        response = queue.send_message(MessageBody=message_json)
        logger('Got response for:', message_object, '::', response)
        return None

    def create_message(self, queue_url, message_object):
        try:
            queue = self._get_queue_from_url(queue_url)
        except QueueDoesNotExistException:
            logger('Error: could not send message to:', queue_url, 'there is no queue with that url')
            return None
        self._send_message(queue, message_object)
        return None

    def send_batch_messages(self, queue_url, messages):
        try:
            queue = self._get_queue_from_url(queue_url)
        except QueueDoesNotExistException:
            logger('Error: could not send message to:', queue_url, 'there is no queue with that url')
            return None
        response = queue.send_messages(Entries=messages)
        return response

    def get_messages(self, queue_url, should_delete=False):
        try:
            queue = self._get_queue_from_url(queue_url)
        except QueueDoesNotExistException:
            logger('Error: could not get message from:', queue_url, 'there is no queue with that url')
            return None
        messages = queue.receive_messages(
            QueueUrl=queue_url,
            MaxNumberOfMessages=self._max_messages_to_retrieve,
            AttributeNames=['All']
        )
        for message in messages:
            if should_delete:
                message.delete()
        return messages

    def _add_to_saved_queues(self, queue):
        if queue.url not in self._queues:
            self._queues[queue.url] = queue
        return None
