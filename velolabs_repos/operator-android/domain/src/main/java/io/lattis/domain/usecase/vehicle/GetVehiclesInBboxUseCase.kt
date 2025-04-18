package io.lattis.domain.usecase.vehicle

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.Location
import io.lattis.domain.models.Vehicle
import io.lattis.domain.repository.VehicleRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetVehiclesInBboxUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val vehicleRepository: VehicleRepository
) : UseCase<List<Vehicle>>(threadExecutor, postExecutionThread) {
    private var fleetId:Int=0
    private lateinit var sw: Location
    private lateinit var ne: Location
    private var name:String?=null
    private var usage :String? = null
    private var maintenance : String? = null
    private var battery_level:Int?=null

    fun withFleedId(fleetId:Int):GetVehiclesInBboxUseCase{
        this.fleetId = fleetId
        return this
    }
    fun withSW(sw:Location):GetVehiclesInBboxUseCase{
        this.sw = sw
        return this
    }
    fun withNE(ne:Location):GetVehiclesInBboxUseCase{
        this.ne = ne
        return this
    }

    fun withName(name:String?):GetVehiclesInBboxUseCase{
        this.name = name
        return this
    }

    fun withMaintenance(maintenance:String?):GetVehiclesInBboxUseCase{
        this.maintenance = maintenance
        return this
    }

    fun withUsage(usage:String?):GetVehiclesInBboxUseCase{
        this.usage = usage
        return this
    }


    fun withBatteryLevel(battery_level:Int?):GetVehiclesInBboxUseCase{
        this.battery_level = battery_level
        return this
    }

    override fun buildUseCaseObservable(): Observable<List<Vehicle>> {
        return vehicleRepository.getVehiclesInBbox(sw,ne,fleetId,name,usage,maintenance,battery_level)
    }

}