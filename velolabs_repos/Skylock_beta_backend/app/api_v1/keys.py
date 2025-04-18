from flask import jsonify, url_for, jsonify, request, g, Blueprint, current_app, abort, render_template, session, redirect
from . import api
from .. import db
from ..models import Key, User, Share, Mobile

from ..KeyShiva import Key as keygen
from ..encoding import *
from ..ecdsa import *
import hashlib
import csv



#MY LOCK LIST
@api.route('/users/<user_id>/locks/', methods=['GET'])
def lock_details(user_id):

    if (g.user == user_id):
        usersLock = Key.query.filter_by(user_id=user_id).first()
        user = Share.query.filter_by(shared_to=user_id).first()
        if usersLock or user:
            return jsonify({'status': 'success', 'message': 'Lock', 'payload': {'shared_locks': [user.export_data() for user in Share.query.filter_by(shared_to=user_id)], 'my_locks': [usersLock.exportLockDetails() for usersLock in Key.query.filter_by(user_id=user_id)]}})
        else:
            return jsonify({'status': 'error', 'message': 'No Locks', 'payload': {}})
    else:
        return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': {}})


@api.route('/keys/', methods=['GET'])
def get_keys():
    # Gives URL links to all Locks (GET) :  """
    return jsonify({'keys': [key.get_url() for key in Key.query.all()]})


@api.route('/keys/<int:key_id>', methods=['GET'])
def get_key(key_id):
    # Gives Lock details  (GET) :  Have to work on it
    return jsonify(Key.query.get_or_404(key_id).export_data())



# Route to Link Locks to Users
@api.route('/users/<user_id>/keys/', methods=['POST'])
def new_customer_key(user_id):

    print request.headers

    if (g.user == user_id):
        user = User.query.filter_by(user_id=user_id).first_or_404()
        locks = Key.query.filter_by(user_id=user_id).all()
        if not locks:
            lock_count=0
            #print (lock_count)
        else:
            #print (len(locks))
            #print (locks[0].mac_id)
            lock_count = len(locks)

        key = Key(user_id=user_id)
        key.import_data(request.json)
        try:
            macidCheck=int(key.mac_id,16)

            if (len(hex(macidCheck))-2) > 12:
                raise ValueError('Mac ID greater than 6-Bytes')
        except:
            return jsonify({'status': 'error', 'message': 'MAC ID Is Not a HEX value or Greater than 6-Bytes', 'payload': {}})
        

        checkMac = Key.query.filter_by(mac_id=key.mac_id).first()
        if checkMac is None:
            if lock_count >= user.maxLocks:
                return jsonify({'status': 'error', 'message': 'Max Locks for this Account Reached', 'payload': {}})
            db.session.add(key)
            db.session.commit()
            messageCombined_Mac, signRequeried, publicKey = key.export_message(user_id=key.user_id, mac_id=key.mac_id)
            return jsonify({'status': 'success', 'message': 'Keys Created', 'payload': {'message': messageCombined_Mac, 'signed_message': signRequeried, 'public_key': publicKey}})


        elif Key.query.filter_by(user_id=user_id, mac_id=key.mac_id).first():
            messageCombined_Mac, signRequeried, publicKey = key.export_message(user_id=key.user_id, mac_id=key.mac_id)
            return jsonify({'status': 'success', 'message': 'Got Owner Keys', 'payload': {'message': messageCombined_Mac, 'signed_message': signRequeried, 'public_key': publicKey}})

        elif Share.query.filter_by(shared_to=user_id, mac_id=key.mac_id).first():
            messageCombined_Mac, signRequeried, publicKey = key.export_message(user_id=key.user_id, mac_id=key.mac_id, owner='01')
            return jsonify({'status': 'success', 'message': 'Got Shared Keys', 'payload': {'message': messageCombined_Mac, 'signed_message': signRequeried, 'public_key': publicKey}})

        else:
            return jsonify({'status': 'error', 'message': 'You Dont Have Access to this Lock or Lock is Linked to another user', 'payload': {'lock_owner': checkMac.user_id, 'key_id': checkMac.key_id }})
    else:
        return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': {}})
    





# DELETE or REMOVE lock from the User and Backend 
@api.route('/users/<user_id>/deletelock/', methods=['POST'])
def delete_user_keys(user_id):
    """ REMOVE Locks LINKED to the User (GET) :   """
    delete_key = Key()
    unshare_user = Share()
    delete_key.delete_keys(request.json)

    checkOwner = Key.query.filter_by(user_id=user_id, mac_id=delete_key.mac_id).first()
    if checkOwner:
        db.session.query(Share).filter_by(mac_id=delete_key.mac_id, shared_by=user_id).delete()
        db.session.query(Key).filter_by(user_id=user_id, mac_id=delete_key.mac_id).delete()
        db.session.commit()
        return jsonify({'status': 'error', 'message': 'Lock Deleted', 'payload': {}})
    else:
        return jsonify({'status': 'error', 'message': 'You are NOT the Owner of this LOCK', 'payload': {}})

