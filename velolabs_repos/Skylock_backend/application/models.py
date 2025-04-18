#!/usr/bin/python
# -*- coding: utf-8 -*-

from datetime import datetime, timedelta
from dateutil import parser as datetime_parser
from dateutil.tz import tzutc
from werkzeug.security import generate_password_hash, check_password_hash
from itsdangerous import TimedJSONWebSignatureSerializer as Serializer
from flask import url_for, current_app, jsonify, request, g, Blueprint, abort, render_template, session, redirect
from . import db
from .exceptions import ValidationError
from flask.ext.httpauth import HTTPBasicAuth
from flask.ext.login import UserMixin
from random import randint 

from .utils import split_url
from sqlalchemy_utils import EncryptedType
from sqlalchemy import *
from sqlalchemy.dialects.postgresql import ARRAY, JSON, JSONB, HSTORE
#from sqlalchemy.ext.mutable import MutableDict

#TWILIO
import twilio
#import message_sending
from twilio.rest import TwilioRestClient
import time, pytz
from threading import Thread
import os
import hashlib, binascii, hmac, base64

#crypto
from KeyShiva import Key as keygen
from encoding import *
import hashlib
import ecdsa
import csv

#secret_key = os.environ['SECRET_KEY']




class Permission:
    LOCK_UNLOCK = 0x01
    CRASH_ALERT = 0x02
    SHARE = 0x04
    MAINTENANCE = 0x08
    ADMINISTER = 0x80


class Role(db.Model):
    __tablename__ = 'roles'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(64), unique=True)
    default = db.Column(db.Boolean, default=False, index=True)
    permissions = db.Column(db.Integer)

    users = db.relationship('User', backref='role', lazy='dynamic')

    @staticmethod
    def insert_roles():
        roles = {
            'Skylock': (Permission.LOCK_UNLOCK | Permission.CRASH_ALERT | Permission.SHARE, True),
            'Fleet': (Permission.LOCK_UNLOCK, False),
            'Maintenance': (Permission.LOCK_UNLOCK | Permission.CRASH_ALERT | Permission.SHARE | Permission.MAINTENANCE, False),
            'Administrator': (0xff, False)
        }
        for r in roles:
            role = Role.query.filter_by(name=r).first()
            if role is None:
                role = Role(name=r)
            role.permissions = roles[r][0]
            role.default = roles[r][1]
            db.session.add(role)
        db.session.commit()

        def __repr__(self):
            return '<Role %r>' % self.name




