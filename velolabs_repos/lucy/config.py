import os


class Config:
    def __init__(self):
        self.read_queue_url = os.environ['LUCY_READ_QUEUE_URL']
        self.write_queue_url = os.environ['LUCY_WRITE_QUEUE_URL']
        self.log_path = os.environ['LUCY_LOG_PATH']
