package io.lattis.domain.usecase.ticket

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.Colleague
import io.lattis.domain.repository.TicketRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetCollegueUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val ticketRepository: TicketRepository
) : UseCase<List<Colleague>>(threadExecutor, postExecutionThread) {
    private var fleetId:Int=0

    fun withFleedId(fleetId:Int):GetCollegueUseCase{
        this.fleetId = fleetId
        return this
    }

    override fun buildUseCaseObservable(): Observable<List<Colleague>> {
        return ticketRepository.getColleagues(fleetId)
    }

}