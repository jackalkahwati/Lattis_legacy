package com.lattis.data.database.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class RealmSubscription : RealmObject(){
    @Required
    @PrimaryKey
    var membership_subscription_id:Int?=null
    var fleet_membership_id:Int?=null
    var user_id:Int?=null
    var activation_date:String?=null
    var deactivation_date:String?=null
    var period_start:String?=null
    var period_end:String?=null
    var fleet_membership: RealmMembership?=null
}