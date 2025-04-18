import os
from flask import Flask, jsonify, g, request, abort

import logging
from logging.handlers import RotatingFileHandler


def create_app(config_name):
    #Create's an application instance.
    app = application = Flask(__name__)

    # apply configuration
    cfg = os.path.join(os.getcwd(), 'config', config_name + '.py')
    application.config.from_pyfile(cfg)


    # register blueprints
    from .api_v1 import api as api_blueprint
    application.register_blueprint(api_blueprint, url_prefix='/api/v1')

    from .api_v1 import application as app_blueprint
    application.register_blueprint(app_blueprint, url_prefix='/api/v1')

    return application
