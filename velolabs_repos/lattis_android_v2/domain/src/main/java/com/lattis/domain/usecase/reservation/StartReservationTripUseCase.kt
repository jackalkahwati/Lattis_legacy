package com.lattis.domain.usecase.reservation

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.StartReservation
import com.lattis.domain.repository.ReservationRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class StartReservationTripUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val reservationRepository: ReservationRepository
) : UseCase<StartReservation>(threadExecutor, postExecutionThread) {

    private var reservationId:Int? =null

    fun withReservation(reservationId: Int) :StartReservationTripUseCase{
        this.reservationId = reservationId
        return this
    }


    override fun buildUseCaseObservable(): Observable<StartReservation> {
        return reservationRepository.start_trip(reservationId!!)
    }

}