class User(UserMixin, db.Model):
    __tablename__='users'
    id = db.Column(db.Integer, primary_key=True)
    first_name = db.Column(db.String(64))
    last_name = db.Column(db.String(64), unique = False)
    user_id = db.Column(db.String(64), unique=True)
    username = db.Column(db.String(64), unique=True)
    password = db.Column(db.String(128))
    date_created = db.Column(db.Integer)
    user_state = db.Column(db.Integer, default=1)
    user_type = db.Column(db.String(64))
    #blacklist = db.Column(db.Integer, default=False)
    password_hint = db.Column(EncryptedType(db.Integer, os.environ['SECRET_KEY']), default=randint(0, 2147483647))
    reg_id = db.Column(db.String(256))
    email = db.Column(db.String(64))
    role_id = db.Column(db.Integer, db.ForeignKey('roles.id'))
    verified = db.Column(db.Integer, default=False)
    maxLocks = db.Column(db.Integer, default=5)
    device = db.Column(db.Integer, default=0)
    gender = db.Column(db.Integer, default=0)
    country_code = db.Column(db.String(16), default=None)

    locks = db.relationship('Lock', backref='user', cascade='save-update', lazy='dynamic')
    mobiles = db.relationship('Mobile', backref='user', cascade='save-update', lazy='dynamic')
    shared = db.relationship('Share', backref='user', cascade='save-update', lazy='dynamic')
    



    @staticmethod
    def can(user_id, permissions):
        roles= User.query.filter_by(user_id=user_id).first()
        if roles is None:
            abort(403)
        return roles.role is not None and (roles.role.permissions & permissions) == permissions
    
    def is_administrator(self):
        return self.can(Permission.ADMINISTER)


    def get_url(self):
        return url_for('api.get_user', user_id=self.user_id, _external=True)



    def export_data(self):
        return{
            'self_url': self.get_url(),
            'first_name': self.first_name,
            'last_name': self.last_name,
            'user_type': self.user_type,
            'user_id': self.user_id,
            'verified': self.verified,
            'maxLocks': self.maxLocks,
            'email': self.email
        }
        
    def import_data(self, data):
        try:
            self.first_name = data['first_name']
            self.last_name = data['last_name']
            self.date_created = time.time()
            self.user_id = data['user_id']
            self.password = data['password']
            self.username = data['user_id']
            self.user_type = data['user_type']
            self.reg_id = data['reg_id']
            self.email = data['email']
            self.country_code = data['country_code']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    def import_name(self, data):
        try:
            self.first_name = data['first_name']
            self.last_name = data['last_name']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self



    def registration(self, data):
        try:
            self.reg_id = data['reg_id']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    #Getting Password reset code
    def password_code(self, data):
        try:
            #self.password = data['password']
            self.password_hint = data['password_hint']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    def password_profile(self, data):
        try:
            self.password = data['password']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    def profile(self, data):
        try:
            self.first_name = data['first_name']
            self.last_name = data['last_name']
            self.password = data['password']
            self.email = data['email']
            self.user_id = data['phone_number']
            self.country_code = data['country_code']
            self.username = data['phone_number']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    def export_token(self):
        return{
            'self_url': self.get_url(),
            'user_id': self.user_id
        }



    def verify_data(self, data):
        try:
            self.password_hint = data['verify_hint']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    def change_number(self, data):
        try:
            self.user_id = data['new_number']
            self.country_code = data['country_code']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    def verify_password(self, password_1):
        return check_password_hash(self.password, password_1)

    def generate_auth_token(self, expires_in=7776000):
        s = Serializer(current_app.config['SECRET_KEY_AUTH'], expires_in=expires_in)
        return s.dumps({'username': self.username}).decode('utf-8')

    def import_c(self, data):
        try:
            self.c_data = str(data['c_data'])
            self.c_key= data['c_key']
        except KeyError as e:
            raise ValidationError('Missing data')
        return self


    @staticmethod
    def verify_auth_token(token):
        s = Serializer(current_app.config['SECRET_KEY_AUTH'])
        try:
            data = s.loads(token)
            print (current_app.config['SECRET_KEY_AUTH'])
        except:
            return None
        return (data['username'])



class DeletedUsers(UserMixin, db.Model):
    __tablename__='deletedusers'
    id = db.Column(db.Integer, primary_key=True)
    email_id = db.Column(db.String(64))
    date_created = db.Column(db.Integer)
    date_deleted = db.Column(db.Integer)
    first_name = db.Column(db.String(64))
    last_name = db.Column(db.String(64))
    mac_id = db.Column(EncryptedType(db.String(200), os.environ['SECRET_KEY']))
    touch_pad = db.Column(EncryptedType(db.String(256), os.environ['SECRET_KEY']))

    def import_userdata(self, first_name, last_name, date_created, email_id, mac_id, touch_pad):
        try:
            self.first_name = first_name
            self.last_name = last_name
            self.date_created = date_created
            self.email_id = email_id
            self.date_deleted = time.time()
            self.mac_id = mac_id
            self.touch_pad = touch_pad
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self








class Lock(db.Model):
    __tablename__ = 'locks'
    key_id = db.Column(db.Integer, primary_key=True)
    users_id = db.Column(db.Integer, db.ForeignKey('users.id'), index=True)
    user_id = db.Column(db.String(64))
    mac_id = db.Column(EncryptedType(db.String(200), os.environ['SECRET_KEY']))
    uuid = db.Column(db.String(128), default=None)
    sk_public_key_1 = db.Column(EncryptedType(db.Binary(64), os.environ['SECRET_KEY']))
    sk_public_key_2 = db.Column(EncryptedType(db.Binary(64), os.environ['SECRET_KEY']))
    sk_private_key = db.Column(EncryptedType(db.Binary(64), os.environ['SECRET_KEY']))
    lock_name = db.Column(db.String(64))
    lock_added = db.Column(db.Integer)
    touch_pad = db.Column(EncryptedType(db.String(256), os.environ['SECRET_KEY']))


    def get_url(self):
        return url_for('api.get_key', key_id=self.key_id, _external=True)




