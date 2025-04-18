from config import Config
import os
from datetime import datetime


def logger(*args):
    message = datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S') + ' :: '
    counter = 0
    for arg in args:
        if isinstance(arg, str):
            message += arg
        else:
            message += str(arg)
        if counter != len(args) - 1:
            message += ' '
    print(message)
    log_path = Config().log_path
    if os.path.exists(log_path):
        with open(log_path, 'a') as log_file:
            log_file.write(message + '\n')
        log_file.close()
    else:
        with open(log_path, 'w+') as log_file:
            log_file.write(message + '\n')
        log_file.close()
    return None
