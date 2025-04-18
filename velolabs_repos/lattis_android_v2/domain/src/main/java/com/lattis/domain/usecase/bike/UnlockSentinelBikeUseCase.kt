package com.lattis.domain.usecase.bike

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.IoTBikeLockUnlockCommandStatus
import com.lattis.domain.repository.BikeRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class UnlockSentinelBikeUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val bikeRepository: BikeRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {

    private var bikeId: Int?=null


    fun withBikeId(bikeId: Int): UnlockSentinelBikeUseCase {
        this.bikeId = bikeId
        return this
    }


    override fun buildUseCaseObservable(): Observable<Boolean> {
        return bikeRepository.unlockSentinelBike(bikeId!!)
    }
}