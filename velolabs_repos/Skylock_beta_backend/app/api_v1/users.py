import os
from flask import jsonify, url_for, jsonify, request, g, Blueprint, current_app, abort, render_template, session, redirect
from werkzeug.security import generate_password_hash, check_password_hash
from . import api, app
from .. import db
from ..models import User, Role, Permission
import twilio, time
from threading import Thread
from ..decorators import async
from text import *
from notification import *
import binascii
from ..decorators import admin_required, permission_required

from sqlalchemy.types import Integer



otp_secret = os.environ['OTP_SECRET']
admin = os.environ['ADMIN']







    

@api.route('/users/', methods=['GET'])
def get_users():
    """ Gets the URL of existing Users (GET) : """
    return jsonify({'status': 'success', 'message': 'All Users', 'payload':  [user.get_url() for user in User.query.all()]})


@api.route('/users/<user_id>/', methods=['GET'])
def get_user(user_id):
    """ Provides User Complete Detail (GET) : <user_id> """
    return jsonify({'status': 'success', 'message': 'User Details', 'payload': User.query.filter_by(user_id=user_id).first_or_404().export_data()})


@app.route('/users/', methods=['POST'])
def new_user():
    '''Creates Users (POST) : Should Provide user_id, first_name, last_name, user_name'''
    user = User()
    #mobile_message = MetaData.query.filter_by(metadata_ver="metadata").first()
    user.import_data(request.json)

    check_for_user = User.query.filter_by(user_id=user.user_id).first()
    print(admin)
    if check_for_user is None and user.first_name is not None and user.last_name is not None:
        user.password=generate_password_hash(user.password)

        if user.role is None:
            admins=admin.split(',')
            count=0
            while count < len(admins):
                if user.user_id == admins[count]:
                    user.role = Role.query.filter_by(permissions=0xff).first()
                count=count+1
            if user.role is None:
                user.role = Role.query.filter_by(default=True).first()

        db.session.add(user)
        db.session.commit()
        if user.fb_flag is False:
            send_message(user.user_id, text_message, render_template('Welcome.txt', user_name=user.first_name.title()))
        user_out = User.query.filter_by(user_id=user.user_id).first()
        return jsonify({'status': 'success', 'message': 'User Created', 'payload': {'user': user_out.export_data(), 'token': user.generate_auth_token()} }), 200, {'Location': user.get_url()}
    if verify_password(user.user_id, user.password) and check_for_user.blacklist == False:
        if user.reg_id != check_for_user.reg_id and user.reg_id is not None:
            db.session.query(User).filter_by(user_id=user.user_id).update({"reg_id":user.reg_id})
            db.session.commit()
        #if 
        return jsonify({'status': 'success', 'message': 'User Present', 'payload': {'user': check_for_user.export_data(), 'token': g.user.generate_auth_token()} })
    else:
        return jsonify({'status': 'error', 'message': 'Invalid User or Wrong Password', 'payload': {}})



#Fleet Users login modify 
@app.route('/fleet_users/', methods=['POST'])
def new_fleet_user():
    '''Creates Users (POST) : Should Provide user_id, first_name, last_name, user_name'''
    user = User()
    #mobile_message = MetaData.query.filter_by(metadata_ver="metadata").first()
    user.import_data(request.json)

    check_for_user = User.query.filter_by(user_id=user.user_id).first()
    if check_for_user is None and user.first_name is not None and user.last_name is not None:
        user.password=generate_password_hash(user.password)

        if user.role is None:
            admins=admin.split(',')
            count=0
            while count < len(admins):
                if user.user_id == admins[count]:
                    user.role = Role.query.filter_by(permissions=0xff).first()
                count=count+1
            if user.role is None:
                user.role = Role.query.filter_by(default=True).first()

        db.session.query(User).filter_by(user_id=user.user_id).update({"skyfleet_admin":True})
        db.session.add(user)
        db.session.commit()
        if user.fb_flag is False:
            send_message(user.user_id, text_message, render_template('Welcome.txt', user_name=user.first_name.title()))
        user_out = User.query.filter_by(user_id=user.user_id).first()
        return jsonify({'status': 'success', 'message': 'User Created', 'payload': {'user': user_out.export_data(), 'token': user.generate_auth_token(), 'password': user_out.password} }), 200, {'Location': user.get_url()}
    if verify_password(user.user_id, user.password) and check_for_user.blacklist == False:
        if user.reg_id != check_for_user.reg_id and user.reg_id is not None:
            db.session.query(User).filter_by(user_id=user.user_id).update({"reg_id":user.reg_id})
            db.session.commit()
        #if 
        return jsonify({'status': 'success', 'message': 'User Present', 'payload': {'user': check_for_user.export_data(), 'token': g.user.generate_auth_token()} })
    else:
        return jsonify({'status': 'error', 'message': 'Invalid User or Wrong Password', 'payload': {}})



