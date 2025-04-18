package com.lattis.data.entity.response.user

import com.google.gson.annotations.SerializedName

class UsersInResponse (
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String = "",
    @SerializedName("username") val username: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("website") val website: String = "",
    @SerializedName("address") val address :Address,
    @SerializedName("company") val company :Company
){

    class Address(
        @SerializedName("street") val street: String = "",
        @SerializedName("suite") val suite: String = "",
        @SerializedName("city") val city: String = "",
        @SerializedName("zipcode") val zipcode: String = "",
        @SerializedName("geo") val geo: Geo
    ){
        class Geo(
            @SerializedName("lat") val lat: String = "",
            @SerializedName("lng") val lng: String = ""
        )
    }


    class Company(
        @SerializedName("name") val name: String = "",
        @SerializedName("catchPhrase") val catchPhrase: String = "",
        @SerializedName("bs") val bs: String = ""
    )
}