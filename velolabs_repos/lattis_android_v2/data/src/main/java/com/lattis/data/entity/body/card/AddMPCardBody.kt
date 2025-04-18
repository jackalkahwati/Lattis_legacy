package com.lattis.data.entity.body.card

data class AddMPCardBody(
    val token:String,
    val payment_gateway:String,
    val fleet_id:Int?=null
)