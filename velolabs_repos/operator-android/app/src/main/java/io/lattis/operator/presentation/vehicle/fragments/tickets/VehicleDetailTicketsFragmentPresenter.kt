package io.lattis.operator.presentation.vehicle.fragments.tickets

import android.os.Bundle
import com.google.gson.Gson
import io.lattis.domain.models.*
import io.lattis.domain.usecase.ticket.GetCollegueUseCase
import io.lattis.domain.usecase.ticket.GetTicketsUseCase
import io.lattis.domain.usecase.user.GetMeUseCase
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.fragment.FragmentPresenter
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import javax.inject.Inject

class VehicleDetailTicketsFragmentPresenter @Inject constructor(
        val getTicketsUseCase: GetTicketsUseCase,
        val getCollegueUseCase: GetCollegueUseCase,
        val getMeUseCase: GetMeUseCase
): FragmentPresenter<VehicleDetailTicketsFragmentView>() {

    lateinit var vehicle: Vehicle
    var tickets:List<Ticket>?=null
    var colleagues:List<Colleague>?=null
    var user:User.Operator?=null
    var filteredColleague: Colleague?=null

    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(VehicleDetailActivity.VEHICLE)) {
            val referencedFleetString = arguments.getString(VehicleDetailActivity.VEHICLE)
            vehicle = Gson().fromJson(referencedFleetString, Vehicle::class.java)
            getTickets()
            getColleagues()
            getMe()
        }
    }

    fun getTickets() {
        subscriptions.add(
                getTicketsUseCase
                        .withFleedId(vehicle?.fleet?.id!!)
                        .withVehicleId(vehicle?.id!!)
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
                        .withFleedId(vehicle?.fleet?.id!!)
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

    fun getMe() {
        subscriptions.add(
                getMeUseCase
                        .execute(object : RxObserver<User.Operator>(view, false) {
                            override fun onNext(newUser: User.Operator) {
                                super.onNext(newUser)
                                user = newUser

                            }
                            override fun onError(e: Throwable) {
                                super.onError(e)
                            }
                        })
        )
    }
}