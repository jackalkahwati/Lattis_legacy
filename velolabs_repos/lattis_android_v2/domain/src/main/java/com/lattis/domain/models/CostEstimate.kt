package com.lattis.domain.models

data class CostEstimate(
    val amount:String?=null,
    val bike_id:Int?=null,
    val charge_for_duration:String?=null,
    val currency:String? = null,
    val duration:Int?= null,
    val membership_discount:String?=null
)