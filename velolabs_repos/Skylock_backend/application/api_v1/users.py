import os
import hmac
import hashlib
import base64
from flask import jsonify, url_for, jsonify, request, g, Blueprint, current_app, abort, render_template, session, redirect
from werkzeug.security import generate_password_hash, check_password_hash
from . import api, application
from .. import db
from ..models import User, Role, Permission, Lock, Mobile, Share, Countrycode, DeletedUsers
import twilio, time
from threading import Thread
from ..decorators import async
from text import *
from notification import *
import binascii
from ..decorators import admin_required, permission_required
from sqlalchemy.types import Integer

#from logging.handlers import RotatingFileHandler
import logging, logging.handlers
x = logging.getLogger("logfun")
x.setLevel(logging.DEBUG)
h = logging.FileHandler("access.log")
f = logging.Formatter("%(levelname)s %(asctime)s %(funcName)s %(lineno)d %(message)s")
h.setFormatter(f)
#h1.setLevel(logging.DEBUG)
x.addHandler(h)



otp_secret = os.environ['OTP_SECRET']
admin = os.environ['ADMIN']
from gcm import *
gcm = GCM("AIzaSyD1UGnXcTUV7huxZV4D3vR6O6ZYmJlmL7U")






    

@api.route('/users/', methods=['GET'])
def get_users():
    """ Gets the URL of existing Users (GET) : """
    return jsonify({'status': 200, 'error': None, 'payload':  [user.get_url() for user in User.query.all()]})


@api.route('/users/<user_id>/', methods=['GET'])
def get_user(user_id):
    if (g.user == user_id):
        """ Provides User Complete Detail (GET) : <user_id> """
        return jsonify({'status': 200, 'error': None, 'payload': User.query.filter_by(user_id=user_id).first_or_404().export_data()})
    else:
        abort(403)


#Route to add Users First and Last name
@api.route('/users/<user_id>/name/', methods=['POST'])
def add_name(user_id):
    if (g.user == user_id):
        user = User(user_id=user_id)
        user.import_name(request.json)
        if user.first_name != "":
            db.session.query(User).filter_by(user_id=user_id).update({"first_name":user.first_name, "last_name":user.last_name})
            db.session.commit()
            return jsonify({'status': 201, 'error': None, 'payload': {'message': 'User Name Updated'}})
        else:
            return jsonify({'status': 400, 'error': 'Name cant be Empty', 'payload': {}})
    else:
        return abort(403)



@application.route('/users/', methods=['POST'])
def new_user():
    '''Creates Users (POST) : Should Provide user_id, first_name, last_name, user_name'''

    #logging.debug(head)
    client_request = request.get_json()
    head = request.headers

    logfun = logging.getLogger("logfun")
    logfun.debug(head)
    logfun.debug(client_request)
    

    user = User()
    user.import_data(request.json)
    user_type_list = ['ellipse', 'facebook']
    if not any (word in user.user_type for word in user_type_list):
        return jsonify({'status': 401, 'error': 'Invalid User Type', 'payload': {}})

    check_for_user = User.query.filter_by(user_id=user.user_id).first()

    #User Registration
    if check_for_user is None and user.email != "":
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

        if user.user_type == 'facebook':
            user.verified = True
        hotp = pyotp.HOTP(otp_secret)
        at = randint(0, 2147483647)
        user.password_hint=at

        country_code_finder = Countrycode.query.filter_by(letter_code=user.country_code).first()
        if country_code_finder is None:
            return jsonify({'status': 401, 'error': 'Invalid Country Code', 'payload': {}})
        user.country_code = '+' + country_code_finder.telephone_code
        #print (user.country_code)
        db.session.add(user)
        db.session.commit()
        user_out = User.query.filter_by(user_id=user.user_id).first()

        if user.user_type == 'ellipse':
            mobile = Mobile(user_id=user.user_id, users_id=user_out.id, mobile=(user.country_code+user.user_id))
            db.session.add(mobile)
            db.session.commit()
            send_message((user.country_code+user.user_id), text_message, render_template('Welcome.txt', code=hotp.at(at)))
        if user.user_type == 'facebook':
            mobile = Mobile(user_id=user.user_id, users_id=user_out.id)
            db.session.add(mobile)
            db.session.commit()
        return jsonify({'status': 201, 'error': None, 'payload': {'user': user_out.export_data(), 'token': user.generate_auth_token(), 'mobile_number' : mobile.mobile if mobile.mobile else '', 'data': 'User Registered' }}), 200, {'Location': user.get_url()}


    #Login
    if verify_password(user.user_id, user.password) and check_for_user.verified == True:
        mobile = Mobile.query.filter_by(user_id=user.user_id).first()
        if user.reg_id != check_for_user.reg_id and user.reg_id is not None:
            db.session.query(User).filter_by(user_id=user.user_id).update({"reg_id":user.reg_id})
            db.session.commit()
        return jsonify({'status': 200, 'error': None, 'payload': {'user': check_for_user.export_data(), 'token': g.user.generate_auth_token(), 'mobile_number' : mobile.mobile if mobile.mobile else '', 'data': 'User Present'} })
    

    #Verification Pending
    if verify_password(user.user_id, user.password) and check_for_user.verified == False and user.user_type == 'ellipse' and user.email == "":
        return jsonify({'status': 401, 'error': 'User Verification Pending', 'payload': {}})
    

    #Unverified Phone Number Users
    if check_for_user and user.user_type == 'ellipse' and check_for_user.verified == False and user.email != "":
        mobile = Mobile.query.filter_by(user_id=user.user_id).first()
        hotp = pyotp.HOTP(otp_secret)
        at = randint(0, 2147483647)
        user.password_hint=at
        send_message((user.country_code+user.user_id), text_message, render_template('verification_code.txt', code=hotp.at(at)))
        user.password=generate_password_hash(user.password)
        db.session.query(User).filter_by(user_id=user.user_id).update({"password":user.password, "password_hint":at})
        db.session.commit()
        return jsonify({'status': 200, 'error': None, 'payload': {'user': check_for_user.export_data(), 'token': g.user.generate_auth_token(), 'mobile_number' : mobile.mobile, 'data': 'User Recreated'} })


    #Unverified Facebook Users      
    if check_for_user and user.user_type == 'facebook' and check_for_user.verified == False and user.email != "":
        mobile = Mobile.query.filter_by(user_id=user.user_id).first()
        db.session.query(User).filter_by(user_id=user.user_id).update({"verified":True})
        db.session.commit()
        return jsonify({'status': 200, 'error': None, 'payload': {'user': check_for_user.export_data(), 'token': g.user.generate_auth_token(), 'mobile_number' : mobile.mobile, 'data': 'User Recreated'} })
    else:
        return jsonify({'status': 401, 'error': 'Invalid User or Wrong Password', 'payload': {}})





