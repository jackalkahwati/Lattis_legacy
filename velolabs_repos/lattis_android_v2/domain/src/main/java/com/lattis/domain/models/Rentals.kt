package com.lattis.domain.models

import com.google.gson.annotations.SerializedName

data class Rentals(
    @SerializedName("bikes")
    var bikeList:List<Bike>?=null,

    @SerializedName("hubs")
    var hubList:List<DockHub>?=null
) {
}