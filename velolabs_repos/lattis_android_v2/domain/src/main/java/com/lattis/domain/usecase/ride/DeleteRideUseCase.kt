package com.lattis.domain.usecase.ride

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.RideRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Ride

import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable

/**
 * Created by ssd3 on 4/4/17.
 */

class DeleteRideUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val rideRepository: RideRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return this.rideRepository.deleteRide()
    }
}
