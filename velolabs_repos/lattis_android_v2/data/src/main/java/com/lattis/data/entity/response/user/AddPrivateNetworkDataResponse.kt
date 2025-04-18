package com.lattis.data.entity.response.user

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.PrivateNetwork

class AddPrivateNetworkDataResponse {
    @SerializedName("lattis_account")
    val lattis_accounts: List<PrivateNetwork>? = null
}