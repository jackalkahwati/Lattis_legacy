from KeyShiva import Key as keygen
from encoding import *
import binascii


class Signer:
    def create_signed_message(self, mac_id, user_id, private_key, public_key, time, security, owner):
        key_gen = keygen.generate(256)
        print('got', mac_id, user_id, private_key, public_key, time, security, owner)
        # private_key and public_key should be assigned here to the key_get _priv & _pub properties
        # key_gen._priv = (256, (int(keys.sk_private_key,16)))
        # key_gen._pub = (256, (int(keys.sk_public_key_1,16),int(keys.sk_public_key_2,16)))
        public_key_1 = public_key[:len(public_key)/2]
        public_key_2 = public_key[len(public_key)/2:]
        print('pub key 1:', public_key_1, 'pub key 2', public_key_2)
        key_gen._priv = (256, (int(private_key,16)))
        key_gen._pub = (256, (int(public_key,16), int(public_key,16)))
        messageEncrypt = key_gen.encrypt(user_id+mac_id)
        messageCombined = self.b2a(messageEncrypt[:31]) + time + security
        messageCombined_Mac =  mac_id + owner + messageCombined

        sign = key_gen.sign(self.a2b(messageCombined_Mac))
        sign = self.b2a(sign)
        return owner + messageCombined + sign

    def a2b(self, data):
        temp_string1=""
        for i in range(0,len(data) - 1, 2):
            data[i:i+2]
            x1 = binascii.a2b_hex(data[i:i+2])
            temp_string1 = temp_string1 + x1
        temp_string2 = temp_string1
        return temp_string2

    def b2a(self, data):
        temp_string_msg = ""
        for letter1 in data:
            x1 = binascii.b2a_hex(letter1)
            temp_string_msg = temp_string_msg + x1
        message = temp_string_msg
        return message
