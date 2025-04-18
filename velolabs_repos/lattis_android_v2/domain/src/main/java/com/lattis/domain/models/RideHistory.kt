package com.lattis.domain.models

import com.google.gson.annotations.SerializedName


data class RideHistory(
    @SerializedName("payload")val rideHistoryData : List<RideHistoryData>?=null
){
    inner class RideHistoryData(
        val trip_id:Int? = null,
        val steps: Array<DoubleArray>? = null,
        val start_address: String? = null,
        val end_address: String? = null,
        val date_endtrip: String? = null,
        val parking_image: String? = null,
        val rating:Float = 0f,
        val user_id:Int = 0,
        val operator_id:Int = 0,
        val customer_id:Int = 0,
        val lock_id:Int = 0,
        val bike_id:Int = 0,
        val fleet_id:Int = 0,
        val price_for_active_bike:Float = 0f,
        val price_for_archived_bike:Float = 0f,
        val price_for_forget_plugin:Float = 0f,
        val price_for_membership: String? = null,
        val price_for_outofservice_bike: String? = null,
        val price_for_ride_deposit: String? = null,
        val price_for_ride_deposit_type: String? = null,
        val price_for_staging_bike: String? = null,
        val price_type: String? = null,
        val price_type_value: String? = null,
        val ride_deposit: String? = null,
        val price_for_penalty_outside_parking_below_battery_charge: String? = null,
        val price_for_penalty_outside_parking: String? = null,
        val transaction_id: String? = null,
        val duration: String? = null,
        val charge_for_duration: String? = null,
        val penalty_fees: String? = null,
        val deposit: String? = null,
        val total: String? = null,
        val excess_usage_fees: String? = null,
        val excess_usage_type_value: String? = null,
        val excess_usage_type: String? = null,
        val excess_usage_type_after_value: String? = null,
        val excess_usage_type_after_type: String? = null,
        val over_usage_fees: String? = null,
        val price_for_bike_unlock: String? = null,
        val user_profile_id: String? = null,
        val card_id: String? = null,
        val date_charged: String? = null,
        val date_created: String? = null,
        val currency: String? = null,
        var cc_type: String? = null,
        var cc_no: String? = null,
        var fleet_name: String? = null,
        var membership_discount :String?=null,
        var promo_code_discount:String?=null,
        var promotionId:Int?=null,
        var taxes:List<RideSummary.Tax>?=null,
        var refunds:List<RideSummary.Refund>?=null
    )
}
