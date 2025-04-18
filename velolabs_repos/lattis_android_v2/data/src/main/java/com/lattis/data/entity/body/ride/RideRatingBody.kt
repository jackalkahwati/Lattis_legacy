package com.lattis.data.entity.body.ride

import com.google.gson.annotations.SerializedName

class RideRatingBody(
    @field:SerializedName("trip_id") private val trip_id: Int,
    @field:SerializedName("rating") private val rating: Int
)