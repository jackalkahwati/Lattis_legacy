package com.lattis.data.entity.body.reservation

import com.google.gson.annotations.SerializedName

class CostEstimateBody (
    @field:SerializedName("reservation_start")
    private val reservation_start: String,

    @field:SerializedName("reservation_end")
    private val reservation_end: String,

    @field:SerializedName("bike_id")
    private val bike_id: Int,

    @field:SerializedName("pricing_option_id")
    private val pricing_option_id: Int?
)