package com.lattis.data.entity.response.authentication

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.user.UserResponse
import com.lattis.data.entity.response.base.AbstractApiResponse

class ValidationResponse : AbstractApiResponse() {
    @SerializedName("payload")
    private var userResponse: UserResponse? = null
    fun getUserResponse(): UserResponse? {
        return userResponse
    }

    override fun toString(): String {
        return "ValidationResponse{" +
                "userResponse=" + userResponse +
                '}'
    }
}