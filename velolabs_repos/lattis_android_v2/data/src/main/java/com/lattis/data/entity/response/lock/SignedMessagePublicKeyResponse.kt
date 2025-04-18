package com.lattis.data.entity.response.lock

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse

class SignedMessagePublicKeyResponse : AbstractApiResponse() {
    @SerializedName("payload")
    var signedMessagePublicKeyPayloadResponse: SignedMessagePublicKeyPayloadResponse? = null

}