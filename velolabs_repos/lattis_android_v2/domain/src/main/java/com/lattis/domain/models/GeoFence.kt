package com.lattis.domain.models

data class GeoFence(
    val geofence_id:Int? = 0,
    val name: String? = null,
    val fleet_id:Int? = 0,
    val date_created: String? = null,
    val geometry: GeoFenceGeometry? = null
){

    data class GeoFenceGeometry(
        val shape: String? = null,
        val center : Center?=null,
        val points : List<Points>?=null,
        val radius : Radius?=null,
        val bbox : List<Double>?=null
    ){
        data class Center(
            val latitude: Double = 0.toDouble(),
            var longitude: Double = 0.toDouble()
        )

        data class Radius(
            val value: Double = 0.toDouble(),
            val units: String? = null
        )

        data class Bbox(
            val coordiante: Double = 0.toDouble()
        )

        data class Points(
            val latitude: Double = 0.toDouble(),
            var longitude: Double = 0.toDouble()
        )

    }

}