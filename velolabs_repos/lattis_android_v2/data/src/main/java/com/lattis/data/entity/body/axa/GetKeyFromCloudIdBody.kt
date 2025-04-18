package com.lattis.data.entity.body.axa

data class GetKeyFromCloudIdBody(
    val hours:Int?=null,
    val passkey_type: String?=null,
    val nr_of_passkeys:Int?=null,
    val segmented:Boolean?=null,
    val tag:String?=null
)