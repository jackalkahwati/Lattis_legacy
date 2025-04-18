package com.lattis.domain.models

class ParkingZone {
    var parking_area_id: Int? = 0
    var zone_Name: String? = null
    var type: String? = null
    var zone: String? = null

    var operatorID: Int? = 0
    var customerID: Int? = 0
    var fleetID: Int? = 0

    var parkingZoneGeometry: List<ParkingZoneGeometry>?=null;

    override fun toString(): String {
        return "Lock{" +
                "parking_area_id='" + parking_area_id + '\''.toString() +
                ", zone_Name='" + zone_Name + '\''.toString() +
                ", type='" + type + '\''.toString() +
                ", zone='" + zone + '\''.toString() +
                ", fleet_id='" + fleetID + '\''.toString() +
                ", operator_id=" + operatorID +
                ", customer_id=" + customerID +
                '}'.toString()
    }

}
