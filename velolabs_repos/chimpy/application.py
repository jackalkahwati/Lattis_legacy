from flask import Flask
from config import ChimpyConfig
from handlers.ChimpyHandler import ChimpyHandler

config = ChimpyConfig()
# application = Flask(__name__)
# application.config.update(SERVER_NAME=config.server_and_port())
#
# # define routes
# @application.route('/say-hello')
# def say_hello():
#     return 'hello there my friend'
#
# if __name__ == '__main__':
#     application.run(debug=True)

chimpy_handler = ChimpyHandler()
chimpy_handler.run_chimpy()
