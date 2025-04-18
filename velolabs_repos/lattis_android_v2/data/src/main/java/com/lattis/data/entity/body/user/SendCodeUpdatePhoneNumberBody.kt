package com.lattis.data.entity.body.user

import com.google.gson.annotations.SerializedName

class SendCodeUpdatePhoneNumberBody(
    @field:SerializedName("country_code") private val countryCode: String,
    @field:SerializedName("phone_number") private val phoneNumber: String
)