package com.lattis.domain.models

data class UserCurrentStatus(
    val supportPhone:String?,
    val onCallOperator:String?,
    val tripId:Int?=null,
    val activeBooking: ActiveBooking?,
    var reservation: Reservation?, // needs to be modified to null when trip started for same
    var vehicle:Vehicle?,
    val dockHub: DockHub?
) {

    class ActiveBooking(
                        val bike_id:Int?=null,
                        val port_id:Int?=null,
                        val hub_id:Int?=null,
                        val device_type:String?=null,
                        val booking_id:Int?=null,
                              val booked_on: Long? = 0,
                              val till: Long? = 0)

    class Vehicle(val is_docked:Boolean?=null)



}