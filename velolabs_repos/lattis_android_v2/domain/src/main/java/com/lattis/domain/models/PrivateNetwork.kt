package com.lattis.domain.models


class PrivateNetwork {

    var private_fleet_user_id: Int = 0
    var user_id: Int = 0
    var email: String? = null
    var fleet_id: Int = 0
    var verified: Int = 0
    var fleet_name: String? = null
    var type: String? = null
    var logo: String? = null
    var address:Address?=null
    
    
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


    override fun toString(): String {
        return "PrivateNetwork{" +
                "private_fleet_user_id='" + private_fleet_user_id + '\''.toString() +
                ", userId='" + user_id + '\''.toString() +
                ", email='" + email + '\''.toString() +
                ", fleet_id='" + fleet_id + '\''.toString() +
                ", verified=" + verified +
                ", fleet_name='" + fleet_name + '\''.toString() +
                ", type='" + type + '\''.toString() +
                ", logo='" + logo + '\''.toString() +
                '}'.toString()
    }


}
