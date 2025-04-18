package io.lattis.operator.presentation.base.activity.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import com.google.android.gms.common.api.ResolvableApiException
import io.lattis.domain.models.LocationSettingsResult
import io.lattis.operator.R
import io.lattis.operator.presentation.ui.base.activity.BaseActivity
import permissions.dispatcher.*

@RuntimePermissions
abstract class BaseLocationActivity<Presenter : BaseLocationActivityPresenter<V>,V:BaseLocationActivityView> :BaseActivity<Presenter,V>(),BaseLocationActivityView{


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
        } catch (e: SendIntentException) { // Ignore the error.
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
    fun showRationaleForLocation(request: PermissionRequest) {
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

    @SuppressLint("NeedOnRequestPermissionsResult")
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

    fun requestFreshLocationUpdates(){
        presenter.requestLocationUpdates(true)
    }

    fun requestStopLocationUpdates(){
        presenter.requestStopLocationUpdates()
    }


    //// Location : end ///

    fun showLocationRequiredPop(requestCode:Int){
        launchPopUpActivity(
            requestCode,
            getString(R.string.location_access_hint),
            null,
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )
    }
}