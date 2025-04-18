package com.lattis.domain.models

import java.io.Serializable

data class Reserve(
    val user_id:Int?=null,
    val bike_id:Int?=null,
    val reservation_id:Int?=null,
    val created_at:String?=null,
    val reservation_end:String? = null,
    val reservation_cancelled:String? = null,
    val reservation_start:String?= null,
    val reservation_timezone:String?=null
): Serializable