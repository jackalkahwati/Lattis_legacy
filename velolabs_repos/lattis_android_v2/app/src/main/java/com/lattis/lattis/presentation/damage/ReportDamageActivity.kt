package com.lattis.lattis.presentation.damage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.jakewharton.rxbinding4.widget.TextViewTextChangeEvent
import com.jakewharton.rxbinding4.widget.textChangeEvents
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusActivity
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.utils.ImageCompressor
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_report_damage.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject


@RuntimePermissions
class ReportDamageActivity : BaseUserCurrentStatusActivity<ReportDamageActivityPresenter, ReportDamageActivityView>(),
    ReportDamageActivityView {


    private val REQUEST_CODE_ERROR = 4393
    private val REQUEST_CODE_DECISION = 4394
    private var categoryName: Array<String>?=null
    private var category: Array<String>?=null

    @Inject
    override lateinit var presenter: ReportDamageActivityPresenter
    override val activityLayoutId = R.layout.activity_report_damage
    override var view: ReportDamageActivityView = this

    companion object{
        val END_RIDE_AFTER_DAMAGE = "END_RIDE_AFTER_DAMAGE"
        val CANCEL_BIKE_BOOKING_AFTER_DAMAGE = "CANCEL_BIKE_BOOKING_AFTER_DAMAGE"
    }

    override fun configureViews() {
        super.configureViews()
        presenter.userCurrentStatus()
        configureClicks()
    }

    fun configureClicks(){
        btn_take_photo_in_report_damage.setOnClickListener {
            takePictureWithPermissionCheck()
        }

        btn_submit_in_report_damage.setOnClickListener {
            showProgressLoadingForReportDamage(getString(R.string.damage_report_loader))
            presenter.uploadImage()
        }

        iv_close_in_report_damage_details.setOnClickListener {
            finish()
        }
    }

    override fun configureSubscriptions() {
        super.configureSubscriptions()
        subscriptions.add(
            et_notes_value_in_report_damage.textChangeEvents()
            .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                presenter.setNotes(et_notes_value_in_report_damage.text.toString())
            }
        )
    }

    override fun activateSubmitBtn(state: Boolean) {
        if(state){
            btn_submit_in_report_damage.visibility = View.VISIBLE
            btn_take_photo_in_report_damage.visibility = View.GONE
        }else{
            btn_submit_in_report_damage.visibility = View.GONE
            btn_take_photo_in_report_damage.visibility = View.VISIBLE
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_TAKE_PHOTO && presenter.currentPhotoPath!=null) {
            val filePath = ImageCompressor.resizeAndCompressImageBeforeSend(this, presenter.currentPhotoPath, "damagereport")
            presenter.setImageLink(filePath)
            showPicture(filePath!!)
        } else if (requestCode == REQUEST_CODE_ERROR){

        }else if(requestCode == REQUEST_CODE_DECISION && resultCode == Activity.RESULT_OK) {
            if (data != null && data.hasExtra(PopUpActivity.POSITIVE_LEVEL) && data.getIntExtra(
                    PopUpActivity.POSITIVE_LEVEL, -1
                ) == 1
            ) {

                finish()

            } else if (data != null && data.hasExtra(PopUpActivity.POSITIVE_LEVEL) && data.getIntExtra(
                    PopUpActivity.POSITIVE_LEVEL, -1
                ) == 2
            ) {

                if(presenter.ride!=null && presenter.ride?.rideId!==0) {
                    var intent = Intent()
                    intent.putExtra(END_RIDE_AFTER_DAMAGE, true)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }else {
                    var intent = Intent()
                    intent.putExtra(CANCEL_BIKE_BOOKING_AFTER_DAMAGE, true)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }

            }
        }
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


    fun showPicture(filePath:String){
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.bike_default)
        requestOptions.error(R.drawable.bike_default)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(this)
            .load(filePath)
            .apply(requestOptions)
            .into(iv_damage_photo_preview_in_report_damage)
    }

    override fun onUploadImageSuccess() {
        showProgressLoadingForReportDamage(getString(R.string.damage_report_loader))
    }

    override fun showViewWith(ride: Boolean) {
        if(ride){
            PopUpActivity.launchForResult(
                this,
                REQUEST_CODE_DECISION,
                getString(R.string.damage_report_success_title),
                getString(R.string.damage_report_success_message),
                null,
                getString(R.string.damage_report_success_continue_ride),
                getString(R.string.end_ride),
                null,
                null
            )
        }else{
            PopUpActivity.launchForResult(
                this,
                REQUEST_CODE_DECISION,
                getString(R.string.damage_report_success_title),
                getString(R.string.damage_report_success_message),
                null,
                getString(R.string.damage_report_success_continue_ride),
                getString(R.string.damage_report_success_cancel_booking),
                null,
                null
            )
        }
    }


    override fun onReportDamageFailure() {
        hideProgressLoadingForReportDamage()
        showServerGeneralError(REQUEST_CODE_ERROR)
    }

    override fun onUserCurrentStatusSuccess() {
        presenter.checkOnStatus()
    }

    override fun onUserCurrentStatusFailure() {

    }

    fun showProgressLoadingForReportDamage(message:String){
        report_damage_loading.visibility= View.VISIBLE
        report_damage_loading.ct_loading_title.text = message
    }

    fun hideProgressLoadingForReportDamage(){
        report_damage_loading.visibility= View.GONE
    }

    override fun onUploadImageFailure() {
        hideProgressLoadingForReportDamage()
        showServerGeneralError(REQUEST_CODE_ERROR)
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {
    }
}