package com.lattis.domain.models

data class KeyCache(
    var signedMessage: String? = null,
    var publicKey: String? = null,
    var macId: String? = null,
    var time: Long? = null
)