from gcm import *
gcm = GCM("AIzaSyD1UGnXcTUV7huxZV4D3vR6O6ZYmJlmL7U")

@app.route('/push', methods= ['GET'])
def push():
    data = {'title': 'Shiva Sent A Notification', 'message': 'hey arun', 'id': 3}
    registration_ids = [u'f4gIrra7k1Y:APA91bH6ysn8DhiYhYDMV6Pe_gxLiB3BRHa3FR3oeMWt5wKWktrqHG3TT-w74BOw-q-7JoctZTIjBD_E2OOc2w3885sCduuyTFN-nBwJL2si0LArYKF-4t5RqhbOdDHCezLrWlTZOcyc']
    send_notification(registration_ids, data)
    print(admin)
    return jsonify({'status':'success'})

#APA91bG5rB_tFnyaJ7f61mj0ks7u0UNPIc0e-cf5Dda8DzQeW_dRUOi60dquIHZmRhldwVS4MO9mk41i_x0SxKsATo6g0a_u2FF83sf-fjfPZbrMWNpRFzjUU2pW9fbhCCpK1WJ8ssrZ

@app.route('/users/<user_id>/phoneverification/', methods=['GET'])
#@permission_required(Permission.ADMINISTER)
#@admin_required
def verify_code(user_id):
    user = User.query.filter_by(user_id=user_id).first()
    #mobile_message = MetaData.query.filter_by(metadata_ver="metadata").first()
    if user and user.fb_flag is False:
        hotp = pyotp.HOTP(otp_secret)
        at = randint(0, 2147483647)
        db.session.query(User).filter_by(user_id=user_id).update({"password_hint":at})
        send_message(user.user_id, text_message, render_template('verification_code.txt', code=hotp.at(at)))
        db.session.commit()
        #return jsonify({'status': hotp.at(at)})
        return jsonify({'status': 'success', 'message': 'Mobile Verification Code Sent', 'payload': {}})
    else:
        return jsonify({'status': 'error', 'message': 'User Dosent Exist', 'payload': {}})


@app.route('/users/<user_id>/phoneverify/', methods=['POST'])
def verify_mobile(user_id):
    hotp = pyotp.HOTP(otp_secret)
    user= User()
    #check_for_user = User.query.filter_by(user_id=user_id).first()
    user = User.query.filter_by(user_id=user_id).first()
    if user and user.fb_flag is False:
        hint = user.password_hint #changes suggesed by rupesh
        user.verify_data(request.json)
        if hotp.verify(user.password_hint, hint):
            user.verified=True
            db.session.add(user)
            db.session.commit()
            return jsonify({'status': 'success', 'message': 'User Verified', 'payload': {'user': user.export_data(), 'token': user.generate_auth_token()} })
        else:
            return jsonify({'status': 'error', 'message': 'Code Dosent Match', 'payload': {}})
    else:
        return jsonify({'status': 'error', 'message': 'User Dosent Exist', 'payload': {}})



@app.route('/users/<user_id>/passwordreset/', methods=['GET'])
#@permission_required(Permission.ADMINISTER)
#@admin_required
def get_code(user_id):
    user = User.query.filter_by(user_id=user_id).first()
    #mobile_message = MetaData.query.filter_by(metadata_ver="metadata").first()
    if user and user.fb_flag is False:
        hotp = pyotp.HOTP(otp_secret)
        at = randint(0, 2147483647)
        db.session.query(User).filter_by(user_id=user_id).update({"password_hint":at})
        send_message(user.user_id, text_message, render_template('Password_reset.txt', code=hotp.at(at)))
        db.session.commit()
        #return jsonify({'status': hotp.at(at)})
        return jsonify({'status': 'success', 'message': 'Password Reset Code Sent', 'payload': {}})
    else:
        return jsonify({'status': 'error', 'message': 'User Dosent Exist', 'payload': {}})


