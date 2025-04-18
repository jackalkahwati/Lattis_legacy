package com.lattis.data.entity.response.user

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.private_networks.PrivateNetworkResponse
import com.lattis.data.entity.response.user.UserResponse


class GetUserPayloadResponse {

    @SerializedName("user")
    var userResponse: UserResponse? = null
    @SerializedName("private_account")
    var privateNetworkResponse: List<PrivateNetworkResponse>? = null
}
