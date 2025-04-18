import os


class LucyReadQueue:
    def __init__(self):
        self.name = 'lucy-read-queue'
        self.url = os.environ['LUCY_READ_QUEUE_URL']
