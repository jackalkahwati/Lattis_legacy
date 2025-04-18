package io.lattis.operator.presentation.fleet.fragments.vehicles

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import io.lattis.domain.models.Fleet
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.base.fragment.BaseTabFragment
import io.lattis.operator.presentation.fleet.FleetDetailActivity
import io.lattis.operator.presentation.fleet.FleetDetailActivity.Companion.FLEET
import io.lattis.operator.presentation.qrcodescan.ScanQRCodeActivity
import io.lattis.operator.presentation.qrcodescan.ScanQRCodeActivity.Companion.VEHICLE_DATA
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import kotlinx.android.synthetic.main.fragment_fleet_detail_ticket.*
import kotlinx.android.synthetic.main.fragment_fleet_detail_vehicle.*
import kotlinx.android.synthetic.main.layout_live_vehicle_filters.view.*
import kotlinx.android.synthetic.main.layout_maintenance_vehicle_filters.view.*
import kotlinx.android.synthetic.main.layout_out_of_service_vehicle_filters.view.*
import kotlinx.android.synthetic.main.layout_staging_vehicle_filters.view.*
import kotlinx.android.synthetic.main.layout_vehicle_battery_filter.view.*
import kotlinx.android.synthetic.main.layout_vehicle_filters.view.*
import javax.inject.Inject


