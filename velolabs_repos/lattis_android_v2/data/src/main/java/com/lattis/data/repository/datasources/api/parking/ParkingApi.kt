package com.lattis.data.repository.datasources.api.parking

import com.lattis.data.entity.body.parking.GetParkingFeeForFleetBody
import com.lattis.data.entity.body.parking.GetParkingZoneBody
import com.lattis.data.entity.response.bike.BikeSearchResponse
import com.lattis.data.entity.response.parking.FindParkingResponse
import com.lattis.data.entity.response.parking.GetDockHubResponse
import com.lattis.data.entity.response.parking.GetParkingFeeForFleetResponse
import com.lattis.data.entity.response.parking.GetParkingZoneRepsonse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import com.lattis.data.entity.response.parking.GetGeoFenceResponse

interface ParkingApi {
    @POST("api/fleet/check-parking-fee/")
    fun getParkingFeeForFleet(@Body getParkingFeeForFleetBody: GetParkingFeeForFleetBody): Observable<GetParkingFeeForFleetResponse>

    @POST("api/parking/get-parking-zones-for-fleet/")
    fun getParkingZone(@Body getParkingZoneBody: GetParkingZoneBody): Observable<GetParkingZoneRepsonse>

    @POST("api/parking/get-parking-spots-for-fleet/")
    fun getParkingSpotForFleet(@Body getParkingZoneBody: GetParkingZoneBody): Observable<FindParkingResponse>

    @GET("api/hubs/parking")
    fun getDockHub(@Query("bikeId")bikeId:Int, @Query("lat")lat:Double,@Query("lon")lon:Double): Observable<GetDockHubResponse>

    @GET("api/fleet/geofences")
    fun getGeoFence(@Query("fleet_id") fleet_id:Int): Observable<GetGeoFenceResponse>

}