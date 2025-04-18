package com.lattis.data.entity.body.user

import com.google.gson.annotations.SerializedName

class SendForgotPasswordCodeBody(@field:SerializedName("email") private val email: String)