package com.lattis.domain.models

import java.io.Serializable

data class Reservation(
    val user_id:Int?=null,
    val bike_id:Int?=null,
    val reservation_id:Int?=null,
    val created_at:String?=null,
    val reservation_end:String? = null,
    val reservation_cancelled:String? = null,
    val reservation_start:String?= null,
    val reservation_timezone:String?=null,
    val trip_payment_transaction: TripPaymentTransaction?=null,
    val bike:Bike?=null
){

    data class TripPaymentTransaction(
        val id:Int?=null,
        val trip_id:Int?=null,
        val fleet_id:Int?=null,
        val transaction_id:String?=null,
        val charge_for_duration:String?=null,
        val penalty_fee:String?=null,
        val deposit:String?=null,
        val total:String?=null,
        val over_usage_fees:String?=null,
        val user_profile_id:String?=null,
        val card_id:String?=null,
        val date_charge:String?=null,
        val currency:String?=null,
        val application_fee:String?=null,
        val bike_unlock_fee:String?=null,
        val membership_discount:String?=null,
        val reservation_id:String?=null
    )

}