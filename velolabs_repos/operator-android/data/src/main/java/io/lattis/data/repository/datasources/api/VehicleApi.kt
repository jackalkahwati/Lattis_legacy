package io.lattis.data.repository.datasources.api

import io.lattis.data.entity.body.vehicle.ChangeStatusBody
import io.lattis.data.entity.response.base.BasicResponse
import io.lattis.domain.models.Fleet
import io.lattis.domain.models.Location
import io.lattis.domain.models.Vehicle
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*

interface VehicleApi {

    @GET("operator/vehicles")
    fun getVehicles(@Query("fleet_id")fleet_id:Int,
                    @Query("page")page:Int,
                    @Query("per")per:Int,
                    @Query("name")name:String?,
                    @Query("usage")usage:String?,
                    @Query("maintenance")maintenance:String?,
                    @Query("battery-level")battery_level:Int?): Observable<List<Vehicle>>


    @GET("operator/vehicles/find")
    fun findVehicleFromQRCode(@Query("fleet_id")fleet_id:Int,@Query("qr_code")qr_code:String?,@Query("thing_qr_code")thing_qr_code:String?): Observable<Vehicle>


    @PATCH("operator/vehicles/{vehicle_id}")
    fun changeStatus(@Path("vehicle_id") vehicle_id:Int,@Body body:ChangeStatusBody):Observable<ResponseBody>

    @PATCH("operator/vehicles")
    fun changeBulkStatus(@Query("batch") batch:String, @Body body:ChangeStatusBody):Observable<ResponseBody>

    @GET("operator/map/vehicles")
    fun getVehiclesInBbox(@Query("bbox")bbox:String,
                          @Query("fleet_id")fleet_id:Int,
                          @Query("name")name:String?,
                          @Query("usage")usage:String?,
                          @Query("maintenance")maintenance:String?,
                          @Query("battery-level")battery_level:Int?
    ):Observable<List<Vehicle>>

    @GET("operator/vehicles/{vehicle_id}/location")
    fun getVehicleLocation(@Path("vehicle_id") vehicle_id:Int):Observable<Location>
}