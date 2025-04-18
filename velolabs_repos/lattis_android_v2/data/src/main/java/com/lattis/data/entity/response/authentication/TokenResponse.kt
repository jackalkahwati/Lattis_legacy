package com.lattis.data.entity.response.authentication

import com.google.gson.annotations.SerializedName

/**
 * Created by ssd3 on 4/25/17.
 */

class TokenResponse {

    @SerializedName("rest_token")
    var restToken: String? = null
    @SerializedName("refresh_token")
    var refreshToken: String? = null
}
