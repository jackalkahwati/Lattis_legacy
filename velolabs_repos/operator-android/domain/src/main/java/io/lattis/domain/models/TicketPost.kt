package io.lattis.domain.models

data class TicketPost(
    val fleetId:Int?=null,
    val vehicle:Int?=null,
    val assignee:Int?=null,
    val notes:String?=null,
    val createdBy:Int?=null,
    val category:String?=null
)