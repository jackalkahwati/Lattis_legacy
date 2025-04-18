package io.lattis.data.repository.datasources.api

import io.lattis.data.entity.body.ticket.UpdateTicket
import io.lattis.data.entity.response.base.BasicResponse
import io.lattis.domain.models.Colleague
import io.lattis.domain.models.Fleet
import io.lattis.domain.models.Ticket
import io.lattis.domain.models.TicketPost
import io.reactivex.Observable
import retrofit2.http.*

interface TicketApi {
    @GET("operator/tickets")
    fun getTickets(@Query("fleet_id")fleet_id:Int,
                   @Query("vehicle_id")vehicle_id:Int?,
                   @Query("assignee")assignee:Int?): Observable<List<Ticket>>

    @GET("operator/colleagues")
    fun getColleagues(@Query("fleet_id")fleet_id:Int): Observable<List<Colleague>>

    @POST("operator/tickets")
    fun createTicket(@Body ticketPost: TicketPost): Observable<Ticket>

    @PATCH("operator/tickets/{ticket_id}")
    fun upadateTicket(@Path("ticket_id") ticket_id:Int,@Body updateTicket: UpdateTicket): Observable<Ticket>
}