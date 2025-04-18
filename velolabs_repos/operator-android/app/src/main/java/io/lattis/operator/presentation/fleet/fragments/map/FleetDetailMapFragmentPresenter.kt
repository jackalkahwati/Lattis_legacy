package io.lattis.operator.presentation.fleet.fragments.map

import android.os.Bundle
import android.text.TextUtils
import com.google.gson.Gson
import io.lattis.domain.models.Fleet
import io.lattis.domain.models.Location
import io.lattis.domain.models.Vehicle
import io.lattis.domain.usecase.vehicle.GetVehiclesInBboxUseCase
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.fragment.FragmentPresenter
import io.lattis.operator.presentation.fleet.FleetDetailActivity
import io.lattis.operator.presentation.map.uimodels.GoogleMapMarker
import io.operator.lattis.presentation.base.fragment.location.BaseLocationFragmentPresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FleetDetailMapFragmentPresenter @Inject constructor(
    val getVehiclesInBboxUseCase: GetVehiclesInBboxUseCase
):BaseLocationFragmentPresenter<FleetDetailMapFragmentView>(){

    var markers : ArrayList<GoogleMapMarker> = ArrayList()
    lateinit var fleet: Fleet
    var vehicles:List<Vehicle>?=null
    private var getVehiclesInBboxDisposable: Disposable? = null

    var name:String?=null

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

    var selectedVehicle:Vehicle?=null

    var repositionTimerDisposable:Disposable?=null
    val MILLISECONDS_REPOSITION_TIMER = 10000L

    var batteryPercentageFiltered = false
    var batteryPercentage:Int?=null

    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(FleetDetailActivity.FLEET)) {
            val referencedFleetString = arguments.getString(FleetDetailActivity.FLEET)
            fleet = Gson().fromJson(referencedFleetString, Fleet::class.java)
        }
    }

    fun getMarkers():Collection<GoogleMapMarker>{
        return markers
    }

    fun createMarkers(){
        markers.clear()
        for(vehicle in vehicles!!){
            markers.add(GoogleMapMarker(vehicle.latitude!!,vehicle.longitude!!,vehicle))
        }
        view?.showMarkers()
    }


    fun getVehiclesInBbox(sw: Location, ne: Location){
        getVehiclesInBboxDisposable?.dispose()
        subscriptions.add(
            getVehiclesInBboxUseCase
                .withFleedId(fleet.id!!)
                .withNE(ne)
                .withSW(sw)
                .withUsage(getVehicleFiltersForUsage())
//                .withMaintenance(getVehicleFiltersForMaintenance())
                .withName(if(TextUtils.isEmpty(name))null else name)
                .withBatteryLevel(if(batteryPercentageFiltered)batteryPercentage else null)
                .execute(object : RxObserver<List<Vehicle>>(view, false) {
                    override fun onNext(newVehicles:List<Vehicle>) {
                        super.onNext(newVehicles)
                        vehicles = newVehicles
                        createMarkers()
                        restartRepositionTimer()
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onVehiclesInBboxFailure()
                    }
                })
                .also {
                    getVehiclesInBboxDisposable = it
                }
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

    fun getVehicleFiltersForMaintenance():String?{
        return if(maintenance_low_battery)
            "low-battery"
        else
            null
    }


    fun startRepositionTimer(){
        repositionTimerDisposable = Observable.timer(MILLISECONDS_REPOSITION_TIMER, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view?.onRepositionTimerOver()
                }, {
                })
    }

    fun cancelRepositionTimer(){
        repositionTimerDisposable?.dispose()
        repositionTimerDisposable=null
    }

    fun restartRepositionTimer(){
        view?.onRepositionTimerStart()
        cancelRepositionTimer()
        startRepositionTimer()
    }





}