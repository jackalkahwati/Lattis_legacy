package io.lattis.domain.models

data class ThingStatus (
    val batteryLevel:String?=null,
    val charging:Boolean?=null,
    val online:Boolean?=null,
    val locked:Boolean?=null,
    val lockStatus : Boolean?=null,
    val coordinate: Coordinate?=null
){
    data class Coordinate(
        val longitude:Double?=null,
        val latitude:Double?=null
    )
}