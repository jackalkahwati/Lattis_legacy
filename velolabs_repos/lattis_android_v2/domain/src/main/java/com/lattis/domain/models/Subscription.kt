package com.lattis.domain.models

import java.io.Serializable

data class Subscription(
    var membership_subscription_id:Int?=null,
    var fleet_membership_id:Int?=null,
    var user_id:Int?=null,
    var activation_date:String?=null,
    var deactivation_date:String?=null,
    var period_start:String?=null,
    var period_end:String?=null,
    var fleet_membership: Membership?=null
):Serializable