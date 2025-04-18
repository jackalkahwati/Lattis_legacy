package com.lattis.domain.models

class ParkingZoneGeometry {
    var latitude: Double = 0.toDouble()
    var longitude: Double = 0.toDouble()
    var radius: Double = 0.toDouble()

    override fun toString(): String {


        return "ParkingZoneGeometryResponse{" +
                "latitude=" + latitude +
                "longitude=" + longitude +
                '}'.toString()

    }
}
