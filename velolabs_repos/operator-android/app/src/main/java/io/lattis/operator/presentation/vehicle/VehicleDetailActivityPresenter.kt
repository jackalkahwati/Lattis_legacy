package io.lattis.operator.presentation.vehicle

import android.os.Bundle
import com.google.gson.Gson
import io.lattis.domain.models.Ticket
import io.lattis.domain.models.Vehicle
import io.lattis.operator.presentation.base.activity.ActivityPresenter
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity.Companion.TICKET
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity.Companion.VEHICLE
import javax.inject.Inject

class VehicleDetailActivityPresenter @Inject constructor(

):ActivityPresenter<VehicleDetailActivityView>() {


    lateinit var vehicle: Vehicle
    var ticket:Ticket?=null

    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(VEHICLE)) {
            val referencedFleetString = arguments.getString(VEHICLE)
            vehicle = Gson().fromJson(referencedFleetString, Vehicle::class.java)
            view?.setTitle(vehicle?.name!!)
        }

        if (arguments != null && arguments.containsKey(TICKET)) {
            val referencedTicketString = arguments.getString(TICKET)
            ticket = Gson().fromJson(referencedTicketString, Ticket::class.java)
        }

    }
}