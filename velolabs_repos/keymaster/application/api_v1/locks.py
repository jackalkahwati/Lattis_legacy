from flask import  jsonify, request
from . import api
from ..decorators import rate_limit
from ..signer import Signer


@api.route('/signed-message', methods=['POST'])
@rate_limit(limit=5, period=30)
def signed_message():
    signer_data = request.get_json()
    print 'got request body:', signer_data
    signer = Signer()
    message = signer.create_signed_message(
        signer_data['mac_id'],
        signer_data['user_id'],
        signer_data['private_key'],
        signer_data['public_key'],
        signer_data['time'],
        signer_data['security'],
        signer_data['owner']
    )

    return jsonify({
        'status': 200,
        'error': None,
        'payload': {'signed_message': message}
    })
