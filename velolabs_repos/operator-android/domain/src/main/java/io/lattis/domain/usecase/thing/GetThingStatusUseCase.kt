package io.lattis.domain.usecase.thing

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.Fleet
import io.lattis.domain.models.ThingStatus
import io.lattis.domain.repository.FleetRepository
import io.lattis.domain.repository.ThingRepository
import io.lattis.domain.usecase.base.UseCase
import io.lattis.domain.usecase.vehicle.GetVehicleFromQRCodeUseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetThingStatusUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val thingRepository: ThingRepository
) : UseCase<ThingStatus>(threadExecutor, postExecutionThread) {
    private var thingId:Int=0

    fun withThingId(thingId:Int): GetThingStatusUseCase {
        this.thingId = thingId
        return this
    }

    override fun buildUseCaseObservable(): Observable<ThingStatus> {
        return thingRepository.getThingStatus(thingId)
    }

}