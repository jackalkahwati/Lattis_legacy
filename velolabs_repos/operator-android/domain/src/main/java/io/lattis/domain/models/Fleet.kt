package io.lattis.domain.models

import com.google.gson.annotations.SerializedName

data class Fleet (
    @SerializedName("vehiclesCount")
    var vehiclesCount:Int?=null,

    @SerializedName("legal")
    var legal:String?=null,

    @SerializedName("type")
    var type:String?=null,

    @SerializedName("name")
    var name:String?=null,

    @SerializedName("logo")
    var logo:String?=null,

    @SerializedName("id")
    var id:Int?=null,

    @SerializedName("address")
    var address:Address?=null
){

    data class Address(
        @SerializedName("city")
        var city:String?=null,

        @SerializedName("country")
        var country:String?=null,

        @SerializedName("id")
        var id:Int?=null,

        @SerializedName("postalCode")
        var postalCode:String?=null,

        @SerializedName("address1")
        var address1:String?=null,

        @SerializedName("state")
        var state:String?=null,

        @SerializedName("address2")
        var address2:String?=null
    )

}