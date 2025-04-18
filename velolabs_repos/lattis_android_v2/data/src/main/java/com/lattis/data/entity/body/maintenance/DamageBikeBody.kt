package com.lattis.data.entity.body.maintenance

import com.google.gson.annotations.SerializedName

class DamageBikeBody(
    @field:SerializedName("category") private val category: String?,
    @field:SerializedName("notes") private val rider_notes: String?,
    @field:SerializedName("bike_id") private val bike_id: Int,
    @field:SerializedName("image") private val maintenance_image: String?,
    trip_id: Int
) {

    @SerializedName("trip_id")
    private var trip_id: Int? = null

    init {
        if (trip_id > 0) {
            this.trip_id = trip_id
        }
    }
}