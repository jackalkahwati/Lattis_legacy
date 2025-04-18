package com.lattis.data.mapper

import com.lattis.data.entity.response.parking.GetParkingZoneDataResponse
import com.lattis.data.entity.response.parking.ParkingZoneGeometryResponse
import com.lattis.domain.models.ParkingZone
import com.lattis.domain.models.ParkingZoneGeometry
import java.util.*
import javax.inject.Inject

class ParkingZoneResponseMapper @Inject constructor() :
    AbstractDataMapper<GetParkingZoneDataResponse, ParkingZone>() {

    override fun mapIn(getParkingZoneDataResponse: GetParkingZoneDataResponse?): ParkingZone {
        val parkingZone = ParkingZone()
        if(getParkingZoneDataResponse!=null) {
            parkingZone?.customerID = (getParkingZoneDataResponse?.customerID)
            parkingZone?.fleetID = (getParkingZoneDataResponse?.fleetID)
            parkingZone?.parking_area_id = (getParkingZoneDataResponse?.parking_area_id)
            parkingZone?.type = (getParkingZoneDataResponse?.type)
            parkingZone?.zone_Name = (getParkingZoneDataResponse?.zone_Name)
            parkingZone?.zone = (getParkingZoneDataResponse?.zone)
            val responses: List<ParkingZoneGeometryResponse>? =
                getParkingZoneDataResponse?.parkingZoneGeometry
            val parkingZoneGeometryList: MutableList<ParkingZoneGeometry> =
                ArrayList<ParkingZoneGeometry>()
            if (responses != null) {
                for (parkingZoneGeometryResponse in responses) {
                    if (parkingZoneGeometryResponse != null) {
                        val parkingZoneGeometry = ParkingZoneGeometry()
                        parkingZoneGeometry.latitude = (parkingZoneGeometryResponse.latitude)
                        parkingZoneGeometry.longitude = (parkingZoneGeometryResponse.longitude)
                        parkingZoneGeometry.radius = (parkingZoneGeometryResponse.radius)
                        parkingZoneGeometryList.add(parkingZoneGeometry)
                    }
                }
            }
            parkingZone.parkingZoneGeometry = (parkingZoneGeometryList)
        }
        return parkingZone
    }

    override fun mapOut(parkingZone: ParkingZone?): GetParkingZoneDataResponse? {
        return null
    }
}