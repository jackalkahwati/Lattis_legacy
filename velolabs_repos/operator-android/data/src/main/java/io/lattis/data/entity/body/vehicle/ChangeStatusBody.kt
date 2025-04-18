package io.lattis.data.entity.body.vehicle

import com.google.gson.annotations.SerializedName

class ChangeStatusBody(
    @field:SerializedName("status")
    var status: String,
    @field:SerializedName("usage")
    var usage: String,
    @field:SerializedName("maintenance")
    var maintenance: String?
)