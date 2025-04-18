package io.lattis.operator.presentation.qrcodescan

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import io.lattis.operator.R
import io.lattis.operator.presentation.ui.base.activity.BaseActivity
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.lattis.operator.utils.ResourceUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_scan_qr_code.*

import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RuntimePermissions
class ScanQRCodeActivity : BaseActivity<ScanQRCodeActivityPresenter, ScanQRCodeActivityView>(),
    ScanQRCodeActivityView, BarcodeCallback, ScanQRCodeVehicleListAdapterListener {

    @Inject
    override lateinit var presenter: ScanQRCodeActivityPresenter
    override val activityLayoutId = R.layout.activity_scan_qr_code
    override var view: ScanQRCodeActivityView = this
    private val REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY = 346


    companion object{
        val FLEET_ID = "FLEET_ID"
        val VEHICLE_DATA = "VEHICLE_DATA"

        fun getIntent(context: Context, fleet_id:Int):Intent{
            val intent = Intent(context, ScanQRCodeActivity::class.java)
            intent.putExtra(FLEET_ID, fleet_id)
            return intent
        }
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        decoratedBarcodeView.setStatusText("")
        requestCameraPermissionWithPermissionCheck()
    }

    override fun configureViews() {
        super.configureViews()
        rv_vehicles.setLayoutManager(LinearLayoutManager(this))
        rv_vehicles.setAdapter(
            ScanQRCodeVehicleListAdapter(
                this,
                this
            )
        )
        setTitle()
        ct_change_status.setPaintFlags(ct_change_status.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
        ct_change_status.setOnClickListener {
            showChangeStatusMainDialog(
                ResourceUtils.changeStatusList(
                    false,
                    this,
                    null
                )
            )
        }

        iv_close_in_qr_code.setOnClickListener {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated function
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun requestCameraPermission() {
        decoratedBarcodeView.decodeSingle(this)
    }

    override fun restartScanner() {
        Observable.timer(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ decoratedBarcodeView.decodeSingle(this) }) { }
    }

    override fun possibleResultPoints(list: List<ResultPoint>) {

    }

    override fun barcodeResult(barcodeResult: BarcodeResult) {
        presenter.processQRCode(barcodeResult.text)
    }

    override fun onQRCodeVehicleSuccess() {
        ct_change_status.visibility = View.VISIBLE
        setTitle()
        updateVehiclesList()
        restartScanner()
    }

    override fun onQRCodeVehicleFailure() {
        restartScanner()
    }

    override fun onVehicleClicked(position: Int) {
        showVehicleDetails(position)
    }

    private fun showVehicleDetails(position:Int) {
        showProgressLoading()
        startActivityForResult(VehicleDetailActivity.getIntent(this,presenter.vehicles.get(position)),REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY)
    }

    private fun updateVehiclesList(){
        (rv_vehicles.adapter as ScanQRCodeVehicleListAdapter).updateVehicleList(presenter.vehicles)
    }

    override fun hideProgressbar() {
        pb_scanning_in_qr_code.visibility = View.GONE
    }

    override fun showProgressbar() {
        pb_scanning_in_qr_code.visibility = View.VISIBLE
    }

    protected override fun onResume() {
        super.onResume()
        resumeScanner()
    }

    protected fun resumeScanner() {
        if (!decoratedBarcodeView.isActivated()) decoratedBarcodeView.resume()
    }

    protected fun pauseScanner() {
        decoratedBarcodeView.pause()
    }

    protected override fun onPause() {
        super.onPause()
        pauseScanner()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY) hideProgressLoading()
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

    fun setTitle(){
        ct_title_in_qr_code.text =  getString(R.string._0025_0040_qr_codes,presenter.vehicles.size.toString())
    }
    
    
    //// status, sub status :start
    private fun showChangeStatusMainDialog(mapOfStatus:Map<String,String>) {
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("")
        val array = mapOfStatus.values.toTypedArray()
        builder.setSingleChoiceItems(array,-1) { _, which ->
            try {
                val key =
                    (mapOfStatus.filterValues { it == array[which] }.keys).toTypedArray().get(0)
                showSubStatusDialog(
                    ResourceUtils.returnSubStatusMapDependingUponStatus(
                        this,
                        key
                    ),key)
            } catch (e: IllegalArgumentException) {
            }
            dialog.dismiss()
        }
        dialog = builder.create()
        dialog.show()
    }


    private fun showSubStatusDialog(mapOfSubStatus:Map<String,String>?,status:String) {
        if(mapOfSubStatus==null){
            onChangeBuldStatusFailure()
            return
        }
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("")
        val array = mapOfSubStatus!!.values.toTypedArray()
        builder.setSingleChoiceItems(array,-1,{ _, which->
            try {
                val subStatus = (mapOfSubStatus!!.filterValues { it == array[which] }.keys).toTypedArray().get(0)
                startChangeStatusRequest(status,subStatus)
            }catch (e:IllegalArgumentException){
            }
            dialog.dismiss()
        })
        dialog = builder.create()
        dialog.show()
    }


    fun startChangeStatusRequest(status: String,subStatus:String?){
        val changeStatus = ResourceUtils.returnChangeStatus(status, subStatus)
        if(changeStatus==null){
            onChangeBuldStatusFailure()
        }else{
            showProgressLoading()
            presenter.changeBulkStatus(changeStatus)
        }
    }

    override fun onChangeBulkStatusSuccess() {
        updateVehiclesList()
        hideProgressLoading()
    }

    override fun onChangeBuldStatusFailure() {
        hideProgressLoading()
    }

    fun showProgressLoading(){
        if(scan_qr_code_loading!=null){
            scan_qr_code_loading.visibility= View.VISIBLE
        }
    }

    fun hideProgressLoading(){
        if(scan_qr_code_loading!=null){
            scan_qr_code_loading.visibility= View.GONE
        }
    }
    
    
    //// status, sub status :end
    
}