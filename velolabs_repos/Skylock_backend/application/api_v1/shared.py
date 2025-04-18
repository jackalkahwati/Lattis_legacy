from flask import jsonify, url_for, jsonify, request, g, Blueprint, current_app, abort, render_template, session, redirect
from . import api
from .. import db
from ..models import User, Lock, Share

from notification import *
from text import *
from sqlalchemy.orm import load_only

otp_secret = os.environ['OTP_SECRET']





@api.route('/users/<user_id>/share/', methods=['POST'])
#@auth.login_required
def share(user_id):
    #user = User.query.filter_by(user_id=user_id).first_or_404()
    shareLimit=1
    #Share keys to other Application Users
    sharedUser = Share(shared_by=user_id)
    sharedUser.import_mac(request.json)

    if (g.user == user_id):
        #Check for the Lock Owner
        checkOwner = Lock.query.filter_by(user_id=user_id, mac_id=sharedUser.mac_id).first()
        if checkOwner:

            maxLockshared = Share.query.filter_by(mac_id=sharedUser.mac_id, status=1).count()
            if maxLockshared < shareLimit:
                #mac_pad = checkOwner.mac_id[:6]
                print(otp_secret)
                hotp = pyotp.HOTP(otp_secret)
                at = randint(0, 2147483647)

                #Pulling the Private and Public Key from the Key Table
                sharedUser.import_sharedkeys(checkOwner.sk_public_key_1, checkOwner.sk_public_key_2, checkOwner.sk_private_key, checkOwner.users_id,  at)
            
                checkShared = Share.query.filter_by(shared_by=user_id, mac_id=sharedUser.mac_id, shared_to=None).first()
                if checkShared:
                    db.session.query(Share).filter_by(shared_by=user_id, shared_to=None).update({"status_code":at, "sent_to": sharedUser.sent_to})
                    db.session.commit()
                    send_message(sharedUser.sent_to, text_message, render_template('Share_code.txt', code=hotp.at(at)))
                    return jsonify({'status': 201, 'error': None, 'payload': {'share_code': hotp.at(at)}})
                else:
                    db.session.add(checkOwner)
                    db.session.add(sharedUser)
                    db.session.commit()
                    send_message(sharedUser.sent_to, text_message, render_template('Share_code.txt', code=hotp.at(at)))
                    
                    return jsonify({'status': 201, 'error': None, 'payload': {'share_code': hotp.at(at)} })                      
            else:
                return jsonify({'status': 400, 'error': 'Max Share for this lock is Reached', 'payload': {}})
        else:
            return jsonify({'status': 400, 'error': 'You are Not the Owner of this Lock', 'payload': {}})
    else:
        return abort(403)




#Route for sending Share Code to Friends or Family
@api.route('/users/<user_id>/sharecode/', methods=['POST'])
def sharecode(user_id):
    if (g.user == user_id):
        user = Share(shared_by=user_id)
        user.import_sharecode(request.json)

        if user.shared_to != user_id:

            send_message(user.shared_to, text_message, render_template('Share_code.txt', code=user.status_code))

            return jsonify({'status': 201, 'error': None, 'payload':  {'message': 'Message sent' }})
        
        else:
            return jsonify({'status': 400, 'error': 'Enter a Valid Phone Number', 'payload': {}})
    else:
        return abort(403)





@api.route('/users/<user_id>/acceptsharing/', methods=['POST'])
def acceptsharing(user_id):
    acceptedLocks = User.query.filter_by(user_id=user_id).first_or_404()
    hotp = pyotp.HOTP(otp_secret)
    shareLimit=1
    if (g.user == user_id):
        sharedUser = Share(shared_to=user_id)
        sharedUser.import_code(request.json)
        #pooling in records with sharing not established
        users = (Share.query.filter_by(shared_to = None).all())
        #Pulling out each record for status code to compare with the user entered code
        user = None
        check_code = False
        for user in users:
            if hotp.verify(sharedUser.status_code, user.status_code):
                check_code = True
                break
        if check_code:
            if user:
                #check for unique
                check_for_uniq = Share.query.filter_by(shared_to=user_id, mac_id=user.mac_id, status=1).first()
                if check_for_uniq is None and user.shared_by != user_id:
                    maxLockshared = Share.query.filter_by(mac_id=user.mac_id, status=1).count()
                    if maxLockshared < acceptedLocks.maxLocks:

                        user.shared_to = user_id
                        user.status = 1
                        db.session.commit()
                        ellipse_name = Lock.query.filter_by(mac_id=user.mac_id).first()
                        return jsonify({'status': 201, 'error': None, 'payload': {'mac_id': user.mac_id, 'lock_name': ellipse_name.lock_name}})
                    else:
                        return jsonify({'status': 400, 'error': 'Max Share for this lock is Reached', 'payload': {}})        
                else:
                    return jsonify({'status': 400, 'error': 'you are already having access to this lock', 'payload':{}})
            else:
                return jsonify({'status': 404,  'error' : 'Entered code is Wrong', 'payload': {}})
        else:
            return jsonify({'status': 404,  'error' : 'Code Not Valid', 'payload': {}})
    else:
        return abort(403)  









#UNSHARE
@api.route('/users/<user_id>/unshare/', methods=['POST'])
def unshare(user_id):
    """ Removes Sharing Access """

    if (g.user == user_id):
        user = Share()
        user.import_details(request.json)

        #check from owner end
        checkSharing = Share.query.filter_by(shared_by=user_id, mac_id=user.mac_id, shared_to=user.user_id).first()
        if checkSharing:

            userDetails = User.query.filter_by(user_id=user.user_id).first()
            
            db.session.query(Share).filter_by(shared_by=user_id, mac_id=user.mac_id, shared_to=user.user_id).update({"status":0})
            db.session.commit()

            data = {'title': 'Lock Revoked', 'message': userDetails.first_name.upper()+' '+'Revoked his Skylock', 'mac_id': user.mac_id}
            send_notification(userDetails.reg_id, data)

            return jsonify({'status': 200, 'error': None, 'payload': {'message': 'Lock Revocked'}})

        elif Share.query.filter_by(shared_by=user.user_id, mac_id=user.mac_id, shared_to=user_id).first():

            userDetails = User.query.filter_by(user_id=user.user_id).first()
            
            checkloc = Lock.query.filter_by(user_id=user.user_id, mac_id=user.mac_id).first()
            db.session.add(checkloc)
            db.session.query(Share).filter_by(shared_by=user.user_id, mac_id=user.mac_id, shared_to=user_id).update({"status":0})
            db.session.commit()

            data = {'title': 'Lock Surrendered', 'message': userDetails.first_name.upper()+' '+'Surrendered his Access to your SKYLOCK', 'mac_id': user.mac_id}
            send_notification(userDetails.reg_id, data)

            return jsonify({'status': 200, 'error': None, 'payload': {'message': 'Lock Surrendered'}})

        else:
            return jsonify({'status': 204, 'error': 'No Shared Locks', 'payload': {}})
    else:
        return abort(403)

