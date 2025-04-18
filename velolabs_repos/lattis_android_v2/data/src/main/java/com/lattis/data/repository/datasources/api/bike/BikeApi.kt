package com.lattis.data.repository.datasources.api.bike

import com.lattis.data.entity.body.bike.*
import com.lattis.data.entity.response.BasicResponse
import com.lattis.data.entity.response.bike.*
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*

interface BikeApi{

    @GET("api/rentals")
    fun searchBike(@Query("ne")latLongNE:String, @Query("sw")latLongSW:String): Observable<BikeSearchResponse>

    @GET("api/rentals/find")
    fun findByQRCode(@Query("qr_code")qr_code:String): Observable<FindByQRCodeResponse>

    @POST("api/bikes/create-booking/")
    fun bookBike(@Body bookBikeBody: BookBikeBody): Observable<ReserveBikeResponse>

    @POST("api/bikes/get-bike-details/")
    fun bikeDetails(@Body bikeDetailBody: BikeDetailBody): Observable<BikeDetailResponse>

    @POST("api/bikes/update-metadata-for-user/")
    fun updateBikeMetaData(@Body bikeMetaDataBoday: BikeMetaDataBody): Observable<BasicResponse>

    @POST("api/bikes/cancel-booking/")
    fun cancelBikes(@Body bookBikeBody: CancelBikeBody): Observable<CancelBikeReservationResponse>

    @POST("/api/bikes/{bikeId}/user-lock")
    fun lockIotBike(@Path("bikeId") bike_id:Int, @Body controllerKeyBody: ControllerKeyBody):Observable<IoTBikeLockUnlockCommandStatusResponse>

    @POST("/api/bikes/{bikeId}/user-unlock")
    fun unLockIotBike(@Path("bikeId") bike_id:Int, @Body controllerKeyBody: ControllerKeyBody):Observable<IoTBikeLockUnlockCommandStatusResponse>

    @GET("/api/bikes/{bikeId}/iot/status")
    fun getIotBikeStatus(@Path("bikeId") bike_id:Int,@Query("controller_key")controller_key:String?):Observable<IoTBikeStatusResponse>

    @GET("/api/bikes/{bikeId}/command/{commandId}")
    fun getLinkaIotBikeStatus(@Path("bikeId") bike_id:Int,@Path("commandId") commandId:String):Observable<GetIoTBikeLockUnlockStatusResponse>

    @GET("api/rentals/search")
    fun searchBikeByName(@Query("bike_name")bike_name:String?):Observable<SearchBikeByNameResponse>

    @POST("api/sentinel/{bikeId}/unlock")
    fun unlockSentinel(@Path("bikeId") bike_id:Int):Observable<BasicResponse>



}