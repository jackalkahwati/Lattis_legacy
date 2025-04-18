package io.lattis.operator.presentation.map.filter

import android.os.Bundle
import com.google.gson.Gson
import io.lattis.domain.models.Fleet
import io.lattis.domain.models.Location
import io.lattis.domain.models.Vehicle
import io.lattis.domain.usecase.vehicle.GetVehiclesInBboxUseCase
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.activity.location.BaseLocationActivityPresenter
import io.lattis.operator.presentation.fleet.FleetDetailActivity
import io.lattis.operator.presentation.map.uimodels.GoogleMapMarker
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class FilterVehiclesActivityPresenter @Inject constructor(
    val getVehiclesInBboxUseCase: GetVehiclesInBboxUseCase
): BaseLocationActivityPresenter<FilterVehiclesActivityView>() {

    var markers : ArrayList<GoogleMapMarker> = ArrayList()
    lateinit var fleet:Fleet
    var vehicles:List<Vehicle>?=null
    private var getVehiclesInBboxDisposable: Disposable? = null


    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(FleetDetailActivity.FLEET)) {
            val referencedFleetString = arguments.getString(FleetDetailActivity.FLEET)
            fleet = Gson().fromJson(referencedFleetString, Fleet::class.java)
            view?.setTitle(fleet?.name!!)
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


    fun getVehiclesInBbox(sw:Location,ne:Location){
        getVehiclesInBboxDisposable?.dispose()
        subscriptions.add(
                getVehiclesInBboxUseCase
                        .withFleedId(fleet.id!!)
                        .withNE(ne)
                        .withSW(sw)
                        .execute(object : RxObserver<List<Vehicle>>(view, false) {
                            override fun onNext(newVehicles:List<Vehicle>) {
                                super.onNext(newVehicles)
                                vehicles = newVehicles
                                createMarkers()
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

    fun mockMarkers():Collection<GoogleMapMarker>{
        var lat = 51.5145160
        var lng = -0.1270060

        // Add ten cluster items in close proximity, for purposes of this example.
        for (i in 0..9) {
            val offset = i / 60.0
            lat += offset
            lng += offset
            val offsetItem =
                    GoogleMapMarker(lat, lng, null)
            markers.add(offsetItem)
        }
        if(currentUserLocation!=null)
            markers.add(GoogleMapMarker(currentUserLocation?.latitude!!,currentUserLocation?.longitude!!,null,true))

        return markers
    }
}