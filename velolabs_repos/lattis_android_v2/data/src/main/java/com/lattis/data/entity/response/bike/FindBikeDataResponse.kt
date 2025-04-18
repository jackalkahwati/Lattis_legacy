package com.lattis.data.entity.response.bike

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Promotion
import com.lattis.domain.models.Reservation


class FindBikeDataResponse {

    @SerializedName("bike_id")
    var bike_id: Int = 0
    @SerializedName("bike_name")
    var bike_name: String? = null
    @SerializedName("make")
    var make: String? = null
    @SerializedName("model")
    var model: String? = null
    @SerializedName("type")
    var type: String? = null
    @SerializedName("description")
    var description: String? = null
    @SerializedName("date_created")
    var date_created: Int = 0
    @SerializedName("status")
    var status: String? = null
    @SerializedName("battery_level")
    var battery_level: String? = null
    @SerializedName("bike_battery_level")
    var bike_battery_level: String? = null
    @SerializedName("current_status")
    var current_status: String? = null
    @SerializedName("maintenance_status")
    var maintenance_status: String? = null
    @SerializedName("lock_id")
    var lock_id: Int = 0
    @SerializedName("fleet_id")
    var fleet_id: Int = 0
    @SerializedName("user_id")
    var user_id: Int = 0
    @SerializedName("mac_id")
    var mac_id: String? = null
    @SerializedName("key")
    var key: String? = null
    @SerializedName("name")
    var name: String? = null
    @SerializedName("operator_id")
    var operator_id: Int = 0
    @SerializedName("customer_id")
    var customer_id: Int = 0
    @SerializedName("customer_name")
    var customer_name: String? = null
    @SerializedName("hub_id")
    var hub_id: Int = 0
    @SerializedName("latitude")
    var latitude: Double = 0.toDouble()
    @SerializedName("longitude")
    var longitude: Double = 0.toDouble()
    @SerializedName("distance")
    var distance: Double = 0.toDouble()
    @SerializedName("pic")
    var pic: String? = null
    @SerializedName("fleet_key")
    var fleet_key: String? = null
    @SerializedName("tariff")
    var tariff: String? = null
    @SerializedName("fleet_name")
    var fleet_name: String? = null
    @SerializedName("fleet_logo")
    var fleet_logo: String? = null
    @SerializedName("fleet_bikes")
    var fleet_bikes: String? = null
    @SerializedName("fleet_parking_spots")
    var fleet_parking_spots: String? = null
    @SerializedName("fleet_t_and_c")
    var terms_condition_url: String? = null

    @SerializedName("price_for_membership")
    var price_for_membership: String? = null

    @SerializedName("price_type_value")
    var price_type_value: String? = null

    @SerializedName("price_type")
    var price_type: String? = null

    @SerializedName("ride_deposit")
    var ride_deposit: String? = null

    @SerializedName("price_for_ride_deposit")
    var price_for_ride_deposit: String? = null

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

    @SerializedName("skip_parking_image")
    val skip_parking_image: Boolean = false

    @SerializedName("max_trip_length")
    val max_trip_length: Int = 0

    @SerializedName("usage_surcharge")
    val usage_surcharge: String? = null

    @SerializedName("currency")
    var currency: String? = null

    @SerializedName("require_phone_number")
    val isRequire_phone_number: Boolean = false

    @SerializedName("do_not_track_trip")
    var isDo_not_track_trip: Boolean = false

    @SerializedName("fleet_type")
    var fleet_type: String? = null

    @SerializedName("price_for_bike_unlock")
    var price_for_bike_unlock: String? = null

    @SerializedName("price_for_penalty_outside_parking")
    var price_for_penalty_outside_parking: String? = null

    @SerializedName("controllers")
    var controllers: List<Bike.Controller>?=null

    @SerializedName("qr_code_id")
    var qr_code_id: Int?=null

    @SerializedName("payment_gateway")
    var payment_gateway: String?=null

    @SerializedName("enable_preauth")
    var enable_preauth: String?=null

    @SerializedName("preauth_amount")
    var preauth_amount: String?=null

    @SerializedName("bike_uuid")
    var bike_uuid: String?=null


    @SerializedName("pricing_options")
    var pricing_options:ArrayList<Bike.Pricing_options?>?=null

    @SerializedName("promotions")
    var promotions : ArrayList<Promotion?>?=null

    @SerializedName("reservation")
    var reservation : Reservation?=null


}