@api.route('/users/<user_id>/phoneverification/', methods=['GET'])
def verify_code(user_id):
    user = User.query.filter_by(user_id=user_id).first()
    if user and user.user_type == 'ellipse':

        hotp = pyotp.HOTP(otp_secret)
        at = randint(0, 2147483647)
        db.session.query(User).filter_by(user_id=user_id).update({"password_hint":at})
        send_message((user.country_code+user.user_id), text_message, render_template('verification_code.txt', code=hotp.at(at)))
        db.session.commit()
        return jsonify({'status': 200, 'error': None, 'payload': {'message': 'Mobile Verification Code Sent'}})
    else:
        return jsonify({'status': 404, 'error': 'User Dosent Exist', 'payload': {}})




@api.route('/users/<user_id>/phoneverify/', methods=['POST'])
def verify_mobile(user_id):
    hotp = pyotp.HOTP(otp_secret)
    user= User()
    user = User.query.filter_by(user_id=user_id).first()
    if user and user.user_type == 'ellipse':
        hint = user.password_hint
        user.verify_data(request.json)
        if hotp.verify(user.password_hint, hint):
            user.verified=True
            db.session.add(user)
            db.session.commit()
            return jsonify({'status': 201, 'error': None, 'payload': {'user': user.export_data(), 'token': user.generate_auth_token()} })
        else:
            return jsonify({'status': 400, 'error': 'Code Dosent Match', 'payload': {}})
    else:
        return jsonify({'status': 404, 'error': 'User Dosent Exist', 'payload': {}})




#Route to send Password Reset Code
@application.route('/users/<user_id>/passwordreset/', methods=['GET'])
#@permission_required(Permission.ADMINISTER)
def get_code(user_id):
    user = User.query.filter_by(user_id=user_id).first()
    if user and user.user_type == 'ellipse' and user.verified:
        hotp = pyotp.HOTP(otp_secret)
        at = randint(0, 2147483647)
        db.session.query(User).filter_by(user_id=user_id).update({"password_hint":at})
        send_message((user.country_code+user.user_id), text_message, render_template('Password_reset.txt', code=hotp.at(at)))
        db.session.commit()
        return jsonify({'status': 200, 'error': None, 'payload': {'message': 'Password Reset Code Sent'}})
    else:
        return jsonify({'status': 404, 'error': 'User Dosent Exist', 'payload': {}})