#--------------------------------WORKING -----------------------------------------------------------------------

    def export_message(self, mac_id, user_id, unixtime=str(int(time.time())), time='ffffffff', security='00', owner='00'):

        if owner == '00':
            keys = Lock.query.filter_by(user_id=user_id, mac_id=mac_id).first()
        elif owner == '01':
            keys = Share.query.filter_by(shared_to=user_id, mac_id=mac_id).first()

        key_gen = keygen.generate(256)
        key_gen._priv = (256, (int(keys.sk_private_key,16)))
        key_gen._pub = (256, (int(keys.sk_public_key_1,16),int(keys.sk_public_key_2,16)))
        messageEncrypt = key_gen.encrypt(user_id+mac_id)
        messageCombined = b2a(messageEncrypt[:31]) + time + security
        messageCombined_Mac =  mac_id + owner + messageCombined

        sign = key_gen.sign(a2b(messageCombined_Mac))
        sign = b2a(sign)
        signRequeried = owner + messageCombined + sign

        public_key_1 = hex(key_gen._pub[1][0]).rstrip("L").lstrip("0x")
        public_key_2 = hex(key_gen._pub[1][1]).rstrip("L").lstrip("0x")
        publicKey = public_key_1+public_key_2

        return (messageCombined_Mac, signRequeried, publicKey)



#--------------------------------------------------------------------------------------------------------------


    
    def import_data(self, data):
        keyvalues=keygen.generate(256)
        check_validate=keygen(keyvalues._pub, keyvalues._priv)
        count =0
        while len(hex(keyvalues._pub[1][0]).rstrip("L").lstrip("0x")+hex(keyvalues._pub[1][1]).rstrip("L").lstrip("0x")) != 128:
            count=count+1
            keyvalues=keygen.generate(256)
        try:
            self.mac_id = data['mac_id']
            self.lock_name = "Ellipse-"+str(randint(100000, 999999))
            self.sk_private_key = hex(keyvalues._priv[1]).rstrip("L").lstrip("0x")
            self.sk_public_key_1 = hex(keyvalues._pub[1][0]).rstrip("L").lstrip("0x")
            self.sk_public_key_2 = hex(keyvalues._pub[1][1]).rstrip("L").lstrip("0x")
            self.lock_added = time.time()

        except KeyError as e:
            raise ValidationError('Invalid Key: missing ' + e.args[0])
        return self
    

    def delete_keys(self, data):
        try:
            self.mac_id = data['mac_id']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    #FOR CHALLENGE DATA
    def import_c(self, data):
        try:
            self.c_data = str(data['c_data'])
            self.c_key= data['c_key']
        except KeyError as e:
            raise ValidationError('Missing data')
        return self

    def exportLockDetails(self):
        return{
            'user_id': self.user_id,
            'mac_id': self.mac_id,
            'lock_name': self.lock_name,
            'touch_pad': self.touch_pad,
            'self_url': self.get_url(),
            'sk_private_key' : self.sk_private_key,
            'sk_public_key_1' : self.sk_public_key_1,
            'sk_public_key_2' : self.sk_public_key_2
        }

    def add_name(self, data):
        try:
            self.mac_id = data['mac_id']
            self.lock_name = data['lock_name']
        except KeyError as e:
            raise ValidationError('Missing data')
        return self

    
    def getlock_name(self, data):
        try:
            self.mac_id = data['mac_id']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    #Route to add user touchpad Combination
    def add_combination(self, data):
        try:
            self.mac_id = data['mac_id']
            self.touch_pad = data['touch_pad']
        except KeyError as e:
            raise ValidationError('Missing data')
        return self











