package io.lattis.operator.presentation.vehicle.fragments.equipment

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.addisonelliott.segmentedbutton.SegmentedButtonGroup.OnPositionChangedListener
import com.google.gson.Gson
import io.lattis.domain.models.ThingStatus
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.base.fragment.BaseTabFragment
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.lattis.operator.presentation.vehicle.fragments.equipment.other.VehicleDetailOtherEquipmentActivity
import io.lattis.operator.utils.ResourceUtils.headTailOptionsList
import io.lattis.operator.utils.ResourceUtils.soundOptionsList
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_vehicle_detail_equipment.*
import kotlinx.android.synthetic.main.fragment_vehicle_detail_equipment_control.*
import kotlinx.android.synthetic.main.fragment_vehicle_detail_equipment_info.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class VehicleDetailEquipmentFragment : BaseTabFragment<VehicleDetailEquipmentFragmentPresenter, VehicleDetailEquipmentFragmentView>()
    , VehicleDetailEquipmentFragmentView, VehicleDetailOtherEquipmentsListener {
    @Inject
    override lateinit var presenter: VehicleDetailEquipmentFragmentPresenter;
    override val fragmentLayoutId = R.layout.fragment_vehicle_detail_equipment;
    override var view: VehicleDetailEquipmentFragmentView = this
    var lockUnLockButtonEnabled = true

    private val REQUEST_CODE_VEHICLE_DETAIL_OTHER_EQUIPMENT_ACTIVITY = 102

    override fun getTitle(): String {
        return arguments?.getString(VehicleDetailActivity.TAB_TITLE,"Equipment")!!
    }

    companion object{
        val CURRENT_EQUIPMENT_POSITION = "CURRENT_EQUIPMENT_POSITION"
        fun getInstance(vehicle: Vehicle,tabTitle:String,currentPosition:Int=0): BaseTabFragment<*, *> {
            val bundle = Bundle()
            bundle.putString(VehicleDetailActivity.VEHICLE, Gson().toJson(vehicle))
            bundle.putString(VehicleDetailActivity.TAB_TITLE,tabTitle)
            bundle.putInt(CURRENT_EQUIPMENT_POSITION,currentPosition)
            val fragment = VehicleDetailEquipmentFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun configureViews() {
        super.configureViews()
        buttonGroup_security.setOnPositionChangedListener(OnPositionChangedListener {
            if(lockUnLockButtonEnabled) {
                if (it == 0) presenter.unlockIt(presenter.vehicle?.things?.get(presenter.currentThingPosition)?.id!!) else presenter.lockIt(
                    presenter.vehicle?.things?.get(presenter.currentThingPosition)?.id!!
                )
            }
        })

        cl_equipment_headlight.setOnClickListener {
            showHeadTailLightOptions(presenter.vehicle?.things?.get(presenter.currentThingPosition)?.id!!,true,false)
        }

        cl_equipment_taillight.setOnClickListener {
            showHeadTailLightOptions(presenter.vehicle?.things?.get(presenter.currentThingPosition)?.id!!,false,true)
        }

        cl_equipment_unlock_battery_cover.setOnClickListener {
            presenter.unCoverIt(presenter.vehicle?.things?.get(presenter.currentThingPosition)?.id!!)
        }

        cl_equipment_sound.setOnClickListener {
            showSoundOptions(presenter.vehicle?.things?.get(presenter.currentThingPosition)?.id!!)
        }

        ct_equipment_connection_refresh_label.setOnClickListener {
            refreshThingStatus()
        }
    }

    override fun startShowingInformation() {
        showEquipmentInfoIfApplicable()
        showEquipmentControlIfApplicable()
        showOtherEquipmentsIfAvailable()
    }

    fun showEquipmentInfoIfApplicable(){
        if(presenter.vehicle.things!=null && presenter.vehicle.things?.size!!>0){
            fragment_vehicle_detail_equipment_info.visibility = View.VISIBLE
            val thing = presenter.vehicle?.things?.get(presenter.currentThingPosition)
            ct_equipment_vendor_value.text = thing?.vendor
            ct_equipment_key_value.text = thing?.key
            ct_equipment_device_type_value.text = thing?.deviceType
        }else{
            hideEquipmentInfo()
        }
    }

    fun showEquipmentControlIfApplicable(){
        if(presenter.shouldShowEquipmentControl()){
            refreshThingStatus()
        }else{
            hideEquipmentControl()
        }
    }


    fun showEquipmentControl(thingStatus: ThingStatus){
        if(presenter.shouldShowEquipmentControl()) {
            setLockUnLockPosition(if ((thingStatus.locked != null && thingStatus.locked!!) || (thingStatus.lockStatus!=null && thingStatus.lockStatus!!)) 1 else 0)
            fragment_vehicle_detail_equipment_control.visibility = View.VISIBLE
            ct_control_label.visibility = View.VISIBLE
        }else{
            hideEquipmentControl()
        }
    }

    fun showOtherEquipmentsIfAvailable(){
        if(presenter.shouldShowOtherEquipmentControl()){
            showOtherEquipments()
            instantiateOtherEquipmentAdapter()
        }else{
            hideOtherEquipments()
        }
    }

    fun instantiateOtherEquipmentAdapter(){
        rv_other_equipment_control.setLayoutManager(LinearLayoutManager(requireContext()))
        rv_other_equipment_control.setAdapter(VehicleDetailOtherEquipmentsAdapter(requireContext(),this, presenter.vehicle?.things!!))
    }

    override fun onOtherEquipmentClicked(position: Int) {
        launchVehicleDetailOtherEquipmentActivity(position)
    }

    fun launchVehicleDetailOtherEquipmentActivity(position: Int){
        startActivityForResult(VehicleDetailOtherEquipmentActivity.getIntent(requireActivity(),presenter.vehicle,position),REQUEST_CODE_VEHICLE_DETAIL_OTHER_EQUIPMENT_ACTIVITY)
    }

    fun showOtherEquipments(){
        ct_other_equipment_label.visibility = View.VISIBLE
        rv_other_equipment_control.visibility = View.VISIBLE
    }

    fun hideOtherEquipments(){
        ct_other_equipment_label.visibility = View.GONE
        rv_other_equipment_control.visibility = View.GONE
    }

    override fun onThingStatusSuccess(thingStatus: ThingStatus) {
        onThingStatusResponse()
        ct_equipment_connection_value.text = if(thingStatus.online!=null && thingStatus.online!!) getString(
            R.string.online
        ) else getString(R.string.offline)
        ct_equipment_battery_value.text = if(TextUtils.isEmpty(thingStatus.batteryLevel) || "null".equals(thingStatus.batteryLevel)) "" else thingStatus.batteryLevel+"%"
        if(thingStatus.online!=null && thingStatus.online!!)showEquipmentControl(thingStatus) else hideEquipmentControl()
    }

    override fun onThingStatusFailure() {
        onThingStatusResponse()
        ct_equipment_connection_value.text = getString(R.string.offline)
        hideEquipmentControl()
    }

    fun onThingStatusResponse(){
        pb_equipment_connection_progress.visibility = View.GONE
        ct_equipment_connection_value.visibility = View.VISIBLE
        ct_equipment_connection_refresh_label.visibility = View.VISIBLE
    }

    fun refreshThingStatus(){
        pb_equipment_connection_progress.visibility = View.VISIBLE
        ct_equipment_connection_value.visibility = View.GONE
        ct_equipment_connection_refresh_label.visibility = View.GONE
        presenter.getThingStatus()
    }

    fun showHeadTailLightOptions(thingId:Int, headLight:Boolean, tailLight:Boolean){
        val mapOfCategory = headTailOptionsList(requireContext())
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.category))
        val array = mapOfCategory.values.toTypedArray()
        builder.setSingleChoiceItems(array,-1) { _, which ->
            try {
                val key =
                        (mapOfCategory.filterValues { it == array[which] }.keys).toTypedArray().get(0)

                if(headLight && tailLight){
                    presenter.headTailLightIt(thingId,key.toIntOrNull(),key.toIntOrNull())
                }else if(headLight){
                    presenter.headTailLightIt(thingId,key.toIntOrNull(),null)
                }else if(tailLight){
                    presenter.headTailLightIt(thingId,null,key.toIntOrNull())
                }
            } catch (e: IllegalArgumentException) {
            }
            dialog.dismiss()
        }
        dialog = builder.create()
        dialog.show()
    }

    fun showSoundOptions(thingId:Int){
        val mapOfCategory = soundOptionsList(requireContext())
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.category))
        val array = mapOfCategory.values.toTypedArray()
        builder.setSingleChoiceItems(array,-1) { _, which ->
            try {
                val key =
                    (mapOfCategory.filterValues { it == array[which] }.keys).toTypedArray().get(0)

                if(key.equals(getString(R.string.horn),true)){
                    presenter.sound(thingId,2,null)
                }else if(key.equals(getString(R.string.on),true)){
                    presenter.sound(thingId,null,2)
                }else if(key.equals(getString(R.string.off),true)){
                    presenter.sound(thingId,null,1)
                }

            } catch (e: IllegalArgumentException) {
            }
            dialog.dismiss()
        }
        dialog = builder.create()
        dialog.show()
    }


    fun hideEquipmentInfo(){
        fragment_vehicle_detail_equipment_info.visibility = View.GONE
    }

    fun hideEquipmentControl(){
        fragment_vehicle_detail_equipment_control.visibility = View.GONE
        ct_control_label.visibility = View.GONE
    }


    override fun showProgressLoading(){
        if(fragment_vehicle_detail_equipment_loading!=null){
            fragment_vehicle_detail_equipment_loading.visibility= View.VISIBLE
        }
    }

    override fun hideProgressLoading(){
        if(fragment_vehicle_detail_equipment_loading!=null){
            fragment_vehicle_detail_equipment_loading.visibility= View.GONE
        }
    }


    override fun onLockItFailure() {
        setLockUnLockPosition(0)
    }

    override fun onUnLockItFailure() {
        setLockUnLockPosition(1)
    }

    fun setLockUnLockPosition(position: Int){
        lockUnLockButtonEnabled = false
        buttonGroup_security.setPosition(
            position,
            true
        )
        Observable.timer(3000,TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                lockUnLockButtonEnabled = true
            },{
                lockUnLockButtonEnabled = true
            })
    }
}