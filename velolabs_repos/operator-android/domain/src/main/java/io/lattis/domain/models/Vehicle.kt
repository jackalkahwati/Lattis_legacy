package io.lattis.domain.models

import java.io.Serializable

data class Vehicle(
    val maintenance:String?=null,
    val things:List<Thing>?=null,
    val fleet:Fleet?=null,
    val ellipse: Ellipse?=null,
    val group: Group?=null,
    val batteryLevel:String?=null,
    val latitude:Double?=null,
    val longitude:Double?=null,
    var usage:String?=null,
    val name:String?=null,
    val id:Int?=null,
    val fleetId:Int?=null,
    var status:String?=null,
    val qrCode:String?=null
): Serializable {
    data class Group(
        val id :Int?=null,
        val description:String?=null,
        val type:String?=null,
        val model:String?=null,
        val image:String?=null,
        val make:String?=null,
        val fleetId:Int?=null
    ): Serializable

    data class Fleet(
        val id :Int?=null
    ): Serializable

    data class Ellipse(
        val id:Int?=null,
        val macId:String?=null,
        val name:String?=null ,
        val fleet:Fleet?=null
    ): Serializable

    data class Bike(
        val id :Int?=null
    ): Serializable

    data class Thing(
        val fleet:Fleet?=null,
        val bike:Bike?=null,
        val deviceType:String?=null,
        val id:Int?=null,
        val vendor:String?=null,
        val key:String?=null,
        val qrCode:String?=null
    ): Serializable

}