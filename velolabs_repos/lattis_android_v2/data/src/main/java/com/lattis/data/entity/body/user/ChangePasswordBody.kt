package com.lattis.data.entity.body.user

import com.google.gson.annotations.SerializedName

class ChangePasswordBody(
    @field:SerializedName("password") private val password: String,
    @field:SerializedName("new_password") private val new_password: String
)