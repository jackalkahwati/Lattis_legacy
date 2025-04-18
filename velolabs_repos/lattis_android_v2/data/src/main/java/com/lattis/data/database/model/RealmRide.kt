package com.lattis.data.database.model

import com.lattis.domain.models.Ride
import com.lattis.domain.utils.Constants
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class RealmRide : RealmObject() {

    @PrimaryKey
    var id: String? = null
    var bikeId: Int = 0
    var bike_booked_on: Long = 0
    var bike_expires_in: Int = 0


    var bike_booking_id:Int?=null
    var bike_id: Int? = null
    var port_id: Int? = null
    var hub_id: Int? = null
    var device_type: String? = null
    var trip_id: Int? = null

    var rideId: Int = 0
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
    var bike_bike_operator_email: String? = null
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
    var bike_fleet_type: String? = null

    var bike_skip_parking_image: Boolean = false
    var bike_max_trip_length: Int = 0

    var isFirst_lock_connect: Boolean = false
    var do_not_track_trip: Boolean? = null
    var currency: String? = null
    var price_for_bike_unlock :String?=null
    var price_for_penalty_outside_parking :String?=null

    fun isBike_isBikeBooked(): Boolean? {
        return bike_isBikeBooked
    }

    var controllers: RealmList<RealmBikeController>?=null
    var qr_code_id: String?=null
    var dock_hub_bike_docked : Boolean? =null
    var payment_gateway:String?=null
    var pricing_options:RealmList<RealmPricingOption?>?=null
    var bike_uuid:String?=null
    var bike_originalTypeOfObject: String = Constants.bike
    var promotions:RealmList<RealmPromotion>?=null

    override fun toString(): String {
        return "RealmUser{" +
                "bikeId='" + bikeId + '\''.toString() +
                ", bike_booked_on='" + bike_booked_on + '\''.toString() +
                ", bike_expires_in='" + bike_expires_in + '\''.toString() +
                ", rideId='" + rideId + '\''.toString() +
                ", ride_booked_on=" + ride_booked_on +
                '}'.toString()
    }


}
