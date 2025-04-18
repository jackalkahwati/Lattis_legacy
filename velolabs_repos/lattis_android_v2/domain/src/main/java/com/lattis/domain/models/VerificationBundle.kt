package com.lattis.domain.models

class VerificationBundle {

    var userId: String? = null

    var isVerified: Boolean? = false

    override fun toString(): String {
        return "VerificationBundle{" +
                "userId=" + userId +
                ", isVerified=" + isVerified +
                '}'.toString()
    }
}
