package com.lattis.domain.models

data class Promotion(
    var promotion_id:Int?=null,
    var promotion_code:String?=null,
    var amount:Int?=null,
    var usage:String?=null,
    var fleet_id:Int?=null,
    var deactivated_at:String?=null,
    var created_at:String?=null,
    var promotion_users:PromotionUsers?=null,
    var fleet:Bike.Fleet?=null
) {
    data class PromotionUsers(
        var promotion_users_id:Int?=null,
        var promotion_id:Int?=null,
        var user_id:Int?=null,
        var claimed_at:String?=null
    )
}