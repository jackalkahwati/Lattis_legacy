package com.lattis.domain.usecase.bike

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.IoTBikeLockUnlockCommandStatus
import com.lattis.domain.models.IoTBikeStatus
import com.lattis.domain.repository.BikeRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetLinkaIoTBikeStatusUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val bikeRepository: BikeRepository
) : UseCase<IoTBikeLockUnlockCommandStatus>(threadExecutor, postExecutionThread) {
    private var bike_id: Int = 0
    private var commandId:String?=null


    fun withBikeId(bike_id: Int): GetLinkaIoTBikeStatusUseCase {
        this.bike_id = bike_id
        return this
    }

    fun withCommandId(commandId: String): GetLinkaIoTBikeStatusUseCase {
        this.commandId = commandId
        return this
    }

    override fun buildUseCaseObservable(): Observable<IoTBikeLockUnlockCommandStatus> {
        return bikeRepository.getLinkaIotBikeStatus(bike_id,commandId!!)
    }

}
