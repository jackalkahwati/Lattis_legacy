package io.lattis.operator.presentation.fleet.fragments.tickets

import android.os.Bundle
import com.google.gson.Gson
import io.lattis.domain.models.Colleague
import io.lattis.domain.models.Fleet
import io.lattis.domain.models.Ticket
import io.lattis.domain.usecase.ticket.GetCollegueUseCase
import io.lattis.domain.usecase.ticket.GetTicketsUseCase
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.fragment.FragmentPresenter
import io.lattis.operator.presentation.fleet.FleetDetailActivity
import javax.inject.Inject

class FleetDetailTicketFragmentPresenter @Inject constructor(
    val getTicketsUseCase: GetTicketsUseCase,
    val getCollegueUseCase: GetCollegueUseCase
) : FragmentPresenter<FleetDetailTicketFragmentView>(){

    var tickets:List<Ticket>?=null
    var fleet:Fleet?=null
    var colleagues:List<Colleague>?=null
    var filteredColleague: Colleague?=null

    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(FleetDetailActivity.FLEET)) {
            val referencedFleetString = arguments.getString(FleetDetailActivity.FLEET)
            fleet = Gson().fromJson(referencedFleetString, Fleet::class.java)
        }
        getColleagues()
    }

    fun getTickets() {
        subscriptions.add(
            getTicketsUseCase
                .withFleedId(fleet?.id!!)
                .withAssigneeId(if(filteredColleague==null)null else filteredColleague?.id)
                .execute(object : RxObserver<List<Ticket>>(view, false) {
                    override fun onNext(newTickets:List<Ticket>) {
                        super.onNext(newTickets)
                        tickets = newTickets
                        view?.onTicketsSuccess()
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onTicketFailure()

                    }
                })
        )
    }

    fun getColleagues(){
        subscriptions.add(
            getCollegueUseCase
                .withFleedId(fleet?.id!!)
                .execute(object : RxObserver<List<Colleague>>(view, false) {
                    override fun onNext(newColleagues:List<Colleague>) {
                        super.onNext(newColleagues)
                        colleagues = newColleagues
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)

                    }
                })
        )
    }
}