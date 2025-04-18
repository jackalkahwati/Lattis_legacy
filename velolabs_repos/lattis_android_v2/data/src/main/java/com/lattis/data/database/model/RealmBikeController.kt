package com.lattis.data.database.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmBikeController: RealmObject(){
    @PrimaryKey
    var controller_id:Int?=null

    var gps_log_time:String?=null
    var hw_version:String?=null
    var qr_code:String?=null
    var fleet_id:Int?=null
    var longitude:Double?=null
    var fw_version:String?=null
    var latitude:Double?=null
    var battery_level:Float?=null
    var device_type:String?=null
    var key:String?=null
    var added_by_operator_id:Int?=null
    var vendor:String?=null

    var make:String?=null
    var bike_id:Int?=null
    var date_created:String?=null
    var model:String?=null
    var status:String?=null
}