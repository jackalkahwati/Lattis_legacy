package io.lattis.data.repository.implementation.api

import io.lattis.data.entity.body.vehicle.ChangeStatusBody
import io.lattis.data.net.vehicle.VehicleApiClient
import io.lattis.domain.models.Location
import io.lattis.domain.models.Vehicle
import io.lattis.domain.repository.VehicleRepository
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.Path
import javax.inject.Inject

class VehicleRepositoryImp @Inject constructor(
    val vehicleApiClient: VehicleApiClient
):VehicleRepository {

    override fun getVehicles(fleetId: Int, page: Int, per: Int,name:String?,usage:String?,
                             maintenance: String?,battery_level:Int?): Observable<List<Vehicle>> {
        return vehicleApiClient.api.getVehicles(fleetId,page,per,name,usage,maintenance,battery_level)
    }

    override fun findVehicleFromQRCode(fleetId: Int, qr_code: String?,thing_qr_code:String?): Observable<Vehicle> {
        return vehicleApiClient.api.findVehicleFromQRCode(fleetId,qr_code,thing_qr_code)
    }

    override fun changeStatus(vehicle_id: Int, status: String,usage:String,maintenance:String?): Observable<ResponseBody> {
        return vehicleApiClient.api.changeStatus(vehicle_id, ChangeStatusBody(status,usage,maintenance))
    }
    override fun changeBulkStatus(batch:String, status: String,usage:String,maintenance:String?): Observable<ResponseBody> {
        return vehicleApiClient.api.changeBulkStatus(batch, ChangeStatusBody(status,usage,maintenance))
    }

    override fun getVehiclesInBbox(
                          sw:Location,
                          ne:Location,
                          fleetId: Int,
                          name:String?,
                          usage:String?,
                          maintenance: String?,
                          battery_level:Int?
    ):Observable<List<Vehicle>>{
        return vehicleApiClient.api.getVehiclesInBbox("${sw.latitude},${ne.latitude},${sw.longitude},${ne.longitude}",fleetId,name,usage,maintenance,battery_level)
    }

    override fun getVehicleLocation(vehicle_id:Int):Observable<Location>{
        return vehicleApiClient.api.getVehicleLocation(vehicle_id)
    }
}