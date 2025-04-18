package io.lattis.domain.usecase.vehicle

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.Ticket
import io.lattis.domain.models.Vehicle
import io.lattis.domain.repository.TicketRepository
import io.lattis.domain.repository.VehicleRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetVehiclesUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val vehicleRepository: VehicleRepository
) : UseCase<List<Vehicle>>(threadExecutor, postExecutionThread) {
    private var fleetId:Int=0
    private var page:Int=0
    private var per:Int=0
    private var name:String?=null
    private var usage :String? = null
    private var maintenance : String? = null
    private var battery_level:Int?=null

    fun withFleedId(fleetId:Int):GetVehiclesUseCase{
        this.fleetId = fleetId
        return this
    }
    fun withPage(page:Int):GetVehiclesUseCase{
        this.page = page
        return this
    }
    fun withPer(per:Int):GetVehiclesUseCase{
        this.per = per
        return this
    }

    fun withName(name:String?):GetVehiclesUseCase{
        this.name = name
        return this
    }

    fun withMaintenance(maintenance:String?):GetVehiclesUseCase{
        this.maintenance = maintenance
        return this
    }

    fun withUsage(usage:String?):GetVehiclesUseCase{
        this.usage = usage
        return this
    }

    fun withBatteryLevel(battery_level:Int?):GetVehiclesUseCase{
        this.battery_level = battery_level
        return this
    }


    override fun buildUseCaseObservable(): Observable<List<Vehicle>> {
        return vehicleRepository.getVehicles(fleetId,page,per,name,usage,maintenance,battery_level)
    }

}