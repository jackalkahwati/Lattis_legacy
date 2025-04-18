package io.lattis.operator.presentation.map.locate

import android.os.Bundle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import io.lattis.domain.models.Location
import io.lattis.domain.models.User
import io.lattis.domain.models.Vehicle
import io.lattis.domain.usecase.vehicle.GetVehicleLocationUseCase
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.activity.ActivityPresenter
import io.lattis.operator.presentation.base.activity.location.BaseLocationActivityPresenter
import io.lattis.operator.presentation.map.uimodels.GoogleMapMarker
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.lattis.operator.utils.MarkerImageUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LocateVehicleActivityPresenter @Inject constructor(
    val getVehicleLocationUseCase: GetVehicleLocationUseCase
) : BaseLocationActivityPresenter<LocateVehicleActivityView>(){
    lateinit var location: Location
    lateinit var mapTitle:String
    lateinit var vehicle: Vehicle
    var repositionTimerDisposable: Disposable?=null
    val MILLISECONDS_REPOSITION_TIMER = 10000L
    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(VehicleDetailActivity.VEHICLE)) {
            val referencedFleetString = arguments.getString(VehicleDetailActivity.VEHICLE)
            vehicle = Gson().fromJson(referencedFleetString, Vehicle::class.java)
            location = Location(vehicle.latitude!!,vehicle.longitude!!)
            mapTitle = vehicle.name!!
            view?.startShowingMap()
        }
    }

    fun getVehiclePosition(){
        subscriptions.add(
                getVehicleLocationUseCase
                        .withVehicleId(vehicle.id!!)
                        .execute(object : RxObserver<Location>(view, false) {
                            override fun onNext(newLocation: Location) {
                                super.onNext(newLocation)
                                location = newLocation
                                view?.updateVehicleLocation()
                                view?.hideProgressLoading()
                            }
                            override fun onError(e: Throwable) {
                                super.onError(e)
                                view?.hideProgressLoading()
                            }
                        })
        )
    }

    fun getVehicleMarkerOptions():MarkerOptions{
        var markerOptions = MarkerOptions()
        markerOptions?.icon(
            MarkerImageUtil.getBikeResource(
                vehicle?.group?.type!!,
                vehicle?.batteryLevel
            )
        )
        markerOptions?.position(LatLng(location.latitude, location.longitude))
        markerOptions?.title(mapTitle)
        return markerOptions!!
    }

    fun getCurrentLocationMarkerOptions():MarkerOptions{
        var markerOptions = MarkerOptions()
        return markerOptions
                .position(LatLng(currentUserLocation?.latitude!!, currentUserLocation?.longitude!!))
                .icon(MarkerImageUtil.getLocationMarker())

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