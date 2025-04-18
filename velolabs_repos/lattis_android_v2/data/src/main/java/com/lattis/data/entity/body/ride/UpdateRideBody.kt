package com.lattis.data.entity.body.ride

import com.google.gson.annotations.SerializedName

class UpdateRideBody(@field:SerializedName("trip_id")
                     private val trip_id: Int,
                     @field:SerializedName("steps")
                     private val steps: Array<DoubleArray>)
