from flask import jsonify, url_for, jsonify, request, g, Blueprint, current_app, abort, render_template, session, redirect
from . import api, application
from .. import db
from ..models import Lock, User, Share, Mobile
from ..auth import auth1_token
from ..decorators import rate_limit, etag
from ..KeyShiva import Key as keygen
from ..encoding import *
from ..ecdsa import *
import hashlib
import csv
from ..decorators import admin_required



@api.route('/users/<user_id>/alllocks/', methods=['GET'])
@admin_required
def alllock_details(user_id):

    if (g.user == user_id):
        usersLock = Lock.query.all()
        if usersLock:
            return jsonify({'status': 200, 'error': None, 'payload': {'location': {},  \
                'locks':{'locks': [usersLock.exportLockDetails() for usersLock in Lock.query.all()]}}})
        else:
            return jsonify({'status': 400, 'error': 'No Locks', 'payload': {}})
    else:
        return abort(403)

#MY LOCK LIST
@api.route('/users/<user_id>/locks/', methods=['GET'])
def lock_details(user_id):
    if (g.user == user_id):
        check_user = User.query.filter_by(user_id=user_id).first_or_404()
        locks = [usersLock.exportLockDetails() for usersLock in Lock.query.filter_by(user_id=user_id)]
        shared_locks = Share.query.filter_by(shared_to=user_id, status=1).first()
        locks_shared = Share.query.filter_by(shared_by=user_id, status=1).first()

        if locks is not None or shared_locks is not None or locks_shared is not None:
            #user = Share.query.filter_by(shared_to=user_id, status=1).first()
            if shared_locks is not None or locks_shared is not None:
                all_locks = {
                    'locks_shared_to_me': [shared_locks.export_data() for shared_locks in Share.query.filter_by(shared_to=user_id, status=1)],
                    'locks_shared_by_me': [locks_shared.export_shared_data() for locks_shared in Share.query.filter_by(shared_by=user_id, status=1)],
                    'my_locks': locks
                    }
            else:
                all_locks = {
                    'locks_shared_to_me': [],
                    'locks_shared_by_me': [],
                    'my_locks': locks
                }
        else:
            all_locks = {
                'locks_shared_to_me': [],
                'locks_shared_by_me': [],
                'my_locks': []
            }
        return jsonify({
                    'status': 200,
                    'error': None,
                    'payload': {
                        'location': {},
                        'locks': all_locks
                    }
                })
    else:
        return abort(403)


@api.route('/keys/', methods=['GET'])
def get_keys():
    # Gives URL links to all Locks (GET) :  """
    return jsonify({'keys': [lock.get_url() for lock in Lock.query.all()]})


@api.route('/keys/<int:key_id>', methods=['GET'])
def get_key(key_id):
    # Gives Lock details by sending in SELF URL  (GET) 
    return jsonify(Lock.query.get_or_404(key_id).exportLockDetails())



