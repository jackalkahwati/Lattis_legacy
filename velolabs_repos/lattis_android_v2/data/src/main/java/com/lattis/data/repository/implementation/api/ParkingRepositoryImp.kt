package com.lattis.data.repository.implementation.api

import com.lattis.data.entity.body.parking.GetParkingFeeForFleetBody
import com.lattis.data.entity.body.parking.GetParkingZoneBody
import com.lattis.data.entity.response.parking.GetGeoFenceResponse
import com.lattis.data.mapper.ParkingResponseMapper
import com.lattis.data.mapper.ParkingZoneResponseMapper
import com.lattis.data.net.parking.ParkingApiClient
import com.lattis.domain.models.*
import com.lattis.domain.repository.ParkingRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class ParkingRepositoryImp @Inject constructor(
    val parkingApiClient: ParkingApiClient,
    val parkingZoneResponseMapper: ParkingZoneResponseMapper,
    val parkingResponseMapper: ParkingResponseMapper
):ParkingRepository{


    override fun getParkingFeeForFleet(
        location: Location,
        fleet_id: Int
    ): Observable<ParkingFeeForFleet> {
        return this.parkingApiClient.api.getParkingFeeForFleet(GetParkingFeeForFleetBody(location, fleet_id))
            .map {
                ParkingFeeForFleet(
                    it.getParkingFeeForFleetData()?.isOutside,
                    it.getParkingFeeForFleetData()?.isNot_allowed,
                    it.getParkingFeeForFleetData()?.fee,
                    it.getParkingFeeForFleetData()?.currency
                )
            }
    }

    override fun getParkingSpotsForFleet(fleetId: Int): Observable<List<Parking>> {
        return this.parkingApiClient.api.getParkingSpotForFleet(GetParkingZoneBody(fleetId)).map {findParkingResponse ->
            val parkings = ArrayList<Parking>()
            if(findParkingResponse!=null && findParkingResponse?.findParkingDataResponse()!=null){
                parkings.addAll(parkingResponseMapper.mapIn(findParkingResponse?.findParkingDataResponse()!!))
            }
            parkings
        }
    }

    override fun getParkingZone(fleetID: Int): Observable<List<ParkingZone>> {
        return this.parkingApiClient.api.getParkingZone(GetParkingZoneBody(fleetID)).map {getParkingZoneResponse ->

            val parkingZones = ArrayList<ParkingZone>()
            if(getParkingZoneResponse!=null && getParkingZoneResponse?.getParkingZoneDataResponse()!=null){
                parkingZones.addAll(parkingZoneResponseMapper.mapIn(getParkingZoneResponse?.getParkingZoneDataResponse()!!))
            }
            parkingZones

        }
    }

    override fun getDockHub(bikeId:Int,location: Location):Observable<List<DockHub>> {
        return parkingApiClient.api.getDockHub(bikeId, location.latitude, location.longitude)
            .map {
                it.hubs
            }
    }
    override fun getGeoFences(fleetID: Int): Observable<List<GeoFence>> {
        return this.parkingApiClient.api.getGeoFence(fleetID)
            .map {  it.geoFences }
    }
}