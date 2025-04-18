package com.lattis.domain.usecase.reservation

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.ReservationRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Bike
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class CancelReservationUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val reservationRepository: ReservationRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {

    private var reservationId:Int? =null

    fun withReservation(reservationId: Int) :CancelReservationUseCase{
        this.reservationId = reservationId
        return this
    }


    override fun buildUseCaseObservable(): Observable<Boolean> {
        return reservationRepository.cancel(reservationId!!)
    }

}