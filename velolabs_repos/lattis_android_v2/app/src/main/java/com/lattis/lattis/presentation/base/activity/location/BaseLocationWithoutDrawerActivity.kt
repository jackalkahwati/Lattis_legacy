package com.lattis.lattis.presentation.base.activity.location

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import com.google.android.gms.common.api.ResolvableApiException
import com.lattis.domain.models.LocationSettingsResult
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import permissions.dispatcher.*
import io.lattis.lattis.R

@RuntimePermissions
abstract class BaseLocationWithoutDrawerActivity<Presenter : BaseLocationActivityPresenter<V>,V:BaseLocationActivityView> :
    BaseActivity<Presenter, V>(),BaseLocationActivityView{


    private val REQUEST_LOCATION_SETTINGS_REQUIRED = 1024
    private val REQUEST_CODE_FOR_LOCATION_SETTINGS_AFTER_DENY = 2015
    private val REQUEST_CODE_FOR_LOCATION_AFTER_DENY = 2016

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun configureViews() {
        super.configureViews()
    }


    fun fetchLocation(){
        presenter.getLocation()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_LOCATION_SETTINGS_REQUIRED) {
            if (resultCode == Activity.RESULT_OK) {
                getLocationWithPermissionCheck()
            } else {
                showLocationRequiredPop(
                    REQUEST_CODE_FOR_LOCATION_SETTINGS_AFTER_DENY
                )
            }
        }else if(requestCode == REQUEST_CODE_FOR_LOCATION_SETTINGS_AFTER_DENY){
            presenter.getLocation()
        }else if(requestCode == REQUEST_CODE_FOR_LOCATION_AFTER_DENY){
            getLocationWithPermissionCheck()
        }
    }


    //// Location settings : start ////
    override fun onLocationSettingsPermissionRequired(locationSettingsResult: LocationSettingsResult) {
        try {
            val resolvable =
                locationSettingsResult.apiException as ResolvableApiException
            resolvable.startResolutionForResult(
                this,
                REQUEST_LOCATION_SETTINGS_REQUIRED
            )
        } catch (e: IntentSender.SendIntentException) { // Ignore the error.
            onLocationSettingsNotAvailable()
        }
    }


    override fun onLocationSettingsON() {
        getLocationWithPermissionCheck()
    }

    override fun onLocationSettingsNotAvailable() {
    }
    //// Location settings : end ////


    //// Location : start ///
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getLocation() {
        onLocationPermissionsAvailable()
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    open fun showRationaleForLocation(request: PermissionRequest) {
        request.proceed()
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationDenied() {
        showLocationRequiredPop(REQUEST_CODE_FOR_LOCATION_AFTER_DENY)
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationNeverAskAgain() {
        onLocationPermissionsDenied()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated function
        onRequestPermissionsResult(requestCode, grantResults)
    }

    abstract fun onLocationPermissionsAvailable()
    abstract fun onLocationPermissionsDenied()


    fun requestLocationUpdates(){
        presenter.requestLocationUpdates()
    }

    fun requestStopLocationUpdates(){
        presenter.requestStopLocationUpdates()
    }


    //// Location : end ///
}

