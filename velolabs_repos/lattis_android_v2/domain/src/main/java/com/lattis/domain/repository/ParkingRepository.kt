package com.lattis.domain.repository

import com.lattis.domain.models.*
import io.reactivex.rxjava3.core.Observable

interface ParkingRepository {
    fun getParkingFeeForFleet(
        location: Location,
        fleet_id: Int
    ): Observable<ParkingFeeForFleet>

    fun getParkingSpotsForFleet(fleetId: Int): Observable<List<Parking>>
    fun getParkingZone(fleetID: Int): Observable<List<ParkingZone>>
    fun getDockHub(bikeId:Int,location: Location):Observable<List<DockHub>>
    fun getGeoFences(fleetID: Int): Observable<List<GeoFence>>
}