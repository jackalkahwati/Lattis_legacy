package io.lattis.operator.presentation.base.activity.location


import com.google.android.gms.location.LocationSettingsStatusCodes
import io.lattis.domain.usecase.location.GetLocationSettingsUseCase
import io.lattis.domain.usecase.location.GetLocationUpdatesUseCase
import io.lattis.domain.models.Location
import io.lattis.domain.models.LocationSettingsResult
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.activity.ActivityPresenter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

open abstract class BaseLocationActivityPresenter<View:BaseLocationActivityView>: ActivityPresenter<View>(){

    @Inject
    lateinit var getLocationSettingsUseCase: GetLocationSettingsUseCase

    @Inject
    lateinit var getLocationUpdatesUseCase: GetLocationUpdatesUseCase
    private var locationSubscription: Disposable? = null
    private var locationSettingSubscription : Disposable?=null
    var currentUserLocation: Location? = null


    protected var getLocationSettingsSubscription = CompositeDisposable()

    protected open fun onLocationSettingsON() {

    }


    open fun getLocation() {
        cancelLocationSettingsSubscription()
        getLocationSettingsSubscription.add(
            getLocationSettingsUseCase
                .execute(object : RxObserver<LocationSettingsResult>(view) {
                    override fun onNext(locationSettingsResult: LocationSettingsResult) {
                        super.onNext(locationSettingsResult)
                        cancelLocationSettingsSubscription()
                        when (locationSettingsResult.status) {
                            LocationSettingsStatusCodes.SUCCESS ->  // All location settings are satisfied. The client can
// initialize location requests here.
                                if (view != null) {
                                    view.onLocationSettingsON()
                                    onLocationSettingsON()
                                }
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->  // Location settings are not satisfied, but this can be fixed
// by showing the user a dialog.
                                if (view != null)
                                    view.onLocationSettingsPermissionRequired(
                                    locationSettingsResult
                                )
                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->  // Location settings are not satisfied. However, we have no way
// to fix the settings so we won't show the dialog.
// ...
                                if (view != null)
                                    view.onLocationSettingsNotAvailable()
                        }
                    }

                     override fun onError(e: Throwable) {
                        super.onError(e)
                        if (view != null) view.onLocationSettingsNotAvailable()
                    }
                }).also { locationSettingSubscription = it }
        )
    }

     open fun cancelLocationSettingsSubscription() {
        if (getLocationSettingsSubscription != null) {
            getLocationSettingsSubscription.clear()
        }
         if(locationSettingSubscription!=null){
             locationSettingSubscription?.dispose()
             locationSettingSubscription=null
         }
    }


    open fun requestLocationUpdates(freshLocationData:Boolean=false) {
        requestStopLocationUpdates()
        locationSubscription = getLocationUpdatesUseCase
            .withFreshLocationData(freshLocationData)
            .execute(object : RxObserver<Location>() {
            override fun onNext(location: Location) {
                requestStopLocationUpdates()
                currentUserLocation = location
                if (view != null) view.setUserPosition(location)
            }
        })
    }

    open fun requestStopLocationUpdates() {
        if (locationSubscription != null) locationSubscription!!.dispose()
    }

}