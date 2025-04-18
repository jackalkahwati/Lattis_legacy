package com.lattis.data.database.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmPricingOption : RealmObject() {

    @PrimaryKey
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