from source.queues.LucyReadQueue import LucyReadQueue
from source.queues.LucyWriteQueue import LucyWriteQueue
from source.handlers.ReadQueueHandler import ReadQueueHandler
from source.handlers.WriteQueueHandler import WriteQueueHandler
from source.handlers.RankingHandler import RankingHandler
from source.utils.logger import logger


class Retriever:
    def __init__(self):
        self._read_queue = LucyReadQueue()
        self._write_queue = LucyWriteQueue()
        self._read_queue_handler = ReadQueueHandler(self._read_queue.url, self._handle_messages)
        self._write_queue_handler = WriteQueueHandler(self._write_queue.url, 'crash', 0.5)
        self._ranking_handler = RankingHandler()

    def start(self):
        self._read_queue_handler.start_reading()
        self._write_queue_handler.start()
        return None

    def _handle_messages(self, messages):
        logger('got', len(messages), 'from lucy read queue')
        self._write_queue_handler.add_messages([self._ranking_handler.rank(message) for message in messages])
        return None
