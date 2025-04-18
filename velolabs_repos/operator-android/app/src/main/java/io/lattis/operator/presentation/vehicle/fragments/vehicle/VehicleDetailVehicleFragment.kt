package io.lattis.operator.presentation.vehicle.fragments.vehicle

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.google.gson.Gson
import io.lattis.domain.models.Location
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.model.ChangeStatus
import io.lattis.operator.presentation.base.fragment.BaseTabFragment
import io.lattis.operator.presentation.map.locate.LocateVehicleActivity
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity.Companion.TAB_TITLE
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity.Companion.VEHICLE
import io.lattis.operator.utils.ResourceUtils.changeStatusList
import io.lattis.operator.utils.ResourceUtils.convertStatus
import io.lattis.operator.utils.ResourceUtils.convertUsage
import io.lattis.operator.utils.ResourceUtils.downloadImage
import io.lattis.operator.utils.ResourceUtils.returnChangeStatus
import io.lattis.operator.utils.ResourceUtils.returnSubStatusMapDependingUponStatus
import kotlinx.android.synthetic.main.fragment_vehicle_detail_vehicle.*
import kotlinx.android.synthetic.main.fragment_vehicle_detail_vehicle_group.*
import kotlinx.android.synthetic.main.fragment_vehicle_detail_vehicle_info.*
import kotlinx.android.synthetic.main.fragment_vehicle_detail_vehicle_status.*
import javax.inject.Inject

class VehicleDetailVehicleFragment : BaseTabFragment<VehicleDetailVehicleFragmentPresenter, VehicleDetailVehicleFragmentView>()
        , VehicleDetailVehicleFragmentView {
    @Inject
    override lateinit var presenter: VehicleDetailVehicleFragmentPresenter;
    override val fragmentLayoutId = R.layout.fragment_vehicle_detail_vehicle;
    override var view: VehicleDetailVehicleFragmentView = this
    val REQUEST_CODE_MAP_ACTIVITY = 930

    override fun configureViews() {
        super.configureViews()
        btn_change_status_vehicle.setOnClickListener {
            showChangeStatusMainListAlert()
        }

        ct_vehicle_locate_label.setOnClickListener {
            launchMapActivity()
        }

        iv_locate_next_arrow.setOnClickListener {
            launchMapActivity()
        }
    }

    override fun getTitle(): String {
        return arguments?.getString(TAB_TITLE,"Vehicle")!!
    }

    companion object{
        fun getInstance(vehicle: Vehicle,tabTitle:String):BaseTabFragment<*,*>{
            val bundle = Bundle()
            bundle.putString(VEHICLE, Gson().toJson(vehicle))
            bundle.putString(VehicleDetailActivity.TAB_TITLE,tabTitle)
            val fragment = VehicleDetailVehicleFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun startShowingInformation() {
        showVehicleInfo()
        showVehicleStatus()
        showVehicleGroup()
    }

    fun showVehicleInfo(){
        ct_vehicle_name_value.text = presenter.vehicle.name
        if(!TextUtils.isEmpty(presenter.vehicle.batteryLevel)){
            ct_vehicle_battery_value.text = presenter.vehicle.batteryLevel + "%"
        }else{
            ct_vehicle_battery_value.text = ""
        }

        val qrcode = presenter.getQRCode()
        if(qrcode!=null){
            ct_vehicle_qrcode_value.text = qrcode
        }else{
            ct_vehicle_qrcode_value.text = ""
        }
    }


    fun showVehicleStatus(){
        ct_vehicle_usage_value.text = convertUsage(requireContext(),presenter.vehicle.usage)
        ct_vehicle_status_value.text = convertStatus(requireContext(),presenter.vehicle.status)
    }

    fun showVehicleGroup(){
        ct_vehicle_ride_type_value.text = presenter.vehicle.group?.type
        ct_vehicle_make_value.text = presenter.vehicle.group?.make
        ct_vehicle_model_value.text = presenter.vehicle.group?.model
        ct_vehicle_description_label.text = presenter.vehicle.group?.description
        downloadImage(requireContext(),iv_vehicle_group,presenter.vehicle.group?.image)

    }


    fun showChangeStatusMainListAlert(){
        val mapOfStatus = changeStatusList(true,requireContext(),null)
        showChangeStatusMainDialog(mapOfStatus)
    }

    private fun showChangeStatusMainDialog(mapOfStatus:Map<String,String>) {
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("")
        val array = mapOfStatus.values.toTypedArray()
        builder.setSingleChoiceItems(array,-1) { _, which ->
            try {
                val key =
                    (mapOfStatus.filterValues { it == array[which] }.keys).toTypedArray().get(0)
                showSubStatusDialog(returnSubStatusMapDependingUponStatus(requireContext(),key),key)
            } catch (e: IllegalArgumentException) {
            }
            dialog.dismiss()
        }
        dialog = builder.create()
        dialog.show()
    }


    private fun showSubStatusDialog(mapOfSubStatus:Map<String,String>?,status:String) {
        if(mapOfSubStatus==null){
            onChangeStatusFailure()
            return
        }
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(requireContext())
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
        val changeStatus = returnChangeStatus(status,subStatus)
        if(changeStatus==null){
            onChangeStatusFailure()
        }else{
            showProgressLoading()
            presenter.changeStatus(changeStatus)
        }
    }

    override fun onChangeStatusSuccess(changeStatus: ChangeStatus) {
        hideProgressLoading()
        ct_vehicle_usage_value.text = convertUsage(requireContext(),changeStatus.usage)
        ct_vehicle_status_value.text = convertStatus(requireContext(),changeStatus.status)
    }

    override fun onChangeStatusFailure() {
        hideProgressLoading()
    }

    fun showProgressLoading(){
        if(fragment_vehicle_detail_vehicle_loading!=null){
            fragment_vehicle_detail_vehicle_loading.visibility= View.VISIBLE
        }
    }

    fun hideProgressLoading(){
        if(fragment_vehicle_detail_vehicle_loading!=null){
            fragment_vehicle_detail_vehicle_loading.visibility= View.GONE
        }
    }

    fun launchMapActivity(){
        if(presenter.vehicle.latitude!=null && presenter.vehicle.longitude!=null) {
            startActivityForResult(
                LocateVehicleActivity.getIntent(
                    requireContext(), presenter.vehicle
                ), REQUEST_CODE_MAP_ACTIVITY
            )
        }
    }
}