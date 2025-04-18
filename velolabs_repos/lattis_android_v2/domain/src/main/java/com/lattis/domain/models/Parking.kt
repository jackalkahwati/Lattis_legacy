package com.lattis.domain.models

/**
 * Created by ssd3 on 3/21/17.
 */

class Parking {


    var parking_spot_id: Int? = 0
    var name: String? = null
    var description: String? = null
    var pic: String? = null
    var type: String? = null
    var parking_area_id: Int? = 0
    var fleet_id: Int? = 0
    var operator_id: Int? = 0
    var customer_id: Int? = 0
    var latitude: Double? = 0.toDouble()
    var longitude: Double? = 0.toDouble()


    override fun toString(): String {
        return "Lock{" +
                "parking_spot_id='" + parking_spot_id + '\''.toString() +
                ", name='" + name + '\''.toString() +
                ", description='" + description + '\''.toString() +
                ", pic='" + pic + '\''.toString() +
                ", fleet_id='" + fleet_id + '\''.toString() +
                ", operator_id=" + operator_id +
                ", customer_id=" + customer_id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", type='" + type + '\''.toString() +
                '}'.toString()
    }


}
