package com.lattis.data.entity.body.authentication

import com.google.gson.annotations.SerializedName

import android.R.attr.phoneNumber

class SignInRequestBody(@field:SerializedName("user_type")
                        var userType: String?, @field:SerializedName("users_id")
                        var usersId: String?, @field:SerializedName("reg_id")
                        var regId: String?, @field:SerializedName("password")
                        var password: String?, @field:SerializedName("is_signing_up")
                        var isSigningUp: Boolean) {

    override fun toString(): String {
        return "SignInRequestBody{" +
                "userType='" + userType + '\''.toString() +
                ", usersId='" + usersId + '\''.toString() +
                ", regId='" + regId + '\''.toString() +
                ", password='" + password + '\''.toString() +
                ", phoneNumber='" + phoneNumber + '\''.toString() +
                ", isSigningUp=" + isSigningUp +
                '}'.toString()
    }
}
