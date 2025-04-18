package com.lattis.domain.usecase.ride

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.RideRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class RideRatingUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val rideRepository: RideRepository
) : UseCase<Boolean>(threadExecutor!!, postExecutionThread) {
    private var trip_id = 0
    private var rating = 0
    fun withTripId(trip_id: Int): RideRatingUseCase {
        this.trip_id = trip_id
        return this
    }

    fun withRating(rating: Int): RideRatingUseCase {
        this.rating = rating
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return rideRepository.rateRide(trip_id, rating)
    }

}