package com.lattis.lattis.presentation.ride

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import com.lattis.domain.models.Location
import com.lattis.lattis.presentation.base.activity.location.BaseLocationWithoutDrawerActivity
import com.lattis.lattis.presentation.bikelist.BikeListFragment
import com.lattis.lattis.presentation.payment.add.AddPaymentCardActivity
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.ride.BikeBookedOrActiveRideFragment.Companion.RIDE_SUMMARY_DATA
import com.lattis.lattis.presentation.utils.ImageCompressor
import io.lattis.lattis.R
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_end_ride_checklist.*
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.content_end_ride_checklist.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RuntimePermissions
class EndRideActivity : BaseLocationWithoutDrawerActivity<EndRidePresenter, EndRideView>(),EndRideView{

    override val activityLayoutId = R.layout.activity_end_ride_checklist;
    override var view: EndRideView = this


    private val REQUEST_SERVER_ERROR_END_RIDE = 492
    private val REQUEST_SERVER_ERROR_UPLOAD_IMAGE = 493
    private val REQUEST_CREDIT_CARD_MISSING = 494
    private val REQUEST_CREDIT_ADD_CARD = 495
    private val REQUEST_FAILED_TO_DISPATCH_TAKE_PHOTO_INTENT = 496
    private val REQUEST_ENFORCE_PARKING_FAILURE = 497
    @Inject
    override lateinit var presenter: EndRidePresenter

    companion object{
        fun getIntent(context: Context,rideId:Int,currentUserLocation: Location?,lock_battery:Int?,parking:Int?,isForceEndRide:Boolean):Intent{
            val intent = Intent(context, EndRideActivity::class.java)
            intent.putExtra(EndRidePresenter.TRIP_ID, rideId)
            if (currentUserLocation != null) {
                val lat: Double = currentUserLocation?.latitude!!
                val longitude: Double = currentUserLocation?.longitude!!
                intent.putExtra(EndRidePresenter.LONGITUDE_END_RIDE_ID, longitude)
                intent.putExtra(EndRidePresenter.LATITUDE_END_RIDE_ID, lat)
            }
            if (lock_battery != null)
                intent.putExtra(
                    EndRidePresenter.LOCK_BATTERY,
                    lock_battery!!
                )
//        if (parking != null) {
//            intent.putExtra(
//                EndRidePresenter.PARKING_END_RIDE_ID,
//                parking.getParking_spot_id()
//            )
//        }
            if (isForceEndRide) {
                intent.putExtra(EndRidePresenter.FORCE_END_RIDE_ID, isForceEndRide)
            }

            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchLocation()
        cl_end_ride_take_photo.setOnClickListener {
            takePictureWithPermissionCheck()
        }
        iv_end_ride_cancel.setOnClickListener {
            finish()
        }
    }

    override fun handleUIForceEndRide() {
        showProgressLoading(getString(R.string.end_ride_loader))
    }

    override fun handleUINotForceEndRide() {
        content_end_ride_checklist.visibility = View.VISIBLE
        hideProgressLoading()
    }

    override fun onLocationPermissionsAvailable() {
        presenter.checkForEndRide()
    }

    override fun onLocationPermissionsDenied() {

    }

    override fun setUserPosition(location: Location) {
        presenter.requestStopLocationUpdates()
        presenter.checkForEndRide()
    }

    @NeedsPermission(
        Manifest.permission.CAMERA
    )
    fun takePicture() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        dispatchTakePictureIntent()
    }

    override fun failedToDispatchTakePhotoIntent() {
        showServerGeneralError(REQUEST_FAILED_TO_DISPATCH_TAKE_PHOTO_INTENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_TAKE_PHOTO && presenter.currentPhotoPath!=null) {
            showProgressLoading(getString(R.string.end_ride_loader))
            Observable.timer(1000,TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribe({
                    val filePath = ImageCompressor.resizeAndCompressImageBeforeSend(this, presenter.currentPhotoPath, "endride")
                    presenter.uploadImage(if(filePath!=null) filePath!! else presenter.currentPhotoPath!!)
                },{
                    hideProgressLoading()
                })
        }else if(requestCode == REQUEST_SERVER_ERROR_END_RIDE){
            finish()
        }else if(requestCode == REQUEST_CREDIT_CARD_MISSING && resultCode == Activity.RESULT_OK) {
            if (data != null && data.hasExtra(PopUpActivity.POSITIVE_LEVEL) && data.getIntExtra(
                    PopUpActivity.POSITIVE_LEVEL, -1
                ) == 1
            ) {
                AddPaymentCardActivity.launchForResult(this,
                    REQUEST_CREDIT_ADD_CARD,null)
            }else{
                finish()
            }
        }else if(requestCode== REQUEST_CREDIT_ADD_CARD && resultCode == Activity.RESULT_OK){
            showProgressLoading(getString(R.string.end_ride_loader))
            presenter.endRide()
        }else if(requestCode == REQUEST_ENFORCE_PARKING_FAILURE){
            finish()
        }
    }

    //// override for end ride and upload :start
    override fun onUploadImageSuccess() {
        fetchLocation()
    }

    override fun onEndTripSuccess() {
        finishWithSuccess()
    }

    override fun onActiveTripStopped() {
        finishWithSuccess()
    }

    override fun onUploadImageFailure() {
        hideProgressLoading()
        showServerGeneralError(REQUEST_SERVER_ERROR_UPLOAD_IMAGE)
    }

    override fun onEndTripFailure() {
        hideProgressLoading()
        showServerGeneralError(REQUEST_SERVER_ERROR_END_RIDE)
    }

    override fun onEndTripPaymentFailure() {
        hideProgressLoading()
       PopUpActivity.launchForResult(this,
           REQUEST_CREDIT_CARD_MISSING,
           getString(R.string.card_details_error),
           null,
           null,
           getString(R.string.add_credit_card),
           null,
           null,
           getString(R.string.cancel)
       )
    }

    override fun onEndTripStripeConnectFailure() {
        hideProgressLoading()
        showServerGeneralError(REQUEST_SERVER_ERROR_END_RIDE)
    }

    override fun onEndTripEnforeParkingFailure() {
        hideProgressLoading()
        PopUpActivity.launchForResult(this,
            REQUEST_ENFORCE_PARKING_FAILURE,
            getString(R.string.parking_restricted_warning_message),
            null,
            null,
            getString(R.string.add_credit_card),
            null,
            null,
            getString(R.string.cancel)
        )
    }

    private fun finishWithSuccess() {
        val intent = Intent()
        intent.putExtra(RIDE_SUMMARY_DATA,presenter.endRideSummary)
        setResult(RESULT_OK,intent)
        finish()
    }

    //// override for end ride :end


    fun showProgressLoading(message:String){
        end_ride_loading.visibility= View.VISIBLE
        end_ride_loading.ct_loading_title.text = message
    }

    fun hideProgressLoading(){
        end_ride_loading.visibility= View.GONE
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}