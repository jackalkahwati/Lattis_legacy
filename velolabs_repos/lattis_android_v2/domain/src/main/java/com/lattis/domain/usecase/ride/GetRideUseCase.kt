package com.lattis.domain.usecase.ride

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.RideRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Ride

import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable

class GetRideUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val rideRepository: RideRepository
) : UseCase<Ride>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<Ride> {
        return this.rideRepository.getRide()
    }
}
