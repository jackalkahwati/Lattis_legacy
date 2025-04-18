package com.lattis.data.entity.response.card

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse
import com.lattis.domain.models.MPPublicKey

class MPPublicKeyResponse: AbstractApiResponse() {
    @SerializedName("payload")
    var mpPublicKey:MPPublicKey?=null
}