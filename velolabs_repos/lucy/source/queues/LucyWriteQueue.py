import os


class LucyWriteQueue:
    def __init__(self):
        self.name = 'lucy-write-queue'
        self.url = os.environ['LUCY_WRITE_QUEUE_URL']
