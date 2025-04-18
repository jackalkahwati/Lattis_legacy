package com.lattis.data.entity.response.authentication

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse

class RefreshTokenResponse : AbstractApiResponse() {

    @SerializedName("payload")
    var tokenResponse: TokenResponse? = null
}
