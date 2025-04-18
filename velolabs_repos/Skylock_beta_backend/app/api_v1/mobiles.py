import os
from flask import jsonify, url_for, jsonify, request, g, Blueprint, current_app, abort, render_template, session, redirect
from . import api, app
from .. import db
from ..models import Mobile, Share
import twilio, time
from threading import Thread
from ..decorators import async
from text import *



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
        mobile = Mobile(user_id=user_id)
        
        user = Mobile.query.filter_by(user_id=user_id).first()
        if user:
            user.import_mobile(request.json)
            db.session.add(user)
        else:
            mobile.import_mobile(request.json)
            db.session.add(mobile)
        db.session.commit()
        return jsonify({'status': 'success', 'message': 'User Mobile Number Added', 'payload': {}}), 200, {'Location': "Mobile_Number_Addition"}
    else:
        return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': {}})




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
                    #mobile_message = MetaData.query.filter_by(metadata_ver="metadata").first()
                    send_message(user.emergency_contact1, text_message, render_template('Emergency_contact.txt', user=(name.first_name + ' ' + name.last_name), emguser=user.emergency_contact1_name ))
                    db.session.commit()
                    return jsonify({'status': 'success', 'message': 'Emergency Mobile number Updated', 'payload': {}}), 200, {'Location': "Emergency_Number_Updation"}
                else:
                    return jsonify({'status': 'error', 'message': 'Emergency Mobile number exists', 'payload': {}})

            elif e_con == '2':
                user.import_emercont2(request.json)
                if (user.emergency_contact2 != user.emergency_contact1 and user.emergency_contact2 != user.emergency_contact3):
                    db.session.add(user)
                    #mobile_message = MetaData.query.filter_by(metadata_ver="metadata").first()
                    send_message(user.emergency_contact2, text_message, render_template('Emergency_contact.txt', user=(name.first_name + ' ' + name.last_name), emguser=user.emergency_contact2_name ))
                    db.session.commit()
                    return jsonify({'status': 'success', 'message': 'Emergency Mobile number Updated', 'payload': {}}), 200, {'Location': "Emergency_Number_Updation"}
                else:
                    return jsonify({'status': 'error', 'message': 'Emergency Mobile number exists', 'payload': {}})

            elif e_con == '3':
                user.import_emercont3(request.json)
                if (user.emergency_contact3 != user.emergency_contact1 and user.emergency_contact3 != user.emergency_contact2):
                    db.session.add(user)
                    #mobile_message = MetaData.query.filter_by(metadata_ver="metadata").first()
                    send_message(user.emergency_contact3, text_message, render_template('Emergency_contact.txt', user=(name.first_name + ' ' + name.last_name), emguser=user.emergency_contact3_name ))
                    db.session.commit()
            
                    return jsonify({'status': 'success', 'message': 'Emergency Mobile number Updated', 'payload': {}}), 200, {'Location': "Emergency_Number_Updation"}
                else:
                    return jsonify({'status': 'error', 'message': 'Emergency Mobile number exists', 'payload': {}})
            else:
                return jsonify({'status': 'error', 'message': 'Wrong Input', 'payload': {}})
        else:
            return jsonify({'status': 'error', 'message': 'Please Add Your Number', 'payload': {}})
    else:
        return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': {}})





# ROUTE to Shoot out Text Messages to Emergency Contacts.
@api.route('/users/<user_id>/sendmessage/', methods=['POST'])
def emer_message(user_id):
    """ Link to Send Crash Alert Message to Emergency Contacts (GET) :  """
    if (g.user == user_id):
    	name = User.query.filter_by(user_id=user_id).first_or_404()
        user = Mobile.query.filter_by(user_id=user_id).first_or_404()
        #mobile_message = MetaData.query.filter_by(metadata_ver="metadata").first()

        user.import_crash(request.json)
        #print (user.mac_id)

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
        return jsonify({'status': 'success', 'message': 'Crash Alert Sent', 'payload': {}}), 200, {'Location': "Crash_Alart_Text"}
    else:
        return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': {}})









