package com.lattis.data.database.model

import io.realm.RealmObject

open class RealmMembership : RealmObject(){
    var fleet_membership_id:Int?=null
    var fleet_id:Int?=null
    var membership_price:String?=null
    var membership_price_currency:String?=null
    var membership_incentive:String?=null
    var payment_frequency:String?=null
    var created_at:String?=null
    var deactivation_date:String?=null
    var deactivation_reason:String?=null
}