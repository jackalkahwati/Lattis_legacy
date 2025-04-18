import os
from flask import jsonify, url_for, jsonify, request, g, Blueprint, current_app, abort, render_template, session, redirect
from . import api, app
from .. import db
from ..models import MetaData, User
import twilio, time
from threading import Thread
from flask.ext import excel
from text import *
from ..decorators import admin_required

adm = os.environ['ADMIN']
otp_secret = os.environ['OTP_SECRET']



@app.route('/admin/<user_id>', methods = ['GET'])
@admin_required
def admin(user_id):
    meta= MetaData.query.filter_by(id=1).first()

    hotp = pyotp.HOTP(otp_secret)
    at = randint(0, 2147483647)
    db.session.query(MetaData).filter_by(id=1).update({"hint":at})
    db.session.commit()
    code = hotp.at(at)
    print(adm) 
    admins=adm.split(',')
    count=0
    while count < len(admins):
        send_message(admins[count], text_message, render_template('Password_reset.txt', code=code))
        print(admins[count])
        count=count+1

    return jsonify(payload={}, status='success', message='Message Sent to Admins')



@app.route('/meta/<int:id>', methods=['GET'])
def get_meta(id):
    """ Meta Data Details (GET) : <int:id> = 1 always """
    return jsonify(MetaData.query.get_or_404(id).export_data())


@app.route('/metadata/', methods=['GET'])
def get_metadata():
    """ Meta DATA Details (GET) : NOT WORKING"""
    meta=MetaData.query.get_or_404(id)
    return jsonify(MetaData.query.get_or_404(id=1).export_data())


#Backup Method
@app.route('/metadata_ver/<int:id>', methods=['PUT'])
def get_metadata_name(id):
    """ Meta DATA Details (PUT) : Changing the Version"""
    metadata = MetaData.query.get_or_404(id)
    metadata.import_metadata_ver(request.json)
    db.session.add(metadata)
    db.session.commit()

    return jsonify({"status": "Meta_Data Name Updated"}), 200, {'Location': metadata.get_url()}


@app.route('/twilio_number/<int:id>', methods=['PUT'])
def twilio_number(id):
    """ Changing Twilio app Number (PUT) : """
    metadata = MetaData.query.get_or_404(id)
    metadata.import_twilio_number(request.json)
    db.session.add(metadata)
    db.session.commit()

    return jsonify({"status": "Twilio Number Updated"}), 200, {'Location': metadata.get_url()}


@app.route('/crash_threshold/<int:id>', methods=['PUT'])
def crash_threshold(id):
    """ Changing Crash Threshold (PUT) : """
    data = MetaData.query.get_or_404(id)
    data.import_crash_threshold(request.json)
    db.session.add(data)
    db.session.commit()

    return jsonify({"status": "Crash Threshold Updated"}), 200, {'Location': data.get_url()}


@app.route('/theft_threshold_low/<int:id>', methods=['PUT'])
def crash_threshold_low(id):
    """ Changing Theft LOW Threshold (PUT) : """
    data = MetaData.query.get_or_404(id)
    data.import_theft_threshold_low(request.json)
    db.session.add(data)
    db.session.commit()

    return jsonify({"status": "Theft_threshold_low Updated"}), 200, {'Location': "Meta Data Updated"}



@app.route('/theft_threshold_med/<int:id>', methods=['PUT'])
def crash_threshold_med(id):
    """ Changing Theft Medium Threshold (PUT) : """
    data = MetaData.query.get_or_404(id)
    data.import_theft_threshold_med(request.json)
    db.session.add(data)
    db.session.commit()

    return jsonify({"status": "Theft_threshold_med Updated"}), 200, {'Location': "Meta Data Updated"}


@app.route('/theft_threshold_high/<int:id>', methods=['PUT'])
def crash_threshold_high(id):
    """ Changing Theft High Threshold (PUT) : """
    data = MetaData.query.get_or_404(id)
    data.import_theft_threshold_high(request.json)
    db.session.add(data)
    db.session.commit()

    return jsonify({"status": "Theft_threshold_high Updated"}), 200, {'Location': "Meta Data Updated"}


@app.route('/shackle_insertion_x/<int:id>', methods=['PUT'])
def shackle_insertion_x(id):
    """ Changing Shackle Insertion X axis Threshold (PUT) : """
    data = MetaData.query.get_or_404(id)
    data.import_shackle_insertion_x(request.json)
    db.session.add(data)
    db.session.commit()

    return jsonify({"status": "Shackle_insertion_x Updated"}), 200, {'Location': "Meta Data Updated"}


@app.route('/shackle_insertion_y/<int:id>', methods=['PUT'])
def shackle_insertion_y(id):
    """ Changing Shackle Insertion Y axis Threshold (PUT) : """
    data = MetaData.query.get_or_404(id)
    data.import_shackle_insertion_y(request.json)
    db.session.add(data)
    db.session.commit()

    return jsonify({"status": "Shackle_insertion_y Updated"}), 200, {'Location': "Meta Data Updated"}


@app.route('/shackle_insertion_z/<int:id>', methods=['PUT'])
def shackle_insertion_z(id):
    """ Changing Shackle Insertion Z axis Threshold (PUT) : """
    data = MetaData.query.get_or_404(id)
    data.import_shackle_insertion_z(request.json)
    db.session.add(data)
    db.session.commit()

    return jsonify({"status": "Shackle_insertion_z Updated"}), 200, {'Location': "Meta Data Updated"}


@app.route('/rssi/<int:id>', methods=['PUT'])
def rssi(id):
    """ Changing RSSI Threshold (PUT) : """
    data = MetaData.query.get_or_404(id)
    data.import_rssi(request.json)
    db.session.add(data)
    db.session.commit()

    return jsonify({"status":  "rssi Updated"}), 200, {'Location': "Meta Data Updated"}


@app.route('/battery/<int:id>', methods=['PUT'])
def battery(id):
    """ Changing Battery Threshold (PUT) : """
    data = MetaData.query.get_or_404(id)
    data.import_battery(request.json)
    db.session.add(data)
    db.session.commit()

    return jsonify({"status": "Battery Updated"}), 200, {'Location': "Meta Data Updated"}


@app.route('/update_detail/<int:id>', methods=['PUT'])
def update_detail(id):
    """ Firmware Update Details (PUT) : """
    data = MetaData.query.get_or_404(id)
    data.import_update(request.json)
    db.session.add(data)
    db.session.commit()

    return jsonify({"status": "Updated"}), 200, {'Location': "Meta Data Updated"}


