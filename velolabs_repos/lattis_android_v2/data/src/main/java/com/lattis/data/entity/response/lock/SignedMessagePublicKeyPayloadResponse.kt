package com.lattis.data.entity.response.lock

import com.google.gson.annotations.SerializedName

class SignedMessagePublicKeyPayloadResponse {
    @SerializedName("signed_message")
    var signed_message: String? = null
    @SerializedName("public_key")
    var public_key: String? = null

}