package com.lattis.domain.usecase.v2

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.Bike
import com.lattis.domain.models.IoTBikeLockUnlockCommandStatus
import com.lattis.domain.models.Ride
import com.lattis.domain.repository.V2ApiRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class LockUnlockV2IotBikeUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val v2ApiRepository: V2ApiRepository
) : UseCase<IoTBikeLockUnlockCommandStatus>(threadExecutor, postExecutionThread) {
    private var bike: Bike?=null
    private var ride: Ride?=null
    private var controller_id:Int?=null
    private var lock:Boolean=false


    fun withBike(bike: Bike): LockUnlockV2IotBikeUseCase {
        this.bike = bike
        return this
    }

    fun withRide(ride: Ride?): LockUnlockV2IotBikeUseCase {
        this.ride = ride
        return this
    }

    fun withControllerId(controller_id:Int?):LockUnlockV2IotBikeUseCase {
        this.controller_id = controller_id
        return this
    }

    fun withLock(lock: Boolean): LockUnlockV2IotBikeUseCase {
        this.lock = lock
        return this
    }

    override fun buildUseCaseObservable(): Observable<IoTBikeLockUnlockCommandStatus> {
        return v2ApiRepository.lockUnlockIotBike(lock,bike,ride,controller_id!!)
    }

}