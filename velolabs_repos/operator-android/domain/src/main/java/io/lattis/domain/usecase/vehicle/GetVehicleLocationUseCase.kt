package io.lattis.domain.usecase.vehicle

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.Location
import io.lattis.domain.repository.VehicleRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetVehicleLocationUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val vehicleRepository: VehicleRepository
) : UseCase<Location>(threadExecutor, postExecutionThread) {
    var vehicleId:Int?=null


    fun withVehicleId(vehicleId:Int):GetVehicleLocationUseCase{
        this.vehicleId = vehicleId
        return this
    }

    override fun buildUseCaseObservable(): Observable<Location> {
        return vehicleRepository.getVehicleLocation(vehicleId!!)
    }
}