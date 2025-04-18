package io.lattis.domain.usecase.vehicle

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.repository.VehicleRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import okhttp3.ResponseBody
import javax.inject.Inject

class ChangeStatusUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val vehicleRepository: VehicleRepository
) : UseCase<ResponseBody>(threadExecutor, postExecutionThread) {
    private var vehicle_id:Int=0
    private var status:String?=null
    private var usage:String?=null
    private var maintenance:String?=null

    fun withStatus(status:String):ChangeStatusUseCase{
        this.status = status
        return this
    }
    fun withUsage(usage:String):ChangeStatusUseCase{
        this.usage = usage
        return this
    }
    fun withMaintenance(maintenance:String?):ChangeStatusUseCase{
        this.maintenance = maintenance
        return this
    }

    fun withVehicleId(vehicle_id:Int):ChangeStatusUseCase{
        this.vehicle_id = vehicle_id
        return this
    }


    override fun buildUseCaseObservable(): Observable<ResponseBody> {
        return vehicleRepository.changeStatus(vehicle_id,status!!,usage!!,maintenance)
    }

}