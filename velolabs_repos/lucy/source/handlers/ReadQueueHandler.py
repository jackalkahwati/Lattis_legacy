from source.handlers.SqsHandler import SQSHandler
from threading import Timer
from source.utils.logger import logger


class ReadQueueHandler:
    def __init__(self, queue_url, callback, should_delete=True, interval=1):
        self._queue_url = queue_url
        self._callback = callback
        self._should_delete = should_delete
        self._interval = interval
        self._sqsHandler = SQSHandler()

    def start_reading(self):
        timer = Timer(self._interval, self._read)
        timer.start()
        return None

    def _read(self):
        self._callback(self._sqsHandler.get_messages(self._queue_url, self._should_delete))
        self.start_reading()
        return None
