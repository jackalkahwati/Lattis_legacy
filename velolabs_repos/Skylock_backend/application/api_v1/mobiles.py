import os
from flask import jsonify, url_for, jsonify, request, g, Blueprint, current_app, abort, render_template, session, redirect
from . import api, application
from .. import db
from ..models import Mobile, Share, Countrycode, Lock
import twilio, time
from threading import Thread
from ..decorators import async
from text import *
from errors import *



@api.route('/usermob/', methods=['GET'])
def get_usermob():
    """ Gives All Users Mobile Number URL (GET) :  """
    return jsonify({'mobiles': [mobile.get_url() for mobile in Mobile.query.all()]})
    
@api.route('/usermob/<user_id>', methods=['GET'])
def get_mobile(user_id):
    """ Returns Individual Users Mobile Number (GET) :  """
    return jsonify(Mobile.query.get_or_404(user_id).export_data())


#
@api.route('/users/<user_id>/mobiles/', methods=['PUT'])
def new_mob(user_id):
    """ Link to POST Individual Users Mobile Number (POST) :  """
    if (g.user == user_id):
        user = User.query.filter_by(user_id=user_id).first_or_404()
        mobile = Mobile(user_id=user_id, users_id=user.id)
        
        user = Mobile.query.filter_by(user_id=user_id).first()

        if user:
            user.import_mobile(request.json)
            
            country_code_finder = Countrycode.query.filter_by(letter_code=user.country_code).first()
            user.mobile = '+' + country_code_finder.telephone_code + user.mobile
            
            db.session.add(user)
        else:
            mobile.import_mobile(request.json)

            country_code_finder = Countrycode.query.filter_by(letter_code=mobile.country_code).first()
            mobile.mobile = '+' + country_code_finder.telephone_code + mobile.mobile

            db.session.add(mobile)
        db.session.commit()
        return jsonify({'status': 200, 'error': None, 'payload': {'message': 'User Mobile Number Added'}}), 200, {'Location': "Mobile_Number_Addition"}
    else:
        abort(403)




# ROUTE to add Emergency Contact Numbers
@api.route('/users/<user_id>/mobiles/<e_con>/', methods=['PUT'])
def emergency_contatcs(user_id, e_con):
    """ Link to ADD Emergency Contact Number (PUT) :  emergency_contact, emergency_contact_name"""
    if (g.user == user_id):
        name = User.query.filter_by(user_id=user_id).first_or_404()
        user = Mobile.query.filter_by(user_id=user_id).first()
        if user:
            if e_con == '1':
                user.import_emercont1(request.json)
                if (user.emergency_contact1 != user.emergency_contact2 and user.emergency_contact1 != user.emergency_contact3):
                    db.session.add(user)
                    send_message(user.emergency_contact1, text_message, render_template('Emergency_contact.txt', user=(name.first_name + ' ' + name.last_name), emguser=user.emergency_contact1_name ))
                    db.session.commit()
                    return jsonify({'status': 200, 'error': None, 'payload': {'message': 'Emergency Mobile number Updated'}}), 200, {'Location': "Emergency_Number_Updation"}
                else:
                    return jsonify({'status': 400, 'error': 'Emergency Mobile number exists', 'payload': {}})

            elif e_con == '2':
                user.import_emercont2(request.json)
                if (user.emergency_contact2 != user.emergency_contact1 and user.emergency_contact2 != user.emergency_contact3):
                    db.session.add(user)
                    send_message(user.emergency_contact2, text_message, render_template('Emergency_contact.txt', user=(name.first_name + ' ' + name.last_name), emguser=user.emergency_contact2_name ))
                    db.session.commit()
                    return jsonify({'status': 200, 'error': None, 'payload': {'message': 'Emergency Mobile number Updated'}}), 200, {'Location': "Emergency_Number_Updation"}
                else:
                    return jsonify({'status': 400, 'error': 'Emergency Mobile number exists', 'payload': {}})

            elif e_con == '3':
                user.import_emercont3(request.json)
                if (user.emergency_contact3 != user.emergency_contact1 and user.emergency_contact3 != user.emergency_contact2):
                    db.session.add(user)
                    send_message(user.emergency_contact3, text_message, render_template('Emergency_contact.txt', user=(name.first_name + ' ' + name.last_name), emguser=user.emergency_contact3_name ))
                    db.session.commit()
            
                    return jsonify({'status': 200, 'error': None, 'payload': {'message': 'Emergency Mobile number Updated'}}), 200, {'Location': "Emergency_Number_Updation"}
                else:
                    return jsonify({'status': 400, 'error': 'Emergency Mobile number exists', 'payload': {}})
            else:
                return jsonify({'status': 400, 'error': 'Wrong Input', 'payload': {}})
        else:
            return jsonify({'status': 400, 'error': 'Please Add Your Number', 'payload': {}})
    else:
        abort(403)





