package io.lattis.data.entity.body.thing

import com.google.gson.annotations.SerializedName

class HeadTailLightBody (
        @field:SerializedName("headLight")
        val headLight:Int?,
        @field:SerializedName("tailLight")
        val tailLight:Int?
)
