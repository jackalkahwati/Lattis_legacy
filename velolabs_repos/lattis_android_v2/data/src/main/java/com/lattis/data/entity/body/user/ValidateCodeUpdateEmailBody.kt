package com.lattis.data.entity.body.user

import com.google.gson.annotations.SerializedName

class ValidateCodeUpdateEmailBody(
    @field:SerializedName("confirmation_code") var confirmation_code: String,
    @field:SerializedName("email") var email: String
)