"""
# Upadted ROUTE to Shoot out Text Messages to Emergency Contacts.
@api.route('/users/<user_id>/sendhelp/', methods=['POST'])
def emergency_message(user_id):
    #Link to Send Crash Alert Message to Emergency Contacts (GET) :
    if (g.user == user_id):
        name = User.query.filter_by(user_id=user_id).first_or_404()
        user = Mobile.query.filter_by(user_id=user_id).first_or_404()

        user.import_details(request.json)

        if user.emergency_contact1:
            send_message(user.emergency_contact1, text_message, render_template('Emergency_contact_message.txt', user=(name.first_name+' '+name.last_name), emguser=user.emergency_contact1_name, lat=user.latitude, long=user.longitude ))

        if user.emergency_contact2:
            send_message(user.emergency_contact2, text_message, render_template('Emergency_contact_message.txt', user=(name.first_name+' '+name.last_name), emguser=user.emergency_contact2_name, lat=user.latitude, long=user.longitude ))

        if user.emergency_contact3:
            send_message(user.emergency_contact3, text_message, render_template('Emergency_contact_message.txt', user=(name.first_name+' '+name.last_name), emguser=user.emergency_contact3_name, lat=user.latitude, long=user.longitude ))


        #sending text for owner of the lock
        shared = Share.query.filter_by(shared_to=user_id, mac_id=user.mac_id).first()
        if shared is not None:
            user_mob = Mobile.query.filter_by(user_id=shared.shared_to).first()
            if user_mob:
                send_message(user_mob.mobile, text_message, render_template('Emergency_contact_message.txt', user=user.user_id, emguser=user.emergency_contact1_name, lat=user.latitude, long=user.longitude ))
        return jsonify({'status': 200, 'error': None, 'payload': {'message': 'Crash Alert Sent'}}), 200, {'Location': "Crash_Alert_Text"}
    else:
        abort(403)
"""



@api.route('/users/<user_id>/sendhelp/', methods=['POST'])
def emergency_message(user_id):

    if (g.user == user_id):
        name = User.query.filter_by(user_id=user_id).first_or_404()
        user = Mobile.query.filter_by(user_id=user_id).first_or_404()

        emegDetails = request.json
        emegContacts = emegDetails['contacts']
        location = emegDetails['position']
        
        try:
            lock_mac = emegDetails['mac_id']
            crash_latitude = location['latitude']
            crash_longitude = location['longitude']
            for emegContact in emegContacts:
                emeg_contact = emegContact['phone_number']
                emeg_first_name = emegContact['first_name']
        except KeyError as e:
            raise ValidationError('Invalid Entry')

        owned_lock = Lock.query.filter_by(user_id=user_id, mac_id=emegDetails['mac_id']).first()
        shared_lock = Share.query.filter_by(shared_to=user_id, mac_id=emegDetails['mac_id']).first()

        if owned_lock or shared_lock:
            for emegContact in emegContacts:
                send_message(emegContact['phone_number'], text_message, render_template('Emergency_contact_message.txt', \
                    user=(name.first_name.title()), \
                    emguser=emegContact['first_name'].title(), lat=location['latitude'], \
                    long=location['longitude'] ))

            #sending text for owner of the lock
            if shared_lock is not None:
                user_mob = Mobile.query.filter_by(user_id=shared_lock.shared_by).first()
                repeat = True
                for emegContact in emegContacts:
                    if emegContact['phone_number'] != user_mob.mobile:
                        repeat = False
                if user_mob and repeat:
                    send_message(user_mob.mobile, text_message, render_template('Emergency_contact_message.txt', \
                        user=user.user_id, emguser=user.emergency_contact1_name, lat=location['latitude'], long=location['longitude'] ))
            
            return jsonify({'status': 200, 'error': None, 'payload': {'message': 'Crash Alert Sent'}}), 200, \
            {'Location': "Crash_Alert_Text"}
        else:
            return jsonify({'status': 400, 'error': 'You Dont Have Access to this Lock', 'payload': {}})
    else:
        abort(403)






