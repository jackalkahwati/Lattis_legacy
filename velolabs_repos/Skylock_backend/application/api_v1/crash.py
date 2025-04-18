from flask import jsonify, request, g, abort
from . import api, application
from .. import db
from ..models import Lock, Crashdata, Share, User



#Work on crash for MYSQL Changes
@api.route('/users/<user_id>/crash/', methods=['POST'])
def crash_report(user_id):
    """ Crash Reporting (POST) :  """

    if (g.user == user_id):
        user = User.query.filter_by(user_id=user_id).first_or_404()
        crash = Crashdata()
        crash.import_details(request.json)
        crash.user_id = user_id
    
        share = Share.query.filter_by(shared_to=user_id, mac_id=crash.mac_id).first()
        if share:
            crash.owner= share.shared_by   
            db.session.add(crash)
            db.session.commit()
            return jsonify({'status': 'success', 'message': 'Crash Details Posted', 'payload': {}})

        elif Lock.query.filter_by(user_id=user_id, mac_id=crash.mac_id).first():
            crash.owner= None
            db.session.add(crash)
            db.session.commit()
            return jsonify({'status': 'success', 'message': 'Crash Details Posted', 'payload': {}})

        else:
            return jsonify({'status': 'error', 'message': 'Something Went Wrong', 'payload': {}})
    else:
        abort(403)



