package com.lattis.data.entity.response.ride

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.Promotion
import com.lattis.domain.models.RideSummary

/**
 * Created by ssd3 on 4/11/17.
 */

class RideSummaryDataResponse {

    @SerializedName("trip_id")
    var trip_id: Int = 0

    @SerializedName("steps")
    var steps: Array<DoubleArray>? = null

    @SerializedName("start_address")
    var start_address: String? = null

    @SerializedName("date_created")
    var date_created: Long = 0

    @SerializedName("date_endtrip")
    var date_endtrip: String? = null

    @SerializedName("parking_image")
    var parking_image: String? = null

    @SerializedName("rating")
    var rating: Float = 0.toFloat()

    @SerializedName("user_id")
    var user_id: Int = 0

    @SerializedName("operator_id")
    var operator_id: Int = 0

    @SerializedName("customer_id")
    var customer_id: Int = 0

    @SerializedName("lock_id")
    var lock_id: Int = 0

    @SerializedName("bike_id")
    var bike_id: Int?=null

    @SerializedName("port_id")
    var port_id: Int?=null

    @SerializedName("hub_id")
    var hub_id: Int ?=null

    @SerializedName("fleet_id")
    var fleet_id: Int = 0

    @SerializedName("transaction_id")
    var transaction_id: String? = null

    @SerializedName("duration")
    var duration: String? = null

    @SerializedName("charge_for_duration")
    var charge_for_duration: String? = null


    @SerializedName("currency")
    var currency: String? = null

    @SerializedName("penalty_fees")
    var penalty_fees: String? = null

    @SerializedName("deposit")
    var deposit: String? = null

    @SerializedName("total")
    var total: String? = null

    @SerializedName("over_usage_fees")
    var over_usage_fees: String? = null

    @SerializedName("user_profile_id")
    var user_profile_id: String? = null

    @SerializedName("card_id")
    var card_id: String? = null

    @SerializedName("price_for_membership")
    var price_for_membership: String? = null

    @SerializedName("price_type_value")
    var price_type_value: String? = null

    @SerializedName("price_type")
    var price_type: String? = null

    @SerializedName("ride_deposit")
    var ride_deposit: String? = null

    @SerializedName("price_for_ride_deposit_type")
    var price_for_ride_deposit_type: String? = null

    @SerializedName("excess_usage_fees")
    var excess_usage_fees: String? = null

    @SerializedName("excess_usage_type_value")
    var excess_usage_type_value: String? = null

    @SerializedName("excess_usage_type")
    var excess_usage_type: String? = null

    @SerializedName("excess_usage_type_after_value")
    var excess_usage_type_after_value: String? = null

    @SerializedName("excess_usage_type_after_type")
    var excess_usage_type_after_type: String? = null


    @SerializedName("first_lock_connect")
    var isFirst_lock_connect: Boolean = false

    @SerializedName("do_not_track_trip")
    var do_not_track_trip: Boolean? = null

    @SerializedName("price_for_bike_unlock")
    var price_for_bike_unlock: String? = null

    @SerializedName("membership_discount")
    var membership_discount: String? = null

    @SerializedName("promo_code_discount")
    var promo_code_discount:String?=null

    @SerializedName("promotion_id")
    var promotion_id:Int?=null

    @SerializedName("promotion")
    var promotion: Promotion?=null

    @SerializedName("taxes")
    var taxes: ArrayList<RideSummary.Tax>?=null
}
