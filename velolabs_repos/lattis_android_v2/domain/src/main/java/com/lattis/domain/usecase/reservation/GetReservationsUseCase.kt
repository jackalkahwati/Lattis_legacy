package com.lattis.domain.usecase.reservation

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.Reservation
import com.lattis.domain.repository.ReservationRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetReservationsUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val reservationRepository: ReservationRepository
) : UseCase<List<Reservation>>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<List<Reservation>> {
        return reservationRepository.getReservations()
    }
}