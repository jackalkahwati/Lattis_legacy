package com.lattis.data.repository.datasources.api.ride

import com.lattis.data.entity.body.ride.*
import com.lattis.data.entity.response.history.RideHistoryResponse
import com.lattis.data.entity.response.ride.*
import com.lattis.domain.models.RideHistory
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RideApi {
    @POST("api/trips/get-trip-details/")
    fun rideSummary(@Body rideSummaryBody: RideSummaryBody): Observable<RideSummaryResponse>

    @POST("api/trips/update-trip/")
    fun updateRide(@Body updateRideBody: UpdateRideBody): Observable<UpdateTripResponse>

    @POST("api/trips/start-trip/")
    fun startRide(@Body startRideBody: StartRideBody): Observable<StartRideResponse>


    @POST("api/trips/end-trip/")
    fun endRide(@Body endRideBody: EndRideBody): Observable<EndRideResponse>

    @POST("api/trips/update-rating/")
    fun rateRide(@Body rideRatingBody: RideRatingBody): Observable<RideRatingResponse>

    @GET("api/trips/get-trips/")
    fun getRideHistory(): Observable<RideHistory>
}