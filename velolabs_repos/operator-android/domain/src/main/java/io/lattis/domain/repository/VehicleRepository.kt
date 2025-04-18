package io.lattis.domain.repository

import io.lattis.domain.models.Location
import io.lattis.domain.models.Vehicle
import io.reactivex.Observable
import okhttp3.ResponseBody

interface VehicleRepository {
    fun getVehicles(fleetId:Int,page:Int,per:Int,name:String?,usage:String?,
                    maintenance: String?,battery_level:Int?): Observable<List<Vehicle>>
    fun findVehicleFromQRCode(fleetId: Int, qr_code: String?,thing_qr_code:String?): Observable<Vehicle>
    fun changeStatus(vehicle_id: Int, status: String,usage:String,maintenance:String?): Observable<ResponseBody>
    fun changeBulkStatus(batch:String, status: String,usage:String,maintenance:String?): Observable<ResponseBody>
    fun getVehiclesInBbox(
            sw: Location,
            ne: Location,
            fleetId: Int,
            name:String?,
            usage:String?,
            maintenance: String?,
            battery_level:Int?
    ):Observable<List<Vehicle>>
    fun getVehicleLocation(vehicle_id:Int):Observable<Location>
}