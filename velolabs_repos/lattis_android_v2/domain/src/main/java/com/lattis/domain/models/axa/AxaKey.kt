package com.lattis.domain.models.axa

data class AxaKey(
    val ekey:String?=null,
    val modified:String?=null,
    val passkey:String?=null,
    val passkey_type:String?=null,
    val tag:String?=null,
    val segmented:Boolean?=null,
    val sequence:Int?=null,
    val slot_position:Int?=null
)