#Route to POST Secret Code
@application.route('/users/<user_id>/passwordcode/', methods=['POST'])
def get_passwordcode(user_id):
    hotp = pyotp.HOTP(otp_secret)
    user = User.query.filter_by(user_id=user_id).first()
    if user and user.user_type == 'ellipse':
        hint = user.password_hint
        print (hint)
        user.password_code(request.json)
        if hotp.verify(user.password_hint, hint):
            user.password_hint=1
            db.session.add(user)
            db.session.commit()
            return jsonify({'status': 201, 'error': None, 'payload': {'verification_code': True}})
        else:
            return jsonify({'status': 404, 'error': 'Code Dosent Match', 'payload': {'verification_code': False}})
    else:
        return jsonify({'status': 404, 'error': 'User Dosent Exist', 'payload': {}})


#Route to POST new password
@application.route('/users/<user_id>/password/', methods=['POST'])
def get_password(user_id):
    user = User.query.filter_by(user_id=user_id).first()
    if user and user.user_type == 'ellipse':
        hint = user.password_hint
        print (hint)
        user.password_profile(request.json)
        if hint == True:
            user.password=generate_password_hash(user.password)
            at = randint(0, 2147483647)
            user.password_hint=at
            db.session.add(user)
            db.session.commit()
            return jsonify({'status': 201, 'error': None, 'payload': {'message': 'Password Changed'}})
        else:
            return jsonify({'status': 404, 'error': 'Code Dosent Match', 'payload': {}})
    else:
        return jsonify({'status': 404, 'error': 'User Dosent Exist', 'payload': {}})

 




@api.route('/users/<user_id>/profilepassword/', methods=['POST'])
def get_profilepassword(user_id):
    if (g.user == user_id):
        user = User.query.filter_by(user_id=user_id).first()
        user.password_profile(request.json)
        if user and user.user_type == 'ellipse':
            user.password=generate_password_hash(user.password)
            db.session.add(user)
            db.session.commit()
            return jsonify({'status': 201, 'error': None, 'payload': {'message': 'Password Changed'}})
        else:
            return jsonify({'status': 404, 'error': 'User Dosent Excist', 'payload': {}})
    else:
        return abort(403)




@api.route('/users/<user_id>/profile/', methods=['POST'])
def profilesettings(user_id):
    if (g.user == user_id):
        user = User()

        check_user = User.query.filter_by(user_id=user_id).first_or_404()
        user.profile(request.json)
        if user.first_name != "":
            db.session.query(User).filter_by(user_id=user_id).update({"first_name":user.first_name})
            db.session.commit()
        if user.last_name != "":
            db.session.query(User).filter_by(user_id=user_id).update({"last_name":user.last_name})
            db.session.commit()
        if user.email != "":
            db.session.query(User).filter_by(user_id=user_id).update({"email":user.email})
            db.session.commit()
        if check_user.user_type == 'ellipse' and user.password != "":
            db.session.query(User).filter_by(user_id=user_id).update({"password":generate_password_hash(user.password)})
            db.session.commit()
        if check_user.user_type == 'facebook' and user.user_id != "" and user.user_id != check_user.user_id:
            country_code_finder = Countrycode.query.filter_by(letter_code=user.country_code).first()
            if country_code_finder is None:
                return jsonify({'status': 401, 'error': 'Invalid Country Code', 'payload': {}})
            user.country_code = '+' + country_code_finder.telephone_code

            db.session.query(Mobile).filter_by(user_id=user_id).update({"mobile":(user.country_code+user.user_id)})
            db.session.commit()


        if check_user.user_type == 'ellipse' and check_user.verified == True and user.user_id != "" and user.user_id != check_user.user_id:
            check_user = User.query.filter_by(user_id=user.user_id).first()
            if check_user:
                return jsonify({'status': 404, 'error': 'Number in use, by Another User', 'payload': {}})
            country_code_finder = Countrycode.query.filter_by(letter_code=user.country_code).first()
            if country_code_finder is None:
                return jsonify({'status': 401, 'error': 'Invalid Country Code', 'payload': {}})
            user.country_code = '+' + country_code_finder.telephone_code

            db.session.query(Lock).filter_by(user_id=user_id).update({"user_id":user.user_id})
            db.session.query(User).filter_by(user_id=user_id).update({"user_id":user.user_id, "username":user.user_id, "country_code":user.country_code})
            db.session.query(Share).filter_by(shared_by=user_id).update({"shared_by":user.user_id})
            db.session.query(Share).filter_by(shared_to=user_id).update({"shared_to":user.user_id})
            db.session.query(Mobile).filter_by(user_id=user_id).update({"user_id":user.user_id, "mobile":(user.country_code+user.user_id)})
            db.session.commit()
            #g.user = user.user_id
            return jsonify({'status': 201, 'error': None, 'payload': {'user': user.export_token(), 'token': user.generate_auth_token()}})
        return jsonify({'status': 201, 'error': None, 'payload': {'message': 'Updated'}})
    else:
        return abort(403)





