import os
from ..decorators import async
from threading import Thread
from flask import jsonify

from . import api
from application import create_app, db
from ..models import User


from gcm import *
gcm_token = os.environ['GCM_TOKEN'] 
gcm = GCM(gcm_token) #AIzaSyD1UGnXcTUV7huxZV4D3vR6O6ZYmJlmL7U



#-----------------bulf notification---------------------------------#

def get_chunks(MyList, n):
    return [MyList[x:x+n] for x in range(0, len(MyList), n)]

#@app.route('/test', methods= ['GET'])
def test(title, message):
    data2 = User.query.all()
    reg_id =[]
    for user in data2:
        reg_id.append(user.reg_id)

    reg_ids = get_chunks(reg_id, 1)
    data = {'title': title, 'message': message}
    count = 0
    while count < len(reg_ids):
        send_notifications(reg_ids[count], data)
        count = count + 1
    return jsonify({'status':'success'})


#For Bulf Notifications 
@async
def send_async_notifications(api, registration_ids, data):
    #with app.app_context():
    gcm.json_request(registration_ids=registration_ids, data=data)

def send_notifications(registration_ids, data):
    count = 0
    thr = Thread(target=send_async_notifications, args=[api, registration_ids, data])
    thr.start()


#--------------------------------------------------------------------------------------------
#For individual Notifications 
@async
def send_async_notification(api, registration_id, data):
    #with app.app_context():
    gcm.plaintext_request(registration_id=registration_id, data=data)

def send_notification(registration_id, data):
    thr = Thread(target=send_async_notification, args=[api, registration_id, data])
    thr.start()

#---------------------------------------------------------------------------------------------
