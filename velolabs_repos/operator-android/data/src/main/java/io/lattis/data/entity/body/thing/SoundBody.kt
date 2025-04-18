package io.lattis.data.entity.body.thing

import com.google.gson.annotations.SerializedName

data class SoundBody (
    @field:SerializedName("controlType")
    val controlType:Int?,
    @field:SerializedName("workMode")
    val workMode:Int?
)