package com.lattis.domain.models

class UpdateTripData(
    var duration: Double?,
    var cost: Float?,
    var currency: String?,
    var endDate:String?,
    var bike_battery_level:String?
) {

    override fun toString(): String {
        return "Duration: $duration cost: $cost currency: $currency"
    }

}