package com.lattis.data.database.model

import com.lattis.domain.models.Bike
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmPromotion : RealmObject() {
    @PrimaryKey
    var promotion_id:Int?=null
    var promotion_code:String?=null
    var amount:Int?=null
    var usage:String?=null
    var fleet_id:Int?=null
    var deactivated_at:String?=null
    var created_at:String?=null
    var promotion_users: RealmPromotionUsers?=null
}