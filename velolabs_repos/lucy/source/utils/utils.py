import time
import numpy
import math
from source.utils.logger import logger


def current_time():
    return round(time.time())


def asymptotic_score(score, date):
    date_value = 2.0*(math.atan(abs(current_time() - date))/math.pi)
    score_value = 2.0*(math.atan(score)/math.pi)
    return 2/3*date_value + 1/3*score_value