@application.route('/users/<user_id>/registration/', methods=['POST'])
def get_registration(user_id):
    user = User.query.filter_by(user_id=user_id).first()
    if user and user.user_type == 'ellipse':
        user.registration(request.json)
        db.session.query(User).filter_by(user_id=user_id).update({"reg_id":user.reg_id})
        db.session.commit()
        return jsonify({'status': 201, 'error': None, 'payload': {'message': 'registration id Changed'}})
    else:
        return jsonify({'status': 404, 'error': 'User Dosent Excist', 'payload': {}})





#User phone number changing
@api.route('/users/<user_id>/numberchange/', methods=['POST'])
def get_newnumber(user_id):
    if (g.user == user_id):
        #lockuser = Lock()
        user = User()
        user.change_number(request.json)
        check_user = User.query.filter_by(user_id=user.user_id).first()
        check_state = User.query.filter_by(user_id=user_id).first_or_404()
        if check_state.user_type == 'ellipse' and check_state.verified == True:
            if check_user is None:
                country_code_finder = Countrycode.query.filter_by(letter_code=user.country_code).first()
                if country_code_finder is None:
                    return jsonify({'status': 401, 'error': 'Invalid Country Code', 'payload': {}})
                user.country_code = '+' + country_code_finder.telephone_code
                db.session.query(Lock).filter_by(user_id=user_id).update({"user_id":user.user_id})
                db.session.query(User).filter_by(user_id=user_id).update({"user_id":user.user_id, "username":user.user_id, "country_code":user.country_code})
                db.session.query(Share).filter_by(shared_by=user_id).update({"shared_by":user.user_id})
                db.session.query(Share).filter_by(shared_to=user_id).update({"shared_to":user.user_id})
                db.session.query(Mobile).filter_by(user_id=user_id).update({"user_id":user.user_id, "mobile":user.user_id})
                db.session.commit()
                return jsonify({'status': 201, 'error': None, 'payload': {'message': 'Number Changed'}})
            else:
                return jsonify({'status': 404, 'error': 'Number in use, by Another User', 'payload': {}})
        else:
            return jsonify({'status': 404, 'error': 'User Dosent Excist', 'payload': {}})
    else:
        return abort(403)


#Delete Account
@api.route('/users/<user_id>/deleteaccount/', methods=['POST'])
def delete_account(user_id):
    if (g.user == user_id):
        check_user = User.query.filter_by(user_id=user_id).first_or_404()
        if check_user.verified == True:
            deleteUser = DeletedUsers()
            check_lock = Lock.query.filter_by(user_id=user_id).first()
            if check_lock is not None:
                deleteUser.import_userdata(check_user.first_name, check_user.last_name, check_user.date_created, check_user.email, check_lock.mac_id, check_lock.touch_pad)
            else:
                deleteUser.import_userdata(check_user.first_name, check_user.last_name, check_user.date_created, check_user.email, None, None)

            db.session.query(Lock).filter_by(user_id=user_id).delete()
            db.session.query(Share).filter_by(shared_by=user_id).delete()
            db.session.query(Share).filter_by(shared_to=user_id).delete()
            db.session.query(Mobile).filter_by(user_id=user_id).delete()
            #db.session.query(Mobile).filter_by(user_id=user_id).update({"mobile":None})
            db.session.query(User).filter_by(user_id=user_id).delete()
            #db.session.query(User).filter_by(user_id=user_id).update({"verified":False})
            db.session.add(deleteUser)
            db.session.commit()
            return jsonify({'status': 201, 'error': None, 'payload':  {'message': 'Account Deleted'}})
        
        else:
            return jsonify({'status': 404, 'error': 'User Dosent Excist', 'payload': {}})
    else:
        return abort(403)




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
        return jsonify({'status': 200, 'error': None, 'payload': {'challenge_key': pad}})
    else:
        abort(403)




@api.route('/users/<user_id>/challenge_data/', methods=['POST'])
def get_challenge_data(user_id):

    if (g.user == user_id):

        user = User.query.filter_by(user_id=user_id).first_or_404()
        user.import_c(request.json)
        key_a = binascii.hexlify(user.c_key)
        pad = key_a.ljust(64, 'f')
        temp = a2b(pad)
        temp_1 = temp + a2b((user.c_data))
        dig = hashlib.new('sha256', temp_1 ).digest()
        c_data = b2a(dig)
        
        return jsonify({'status': 201, 'error': None, 'payload': {'challenge_data': c_data}})
    else:
        abort(403)

