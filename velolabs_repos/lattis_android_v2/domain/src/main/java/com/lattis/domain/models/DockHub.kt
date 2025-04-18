package com.lattis.domain.models

import java.io.Serializable

data class DockHub(
    var hub_id:Int?=null,
    var latitude:Double?=null,
    var longitude:Double?=null,
    var remote_hub_status:String?=null,
    var hub_name:String?=null,
    var name:String?=null,
    var hub_uuid:String?=null,
    var local_hub_status:String?=null,
    var make:String?=null,
    var model:String?=null,
    var description:String?=null,
    var type:String?=null,
    var integration:String?=null,
    var fleet_id:Int?=null,
    var hub_equipment_id:Int?=null,
    var ports:List<Port>?=null,
    var bikes:List<DockHubBike>?=null,
    var qr_code:String?=null,
    var fleet: Bike.Fleet?=null,
    var equipment:Bike.Controller?=null,
    var pricing_options: ArrayList<Bike.Pricing_options?>?=null,
    var promotions : ArrayList<Promotion?>?=null,
    var image: String?=null

) : Serializable{
    data class Port(
        var port_id:Int?=null,
        var port_uuid:String?=null,
        var port_hub_uuid:String?=null,
        var port_status:String?=null,
        var port_vehicle_uuid:String?=null,
        var port_locked:String?=null,
        var port_equipment_id:Int?=null,
        var port_number :String?=null,
        var port_charging_status:String?=null,
        var port_name:String?=null,
        var qr_code:String?=null,
        var equipment:Bike.Controller?=null,
        var hub: DockHub?=null,
        var fleet: Bike.Fleet?=null,
        var pricing_options: ArrayList<Bike.Pricing_options?>?=null,
        var promotions : ArrayList<Promotion?>?=null
    ): Serializable

    data class DockHubBike(
        var bike_fleet_id:Int?=null,
        var bike_uuid:String?=null,
        var bike_id:Int?=null,
        var bike_name:String?=null,
        var bike_status:String?=null,
        var current_bike_status :String?=null
    ): Serializable

}