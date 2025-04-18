package io.lattis.domain.usecase.ticket

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.Ticket
import io.lattis.domain.repository.TicketRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class UpdateTicketUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val ticketRepository: TicketRepository
) : UseCase<Ticket>(threadExecutor, postExecutionThread) {
    private var ticketId:Int=0
    private var assignee:Int?=null
    private var notes:String?=null
    private var status:String?=null

    fun withTicketId(ticketId:Int):UpdateTicketUseCase{
        this.ticketId = ticketId
        return this
    }

    fun withAssigneeId(assignee:Int?):UpdateTicketUseCase{
        this.assignee = assignee
        return this
    }

    fun withNotes(notes:String?):UpdateTicketUseCase{
        this.notes = notes
        return this
    }

    fun withStatus(status:String?):UpdateTicketUseCase{
        this.status = status
        return this
    }

    override fun buildUseCaseObservable(): Observable<Ticket> {
        return ticketRepository.upadateTicket(ticketId,assignee,notes,status)
    }

}