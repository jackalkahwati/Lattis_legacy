package com.lattis.domain.models
import java.io.Serializable
class RideSummary : Serializable{
    var trip_id: Int = 0
    var steps: Array<DoubleArray>? = null
    var start_address: String? = null
    var date_created: Long? = null
    var date_endtrip: String? = null
    var parking_image: String? = null
    var rating: Float = 0.toFloat()
    var user_id: Int = 0
    var operator_id: Int = 0
    var customer_id: Int = 0
    var lock_id: Int = 0
    var bike_id: Int?=null
    var port_id: Int?=null
    var hub_id: Int?=null
    var fleet_id: Int = 0
    var transaction_id: String? = null
    var duration: String? = null
    var charge_for_duration: String? = null
    var currency: String? = null
    var penalty_fees: String? = null
    var deposit: String? = null
    var total: String? = null
    var over_usage_fees: String? = null
    var user_profile_id: String? = null
    var card_id: String? = null
    var price_for_membership: String? = null
    var price_type_value: String? = null
    var price_type: String? = null
    var ride_deposit: String? = null
    var price_for_ride_deposit_type: String? = null
    var excess_usage_fees: String? = null
    var excess_usage_type_value: String? = null
    var excess_usage_type: String? = null
    var excess_usage_type_after_value: String? = null
    var excess_usage_type_after_type: String? = null
    var isFirst_lock_connect: Boolean = false
    var do_not_track_trip: Boolean? = null
    var price_for_bike_unlock: String? = null
    var membership_discount :String?=null
    var dockHub:DockHub?=null
    var promo_code_discount:String?=null
    var promotionId:Int?=null
    var taxes:List<Tax>?=null

    class Tax :Serializable{
        var taxId:Int?=null
        var name:String?=null
        var percentage:String?=null
        var status:String?=null
        var amount:String?=null
    }

    class Refund:Serializable{
        var refund_id:Int?=null
        var user_id:Int?=null
        var fleet_id:Int?=null
        var trip_id:Int?=null
        var deposit_amount:String?=null
        var charge_id:String?=null
        var date_charged:String?=null
        var date_refunded:String?=null
        var amount_refunded:String?=null
        var stripe_refund_id:String?=null
    }
}