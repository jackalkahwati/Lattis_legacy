package com.lattis.domain.models

import com.lattis.domain.utils.Constants
import java.io.Serializable

class Bike {

    var bike_id: Int = 0
    var bike_name: String? = null
    var make: String? = null
    var model: String? = null
    var type: String? = null
    var description: String? = null
    var date_created: Int = 0
    var status: String? = null
    var battery_level: String? = null
    var bike_battery_level: String? = null
    var current_status: String? = null
    var maintenance_status: String? = null
    var pic: String? = null
    var distance: Double = 0.toDouble()
    var latitude: Double = 0.toDouble()
    var lock_id: Int = 0
    var longitude: Double = 0.toDouble()
    var fleet_id: Int = 0
    var parking_spot_id: Int = 0
    var user_id: Int = 0
    var mac_id: String? = null
    var name: String? = null
    var operator_id: Int = 0
    var customer_name:String?=null
    var customer_id: Int = 0
    var hub_id: Int = 0
    var fleet_key: String? = null
    var fleet_logo: String? = null
    var fleet_name: String? = null
    var tariff: String? = null
    var isBikeBooked: Boolean = false
    var booked_on: Long = 0
    var expires_in: Int = 0
    var fleet_bikes: String? = null
    var fleet_parking_spots: String? = null

    var price_for_membership: String? = null
    var price_type_value: String? = null
    var price_type: String? = null
    var ride_deposit: String? = null
    var price_for_ride_deposit: String? = null
    var price_for_ride_deposit_type: String? = null
    var excess_usage_fees: String? = null
    var excess_usage_type_value: String? = null
    var excess_usage_type: String? = null
    var excess_usage_type_after_value: String? = null
    var excess_usage_type_after_type: String? = null
    var fleet_t_and_c: String? = null
    var skip_parking_image: Boolean = false
    var max_trip_length: Int = 0
    var usage_surcharge: String? = null
    var currency: String? = null
    var require_phone_number: Boolean = false
    var isDo_not_track_trip: Boolean = false


    var fleet_type: String? = null
    var price_for_bike_unlock :String?=null
    var price_for_penalty_outside_parking :String?=null
    var reservation_settings : Reservation_settings?=null
    var controllers: List<Controller>?=null
    var qr_code_id: String?=null

    var bike_group_id:Int?=null
    var bike_group:Bike_Group?=null

    var payment_gateway:String?=null

    var enable_preauth:String?=null
    var preauth_amount:String?=null

    var pricing_options:ArrayList<Pricing_options?>?=null

    var bike_uuid:String?=null

    ///only for hub port
    var hud:DockHub?=null
    var port:DockHub.Port?=null

    // for bike booking response
    var bike_booking_id:Int? =null
    var port_id: Int? = null
    var device_type: String? = null
    var trip_id: Int? = null

    var originalTypeOfObject:String= Constants.bike
    var promotions : ArrayList<Promotion?>?=null
    var reservation:Reservation?=null
    
    class Bike_Group{
        var bike_group_id:Int?=null
        var make:String?=null
        var model:String?=null
        var type:String?=null
        var description:String?=null
        var pre_defined_bike_id:String?=null
        var date_created:String?=null
        var pic:String?=null
        var maintenance_schedule:String?=null
        var customer_id:Int?=null
        var operator_id:Int?=null
        var fleet_id:Int?=null
        var iot_module_type:String?=null
    }

    var fleet:Fleet?=null
    class Fleet : Serializable{
        var fleet_id:Int?=null
        var fleet_name:String?=null
        var date_created: Int = 0
        var customer_id:Int?=null
        var operator_id:Int?=null
        var key:String?=null
        var type:String?=null
        var logo:String?=null
        var t_and_c:String?=null
        var skip_parking_image:String?=null
        var do_not_track_trip:String?=null
        var require_phone_number:String?=null
        var max_trip_length:String?=null
        var contact_first_name:String?=null
        var contact_last_name:String?=null
        var contact_email:String?=null
        var contact_phone:String?=null
        var contract_file:String?=null
        var address_id:Int?=null
        var country_code:String?=null
        var parking_area_restriction:String?=null
        var member_csv:String?=null
        var price_for_penalty_outside_spot:Float?=null
        var parking_spot_restriction:String?=null
        var distance_preference:String?=null
        var start_trip_email:String?=null
        var fleet_membership_id:Int?=null
        var contact_web_link:String?=null
        var reservation_settings : Reservation_settings?=null
        var address:Address?=null
        var fleet_payment_settings:Fleet_Payment_Settings?=null
        var pricing_options:ArrayList<Pricing_options?>?=null
    }

    class Address : Serializable{
        var address_id:Int?=null
        var city:String?=null
        var address1:String?=null
        var address2:String?=null
        var state:String?=null
        var postal_code:String?=null
        var country:String?=null
        var type:String?=null
        var type_id:Int?=null
    }

    class  Fleet_Payment_Settings : Serializable
    {
        var id:Int?=null
        var fleet_id:Int?=null
        var price_for_active_bike:String?=null
        var price_for_outofservice_bike:String?=null
        var price_for_staging_bike:String?=null
        var price_for_archived_bike:String?=null
        var payment_mode:String?=null
        var payment_period:String?=null
        var next_billing_date:String?=null
        var current_plan_name:String?=null
        var account_status:String?=null
        var stripe_account_id:String?=null
        var price_for_membership:String?=null
        var price_type_value:String?=null
        var price_type:String?=null
        var usage_surcharge:String?=null
        var excess_usage_fees:String?=null
        var excess_usage_type_value:String?=null
        var excess_usage_type:String?=null
        var excess_usage_type_after_value:String?=null
        var excess_usage_type_after_type:String?=null
        var price_for_penalty_outside_parking:String?=null
        var price_for_penalty_outside_parking_below_battery_charge:String?=null
        var price_for_forget_plugin:String?=null
        var ride_deposit:String?=null
        var price_for_ride_deposit:String?=null
        var price_for_ride_deposit_type:String?=null
        var refund_criteria:String?=null
        var refund_criteria_value:String?=null
        var price_for_penalty_outside_zone:String?=null
        var currency:String?=null
        var price_for_bike_unlock:String?=null
        var price_for_reservation_late_return:String?=null
        var enable_preauth:String?=null
        var preauth_amount:String?=null
        var payment_gateway:String?=null
    }

    class Reservation_settings : Serializable{
        var reservation_settings_id:Int?=null
        var fleet_id:Int?=null
        var min_reservation_duration:String?=null
        var max_reservation_duration:String?=null
        var booking_window_duration:String?=null
        var created_at:String?=null
    }

    class Controller: Serializable{
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

    class Pricing_options : Serializable{
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
        return "Bike{" +
                "bike_id=" + bike_id +
                ", bike_name=" + bike_name +
                ", booked_on=" + booked_on +
                ", expires_in=" + expires_in +
                '}'.toString()
    }
}
