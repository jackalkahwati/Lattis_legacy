package com.lattis.domain.usecase.bike

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.BikeRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Bike
import com.lattis.domain.models.IoTBikeLockUnlockCommandStatus
import com.lattis.domain.models.Ride
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class LockUnlockIotBikeUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val bikeRepository: BikeRepository
) : UseCase<IoTBikeLockUnlockCommandStatus>(threadExecutor, postExecutionThread) {

    private var bikeId: Int?=null
    private var lock:Boolean?=null
    private var controller_key:List<String>?=null

    fun withBikeId(bikeId: Int): LockUnlockIotBikeUseCase {
        this.bikeId = bikeId
        return this
    }

    fun withLock(lock: Boolean): LockUnlockIotBikeUseCase {
        this.lock = lock
        return this
    }

    fun withController_key(controller_key:List<String>?): LockUnlockIotBikeUseCase {
        this.controller_key = controller_key
        return this
    }

    override fun buildUseCaseObservable(): Observable<IoTBikeLockUnlockCommandStatus> {
        return bikeRepository.lockUnlockIotBike(bikeId!!, lock!!,controller_key)
    }
}