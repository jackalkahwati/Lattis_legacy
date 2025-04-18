#!/usr/bin/env python
import os
from application import create_app, db
from application.models import User, MetaData
import logging
from logging.handlers import RotatingFileHandler

#error logging

application = app = create_app(os.environ.get('FLASK_CONFIG', 'development'))


if __name__ == '__main__':

	application.run()


