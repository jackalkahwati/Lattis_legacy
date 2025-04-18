package io.lattis.domain.usecase.ticket

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.Ticket
import io.lattis.domain.repository.TicketRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetTicketsUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val ticketRepository: TicketRepository
) : UseCase<List<Ticket>>(threadExecutor, postExecutionThread) {
    private var fleetId:Int=0
    private var vehicle_id:Int?=null
    private var assignee:Int?=null

    fun withFleedId(fleetId:Int):GetTicketsUseCase{
        this.fleetId = fleetId
        return this
    }

    fun withVehicleId(vehicle_id:Int):GetTicketsUseCase{
        this.vehicle_id = vehicle_id
        return this
    }

    fun withAssigneeId(assignee:Int?):GetTicketsUseCase{
        this.assignee = assignee
        return this
    }

    override fun buildUseCaseObservable(): Observable<List<Ticket>> {
        return ticketRepository.getTickets(fleetId,vehicle_id,assignee)
    }

}