package com.lattis.data.repository.datasources.api.reservation

import com.lattis.data.entity.body.reservation.CancelReservationResponse
import com.lattis.data.entity.body.reservation.CostEstimateBody
import com.lattis.data.entity.body.reservation.ReserveBody
import com.lattis.data.entity.response.BasicResponse
import com.lattis.data.entity.response.reservation.*
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*
import javax.annotation.PostConstruct

interface ReservationApi {


    @GET("/api/reservations/available-vehicles")
    fun getAvailableVehicles(@Query("fleet_id")fleetId:Int, @Query("reservation_start")pickUpTime:String,
        @Query("reservation_end")returnUpTime:String): Observable<AvailableVehiclesResponse>


    @POST("/api/reservations/cost-estimate")
    fun costEstimate(@Body costEstimateBody: CostEstimateBody):Observable<CostEstimationResponse>


    @POST("/api/reservations")
    fun reserve(@Body reserveBody: ReserveBody):Observable<ReserveResponse>

    @PUT("/api/reservations/{reservation_id}/cancel")
    fun cancel(@Path("reservation_id") reservation_id:Int):Observable<CancelReservationResponse>

    @PUT("/api/reservations/{reservation_id}/start-trip")
    fun startTrip(@Path("reservation_id") reservation_id:Int):Observable<StartReservationResponse>

    @GET("/api/reservations")
    fun getReservations():Observable<GetReservationsResponse>
}