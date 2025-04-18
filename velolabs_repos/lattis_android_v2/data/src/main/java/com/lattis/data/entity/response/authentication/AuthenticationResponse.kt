package com.lattis.data.entity.response.authentication

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse
import com.lattis.data.entity.response.user.UserResponse

class AuthenticationResponse : AbstractApiResponse() {
    @SerializedName("payload")
    val user: UserResponse? = null
}
