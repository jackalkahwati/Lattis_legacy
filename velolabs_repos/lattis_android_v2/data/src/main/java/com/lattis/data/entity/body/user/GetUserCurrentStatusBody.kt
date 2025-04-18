package com.lattis.data.entity.body.user

import com.google.gson.annotations.SerializedName

/**
 * Created by ssd3 on 9/6/17.
 */
class GetUserCurrentStatusBody(
    @field:SerializedName("device_model") private val device_model: String,
    @field:SerializedName("device_os") private val device_os: String,
    @field:SerializedName("device_language") private val deviceLanguage: String
)