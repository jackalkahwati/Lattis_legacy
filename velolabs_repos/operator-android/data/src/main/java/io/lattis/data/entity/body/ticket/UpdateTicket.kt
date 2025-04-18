package io.lattis.data.entity.body.ticket

data class UpdateTicket (
    val assignee:Int?=null,
    val notes:String?=null,
    val status:String?=null,
){
}