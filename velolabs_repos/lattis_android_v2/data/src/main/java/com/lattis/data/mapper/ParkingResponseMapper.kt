package com.lattis.data.mapper

import com.lattis.data.entity.response.parking.FindParkingDataResponse
import com.lattis.domain.models.Parking
import javax.inject.Inject

class ParkingResponseMapper @Inject constructor() :
    AbstractDataMapper<FindParkingDataResponse, Parking>() {
    override fun mapIn(findParkingDataResponse: FindParkingDataResponse?): Parking {
        val parking = Parking()
        parking.longitude = findParkingDataResponse?.longitude
        parking.parking_spot_id = findParkingDataResponse?.parking_spot_id
        parking.name = findParkingDataResponse?.name
        parking.description = findParkingDataResponse?.description
        parking.pic = findParkingDataResponse?.pic
        parking.type = findParkingDataResponse?.type
        parking.latitude = findParkingDataResponse?.latitude
        parking.parking_area_id = findParkingDataResponse?.parking_area_id
        parking.fleet_id = findParkingDataResponse?.fleet_id
        parking.operator_id = findParkingDataResponse?.operator_id
        parking.customer_id = findParkingDataResponse?.customer_id
        return parking
    }

    override fun mapOut(parking: Parking?): FindParkingDataResponse? {
        return null
    }
}