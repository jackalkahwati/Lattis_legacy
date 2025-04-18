import os
from twilio.rest import TwilioRestClient
from ..auth import *
import pyotp
from random import randint 
from ..decorators import async
from threading import Thread

account_sid = os.environ['ACCOUNT_SID'] 
auth_token  = os.environ['AUTH_TOKEN']
text_message = os.environ['TEXT_MESSAGE']
client = TwilioRestClient(account_sid, auth_token)



@async
def send_async_msg(to, from_, body):
    #with app.app_context():
    client.messages.create(to=to, from_=from_, body=body)
def send_message(to, from_, body):
    thr = Thread(target=send_async_msg, args=[to, from_, body])
    thr.start()
