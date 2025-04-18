package com.lattis.data.entity.body.authentication

import com.google.gson.annotations.SerializedName

class SignUpRequestBody(@field:SerializedName("user_type")
                        var userType: String?,
                        @field:SerializedName("users_id")
                        var usersId: String?,
                        @field:SerializedName("reg_id")
                        var regId: String?,
                        @field:SerializedName("password")
                        var password: String?,
                        @field:SerializedName("is_signing_up")
                        var isSigningUp: Boolean,
                        @field:SerializedName("first_name")
                        var firstName: String?,
                        @field:SerializedName("last_name")
                        var lastName: String?,
                        @field:SerializedName("device_language")
                        var device_language: String?
) {

    override fun toString(): String {
        return "SignInRequestBody{" +
                "userType='" + userType + '\''.toString() +
                ", usersId='" + usersId + '\''.toString() +
                ", regId='" + regId + '\''.toString() +
                ", password='" + password + '\''.toString() +
                ", isSigningUp=" + isSigningUp +
                '}'.toString()
    }
}
