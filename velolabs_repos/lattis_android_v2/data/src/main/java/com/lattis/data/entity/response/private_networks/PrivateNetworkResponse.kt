package com.lattis.data.entity.response.private_networks

import com.google.gson.annotations.SerializedName

/**
 * Created by ssd3 on 5/8/17.
 */

class PrivateNetworkResponse {

    @SerializedName("private_fleet_user_id")
    val private_fleet_user_id: Int = 0
    @SerializedName("user_id")
    val user_id: Int = 0
    @SerializedName("email")
    val email: String? = null
    @SerializedName("fleet_id")
    val fleet_id: Int = 0
    @SerializedName("verified")
    val verified: Int = 0
    @SerializedName("fleet_name")
    val fleet_name: String? = null
    @SerializedName("type")
    val type: String? = null
    @SerializedName("logo")
    val logo: String? = null
    @SerializedName("address")
    val address: Address? = null

    class Address{
        var address_id:Int? =null
        var city: String?=null
        var address1: String?=null
        var address2: String?=null
        var state: String?=null
        var postal_code: String?=null
        var country: String?=null
        var type: String?=null
        var type_id: Int?=null
    }


}
