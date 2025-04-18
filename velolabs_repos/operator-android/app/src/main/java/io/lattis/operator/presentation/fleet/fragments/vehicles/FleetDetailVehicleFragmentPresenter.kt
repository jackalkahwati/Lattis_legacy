package io.lattis.operator.presentation.fleet.fragments.vehicles

import android.os.Bundle
import android.text.TextUtils
import com.google.gson.Gson
import io.lattis.domain.models.Fleet
import io.lattis.domain.models.Ticket
import io.lattis.domain.models.Vehicle
import io.lattis.domain.usecase.vehicle.GetVehiclesUseCase
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.fragment.FragmentPresenter
import io.lattis.operator.presentation.fleet.FleetDetailActivity
import javax.inject.Inject

class FleetDetailVehicleFragmentPresenter @Inject constructor(
    val getVehiclesUseCase: GetVehiclesUseCase
): FragmentPresenter<FleetDetailVehicleFragmentView>() {

    var vehicles:List<Vehicle>?=null
    var originalVehicles:List<Vehicle>?=null

    var fleet:Fleet?=null
    var page = 1
    var per = 200
    var searchPage = 1
    var searchPer = 200
    var batteryPercentageFiltered = false
    var batteryPercentage:Int?=null


    var name: String?=null

    var live_in_ride : Boolean = false
    var live_parked : Boolean = false
    var live_reserved : Boolean = false
    var live_in_collect : Boolean = false


    var staging_equipement_assigned : Boolean = false
    var staging_equipement_unassigned : Boolean = false
    var staging_balancing : Boolean = false

    var out_of_service_damaged : Boolean = false
    var out_of_service_maintenance : Boolean = false
    var out_of_service_stolen : Boolean = false
    var out_of_service_transport : Boolean = false

    var maintenance_low_battery : Boolean = false


    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(FleetDetailActivity.FLEET)) {
            val referencedFleetString = arguments.getString(FleetDetailActivity.FLEET)
            fleet = Gson().fromJson(referencedFleetString, Fleet::class.java)
        }
    }

    fun getVehicles() {
        subscriptions.add(
            getVehiclesUseCase
                .withFleedId(fleet?.id!!)
                .withPage(page)
                .withPer(per)
                .withBatteryLevel(if(batteryPercentageFiltered)batteryPercentage else null)
                .execute(object : RxObserver<List<Vehicle>>(view, false) {
                    override fun onNext(newVehicles:List<Vehicle>) {
                        super.onNext(newVehicles)
                        vehicles = newVehicles
                        originalVehicles = vehicles
                        view?.onVehiclesSuccess()
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onVehiclesFailure()

                    }
                })
        )
    }

    fun searchVehicles(){
        subscriptions.add(
            getVehiclesUseCase
                .withFleedId(fleet?.id!!)
                .withPage(searchPage)
                .withPer(searchPer)
                .withUsage(getVehicleFiltersForUsage())
//                .withMaintenance(getVehicleFiltersForMaintenance())
                .withName(if(TextUtils.isEmpty(name)) null else name)
                .withBatteryLevel(if(batteryPercentageFiltered)batteryPercentage else null)
                .execute(object : RxObserver<List<Vehicle>>(view, false) {
                    override fun onNext(newVehicles:List<Vehicle>) {
                        super.onNext(newVehicles)
                        vehicles = newVehicles
                        view?.onVehiclesSuccess()
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onVehiclesFailure()
                    }
                })
        )
    }

    fun getVehicleFiltersForUsage():String?{
        val usage = buildString {
            append(if(live_in_ride)",on_trip" else "")
            append(if(live_parked)",parked" else "")
            append(if(live_reserved)",reserved" else "")
            append(if(live_in_collect)",collect" else "")


            append(if(staging_equipement_assigned)",controller_assigned,lock_assigned" else "")
            append(if(staging_equipement_unassigned)",lock_not_assigned" else "")
            append(if(staging_balancing)",balancing" else "")

            append(if(out_of_service_damaged)",damaged" else "")
            append(if(out_of_service_maintenance)",under_maintenance" else "")
            append(if(out_of_service_stolen)",reported_stolen" else "")
            append(if(out_of_service_transport)",transport" else "")

        }

        if(usage.length>0)
            return usage.drop(1)
        else
            return null
    }

    fun showListingVehicles(){
        name = null
        if(originalVehicles!=null) {
            vehicles = originalVehicles
            view?.onVehiclesSuccess()
        }
    }
}