package io.lattis.domain.usecase.ticket

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.Ticket
import io.lattis.domain.models.TicketPost
import io.lattis.domain.repository.TicketRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class CreateTicketUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val ticketRepository: TicketRepository
) : UseCase<Ticket>(threadExecutor, postExecutionThread) {
    private var ticketPost:TicketPost?=null

    fun withTicketPost(ticketPost:TicketPost):CreateTicketUseCase{
        this.ticketPost = ticketPost
        return this
    }

    override fun buildUseCaseObservable(): Observable<Ticket> {
        return ticketRepository.createTicket(ticketPost!!)
    }

}