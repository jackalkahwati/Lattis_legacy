package com.lattis.domain.models

data class IoTBikeStatus(
    val latitude : Double?,
    val longitude : Double?,
    val online : Boolean?,
    val locked : Boolean?,
    val battery_percent : Int?,
    val bike_battery_percent : Int?,
    val last_update_time : String?
)