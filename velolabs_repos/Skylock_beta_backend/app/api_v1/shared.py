from flask import jsonify, url_for, jsonify, request, g, Blueprint, current_app, abort, render_template, session, redirect
from . import api
from .. import db
from ..models import User, Key, Share

from notification import *



@api.route('/users/<user_id>/share/', methods=['POST'])
#@auth.login_required
def share(user_id):
    shareLimit=5
    #Share keys to other Application Users
    sharedUser = Share(shared_by=user_id)
    sharedUser.import_sharee(request.json)

    if (g.user == user_id):
        #Check for the Lock Owner
        checkOwner = Key.query.filter_by(user_id=user_id, mac_id=sharedUser.mac_id).first()
        if checkOwner:
            #Check for sharing user existance
            userPresent = User.query.filter_by(user_id=sharedUser.shared_to).first()
            if userPresent:
                #Pulling the Private and Public Key from the Key Table
                sharedUser.import_sharedkeys(checkOwner.sk_public_key_1, checkOwner.sk_public_key_2, checkOwner.sk_private_key)
        
                #check for unique
                check_for_uniq = Share.query.filter_by(shared_to=sharedUser.shared_to, mac_id=sharedUser.mac_id, shared_by=user_id).first()
                if check_for_uniq is None:
                    maxLockshared = Share.query.filter_by(mac_id=sharedUser.mac_id).count()
                    print ("maxLockshared: %s" %maxLockshared)
                    #check for max shared by one lock
                    #if maxLockshared < checkOwner.maxKeys:
                    if maxLockshared < shareLimit:
                        #check for Shared user limit exceding more than fixed limit
                        #checkShared_by = Share.query.distinct(Share.shared_by, Share.mac_id).filter_by(shared_by=user_id).count() + Share.query.distinct(Share.shared_to, Share.mac_id).filter_by(shared_to=user_id).count()
                        checkShared_to = Share.query.distinct(Share.shared_by, Share.mac_id).filter_by(shared_by=sharedUser.shared_to).count() + Share.query.distinct(Share.shared_to, Share.mac_id).filter_by(shared_to=sharedUser.shared_to).count()
                        #print ("checkShared_by: %s" %checkShared_by)
                        print ("checkShared_to: %s" %checkShared_to)
                        print ("userPresent.maxLocks: %s" %userPresent.maxLocks)
                        #Check How many locks Shared User Ownes
                        checkSharedUserLocks = Key.query.filter_by(user_id=sharedUser.shared_to).count()
                        checkShared_to += checkSharedUserLocks
                        print ("checkShared_to: %s" %checkShared_to)
                        print ("User Owned Locks: %s" %checkShared_to)
                        #Check for MaxLimit and User sharing to himself
                        
                        if checkShared_to < userPresent.maxLocks:
                            if sharedUser.shared_to != sharedUser.shared_by:
                                checkOwner.lock_location(sharedUser.latitude, sharedUser.longitude)
                                db.session.add(checkOwner)
                                db.session.add(sharedUser)
                                db.session.commit()
                                data = {'title': 'Lock Shared', 'message': userPresent.first_name.upper()+' '+'Shared his Skylock', 'id': '11'}
                                send_notification(userPresent.reg_id, data)
                                #return jsonify(Share.query.get_or_404(sharedUser.id).export_data())
                                return jsonify({'status': 'success', 'message': 'Lock Shared', 'payload': {} })
                            else:
                                return jsonify({'status': 'error', 'message': 'You can not share to yourself', 'payload': {}})
                        else:
                            return jsonify({'status': 'error', 'message': "The Person whom you trying to share has reached his LIMIT of Lock Handling",  'payload': {}})
                    else:
                        return jsonify({'status': 'error', 'message': 'Max Share for this lock is Reached', 'payload': {}})
                else:
                    return jsonify({'status': 'error', 'message': 'Lock Already Shared to the User', 'payload': {}})
            else:
                return jsonify({'status': 'error', 'message': 'User Dosent Exists, Please ask them to Install Skylock Application', 'payload': {}})
        else:
            return jsonify({'status': 'error', 'message': 'You are Not the Owner of this Lock', 'payload': {}})
    else:
        return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': {}})    



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
            
            db.session.query(Share).filter_by(shared_by=user_id, mac_id=user.mac_id, shared_to=user.user_id).delete()
            db.session.commit()

            data = {'title': 'Lock Revoked', 'message': userDetails.first_name.upper()+' '+'Revoked his Skylock', 'id': '12'}
            send_notification(userDetails.reg_id, data)

            return jsonify({'status': 'success', 'message': 'Lock Revoked', 'payload': {}})

        elif Share.query.filter_by(shared_by=user.user_id, mac_id=user.mac_id, shared_to=user_id).first():

            userDetails = User.query.filter_by(user_id=user.user_id).first()
            
            checkloc = Key.query.filter_by(user_id=user.user_id, mac_id=user.mac_id).first()
            checkloc.lock_location(user.latitude, user.longitude)
            db.session.add(checkloc)

            db.session.query(Share).filter_by(shared_by=user.user_id, mac_id=user.mac_id, shared_to=user_id).delete()
            db.session.commit()

            data = {'title': 'Lock Surrendered', 'message': userDetails.first_name.upper()+' '+'Surrendered his Access to your SKYLOCK', 'id': '13'}
            send_notification(userDetails.reg_id, data)

            return jsonify({'status': 'success', 'message': 'Lock Surrendered', 'payload': {}})

        else:
            return jsonify({'status': 'error', 'message': 'No Shared Locks', 'payload': {}})
    else:
        return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': {}})

