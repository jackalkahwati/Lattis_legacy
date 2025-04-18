package com.lattis.data.entity.response.user

import com.google.gson.annotations.SerializedName


class GetUserDataResponse {

    @SerializedName("user_id")
    val userId: String? = null
    @SerializedName("users_id")
    val usersId: String? = null
    @SerializedName("rest_token")
    val restToken: String? = null
    @SerializedName("refresh_token")
    val refreshToken: String? = null
    @SerializedName("username")
    val username: String? = null
    @SerializedName("user_type")
    val userType: String? = null
    @SerializedName("verified")
    val isVerified: Boolean = false
    @SerializedName("max_locks")
    val maxLocks: Int = 0
    @SerializedName("title")
    val title: String? = null
    @SerializedName("first_name")
    val firstName: String? = null
    @SerializedName("last_name")
    val lastName: String? = null
    @SerializedName("phone_number")
    val phoneNumber: String? = null
    @SerializedName("email")
    val email: String? = null
    @SerializedName("country_code")
    val countryCode: String? = null

    override fun toString(): String {
        return "UserResponse{" +
                "userId=" + userId +
                ", usersId='" + usersId + '\''.toString() +
                ", restToken='" + restToken + '\''.toString() +
                ", username=" + username +
                ", userType='" + userType + '\''.toString() +
                ", verified=" + isVerified +
                ", maxLocks=" + maxLocks +
                ", title='" + title + '\''.toString() +
                ", firstName='" + firstName + '\''.toString() +
                ", lastName='" + lastName + '\''.toString() +
                ", phoneNumber='" + phoneNumber + '\''.toString() +
                ", email='" + email + '\''.toString() +
                ", countryCode='" + countryCode + '\''.toString() +
                '}'.toString()
    }


}