# Route to Link Locks to Users
@api.route('/users/<user_id>/keys/', methods=['POST'])
@rate_limit(limit=5, period=30)
def new_customer_key(user_id):

    if (g.user == user_id):
        user = User.query.filter_by(user_id=user_id).first_or_404()
        locks = Lock.query.filter_by(user_id=user_id).all()
        if not locks:
            lock_count=0
        else:
            lock_count = len(locks)

        key = Lock(user_id=user_id, users_id=user.id)
        key.import_data(request.json)
        try:
            macidCheck=int(key.mac_id,16)

            if (len(hex(macidCheck))-2) > 12:
                raise ValueError('Mac ID greater than 6-Bytes')
        except:
            return jsonify({'status': 400, 'error': 'MAC ID Is Not a HEX value or Greater than 6-Bytes', 'payload': {}})
        

        checkMac = Lock.query.filter_by(mac_id=key.mac_id).first()
        if checkMac is None:
            if lock_count >= user.maxLocks:
                return jsonify({'status': 400, 'error': 'Max Locks for this Account Reached', 'payload': {}})
            if user.first_name:
                key.lock_name = user.first_name.title() + '\'s' + ' ' + 'Ellipse' + ' ' +str(len(locks) + 1)
            else:
                key.lock_name = 'Ellipse' + ' ' + str(len(locks) + 1)
            db.session.add(key)
            db.session.commit()
            messageCombined_Mac, signRequeried, publicKey = key.export_message(user_id=key.user_id, mac_id=key.mac_id)
            return jsonify({'status': 201, 'error': None, 'payload': {'data': 'Keys Created', 'message': messageCombined_Mac, \
                'signed_message': signRequeried, 'public_key': publicKey}})


        elif Lock.query.filter_by(user_id=user_id, mac_id=key.mac_id).first():
            messageCombined_Mac, signRequeried, publicKey = key.export_message(user_id=key.user_id, mac_id=key.mac_id)
            return jsonify({'status': 200, 'error': None, 'payload': {'data': 'Got Owner Keys', 'message': messageCombined_Mac, \
                'signed_message': signRequeried, 'public_key': publicKey}})

        elif Share.query.filter_by(shared_to=user_id, mac_id=key.mac_id, status=True).first():
            messageCombined_Mac, signRequeried, publicKey = key.export_message(user_id=key.user_id, mac_id=key.mac_id, owner='01')
            return jsonify({'status': 200, 'error': None, 'payload': {'data': 'Got Shared Keys', 'message': messageCombined_Mac, \
                'signed_message': signRequeried, 'public_key': publicKey}})

        else:
            return jsonify({'status': 400, 'error': 'You Dont Have Access to this Lock or Lock is Linked to another user', 'payload': {}})
    else:
        return abort(403)
    

# POST user lock name to backend
@api.route('/users/<user_id>/lockname/', methods=['POST'])
def add_lock_name(user_id):
    if (g.user == user_id):
        add_name = Lock()
        add_name.add_name(request.json)

        checkOwner = Lock.query.filter_by(user_id=user_id, mac_id=add_name.mac_id).first()
        if checkOwner:
            db.session.query(Lock).filter_by(user_id=user_id, mac_id=add_name.mac_id).update({"lock_name":add_name.lock_name})
            db.session.commit()
            return jsonify({'status': 201, 'error': None, 'payload': {'message': 'Lock Name Updated'}})
        else:
            abort(403)
    else:
        return abort(403)



@api.route('/users/<user_id>/getlockname/', methods=['POST'])
def get_lock_name(user_id):
    if (g.user == user_id):
        get_name = Lock()
        get_name.getlock_name(request.json)
        check_name = Lock.query.filter_by(mac_id=get_name.mac_id).first()
        if check_name:
            return jsonify({'status': 201, 'error': None, 'payload': {'lock_name': check_name.lock_name}})
        else:
            return abort(404)
    else:
        return abort(403)


# POST lock touch pad Data
@api.route('/users/<user_id>/touchpad/', methods=['POST'])
def add_touchpad_combination(user_id):
    if (g.user == user_id):
        add_combination= Lock()
        add_combination.add_combination(request.json)

        checkOwner = Lock.query.filter_by(user_id=user_id, mac_id=add_combination.mac_id).first()
        if checkOwner:
            db.session.query(Lock).filter_by(user_id=user_id, mac_id=add_combination.mac_id).update({"touch_pad":add_combination.touch_pad})
            db.session.commit()
            return jsonify({'status': 201, 'error': None, 'payload': {'message': 'Lock TouchPad Updated'}})
        else:
            abort(403)
    else:
        return abort(403)




# DELETE or REMOVE lock from the User and Backend 
@api.route('/users/<user_id>/deletelock/', methods=['POST'])
def delete_user_keys(user_id):
    """ REMOVE Locks LINKED to the User (GET) :   """
    if (g.user == user_id):
        delete_key = Lock()
        unshare_user = Share()
        delete_key.delete_keys(request.json)

        checkOwner = Lock.query.filter_by(user_id=user_id, mac_id=delete_key.mac_id).first()
        if checkOwner:
            db.session.query(Share).filter_by(mac_id=delete_key.mac_id, shared_by=user_id).delete()
            db.session.query(Lock).filter_by(user_id=user_id, mac_id=delete_key.mac_id).delete()
            db.session.commit()
            return jsonify({'status': 201, 'error': None, 'payload': {'message': 'Lock Deleted'}})
        else:
            abort(403)
    else:
        return abort(403)
