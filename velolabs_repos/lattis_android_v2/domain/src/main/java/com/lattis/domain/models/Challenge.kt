package com.lattis.domain.models

class Challenge(val macId: String, var signedMessage: String?, var publicKey: String?) {

    override fun toString(): String {
        return "Challenge{" +
                "signedMessage='" + signedMessage + '\''.toString() +
                ", publicKey='" + publicKey + '\''.toString() +
                '}'.toString()
    }
}
