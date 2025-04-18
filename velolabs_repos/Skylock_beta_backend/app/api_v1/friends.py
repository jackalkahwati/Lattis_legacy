from flask import jsonify, request, g
from . import api
from .. import db
from ..models import User, Friend
from notification import *



@api.route('/friends/', methods=['GET'])
def get_friends():
    """ Gets the URL of existing Users (GET) : """
    return jsonify({'friends': [friend.get_url() for friend in Friend.query.all()]})



@api.route('/users/<user_id>/friends/', methods=['POST'])
def add_friend(user_id):
    if (g.user == user_id):
        user = User.query.filter_by(user_id=user_id).first()
        friend = Friend(user_id=user_id)

        friend.import_data(request.json)
        if user_id != friend.friend_id:
            friendPresent = User.query.filter_by(user_id=friend.friend_id).first()

            if friendPresent:
                check_for_existance = Friend.query.filter_by(user_id=user_id, friend_id=friend.friend_id).first()

                if check_for_existance is None:

                    friend.import_names(friendPresent.first_name, friendPresent.last_name, user_id)
                    mutual = Friend(user_id=user_id)
                    mutual.mutual_friends(friendPresent.user_id, user_id, user.first_name, user.last_name, user_id)
                    db.session.add(friend)
                    db.session.add(mutual)
                    db.session.commit()

                    data = {'title': 'Friend Linked', 'message': friendPresent.first_name.upper()+' '+'Sent a Friend request on Skylock', 'id': '21'}
                    
                    return jsonify({"status": "success", 'message': 'Friend Linked', 'payload': {}}), 201, {'Location': user.get_url()}
                else:
                    return jsonify({'status': 'error', 'message': 'Already a Friend', 'payload': {}})
            else:
                return jsonify({'status': 'error', 'message': 'Ask User to Install Skylock', 'payload': {}})
        else:
            return jsonify({'status': 'error', 'message': 'You Cant add Yourself', 'payload': {}})
    else:
        return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': {}})




@api.route('/users/<user_id>/unfriend/', methods=['POST'])
def remove_friend(user_id):
    if (g.user == user_id):
        user = User.query.filter_by(user_id=user_id).first()
        friend = Friend(user_id=user_id)

        friend.import_data(request.json)
        if user_id != friend.friend_id:
            friendPresent = User.query.filter_by(user_id=friend.friend_id).first()

            if friendPresent:
                check_for_existance = Friend.query.filter_by(user_id=user_id, friend_id=friend.friend_id).first()

                if check_for_existance:
                    check_for_mutual = Friend.query.filter_by(user_id=friend.friend_id, friend_id=user_id).first()
                    db.session.delete(check_for_mutual)
                    db.session.delete(check_for_existance)
                    db.session.commit()
                    return jsonify({"status": "success", 'message': 'Unfriend Success', 'payload': {}})
                else:
                    return jsonify({'status': 'error', 'message': 'You are Not a Friend of this User', 'payload': {}})
            else:
                return jsonify({'status': 'error', 'message': 'Skylock User Dosent Exist', 'payload': {}})
        else:
            return jsonify({'status': 'error', 'message': 'You Cant Unfriend Yourself', 'payload': {}})
    else:
        return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': {}})




@api.route('/users/<user_id>/friendlist/', methods=['GET'])
def get_friend(user_id):
    if (g.user == user_id):
        friend = Friend.query.filter_by(user_id=user_id).first()

        if friend:
            return jsonify({'status': 'success', 'message': 'Friends', 'payload': {'my_friends': [friend.export_data() for friend in Friend.query.filter_by(user_id=user_id)]}})
        else:
            return jsonify({'status': 'success', 'message': 'No Friends', 'payload': {}})
    else:
        return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': {}})



@api.route('/users/<user_id>/confirm/', methods=['POST'])
def confirm_friend(user_id):
    if (g.user == user_id):
        friend = Friend(user_id=user_id)
        friend.import_data(request.json)
        friend_status_change = Friend.query.filter_by(user_id=friend.friend_id, friend_id = user_id).first_or_404()

        if friend_status_change.sent_by != user_id:
            db.session.query(Friend).filter_by(user_id=user_id, friend_id = friend.friend_id).update({"status":1})
            db.session.query(Friend).filter_by(user_id=friend.friend_id, friend_id = user_id).update({"status":1})
            db.session.commit()
            return jsonify({'status': 'success', 'message': 'Friends Updated', 'payload': {}})   
        else:
            return jsonify({'status': 'error', 'message': 'Pending from your Friend', 'payload': {}})
    else:
        return jsonify({'status': 'error', 'message': 'Unauthorized Route', 'payload': {}})