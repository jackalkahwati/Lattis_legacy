package io.lattis.operator.presentation.ticket

import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.lattis.domain.models.*
import io.lattis.domain.usecase.ticket.CreateTicketUseCase
import io.lattis.domain.usecase.ticket.GetCollegueUseCase
import io.lattis.domain.usecase.user.GetMeUseCase
import io.lattis.domain.usecase.vehicle.GetVehiclesUseCase
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.activity.ActivityPresenter
import io.lattis.operator.presentation.ticket.CreateTicketActivity.Companion.COLLEAGUES
import io.lattis.operator.presentation.ticket.CreateTicketActivity.Companion.FLEET_ID
import io.lattis.operator.presentation.ticket.CreateTicketActivity.Companion.OPERATOR
import io.lattis.operator.presentation.ticket.CreateTicketActivity.Companion.VEHICLE
import javax.inject.Inject

class CreateTicketActivityPresenter @Inject constructor(
        val getCollegueUseCase: GetCollegueUseCase,
        val getMeUseCase: GetMeUseCase,
        val createTicketUseCase: CreateTicketUseCase,
        val getVehiclesUseCase: GetVehiclesUseCase
) : ActivityPresenter<CreateTicketActivityView>(){

    var searchPage = 1
    var searchPer = 200
    var name:String?=null
    var vehicles:List<Vehicle>?=null


    var vehicle: Vehicle?=null
    var colleagues:List<Colleague>?=null
    var user: User.Operator?=null
    var fleet_id:Int?=null
    var categoryId:String?=null
    var assigneeId:Int?=null
    var notes:String?=null
    var ticket:Ticket?=null

    override fun setup(arguments: Bundle?) {
        super.setup(arguments)

        if (arguments != null && arguments.containsKey(FLEET_ID)) {
            fleet_id = arguments.getInt(FLEET_ID)
        }

        if (arguments != null && arguments.containsKey(VEHICLE)) {
            val referencedVehicleString = arguments.getString(VEHICLE)
            vehicle = Gson().fromJson(referencedVehicleString, Vehicle::class.java)
        }

        if (arguments != null && arguments.containsKey(OPERATOR)) {
            val referencedOperatorString = arguments.getString(OPERATOR)
            user = Gson().fromJson(referencedOperatorString, User.Operator::class.java)
        }

        if (arguments != null && arguments.containsKey(COLLEAGUES)) {
            val referencedColleaguesString = arguments.getString(COLLEAGUES)
            colleagues = Gson().fromJson(referencedColleaguesString, object : TypeToken<List<Colleague>>() {}.getType())
        }

        if(user==null){
            getMe()
        }

        if(colleagues==null){
            getColleagues()
        }

        if(vehicle!=null){
            view?.showVehicleInfo()
        }
    }


    fun getColleagues(){
        subscriptions.add(
                getCollegueUseCase
                        .withFleedId(fleet_id!!)
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

    fun tryCreatingTicket(){
        if(user==null || vehicle==null || categoryId ==null){
            view?.onCreateTicketValidationFailure()
            return
        }

        subscriptions.add(
                createTicketUseCase
                        .withTicketPost(TicketPost(fleet_id,vehicle?.id!!,assigneeId,notes,user?.id!!.toIntOrNull(),categoryId))
                        .execute(object : RxObserver<Ticket>(view, false) {
                            override fun onNext(newTicket: Ticket) {
                                super.onNext(newTicket)
                                ticket = newTicket
                                view?.onCreateTicketSuccess()

                            }
                            override fun onError(e: Throwable) {
                                super.onError(e)
                                view?.onCreateTicketFailure()
                            }
                        })
        )
    }

    fun searchVehicles(){
        subscriptions.add(
                getVehiclesUseCase
                        .withFleedId(fleet_id!!)
                        .withPage(searchPage)
                        .withPer(searchPer)
                        .withName(name)
                        .execute(object : RxObserver<List<Vehicle>>(view, false) {
                            override fun onNext(newVehicles:List<Vehicle>) {
                                super.onNext(newVehicles)
                                vehicles = newVehicles
                                view?.onVehiclesSearchSuccess()
                            }
                            override fun onError(e: Throwable) {
                                super.onError(e)
                                view?.onVehiclesSearchFailure()
                            }
                        })
        )
    }

}