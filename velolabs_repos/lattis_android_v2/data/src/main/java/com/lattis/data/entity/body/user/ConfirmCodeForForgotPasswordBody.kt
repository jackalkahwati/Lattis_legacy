package com.lattis.data.entity.body.user

import com.google.gson.annotations.SerializedName

class ConfirmCodeForForgotPasswordBody(
    @field:SerializedName("email") private val email: String,
    @field:SerializedName("confirmation_code") private val confirmation_code: String,
    @field:SerializedName("password") private val password: String
) {

    override fun toString(): String {
        return "ResetPasswordBody{" +
                "phoneNumber=" + email +
                '}'
    }

}