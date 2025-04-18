package com.lattis.domain.usecase.v2

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.V2ApiRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class CancelBookingUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    val v2ApiRepository: V2ApiRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {

    private var bookingId = 0
    private var bike_damaged = false
    private var lockIssue = false

    fun withBookingId(bookingId: Int): CancelBookingUseCase {
        this.bookingId = bookingId
        return this
    }

    fun withDamage(bike_damaged: Boolean): CancelBookingUseCase {
        this.bike_damaged = bike_damaged
        return this
    }

    fun withLockIssue(lockIssue: Boolean): CancelBookingUseCase {
        this.lockIssue = lockIssue
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return v2ApiRepository.cancelBooking(bookingId, bike_damaged, lockIssue)
    }
}