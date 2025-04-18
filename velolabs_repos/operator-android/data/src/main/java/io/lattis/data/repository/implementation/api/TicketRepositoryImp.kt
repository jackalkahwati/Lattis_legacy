package io.lattis.data.repository.implementation.api

import io.lattis.data.entity.body.ticket.UpdateTicket
import io.lattis.data.net.ticket.TicketApiClient
import io.lattis.domain.models.Colleague
import io.lattis.domain.models.Ticket
import io.lattis.domain.models.TicketPost
import io.lattis.domain.repository.TicketRepository
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Query
import javax.inject.Inject

class TicketRepositoryImp @Inject constructor(
    val ticketApiClient: TicketApiClient
) : TicketRepository{

    override fun getTickets(fleetId:Int,vehicle_id:Int?,assignee:Int?): Observable<List<Ticket>> {
        return ticketApiClient.api.getTickets(fleetId,vehicle_id,assignee)
    }

    override fun getColleagues(fleet_id:Int): Observable<List<Colleague>>{
        return ticketApiClient.api.getColleagues(fleet_id)
    }

    override fun createTicket(ticketPost: TicketPost): Observable<Ticket>{
        return ticketApiClient.api.createTicket(ticketPost)
    }

    override fun upadateTicket(ticketId:Int,assignee:Int?,notes:String?,status:String?): Observable<Ticket>{
        return ticketApiClient.api.upadateTicket(ticketId, UpdateTicket(assignee,notes,status))
    }
}