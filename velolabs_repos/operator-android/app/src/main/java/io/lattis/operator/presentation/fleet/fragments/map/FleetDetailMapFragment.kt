package io.lattis.operator.presentation.fleet.fragments.map

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import com.google.maps.android.clustering.ClusterManager
import io.lattis.domain.models.Fleet
import io.lattis.domain.models.Location
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.base.fragment.BaseTabFragment
import io.lattis.operator.presentation.fleet.FleetDetailActivity
import io.lattis.operator.presentation.map.base.MarkerClusterRenderer
import io.lattis.operator.presentation.map.uimodels.GoogleMapMarker
import io.lattis.operator.presentation.utils.GooleMapUtil
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.lattis.operator.utils.ResourceUtils
import io.operator.lattis.presentation.base.fragment.location.BaseLocationFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_fleet_detail_map.*
import kotlinx.android.synthetic.main.fragment_fleet_detail_map_vehicle_card.*
import kotlinx.android.synthetic.main.layout_live_vehicle_filters.view.*
import kotlinx.android.synthetic.main.layout_maintenance_vehicle_filters.view.*
import kotlinx.android.synthetic.main.layout_out_of_service_vehicle_filters.view.*
import kotlinx.android.synthetic.main.layout_staging_vehicle_filters.view.*
import kotlinx.android.synthetic.main.layout_vehicle_battery_filter.view.*
import kotlinx.android.synthetic.main.layout_vehicle_filters.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FleetDetailMapFragment : BaseLocationFragment<FleetDetailMapFragmentPresenter, FleetDetailMapFragmentView>()
    , FleetDetailMapFragmentView, OnMapReadyCallback {
    @Inject
    override lateinit var presenter: FleetDetailMapFragmentPresenter;
    override val fragmentLayoutId = R.layout.fragment_fleet_detail_map;
    override var view: FleetDetailMapFragmentView = this

    private lateinit var mMap: GoogleMap
    private lateinit var mClusterManager : ClusterManager<GoogleMapMarker>
    private var northeast : LatLng?=null
    private var southwest : LatLng?=null
    private var center : LatLng?=null
    private var fetchVehiclesForBbox = false
    private var MILLISECONDS_FOR_MAP_MOVE_BUFFER = 1000
    private var mapDelayTimerDisposable: Disposable?=null
    private val REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY = 345
    private val REQUEST_CODE_FOR_GOOGLE_MAP_APP = 283

    companion object{
        fun getInstance(fleet: Fleet, tabTitle:String): BaseTabFragment<*, *> {
            val bundle = Bundle()
            bundle.putString(FleetDetailActivity.FLEET, Gson().toJson(fleet))
            bundle.putString(FleetDetailActivity.TAB_TITLE,tabTitle)
            val fragment = FleetDetailMapFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getTitle(): String {
        return arguments?.getString(VehicleDetailActivity.TAB_TITLE,"Map")!!
    }

    override fun configureViews() {
        super.configureViews()
        var googleMapsFragment = childFragmentManager.findFragmentById(R.id.map_fragment_in_fleet_detail_map_fragment) as SupportMapFragment
        googleMapsFragment.getMapAsync(this)
        configureClicks()
    }

    fun configureClicks(){
        iv_vehicle_filters_in_fleet_detail_map.setOnClickListener {
            setVehicleFilters()
            layout_vehicle_filters_in_fleet_detail_map.visibility= View.VISIBLE
        }

        layout_vehicle_filters_in_fleet_detail_map.btn_done_vehicle_filters.setOnClickListener {
            layout_vehicle_filters_in_fleet_detail_map.visibility= View.GONE
            fetchVehicleFilters()
            forceVehicleFetch()
        }

        fragment_fleet_detail_map_vehicle_card.setOnClickListener {
            startVehicleDetailsActivity()
        }

        iv_vehicle_navigation.setOnClickListener {
            openGoogleMapApp()
        }

        iv_reposition_in_fleet_detail.setOnClickListener {
            forceVehicleFetch()
        }

        layout_vehicle_filters_in_fleet_detail_map.layout_vehicle_battery_filter_in_fleet_detail_map.seekbar_vehicle_battery_filter_percentage.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    layout_vehicle_filters_in_fleet_detail_map.layout_vehicle_battery_filter_in_fleet_detail_map.ct_vehicle_battery_filter_percentage.text = progress.toString() + "%"
                    presenter.batteryPercentage = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
            })




        layout_vehicle_filters_in_fleet_detail_map.layout_vehicle_battery_filter_in_fleet_detail_map.checkbox_vehicle_battery_filter.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                layout_vehicle_filters_in_fleet_detail_map.layout_vehicle_battery_filter_in_fleet_detail_map.cl_vehicle_battery_value.visibility =
                    View.VISIBLE
                presenter.batteryPercentageFiltered = true
                layout_vehicle_filters_in_fleet_detail_map.layout_vehicle_battery_filter_in_fleet_detail_map.ct_vehicle_battery_filter_percentage.text =
                    layout_vehicle_filters_in_fleet_detail_map.layout_vehicle_battery_filter_in_fleet_detail_map.seekbar_vehicle_battery_filter_percentage.progress.toString() + "%"

            }else {
                layout_vehicle_filters_in_fleet_detail_map.layout_vehicle_battery_filter_in_fleet_detail_map.cl_vehicle_battery_value.visibility =
                    View.GONE
                presenter.batteryPercentageFiltered = false
            }
        }

        iv_myposition_in_fleet_detail.setOnClickListener {
            presenter.requestLocationUpdates()
        }
    }

    fun forceVehicleFetch(){
        if(northeast!=null && southwest!=null) {
            fetchVehiclesForBbox=true
            checkIfVehicleCallIsRequired(southwest!!, northeast!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        hideLoading()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isZoomControlsEnabled = false
        fetchLocation()
    }

    fun setMapListeners(){
        mMap.setOnCameraIdleListener {
            val curScreen: LatLngBounds =  mMap!!.getProjection().getVisibleRegion().latLngBounds
            northeast=curScreen.northeast
            southwest=curScreen.southwest
            center=curScreen.center
            Log.v("northeast LatLng", "-:" + northeast)
            Log.v("southwest LatLng", "-:" + southwest)
            Log.v("center LatLng", "-:" + center)
//            mClusterManager.addItem(GoogleMapMarker(northeast.latitude,northeast.longitude,null))
//            mClusterManager.addItem(GoogleMapMarker(southwest.latitude,southwest.longitude,null))
            mClusterManager.onCameraIdle()
            checkIfVehicleCallIsRequired(southwest!!,northeast!!)
        }

        mMap.setOnCameraMoveStartedListener(GoogleMap.OnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                // The user gestured on the map.
                fetchVehiclesForBbox = true
            }
        })

    }

    fun checkIfVehicleCallIsRequired(southwest: LatLng, northeast: LatLng){

        if(!fetchVehiclesForBbox){
            return
        }

        mapDelayTimerDisposable?.dispose()
        mapDelayTimerDisposable = Observable.timer(
            MILLISECONDS_FOR_MAP_MOVE_BUFFER.toLong(),
            TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                presenter.getVehiclesInBbox(Location(southwest.latitude, southwest.longitude), Location(northeast.latitude, northeast.longitude))
                fetchVehiclesForBbox=false
                hideVehicleCard()
            }) { throwable ->

            }
    }

    fun initialiseCluster(){
        mClusterManager = ClusterManager(requireContext(), mMap)
        mClusterManager.renderer = MarkerClusterRenderer(requireContext(), mMap, mClusterManager)


        mMap.setOnMarkerClickListener(mClusterManager);

        mClusterManager
            .setOnClusterClickListener(ClusterManager.OnClusterClickListener<GoogleMapMarker> { cluster ->
                hideVehicleCard()
                GooleMapUtil.zoomToMarkers(mMap, cluster)
                presenter.restartRepositionTimer()
                true
            })


        mClusterManager.setOnClusterItemClickListener(ClusterManager.OnClusterItemClickListener<GoogleMapMarker> {
            if (!it.isLocation()) {
                presenter.selectedVehicle = it.getVehicle()!!
                showVehicleCard()
            }
            false
        })
    }

    override fun showMarkers(){
        mClusterManager.clearItems()
        mClusterManager.cluster()
        mClusterManager.addItems(presenter.getMarkers())
        if(presenter.currentUserLocation!=null){
            mClusterManager.addItem(GoogleMapMarker(presenter.currentUserLocation?.latitude!!, presenter.currentUserLocation?.longitude!!, null, true))
        }
        mClusterManager.cluster()
//        GooleMapUtil.zoomToMakers(mMap, presenter.markers)
    }


    override fun onLocationPermissionsAvailable() {
        requestLocationUpdates()
        initialiseCluster()
        setMapListeners()
    }

    override fun setUserPosition(location: Location) {
        presenter.requestStopLocationUpdates()
        mClusterManager.clearItems()
        mClusterManager.addItem(GoogleMapMarker(location.latitude, location.longitude, null, true))
        zoomAroundCurrentLocation()
    }

    fun zoomAroundCurrentLocation(){
        GooleMapUtil.setFixedZoomForSinglePoint(
            mMap,
            LatLng(presenter.currentUserLocation?.latitude!!, presenter.currentUserLocation?.longitude!!),
            100.0,
            100.0
        )
        fetchVehiclesForBbox = true
    }

    fun startVehicleDetailActivity(vehicle: Vehicle){
        startActivityForResult(VehicleDetailActivity.getIntent(requireContext(),vehicle),REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY)
    }

    /// handle failure here

    override fun onLocationPermissionsDenied() {

    }

    override fun onVehiclesInBboxFailure() {

    }

    fun setVehicleFilters(){
        layout_vehicle_filters_in_fleet_detail_map.layout_live_vehicle_filters.checkbox_in_ride_live_vehicle_filters.isChecked = presenter.live_in_ride
        layout_vehicle_filters_in_fleet_detail_map.layout_live_vehicle_filters.checkbox_parked_live_vehicle_filters.isChecked = presenter.live_parked
        layout_vehicle_filters_in_fleet_detail_map.layout_live_vehicle_filters.checkbox_reserved_live_vehicle_filters.isChecked = presenter.live_reserved
        layout_vehicle_filters_in_fleet_detail_map.layout_live_vehicle_filters.checkbox_collect_live_vehicle_filters.isChecked = presenter.live_in_collect


        layout_vehicle_filters_in_fleet_detail_map.layout_staging_vehicle_filters.checkbox_equipment_assigned_staging_vehicle_filters.isChecked = presenter.staging_equipement_assigned
        layout_vehicle_filters_in_fleet_detail_map.layout_staging_vehicle_filters.checkbox_equipment_unassigned_staging_vehicle_filters.isChecked = presenter.staging_equipement_unassigned
        layout_vehicle_filters_in_fleet_detail_map.layout_staging_vehicle_filters.checkbox_balancing_staging_vehicle_filters.isChecked = presenter.staging_balancing


        layout_vehicle_filters_in_fleet_detail_map.layout_out_of_service_vehicle_filters.checkbox_damage_out_of_service_vehicle_filters.isChecked = presenter.out_of_service_damaged
        layout_vehicle_filters_in_fleet_detail_map.layout_out_of_service_vehicle_filters.checkbox_maintenance_out_of_service_vehicle_filters.isChecked = presenter.out_of_service_maintenance
        layout_vehicle_filters_in_fleet_detail_map.layout_out_of_service_vehicle_filters.checkbox_stolen_out_of_service_vehicle_filters.isChecked = presenter.out_of_service_stolen
        layout_vehicle_filters_in_fleet_detail_map.layout_out_of_service_vehicle_filters.checkbox_transport_out_of_service_vehicle_filters.isChecked = presenter.out_of_service_transport

        layout_vehicle_filters_in_fleet_detail_map.layout_maintenance_vehicle_filters.checkbox_low_battery_maintenance_vehicle_filters.isChecked = presenter.maintenance_low_battery

    }


    fun fetchVehicleFilters(){
        presenter.live_in_ride = layout_vehicle_filters_in_fleet_detail_map.layout_live_vehicle_filters.checkbox_in_ride_live_vehicle_filters.isChecked
        presenter.live_parked = layout_vehicle_filters_in_fleet_detail_map.layout_live_vehicle_filters.checkbox_parked_live_vehicle_filters.isChecked
        presenter.live_reserved = layout_vehicle_filters_in_fleet_detail_map.layout_live_vehicle_filters.checkbox_reserved_live_vehicle_filters.isChecked
        presenter.live_in_collect = layout_vehicle_filters_in_fleet_detail_map.layout_live_vehicle_filters.checkbox_collect_live_vehicle_filters.isChecked


        presenter.staging_equipement_assigned = layout_vehicle_filters_in_fleet_detail_map.layout_staging_vehicle_filters.checkbox_equipment_assigned_staging_vehicle_filters.isChecked
        presenter.staging_equipement_unassigned =
            layout_vehicle_filters_in_fleet_detail_map.layout_staging_vehicle_filters.checkbox_equipment_unassigned_staging_vehicle_filters.isChecked
        presenter.staging_balancing = layout_vehicle_filters_in_fleet_detail_map.layout_staging_vehicle_filters.checkbox_balancing_staging_vehicle_filters.isChecked


        presenter.out_of_service_damaged = layout_vehicle_filters_in_fleet_detail_map.layout_out_of_service_vehicle_filters.checkbox_damage_out_of_service_vehicle_filters.isChecked
        presenter.out_of_service_maintenance = layout_vehicle_filters_in_fleet_detail_map.layout_out_of_service_vehicle_filters.checkbox_maintenance_out_of_service_vehicle_filters.isChecked
        presenter.out_of_service_stolen = layout_vehicle_filters_in_fleet_detail_map.layout_out_of_service_vehicle_filters.checkbox_stolen_out_of_service_vehicle_filters.isChecked
        presenter.out_of_service_transport = layout_vehicle_filters_in_fleet_detail_map.layout_out_of_service_vehicle_filters.checkbox_transport_out_of_service_vehicle_filters.isChecked

        presenter.maintenance_low_battery = layout_vehicle_filters_in_fleet_detail_map.layout_maintenance_vehicle_filters.checkbox_low_battery_maintenance_vehicle_filters.isChecked

        presenter.name = layout_vehicle_filters_in_fleet_detail_map.et_vehicle_name_filter.text.toString()

    }

    fun showLoading(){
        fragment_map_loading.visibility = View.VISIBLE
    }

    fun hideLoading(){
        fragment_map_loading.visibility = View.GONE
    }

    fun showVehicleCard(){

        ct_vehicle_name_value_in_vehicle_card.text = presenter.selectedVehicle?.name

        ct_vehicle_type_value_in_vehicle_card.text = presenter.selectedVehicle?.group?.type

        ct_vehicle_usage_value_in_vehicle_card.text =
            ResourceUtils.convertUsage(requireContext(), presenter.selectedVehicle?.usage)

        ct_vehicle_status_value_in_vehicle_card.text =
            ResourceUtils.convertStatus(requireContext(), presenter.selectedVehicle?.status)

        fragment_fleet_detail_map_vehicle_card.visibility = View.VISIBLE
    }

    fun hideVehicleCard(){
        fragment_fleet_detail_map_vehicle_card.visibility = View.GONE
    }

    fun startVehicleDetailsActivity(){
        showLoading()
        startVehicleDetailActivity(presenter.selectedVehicle!!)
    }

    private fun openGoogleMapApp() {
        try {
            val uri = java.lang.String.format(
                Locale.ENGLISH,
                "http://maps.google.com/maps?daddr=%f,%f(%s)&mode=bicycling",
                presenter.selectedVehicle?.latitude,
                presenter.selectedVehicle?.longitude,
                presenter.selectedVehicle?.name
            )
            val gmmIntentUri = Uri.parse(uri)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(requireActivity().packageManager) == null) {
                // Google map not installed
            } else {
                startActivityForResult(mapIntent, REQUEST_CODE_FOR_GOOGLE_MAP_APP)
            }
        } catch (e: ActivityNotFoundException) {
        }
    }

    override fun onRepositionTimerStart() {
        hideReposition()
    }

    override fun onRepositionTimerOver() {
        showReposition()
    }

    fun showReposition(){
        iv_reposition_in_fleet_detail.visibility = View.VISIBLE
    }

    fun hideReposition(){
        iv_reposition_in_fleet_detail.visibility = View.INVISIBLE
    }
}