@app.route('/users/<user_id>/password/', methods=['POST'])
def get_password(user_id):
    hotp = pyotp.HOTP(otp_secret)
    user = User.query.filter_by(user_id=user_id).first()
    #hint = user.password_hint
    if user and user.fb_flag is False:
        hint = user.password_hint #changes suggesed by rupesh
        user.password_data(request.json)
        if hotp.verify(user.password_hint, hint):
            user.password=generate_password_hash(user.password)
            db.session.add(user)
            db.session.commit()
            return jsonify({'status': 'success', 'message': 'Password Changed', 'payload': {}})
        else:
            return jsonify({'status': 'error', 'message': 'Code Dosent Match', 'payload': {}})
    else:
        return jsonify({'status': 'error', 'message': 'User Dosent Excist', 'payload': {}})



@app.route('/users/<user_id>/list/', methods=['POST'])
def text_promo(user_id):
    add_contatcs = User()
    #add_contatcs = User.query.filter_by(user_id=user_id).first_or_404
    add_contatcs.text_data(request.json)
    
    #db.session.add(add_contatcs)
    db.session.query(User).filter_by(user_id=user_id).update({"mylist":add_contatcs.mylist})
    db.session.commit()
    return jsonify({'status': 'success', 'message': 'updated', 'payload': {}})
    """if (g.user == user_id):
        add_contatcs = User()
        add_contatcs.text_data(request.json)
        #user = User.query.filter_by(user_id=user_id).first_or_404
        print (add_contatcs.text_promo)
        db.session.query(User).filter_by(user_id=user_id).update({"metadata_item":add_contatcs.text_promo})
        db.session.commit()
        return jsonify({'status': 'success', 'message': 'updated', 'payload': {}})
    else:
        return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': {}})"""


@app.route('/users/<user_id>/promo/', methods=['GET'])
def promo(user_id):
    user = User.query.filter_by(user_id=user_id).first()
    return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': user.mylist})

        #dict = {"facebook": {"openid": "123456789"}}
        #dic = user.metadata_item
        #user = cls.query.filter(User.auth.contains(dict)).first()
        #expr = User.metadata_item[("key1", "1")]
        #q = (db.session.query(User.id, expr.label("deep_value")).filter(expr != None).all())
        #print (q)
        #session.query(User.data["name"].astext == "admin").all()
        #return jsonify({'status': 'success', 'message': 'Testing', 'payload': {'data':db.session.query(User.metadata_item["key1"].astext == "admin").all()}})




import hmac
import hashlib
import base64

def a2b(data):
    temp_string1=""
    for i in range(0,len(data) - 1, 2):
        data[i:i+2]
        x1 = binascii.a2b_hex(data[i:i+2])
        temp_string1 = temp_string1 + x1
    temp_string2 = temp_string1
    return temp_string2


def b2a(data):
    temp_string_msg = ""        
    for letter1 in data:
        x1 = binascii.b2a_hex(letter1)
        temp_string_msg = temp_string_msg + x1
    message = temp_string_msg
    return message



@api.route('/users/<user_id>/challenge_key/', methods=['GET'])
def get_challenge_key(user_id):

    if (g.user == user_id):
        key = binascii.hexlify(user_id)
        pad = key.ljust(64, 'f')
        return jsonify({'status': 'success', 'message': 'Challenge Key', 'payload': {'challenge_key': pad}})
        #'status': 'error', 'message': 'You are NOT the Owner of this LOCK', 'payload': {}
    else:
        return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': {}})




@api.route('/users/<user_id>/challenge_data/', methods=['POST'])
def get_challenge_data(user_id):

    if (g.user == user_id):

        user = User.query.filter_by(user_id=user_id).first_or_404()
        #key = Key(user_id=user_id)
        user.import_c(request.json)


        key_a = binascii.hexlify(user.c_key)
        pad = key_a.ljust(64, 'f')
        temp = a2b(pad)

        temp_1 = temp + a2b((user.c_data))
        dig = hashlib.new('sha256', temp_1 ).digest()
        c_data = b2a(dig)
        
        #return jsonify({"challenge_data": c_data})
        return jsonify({'status': 'success', 'message': 'Challenge Data', 'payload': {'challenge_data': c_data}})
    else:
        return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': {}})



