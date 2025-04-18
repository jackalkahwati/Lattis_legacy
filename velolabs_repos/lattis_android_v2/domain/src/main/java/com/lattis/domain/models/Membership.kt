package com.lattis.domain.models

import java.io.Serializable

data class Membership(
    var fleet_membership_id:Int?=null,
    var fleet_id:Int?=null,
    var membership_price:String?=null,
    var membership_price_currency:String?=null,
    var membership_incentive:String?=null,
    var payment_frequency:String?=null,
    var created_at:String?=null,
    var activation_date:String?=null,
    var deactivation_date:String?=null,
    var deactivation_reason:String?=null,
    var fleet: Bike.Fleet?=null,
    val membership_subscription_payments: List<MembershipSubscriptionPayment?>?=null
): Serializable{
    data class MembershipSubscriptionPayment(
        val membership_subscription_payment_id:String?=null,
        val membership_subscription_id:String?=null,
        val currency:String?=null,
        val amount:String?=null,
        val card_id:String?=null,
        val stripe_customer_id:String?=null,
        val transaction_id:String?=null,
        val paid_on:String?=null,
        val period_start:String?=null,
        val period_end:String?=null
    ): Serializable
}