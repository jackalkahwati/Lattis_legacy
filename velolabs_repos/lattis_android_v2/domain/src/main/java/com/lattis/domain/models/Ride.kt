package com.lattis.domain.models

import com.google.gson.annotations.SerializedName
import com.lattis.domain.utils.Constants
import java.io.Serializable


class Ride {

    var id: String? = null
    var bikeId: Int = 0
    var bike_booked_on: Long = 0
    var bike_expires_in: Int = 0


    var bike_booking_id:Int? =null
    var bike_id: Int? = null
    var port_id: Int? = null
    var hub_id: Int? = null
    var device_type: String? = null
    var trip_id: Int? = null

    var rideId = 0
    var ride_booked_on: Long = 0


    var bike_bike_name: String? = null
    var bike_make: String? = null
    var bike_model: String? = null
    var bike_type: String? = null
    var bike_description: String? = null
    var bike_date_created: Int = 0
    var bike_status: String? = null
    var bike_battery_level: String? = null
    var bike_bike_battery_level: String? = null
    var bike_current_status: String? = null
    var bike_maintenance_status: String? = null
    var bike_pic: String? = null
    var bike_distance: Double = 0.toDouble()
    var bike_latitude: Double = 0.toDouble()
    var bike_lock_id: Int = 0
    var bike_longitude: Double = 0.toDouble()
    var bike_fleet_id: Int = 0
    var bike_parking_spot_id: Int = 0
    var bike_user_id: Int = 0
    var bike_mac_id: String? = null
    var bike_name: String? = null
    var bike_bike_operator_id: Int = 0
    var bike_customer_name:String?=null
    var bike_customer_id: Int = 0
    var bike_hub_id: Int = 0
    var bike_bike_fleet_key: String? = null
    var bike_fleet_logo: String? = null
    var bike_fleet_name: String? = null
    var bike_tariff: String? = null
    var bike_isBikeBooked: Boolean? = null
    var bike_on_call_operator: String? = null
    var support_phone: String? = null

    var bike_price_for_membership: String? = null
    var bike_price_type_value: String? = null
    var bike_price_type: String? = null
    var bike_ride_deposit: String? = null
    var bike_price_for_ride_deposit: String? = null
    var bike_price_for_ride_deposit_type: String? = null
    var bike_excess_usage_fees: String? = null
    var bike_excess_usage_type_value: String? = null
    var bike_excess_usage_type: String? = null
    var bike_excess_usage_type_after_value: String? = null
    var bike_excess_usage_type_after_type: String? = null
    var bike_terms_condition_url: String? = null
    var bike_skip_parking_image: Boolean = false
    var bike_max_trip_length: Int = 0
    var bike_usage_surcharge: String? = null
    var do_not_track_trip: Boolean? = null
    var currency: String? = null

    var isFirst_lock_connect: Boolean = false


    var bike_fleet_type: String? = null
    var price_for_bike_unlock :String?=null
    var price_for_penalty_outside_parking :String?=null

    fun isBike_isBikeBooked(): Boolean? {
        return bike_isBikeBooked
    }

    var controllers: List<Controller>?=null
    var qr_code_id: String?=null

    var dock_hub_bike_docked : Boolean? =null
    var payment_gateway:String?=null
    var pricing_options:ArrayList<Pricing_options?>?=null
    var bike_uuid:String?=null
    var bike_originalTypeOfObject: String = Constants.bike
    var promotions : ArrayList<Promotion?>?=null

    class Controller{
        var gps_log_time:String?=null
        var hw_version:String?=null
        var qr_code:String?=null
        var fleet_id:Int?=null
        var longitude:Double?=null
        var fw_version:String?=null
        var latitude:Double?=null
        var battery_level:Float?=null
        var device_type:String?=null
        var key:String?=null
        var added_by_operator_id:Int?=null
        var vendor:String?=null
        var controller_id:Int?=null
        var make:String?=null
        var bike_id:Int?=null
        var date_created:String?=null
        var model:String?=null
        var status:String?=null
    }

    class Pricing_options : Serializable {
        var pricing_option_id:Int?=null
        var fleet_id:Int?=null
        var duration:String?=null
        var duration_unit:String?=null
        var grace_period:String?=null
        var grace_period_unit:String?=null
        var price:String?=null
        var price_currency:String?=null
        var deactivated_at:String?=null
        var deactivation_reason:String?=null
        var created_at:String?=null
    }


    override fun toString(): String {
        return "Ride{" +
                "bikeId='" + bikeId + '\''.toString() +
                ", bike_booked_on='" + bike_booked_on + '\''.toString() +
                ", bike_expires_in='" + bike_expires_in + '\''.toString() +
                ", rideId='" + rideId + '\''.toString() +
                ", ride_booked_on=" + ride_booked_on +

                ", bike_bike_fleet_key='" + bike_bike_fleet_key + '\''.toString() +
                ", bike_mac_id='" + bike_mac_id + '\''.toString() +
                ", bike_bike_operator_id='" + bike_bike_operator_id + '\''.toString() +
                ", bike_fleet_id=" + bike_fleet_id +

                ", bike_bike_name='" + bike_bike_name + '\''.toString() +
                ", bike_make='" + bike_make + '\''.toString() +
                ", bike_on_call_operator='" + bike_on_call_operator + '\''.toString() +
                ", ride_booked_on=" + ride_booked_on +

                '}'.toString()
    }
}
