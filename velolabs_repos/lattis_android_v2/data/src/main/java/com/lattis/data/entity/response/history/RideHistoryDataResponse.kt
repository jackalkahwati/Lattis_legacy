package com.lattis.data.entity.response.history

import com.google.gson.annotations.SerializedName

class RideHistoryDataResponse {
    @SerializedName("trip_id")
    val trip_id = 0

    @SerializedName("steps")
    val steps: Array<DoubleArray>? = null

    @SerializedName("start_address")
    val start_address: String? = null

    @SerializedName("end_address")
    val end_address: String? = null

    @SerializedName("date_endtrip")
    val date_endtrip: String? = null

    @SerializedName("parking_image")
    val parking_image: String? = null

    @SerializedName("rating")
    val rating = 0f

    @SerializedName("user_id")
    val user_id = 0

    @SerializedName("operator_id")
    val operator_id = 0

    @SerializedName("customer_id")
    val customer_id = 0

    @SerializedName("lock_id")
    val lock_id = 0

    @SerializedName("bike_id")
    val bike_id = 0

    @SerializedName("fleet_id")
    val fleet_id = 0

    @SerializedName("price_for_active_bike")
    val price_for_active_bike = 0f

    @SerializedName("price_for_archived_bike")
    val price_for_archived_bike = 0f

    @SerializedName("price_for_forget_plugin")
    val price_for_forget_plugin = 0f

    @SerializedName("price_for_membership")
    val price_for_membership: String? = null

    @SerializedName("price_for_outofservice_bike")
    val price_for_outofservice_bike: String? = null

    @SerializedName("price_for_ride_deposit")
    val price_for_ride_deposit: String? = null

    @SerializedName("price_for_ride_deposit_type")
    val price_for_ride_deposit_type: String? = null

    @SerializedName("price_for_staging_bike")
    val price_for_staging_bike: String? = null

    @SerializedName("price_type")
    val price_type: String? = null

    @SerializedName("price_type_value")
    val price_type_value: String? = null

    @SerializedName("ride_deposit")
    val ride_deposit: String? = null

    @SerializedName("price_for_penalty_outside_parking_below_battery_charge")
    val price_for_penalty_outside_parking_below_battery_charge: String? = null

    @SerializedName("price_for_penalty_outside_parking")
    val price_for_penalty_outside_parking: String? = null

    @SerializedName("transaction_id")
    val transaction_id: String? = null

    @SerializedName("duration")
    val duration: String? = null

    @SerializedName("charge_for_duration")
    val charge_for_duration: String? = null

    @SerializedName("penalty_fees")
    val penalty_fees: String? = null

    @SerializedName("deposit")
    val deposit: String? = null

    @SerializedName("total")
    val total: String? = null

    @SerializedName("excess_usage_fees")
    val excess_usage_fees: String? = null

    @SerializedName("excess_usage_type_value")
    val excess_usage_type_value: String? = null

    @SerializedName("excess_usage_type")
    val excess_usage_type: String? = null

    @SerializedName("excess_usage_type_after_value")
    val excess_usage_type_after_value: String? = null

    @SerializedName("excess_usage_type_after_type")
    val excess_usage_type_after_type: String? = null

    @SerializedName("over_usage_fees")
    val over_usage_fees: String? = null

    @SerializedName("user_profile_id")
    val user_profile_id: String? = null

    @SerializedName("card_id")
    val card_id: String? = null

    @SerializedName("date_charged")
    val date_charged: String? = null

    @SerializedName("date_created")
    val date_created: String? = null

    @SerializedName("currency")
    val currency: String? = null

    @SerializedName("cc_type")
    var cc_type: String? = null

    @SerializedName("cc_no")
    var cc_no: String? = null

    @SerializedName("fleet_name")
    var fleet_name: String? = null

}