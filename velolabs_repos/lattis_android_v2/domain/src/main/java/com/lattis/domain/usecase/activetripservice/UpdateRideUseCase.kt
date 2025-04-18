package com.lattis.domain.usecase.ride

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.UpdateTripData
import com.lattis.domain.repository.RideRepository
import com.lattis.domain.usecase.base.UseCase
import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable


class UpdateRideUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val rideRepository: RideRepository
) : UseCase<UpdateTripData>(threadExecutor, postExecutionThread) {

    private var trip_id: Int = 0
    private lateinit var steps: Array<DoubleArray>

    fun withTripId(trip_id: Int): UpdateRideUseCase {
        this.trip_id = trip_id
        return this
    }

    fun withSteps(steps: Array<DoubleArray>): UpdateRideUseCase {
        this.steps = steps
        return this
    }


    override fun buildUseCaseObservable(): Observable<UpdateTripData> {
        return this.rideRepository.updateRide(trip_id, steps)
    }
}

