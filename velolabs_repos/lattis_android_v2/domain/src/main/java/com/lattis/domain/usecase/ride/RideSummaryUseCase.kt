package com.lattis.domain.usecase.ride


import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.RideSummary
import com.lattis.domain.repository.RideRepository
import com.lattis.domain.usecase.base.UseCase
import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable


class RideSummaryUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val rideRepository: RideRepository
) : UseCase<RideSummary>(threadExecutor, postExecutionThread) {
    private var trip_id: Int = 0
    fun withTripId(trip_id: Int): RideSummaryUseCase {
        this.trip_id = trip_id
        return this
    }

    override fun buildUseCaseObservable(): Observable<RideSummary> {
        return this.rideRepository.getRideSummary(trip_id)
    }
}

