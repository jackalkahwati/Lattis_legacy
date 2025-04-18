package com.lattis.data.entity.response.authentication

import com.google.gson.annotations.SerializedName

/**
 * Created by ssd3 on 4/25/17.
 */

class NewTokenResponse {

    @SerializedName("payload")
    var token: TokenResponse? = null
}
