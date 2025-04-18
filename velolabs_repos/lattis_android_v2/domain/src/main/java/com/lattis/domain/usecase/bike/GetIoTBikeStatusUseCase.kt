package com.lattis.domain.usecase.bike

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.IoTBikeStatus
import com.lattis.domain.repository.BikeRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetIoTBikeStatusUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val bikeRepository: BikeRepository
) : UseCase<IoTBikeStatus>(threadExecutor, postExecutionThread) {
    private var bike_id: Int = 0
    private var controller_key:String?=null


    fun withBikeId(bike_id: Int): GetIoTBikeStatusUseCase {
        this.bike_id = bike_id
        return this
    }

    fun withControllerKey(controller_key: String?): GetIoTBikeStatusUseCase {
        this.controller_key = controller_key
        return this
    }

    override fun buildUseCaseObservable(): Observable<IoTBikeStatus> {
        return bikeRepository.getIotBikeStatus(bike_id,controller_key)
    }

}
