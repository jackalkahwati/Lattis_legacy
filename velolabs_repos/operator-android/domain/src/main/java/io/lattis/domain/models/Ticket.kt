package io.lattis.domain.models

import java.io.Serializable

data class Ticket(
    var assignee:Int?=null,
    val tripId:Int?=null,
    val status:String?=null,
    val createdAt:Long?=null,
    val riderNotes:String?=null,
    val operatorNotes:String?=null,
    val id:Int?=null,
    val fleetId:Int?=null,
    val operatorId:Int?=null,
    val category:String?=null,
    val vehicle: Vehicle?=null
): Serializable