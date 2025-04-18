package com.lattis.domain.models

data class SignedMessageAndPublicKey(
    var signed_message: String? = null,
    var public_key: String? = null
)