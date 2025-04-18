package com.lattis.data.entity.response.parking

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.GeoFence

class GetGeoFenceResponse {
    @SerializedName("payload")
    val geoFences:List<GeoFence>?=null
}