#Add a status column 
class Share(db.Model):
    __tablename__='shared'
    id = db.Column(db.Integer, primary_key=True)
    users_id = db.Column(db.Integer, db.ForeignKey('users.id'), index=True)
    shared_by = db.Column(db.String(64))
    shared_to = db.Column(db.String(64))
    mac_id = db.Column(EncryptedType(db.String(200), os.environ['SECRET_KEY'])) #have the lock id 
    sk_private_key = db.Column(EncryptedType(db.Binary(64), os.environ['SECRET_KEY']))
    sk_public_key_1 = db.Column(EncryptedType(db.Binary(64), os.environ['SECRET_KEY']))
    sk_public_key_2 = db.Column(EncryptedType(db.Binary(64), os.environ['SECRET_KEY']))
    shared_on = db.Column(db.Integer)
    shared_till = db.Column(db.Integer)
    status = db.Column(db.Integer, default=0)
    status_code = db.Column(db.Integer)
    sent_to = db.Column(db.String(64))
    



    def export_data(self):
        return{
            'shared_by': self.shared_by,
            'mac_id': self.mac_id,
            'shared_on': self.shared_on,
            'shared_till': self.shared_till
        }


    def export_shared_data(self):
        return{
            'shared_to': self.shared_to,
            'mac_id': self.mac_id,
            'shared_on': self.shared_on,
            'shared_till': self.shared_till
        }


    
    def import_details(self, data):
        try:
            self.user_id = data['user_id']
            self.mac_id = data['mac_id']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self




    def import_sharee(self, data):
        try:
            self.shared_to = data['shared_to']
            self.mac_id = data['mac_id']
            self.shared_till = time.time() + 7776000
            self.shared_on = time.time()

        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    def import_sharedkeys(self, sk_public_key_1, sk_public_key_2, sk_private_key, users_id, status_code=None):
        try:
            self.sk_private_key = sk_private_key
            self.sk_public_key_1 = sk_public_key_1
            self.sk_public_key_2 = sk_public_key_2
            self.status_code = status_code
            self.users_id = users_id
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    def import_unshare(self, data):
        try:
            self.shared_to = data['shared_to']
            self.mac_id = data['mac_id']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    def import_unsharee(self, data):
        try:
            self.shared_by = data['shared_by']
            self.mac_id = data['mac_id']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    #the present share system
    def import_mac(self, data):
        try:
            self.mac_id = data['mac_id']
            self.sent_to = data['send_to']
            self.shared_on = time.time()
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    def import_code(self, data):
        try:
            self.status_code = data['status_code']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    def import_sharecode(self, data):
        try:
            self.status_code = data['status_code']
            self.shared_to = data['shared_to']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self







class Mobile(db.Model):
    __tablename__ = 'mobiles'
    id = db.Column(db.Integer, primary_key=True)
    users_id = db.Column(db.Integer, db.ForeignKey('users.id'), index=True)
    user_id = db.Column(db.String(64))
    mobile = db.Column(db.String(20))
    emergency_contact1_name = db.Column(db.String(64))
    emergency_contact1 = db.Column(db.String(20))
    emergency_contact2_name = db.Column(db.String(64))
    emergency_contact2 = db.Column(db.String(20))
    emergency_contact3_name = db.Column(db.String(64))
    emergency_contact3 = db.Column(db.String(20))


    def get_url(self):
        return url_for('api.get_mobile', id=self.id, _external=True)


    def export_data(self):
        return{
            'id': self.id,
            'user_id': self.user_id,
            'mobile': self.mobile,
            'emergency_contact1': self.emergency_contact1,
            'emergency_contact2': self.emergency_contact2,
            'emergency_contact3': self.emergency_contact3,
            'emergency_contact1_name': self.emergency_contact1_name,
            'emergency_contact2_name': self.emergency_contact2_name,
            'emergency_contact3_name': self.emergency_contact3_name
        }


    def import_mobile(self, data):
        try:
            self.mobile = data['mobile']
            self.country_code = data['country_code']

        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    def import_emercont1(self, data):
        try:
            self.emergency_contact1 = data['emergency_contact']
            self.emergency_contact1_name = data['emergency_contact_name']

        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    def import_emercont2(self, data):
        try:
            self.emergency_contact2 = data['emergency_contact']
            self.emergency_contact2_name = data['emergency_contact_name']

        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    def import_emercont3(self, data):
        try:
            self.emergency_contact3 = data['emergency_contact']
            self.emergency_contact3_name = data['emergency_contact_name']

        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    
    #method used when adding Emergency contact numbers
    def import_emergency(self, data):
        try:
            self.emergency_contact1 = data['emergency_contact1']
            self.emergency_contact1_name = data['emergency_contact1_name']
            self.emergency_contact2 = data['emergency_contact2']
            self.emergency_contact2_name = data['emergency_contact2_name'] 
            self.emergency_contact3 = data['emergency_contact3']
            self.emergency_contact3_name = data['emergency_contact3_name']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    
    #method used when user gets into crash
    def import_crash(self, data):
        try:
            self.mac_id = data['mac_id']
            self.latitude = data['latitude']
            self.longitude = data['longitude']

        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    #method called when sending text message for help
    def import_details(self, data):
        try:
            self.mac_id = data['mac_id']
            self.latitude = data['latitude']
            self.longitude = data['longitude']
            self.emergency_contact1 = data['emergency_contact1']
            self.emergency_contact1_name = data['emergency_contact1_name']
            self.emergency_contact2 = data['emergency_contact2']
            self.emergency_contact2_name = data['emergency_contact2_name'] 
            self.emergency_contact3 = data['emergency_contact3']
            self.emergency_contact3_name = data['emergency_contact3_name']

        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    def import_emg(self, data):
        try:
            self.mac_id = data['mac_id']
            self.latitude = data['latitude']
            self.longitude = data['longitude']
            self.emergency_contact1 = data['emergency_contact1']
            self.emergency_contact1_name = data['emergency_contact1_name']
            self.emergency_contact2 = data['emergency_contact2']
            self.emergency_contact2_name = data['emergency_contact2_name'] 
            self.emergency_contact3 = data['emergency_contact3']
            self.emergency_contact3_name = data['emergency_contact3_name']

        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self







