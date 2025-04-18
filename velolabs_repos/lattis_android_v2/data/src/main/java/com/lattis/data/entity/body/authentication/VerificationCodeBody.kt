package com.lattis.data.entity.body.authentication

import com.google.gson.annotations.SerializedName

class VerificationCodeBody(
    @field:SerializedName("user_id") private val user_id: String?,
    @field:SerializedName("account_type") private val account_type: String?,
    @field:SerializedName("confirmation_code") private val confirmationCode: String?
)