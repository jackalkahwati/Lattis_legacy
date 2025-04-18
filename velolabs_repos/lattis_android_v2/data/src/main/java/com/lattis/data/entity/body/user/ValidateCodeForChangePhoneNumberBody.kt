package com.lattis.data.entity.body.user

import com.google.gson.annotations.SerializedName

class ValidateCodeForChangePhoneNumberBody(
    @field:SerializedName("confirmation_code") private val confirmation_code: String,
    @field:SerializedName("phone_number") private val phoneNumber: String
)