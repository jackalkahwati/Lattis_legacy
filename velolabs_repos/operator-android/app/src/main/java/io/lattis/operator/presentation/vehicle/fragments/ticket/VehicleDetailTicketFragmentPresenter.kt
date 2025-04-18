package io.lattis.operator.presentation.vehicle.fragments.ticket

import android.os.Bundle
import android.text.TextUtils
import com.google.gson.Gson
import io.lattis.domain.models.Colleague
import io.lattis.domain.models.ThingStatus
import io.lattis.domain.models.Ticket
import io.lattis.domain.models.Vehicle
import io.lattis.domain.usecase.ticket.GetCollegueUseCase
import io.lattis.domain.usecase.ticket.GetTicketsUseCase
import io.lattis.domain.usecase.ticket.UpdateTicketUseCase
import io.lattis.domain.usecase.user.GetMeUseCase
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.fragment.FragmentPresenter
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.lattis.operator.presentation.vehicle.fragments.tickets.VehicleDetailTicketsFragmentView
import javax.inject.Inject

class VehicleDetailTicketFragmentPresenter @Inject constructor(
    val getMeUseCase: GetMeUseCase,
    val getCollegueUseCase: GetCollegueUseCase,
    val updateTicketUseCase: UpdateTicketUseCase
): FragmentPresenter<VehicleDetailTicketFragmentView>() {


    lateinit var vehicle: Vehicle
    lateinit var ticket: Ticket
    var colleagues:List<Colleague>?=null
    var assigneeColleague:Colleague?=null

    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(VehicleDetailActivity.VEHICLE)) {
            val referencedString = arguments.getString(VehicleDetailActivity.VEHICLE)
            vehicle = Gson().fromJson(referencedString, Vehicle::class.java)
        }

        if (arguments != null && arguments.containsKey(VehicleDetailActivity.TICKET)) {
            val referencedString = arguments.getString(VehicleDetailActivity.TICKET)
            ticket = Gson().fromJson(referencedString, Ticket::class.java)
            view?.startShowingTicketInformation()
            getColleagues()
        }
    }

    fun getColleagues(){
        subscriptions.add(
            getCollegueUseCase
                .withFleedId(ticket?.fleetId!!)
                .execute(object : RxObserver<List<Colleague>>(view, false) {
                    override fun onNext(newColleagues:List<Colleague>) {
                        super.onNext(newColleagues)
                        colleagues = newColleagues
                        setAssigneeColleague()

                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)

                    }
                })
        )
    }

    fun setAssigneeColleague(){
        if(colleagues!=null && colleagues?.size!!>0){
            for(colleague in  colleagues!!){
                if(colleague.id == ticket.assignee){
                    assigneeColleague = colleague
                    view?.showAssignee()
                }
            }
        }
    }


    fun setAssigneeOrNotesOrResolveTicket(assignee:Int?,notes:String?,resolveStatus:String?){
        subscriptions.add(
            updateTicketUseCase
                .withTicketId(ticket?.id!!)
                .withAssigneeId(assignee)
                .withNotes(notes)
                .withStatus(resolveStatus)
                .execute(object : RxObserver<Ticket>(view, false) {
                    override fun onNext(newTicket:Ticket) {
                        super.onNext(newTicket)
                        ticket = newTicket
                        setAssigneeColleague()
                        if(TextUtils.isEmpty(resolveStatus))view?.startShowingTicketInformation() else view?.onTicketResolved()
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)

                    }
                })
        )
    }

}