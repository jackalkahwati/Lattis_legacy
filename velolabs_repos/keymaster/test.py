from application.signer import Signer

signer = Signer()
message = signer.create_signed_message(
    mac_id='D7D36363572F',
    user_id='5107171635',
    private_key='e15878baa6c122d3383f93a4b200088550932b317aedf3f65212776262680c1a',
    public_key='c03b3a26443e37d5eefbe937ac82138952fa5ddd8fcd7ada0b145a331e0058a7dcadefdbbc2dc0dae69d73008788927cb9dcfe1d3fdbd2e580be5d5bcac95817',
    time='ffffffff',
    security='00',
    owner='00'
)

print('signed message', message)
print 'signed message length', len(message)/2
