package io.lattis.domain.usecase.vehicle

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.Vehicle
import io.lattis.domain.repository.VehicleRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetVehicleFromQRCodeUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val vehicleRepository: VehicleRepository
) : UseCase<Vehicle>(threadExecutor, postExecutionThread) {
    private var fleetId:Int=0
    private var qrCode:String?=null
    private var thingQRCode:String?=null

    fun withFleedId(fleetId:Int):GetVehicleFromQRCodeUseCase{
        this.fleetId = fleetId
        return this
    }
    fun withQRCode(qrCode:String?):GetVehicleFromQRCodeUseCase{
        this.qrCode = qrCode
        return this
    }
    fun withThingQRCode(thingQRCode:String?):GetVehicleFromQRCodeUseCase{
        this.thingQRCode = thingQRCode
        return this
    }


    override fun buildUseCaseObservable(): Observable<Vehicle>{
        return vehicleRepository.findVehicleFromQRCode(fleetId,qrCode,thingQRCode)
    }

}