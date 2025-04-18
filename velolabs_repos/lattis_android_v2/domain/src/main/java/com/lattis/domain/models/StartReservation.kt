package com.lattis.domain.models

import java.io.Serializable

data class StartReservation(
    val user_id:Int?=null,
    val bike_id:Int?=null,
    val trip_id:Int?=null,
    val reservation_id:Int?=null,
    val customer_id:Int?=null,
    val date_created:String?=null,
    val date_endtrip:String? = null,
    val do_not_track_trip:Boolean?=null,
    val duration:String?=null,
    val end_address:String?=null,
    val first_lock_connect:String?=null,
    val fleet_id:Int?=null,
    val lock_id:Int?=null,
    val reservation_cancelled:String? = null,
    val reservation_start:String?= null,
    val reservation_timezone:String?=null
): Serializable