class FleetDetailVehicleFragment : BaseTabFragment<FleetDetailVehicleFragmentPresenter, FleetDetailVehicleFragmentView>()
    ,FleetDetailVehicleFragmentView, FleetDetailVehicleListener {
    @Inject
    override lateinit var presenter: FleetDetailVehicleFragmentPresenter;
    override val fragmentLayoutId = R.layout.fragment_fleet_detail_vehicle;
    override var view: FleetDetailVehicleFragmentView = this

    private val REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY = 345
    private val REQUEST_CODE_SCAN_QR_CODE_ACTIVITY = 346

    override fun getTitle(): String {
        return arguments?.getString(VehicleDetailActivity.TAB_TITLE,"Vehicles")!!
    }

    companion object{
        fun getInstance(fleet: Fleet,tabTitle:String):BaseTabFragment<*,*>{
            val bundle = Bundle()
            bundle.putString(FLEET, Gson().toJson(fleet))
            bundle.putString(FleetDetailActivity.TAB_TITLE,tabTitle)
            val fragment = FleetDetailVehicleFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun configureViews() {
        super.configureViews()
        showProgressLoading()
        presenter.getVehicles()
        btn_scan_qr_vehicle.setOnClickListener {
            startActivityForResult(ScanQRCodeActivity.getIntent(requireContext(),presenter.fleet?.id!!),REQUEST_CODE_SCAN_QR_CODE_ACTIVITY)
        }

        iv_vehicle_filters_in_fleet_detail_vehicle.setOnClickListener {
            setVehicleFilters()
            layout_vehicle_filters_in_fleet_detail_vehicle.visibility= View.VISIBLE
        }

        layout_vehicle_filters_in_fleet_detail_vehicle.btn_done_vehicle_filters.setOnClickListener {
            layout_vehicle_filters_in_fleet_detail_vehicle.visibility= View.GONE
            showProgressLoading()
            fetchVehicleFilters()
            presenter.searchVehicles()
        }

        layout_vehicle_filters_in_fleet_detail_vehicle.layout_vehicle_battery_filter_in_fleet_detail_map.seekbar_vehicle_battery_filter_percentage.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    layout_vehicle_filters_in_fleet_detail_vehicle.layout_vehicle_battery_filter_in_fleet_detail_map.ct_vehicle_battery_filter_percentage.text = progress.toString() + "%"
                    presenter.batteryPercentage = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
            })




        layout_vehicle_filters_in_fleet_detail_vehicle.layout_vehicle_battery_filter_in_fleet_detail_map.checkbox_vehicle_battery_filter.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                layout_vehicle_filters_in_fleet_detail_vehicle.layout_vehicle_battery_filter_in_fleet_detail_map.cl_vehicle_battery_value.visibility =
                    View.VISIBLE
                presenter.batteryPercentageFiltered = true
                layout_vehicle_filters_in_fleet_detail_vehicle.layout_vehicle_battery_filter_in_fleet_detail_map.ct_vehicle_battery_filter_percentage.text =
                    layout_vehicle_filters_in_fleet_detail_vehicle.layout_vehicle_battery_filter_in_fleet_detail_map.seekbar_vehicle_battery_filter_percentage.progress.toString() + "%"

            }else {
                layout_vehicle_filters_in_fleet_detail_vehicle.layout_vehicle_battery_filter_in_fleet_detail_map.cl_vehicle_battery_value.visibility =
                    View.GONE
                presenter.batteryPercentageFiltered = false
            }
        }


        // Configure the refreshing colors
        srl_rv_vehicles_in_fleet_details_ticket.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light);

        srl_rv_vehicles_in_fleet_details_ticket.setOnRefreshListener {
            presenter.getVehicles()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== REQUEST_CODE_SCAN_QR_CODE_ACTIVITY && data!=null &&
                data.hasExtra(VEHICLE_DATA)){
            startVehicleDetailActivity( data.getSerializableExtra(VEHICLE_DATA) as Vehicle)
        }else if(requestCode == REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY){
            showProgressLoading()
            presenter.getVehicles()
        }
    }

    override fun onVehiclesSuccess() {
        srl_rv_vehicles_in_fleet_details_ticket.setRefreshing(false)
        rv_vehicles_in_fleet_details_vehicle.setLayoutManager(LinearLayoutManager(requireContext()))
        rv_vehicles_in_fleet_details_vehicle.setAdapter(
            FleetDetailVehicleAdapter(
                requireContext(),
                presenter.vehicles!!,
                this
            )
        )

        hideProgressLoading()
    }

    override fun onVehicleClicked(position: Int) {
        startVehicleDetailActivity(presenter.vehicles?.get(position)!!)
    }

    fun startVehicleDetailActivity(vehicle: Vehicle){
        startActivityForResult(VehicleDetailActivity.getIntent(requireContext(),vehicle),REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY)
    }

    override fun onVehiclesFailure() {
        srl_rv_vehicles_in_fleet_details_ticket.setRefreshing(false)
        hideProgressLoading()
    }


    fun setVehicleFilters(){
        layout_vehicle_filters_in_fleet_detail_vehicle.layout_live_vehicle_filters.checkbox_in_ride_live_vehicle_filters.isChecked = presenter.live_in_ride
        layout_vehicle_filters_in_fleet_detail_vehicle.layout_live_vehicle_filters.checkbox_parked_live_vehicle_filters.isChecked = presenter.live_parked
        layout_vehicle_filters_in_fleet_detail_vehicle.layout_live_vehicle_filters.checkbox_reserved_live_vehicle_filters.isChecked = presenter.live_reserved
        layout_vehicle_filters_in_fleet_detail_vehicle.layout_live_vehicle_filters.checkbox_collect_live_vehicle_filters.isChecked = presenter.live_in_collect


        layout_vehicle_filters_in_fleet_detail_vehicle.layout_staging_vehicle_filters.checkbox_equipment_assigned_staging_vehicle_filters.isChecked = presenter.staging_equipement_assigned
        layout_vehicle_filters_in_fleet_detail_vehicle.layout_staging_vehicle_filters.checkbox_equipment_unassigned_staging_vehicle_filters.isChecked = presenter.staging_equipement_unassigned
        layout_vehicle_filters_in_fleet_detail_vehicle.layout_staging_vehicle_filters.checkbox_balancing_staging_vehicle_filters.isChecked = presenter.staging_balancing


        layout_vehicle_filters_in_fleet_detail_vehicle.layout_out_of_service_vehicle_filters.checkbox_damage_out_of_service_vehicle_filters.isChecked = presenter.out_of_service_damaged
        layout_vehicle_filters_in_fleet_detail_vehicle.layout_out_of_service_vehicle_filters.checkbox_maintenance_out_of_service_vehicle_filters.isChecked = presenter.out_of_service_maintenance
        layout_vehicle_filters_in_fleet_detail_vehicle.layout_out_of_service_vehicle_filters.checkbox_stolen_out_of_service_vehicle_filters.isChecked = presenter.out_of_service_stolen
        layout_vehicle_filters_in_fleet_detail_vehicle.layout_out_of_service_vehicle_filters.checkbox_transport_out_of_service_vehicle_filters.isChecked = presenter.out_of_service_transport

        layout_vehicle_filters_in_fleet_detail_vehicle.layout_maintenance_vehicle_filters.checkbox_low_battery_maintenance_vehicle_filters.isChecked = presenter.maintenance_low_battery

    }


    fun fetchVehicleFilters(){
        presenter.live_in_ride = layout_vehicle_filters_in_fleet_detail_vehicle.layout_live_vehicle_filters.checkbox_in_ride_live_vehicle_filters.isChecked
        presenter.live_parked = layout_vehicle_filters_in_fleet_detail_vehicle.layout_live_vehicle_filters.checkbox_parked_live_vehicle_filters.isChecked
        presenter.live_reserved = layout_vehicle_filters_in_fleet_detail_vehicle.layout_live_vehicle_filters.checkbox_reserved_live_vehicle_filters.isChecked
        presenter.live_in_collect = layout_vehicle_filters_in_fleet_detail_vehicle.layout_live_vehicle_filters.checkbox_collect_live_vehicle_filters.isChecked


        presenter.staging_equipement_assigned = layout_vehicle_filters_in_fleet_detail_vehicle.layout_staging_vehicle_filters.checkbox_equipment_assigned_staging_vehicle_filters.isChecked
        presenter.staging_equipement_unassigned =
            layout_vehicle_filters_in_fleet_detail_vehicle.layout_staging_vehicle_filters.checkbox_equipment_unassigned_staging_vehicle_filters.isChecked
        presenter.staging_balancing = layout_vehicle_filters_in_fleet_detail_vehicle.layout_staging_vehicle_filters.checkbox_balancing_staging_vehicle_filters.isChecked


        presenter.out_of_service_damaged = layout_vehicle_filters_in_fleet_detail_vehicle.layout_out_of_service_vehicle_filters.checkbox_damage_out_of_service_vehicle_filters.isChecked
        presenter.out_of_service_maintenance = layout_vehicle_filters_in_fleet_detail_vehicle.layout_out_of_service_vehicle_filters.checkbox_maintenance_out_of_service_vehicle_filters.isChecked
        presenter.out_of_service_stolen = layout_vehicle_filters_in_fleet_detail_vehicle.layout_out_of_service_vehicle_filters.checkbox_stolen_out_of_service_vehicle_filters.isChecked
        presenter.out_of_service_transport = layout_vehicle_filters_in_fleet_detail_vehicle.layout_out_of_service_vehicle_filters.checkbox_transport_out_of_service_vehicle_filters.isChecked

        presenter.maintenance_low_battery = layout_vehicle_filters_in_fleet_detail_vehicle.layout_maintenance_vehicle_filters.checkbox_low_battery_maintenance_vehicle_filters.isChecked

        presenter.name = layout_vehicle_filters_in_fleet_detail_vehicle.et_vehicle_name_filter.text.toString()

    }
    

    fun showProgressLoading(){
        if(fragment_vehicle_loading!=null){
            fragment_vehicle_loading.visibility= View.VISIBLE
        }
    }

    fun hideProgressLoading(){
        if(fragment_vehicle_loading!=null){
            fragment_vehicle_loading.visibility= View.GONE
        }
    }
}