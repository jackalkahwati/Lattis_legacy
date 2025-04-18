import hashlib

def md5_hasher(input_value):
    return hashlib.md5(input_value.encode()).hexdigest()
