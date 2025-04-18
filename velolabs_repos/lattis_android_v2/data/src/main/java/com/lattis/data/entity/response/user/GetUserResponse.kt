package com.lattis.data.entity.response.user

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse
import com.lattis.data.entity.response.private_networks.PrivateNetworkResponse
import com.lattis.data.entity.response.user.UserResponse
import com.lattis.domain.models.User

class GetUserResponse : AbstractApiResponse() {
    @SerializedName("payload")
    var getUserPayloadResponse: GetUserPayloadResponse? = null

    fun getUserPayload():UserResponse?{
        return getUserPayloadResponse?.userResponse
    }

    var userResponse: UserResponse? = getUserPayloadResponse?.userResponse

    val privateNetworkResponse: List<PrivateNetworkResponse>?
        get() = getUserPayloadResponse?.privateNetworkResponse

    override fun toString(): String {
        return "GetUserResponse{" +
                "userResponse=" + getUserPayloadResponse +
                '}'.toString()
    }
}