class Crashdata(db.Model):
    __tablename__='crash'
    id = db.Column(db.Integer, primary_key=True)
    #locks_id = db.Column(db.Integer, db.ForeignKey('locks.key_id'), index=True)
    lock_name = db.Column(db.String(64))
    #mac_id = db.Column(EncryptedType(db.String(200), secret_key), db.ForeignKey('keys.mac_id'))
    mac_id = db.Column(EncryptedType(db.String(200), os.environ['SECRET_KEY']))
    user_id = db.Column(db.String(64))
    time_of_crash = db.Column(db.Integer)
    crash_mav = db.Column(db.String(256))
    crash_sd = db.Column(db.String(256))
    owner = db.Column(db.String(64))


    def import_details(self, data):
        try:
            self.mac_id = data['mac_id']
            self.crash_mav = data['crash_mav']
            self.crash_sd = data['crash_sd']
            self.latitude = data['latitude']
            self.longitude = data['longitude']
            self.time_of_crash = time.time()

        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self




class MetaData(db.Model):
    __tablename__ = 'metadata'
    id = db.Column(db.Integer, primary_key=True)
    metadata_ver = db.Column(db.String(64))
    time_stamp = db.Column(db.Integer)
    twilio_message_number = db.Column(db.String(64))
    hint = db.Column(EncryptedType(db.Integer, os.environ['SECRET_KEY']))
    
    crash_mav = db.Column(db.String(64))
    crash_sd = db.Column(db.String(64))

    theft_low_mav = db.Column(db.String(64))
    theft_low_sd = db.Column(db.String(64))

    theft_med_mav = db.Column(db.String(64))
    theft_med_sd = db.Column(db.String(64))

    theft_high_mav = db.Column(db.String(64))
    theft_high_sd = db.Column(db.String(64))

    shackle_x_mav = db.Column(db.String(64))
    shackle_x_sd = db.Column(db.String(64))

    shackle_y_mav = db.Column(db.String(64))
    shackle_y_sd = db.Column(db.String(64))

    shackle_z_mav = db.Column(db.String(64))
    shackle_z_sd = db.Column(db.String(64))

    rssi_1 = db.Column(db.String(64))
    rssi_2 = db.Column(db.String(64))
    rssi_3 = db.Column(db.String(64))
    rssi_4 = db.Column(db.String(64))
    rssi_5 = db.Column(db.String(64))

    battery_25 = db.Column(db.String(64))
    battery_50 = db.Column(db.String(64))
    battery_75 = db.Column(db.String(64))
    battery_100 = db.Column(db.String(64))

    update_firmware = db.Column(db.String(64))
    update_android = db.Column(db.String(64))
    update_iOS = db.Column(db.String(64))
    firmware_fixes = db.Column(db.String(1000))

    def get_url(self):
        return url_for('app.get_meta', id=self.id, _external=True)


    def export_data(self):
        return{
            'time_stamp': self.time_stamp,
            'metadata_ver': self.metadata_ver,
            'twilio_message_number': self.twilio_message_number,
            'crash_threshold': {
                'crash_mav': self.crash_mav,
                'crash_sd': self.crash_sd
            },

            'theft_threshold': {
                'low': {
                    'theft_low_mav': self.theft_low_mav,
                    'theft_low_sd': self.theft_low_sd
                },
                'medium':{
                    'theft_med_mav': self.theft_med_mav,
                    'theft_med_sd': self.theft_med_sd
                },
                'high':{
                    'theft_high_mav': self.theft_high_mav,
                    'theft_high_sd': self.theft_high_sd
                }
                
            },


            'shackle_insertion':{
                'x': {
                    'shackle_x_mav': self.shackle_x_mav,
                    'shackle_x_sd': self.shackle_x_sd
                },
                'y': {
                    'shackle_y_mav': self.shackle_y_mav,
                    'shackle_y_sd': self.shackle_y_sd
                },
                'z': {
                    'shackle_z_mav': self.shackle_z_mav,
                    'shackle_z_sd': self.shackle_z_sd
                }
            },


            'rssi':{
                'rssi_1': self.rssi_1,
                'rssi_2': self.rssi_2,
                'rssi_3': self.rssi_3,
                'rssi_4': self.rssi_4,
                'rssi_5': self.rssi_5
            },

            'battery':{
                'battery_25': self.battery_25,
                'battery_50': self.battery_50,
                'battery_75': self.battery_75,
                'battery_100': self.battery_100,
            },

            'Update':{
                'update_firmware': self.update_firmware,
                'update_android': self.update_android,
                'update_iOS': self.update_iOS
            }

        }


    def import_metadata_ver(self, data):
        try:
            self.time_stamp = str(int(time.time()))
            self.metadata_ver = data['metadata_ver']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    def import_twilio_number(self, data):
        try:
            self.time_stamp = str(int(time.time()))
            self.twilio_message_number = data['twilio_message_number']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    def import_crash_threshold(self, data):
        try:
            self.crash_mav = data['crash_mav']
            self.crash_sd = data['crash_sd']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    def import_theft_threshold_low(self, data):
        try:
            self.theft_low_mav = data['theft_low_mav']
            self.theft_low_sd = data['theft_low_sd']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    def import_theft_threshold_med(self, data):
        try:
            self.theft_med_mav = data['theft_med_mav']
            self.theft_med_sd = data['theft_med_sd']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    def import_theft_threshold_high(self, data):
        try:
            self.theft_high_mav = data['theft_high_mav']
            self.theft_high_sd = data['theft_high_sd']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    def import_shackle_insertion_x(self, data):
        try:
            self.shackle_x_mav = data['shackle_x_mav']
            self.shackle_x_sd = data['shackle_x_sd']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    def import_shackle_insertion_y(self, data):
        try:
            self.shackle_y_mav = data['shackle_y_mav']
            self.shackle_y_sd = data['shackle_y_sd']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    def import_shackle_insertion_z(self, data):
        try:
            self.shackle_z_mav = data['shackle_z_mav']
            self.shackle_z_sd = data['shackle_z_sd']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    def import_rssi(self, data):
        try:
            self.rssi_1 = data['rssi_1']
            self.rssi_2 = data['rssi_2']
            self.rssi_3 = data['rssi_3']
            self.rssi_4 = data['rssi_4']
            self.rssi_5 = data['rssi_5']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self


    def import_battery(self, data):
        try:
            self.battery_25 = data['battery_25']
            self.battery_50 = data['battery_50']
            self.battery_75 = data['battery_75']
            self.battery_100 = data['battery_100']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self



    def import_update(self, data):
        try:
            self.update_firmware = data['update_firmware']
            self.update_android = data['update_android']
            self.update_iOS = data['update_iOS']
        except KeyError as e:
            raise ValidationError('Invalid Entry')
        return self

    #Exports Firmware Data
    def export_firmware_version(self):
        return{
            'update_firmware': self.update_firmware,
            'firmware_fixes': self.firmware_fixes
        }


class Countrycode(db.Model):
    __tablename__ = 'countrycodes'
    id = db.Column(db.Integer, primary_key=True)
    telephone_code = db.Column(db.String(64))
    letter_code = db.Column(db.String(64))
    common_name = db.Column(db.String(64))
    
    
    def __init__(self, telephone_code, letter_code, common_name):
        self.telephone_code = telephone_code
        self.letter_code = letter_code
        self.common_name = common_name




class Firmware(db.Model):
    #__tablename__ = 'firmware'
    f_id = db.Column(db.Integer, primary_key=True)
    id = db.Column(db.Integer)
    boot_loader = db.Column(db.String(512))

    def __init__(self, boot_loader):
        self.boot_loader = boot_loader

    def export_data(self):
        return{
            'id': self.id,
            'boot_loader': self.boot_loader 
        }


class FirmwareBackup(db.Model):
    #__tablename__ = 'firmwarebackup'
    f_id = db.Column(db.Integer, primary_key=True)
    id = db.Column(db.Integer)
    boot_loader = db.Column(db.String(512))

    def __init__(self, boot_loader):
        self.boot_loader = boot_loader

    def export_data(self):
        return{
            'id': self.id,
            'boot_loader': self.boot_loader 
        }


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