package com.lattis.domain.usecase.bike

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.BikeRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class CancelBikeReservationUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    val bikeRepository: BikeRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {

    private var bike_id = 0
    private var bike_damaged = false
    private var lockIssue = false

    fun withBikeId(bike_id: Int): CancelBikeReservationUseCase {
        this.bike_id = bike_id
        return this
    }

    fun withDamage(bike_damaged: Boolean): CancelBikeReservationUseCase {
        this.bike_damaged = bike_damaged
        return this
    }

    fun withLockIssue(lockIssue: Boolean): CancelBikeReservationUseCase {
        this.lockIssue = lockIssue
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return bikeRepository.cancelBike(bike_id, bike_damaged, lockIssue)
    }
}