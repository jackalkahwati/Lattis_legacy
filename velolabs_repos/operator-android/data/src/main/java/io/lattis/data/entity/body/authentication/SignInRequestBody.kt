package io.lattis.data.entity.body.authentication

import com.google.gson.annotations.SerializedName

import android.R.attr.phoneNumber

class SignInRequestBody(
        @field:SerializedName("email")
        var email: String?,
        @field:SerializedName("password")
        var password: String?) {

    override fun toString(): String {
        return "SignInRequestBody{" +
                ", password='" + password + '\''.toString() +
                ", phoneNumber='" + email + '\''.toString() +
                '}'.toString()
    }
}
