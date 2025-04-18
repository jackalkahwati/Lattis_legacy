package com.lattis.data.repository.datasources.api.v2

import com.lattis.data.entity.body.bike.CancelBikeBody
import com.lattis.data.entity.body.bike.ControllerKeyBody
import com.lattis.data.entity.body.v2.BikeHubPortBody
import com.lattis.data.entity.body.v2.BookingsBody
import com.lattis.data.entity.body.v2.StartTripBody
import com.lattis.data.entity.response.bike.CancelBikeReservationResponse
import com.lattis.data.entity.response.bike.IoTBikeLockUnlockCommandStatusResponse
import com.lattis.data.entity.response.bike.IoTBikeStatusResponse
import com.lattis.data.entity.response.bike.ReserveBikeResponse
import com.lattis.data.entity.response.ride.StartRideResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*

interface V2Api {

    @POST("v2/api/bookings/{endUrl}")
    fun booking(@Path("endUrl")endUrl: String,@Body bookingsBody: BookingsBody): Observable<ReserveBikeResponse>

    @PATCH("v2/api/bookings/{bookingId}/cancel")
    fun cancelBooking(@Path("bookingId")bookingId: Int,@Body bookBikeBody: CancelBikeBody): Observable<CancelBikeReservationResponse>

    @POST("v2/api/trips/start-trip")
    fun startTrip(@Body startTripBody: StartTripBody): Observable<StartRideResponse>

    @GET("/api/equipment/{controllerId}/status/")
    fun getIoTBikeStatus(@Path("controllerId")controllerId: Int,@QueryMap(encoded = false)options:Map<String, Int>? ):Observable<IoTBikeStatusResponse>


    @POST("/api/equipment/{controllerId}/lock")
    fun lockIotBike(@Path("controllerId")controllerId:Int, @Body bikeHubPortBody: BikeHubPortBody):Observable<IoTBikeLockUnlockCommandStatusResponse>

    @POST("/api/equipment/{controllerId}/unlock")
    fun unLockIotBike(@Path("controllerId")controllerId:Int, @Body bikeHubPortBody: BikeHubPortBody):Observable<IoTBikeLockUnlockCommandStatusResponse>

}