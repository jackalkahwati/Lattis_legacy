package io.lattis.domain.repository

import io.lattis.domain.models.Colleague
import io.lattis.domain.models.Ticket
import io.lattis.domain.models.TicketPost
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Query

interface TicketRepository {
    fun getTickets(fleetId:Int,vehicle_id:Int?,assignee:Int?): Observable<List<Ticket>>
    fun getColleagues(fleet_id:Int): Observable<List<Colleague>>
    fun createTicket(ticketPost: TicketPost): Observable<Ticket>
    fun upadateTicket(ticketId:Int,assignee:Int?,notes:String?,status:String?): Observable<Ticket>
}