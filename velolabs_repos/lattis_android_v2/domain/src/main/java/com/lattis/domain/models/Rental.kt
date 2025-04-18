package com.lattis.domain.models

import com.google.gson.annotations.SerializedName

data class Rental(
    @SerializedName("bike")
    var bike:Bike?=null,

    @SerializedName("hub")
    var hub:DockHub?=null,


    @SerializedName("port")
    var port: DockHub.Port?=null
)