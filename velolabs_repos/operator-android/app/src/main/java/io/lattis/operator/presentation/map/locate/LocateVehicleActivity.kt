package io.lattis.operator.presentation.map.locate

import android.content.Context
import android.content.Intent
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import io.lattis.domain.models.Location
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.base.activity.location.BaseLocationActivity
import io.lattis.operator.presentation.ui.base.activity.BaseActivity
import io.lattis.operator.presentation.utils.GooleMapUtil
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.lattis.operator.utils.GeneralUtils
import io.lattis.operator.utils.MarkerImageUtil.Companion.getLocationMarker
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.layout_headers.*
import kotlinx.android.synthetic.main.view_toolbar.*
import javax.inject.Inject


class LocateVehicleActivity : BaseLocationActivity<LocateVehicleActivityPresenter, LocateVehicleActivityView>(),
    LocateVehicleActivityView, OnMapReadyCallback {

    @Inject
    override lateinit var presenter: LocateVehicleActivityPresenter
    override val activityLayoutId = R.layout.activity_map
    override var view: LocateVehicleActivityView = this

    private lateinit var mMap: GoogleMap
    private var currentLocationMarkerAdded=false

    companion object {
        fun getIntent(context: Context, vehicle: Vehicle): Intent {
            val intent = Intent(context, LocateVehicleActivity::class.java)
            intent.putExtra(VehicleDetailActivity.VEHICLE, Gson().toJson(vehicle))
            return intent
        }
    }

    override fun configureViews() {
        super.configureViews()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.marginEnd = GeneralUtils.dpToPx(this, 72)
        toolbar.layoutParams =params

        iv_reposition_in_locate_vehicle.setOnClickListener {
            showProgressLoading()
            presenter.getVehiclePosition()
        }
        iv_myposition_in_locate_vehicle.setOnClickListener {
            zoomAroundCurrentLocationAndShowMarkers()
        }
    }

    override fun startShowingMap() {
        toolbar_title.text = presenter.mapTitle
        (map_fragment as SupportMapFragment).getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        addVehicleMarker()
        fetchLocation()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun updateVehicleLocation() {
        addVehicleMarker()
    }

    fun addVehicleMarker(){
        mMap.clear()
        val vehiclePosition = LatLng(presenter.location.latitude, presenter.location.longitude)
        mMap.addMarker(presenter.getVehicleMarkerOptions())
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vehiclePosition,15.0f))
        presenter.restartRepositionTimer()
        addCurrentLocationMarker()
    }

    fun addCurrentLocationAndVehicleMarker(){
        mMap.clear()
        mMap.addMarker(presenter.getVehicleMarkerOptions())
        addCurrentLocationMarker()
    }

    fun addCurrentLocationMarker(){
        if(presenter.currentUserLocation!=null) {
            mMap.addMarker(presenter.getCurrentLocationMarkerOptions())
            currentLocationMarkerAdded = true
        }
    }

    fun showProgressLoading(){
        if(locate_map_loading!=null){
            locate_map_loading.visibility= View.VISIBLE
        }
    }

    override fun hideProgressLoading(){
        if(locate_map_loading!=null){
            locate_map_loading.visibility= View.GONE
        }
    }

    override fun onRepositionTimerStart() {
        hideReposition()
    }

    override fun onRepositionTimerOver() {
        showReposition()
    }

    fun showReposition(){
        iv_reposition_in_locate_vehicle.visibility = View.VISIBLE
    }

    fun hideReposition(){
        iv_reposition_in_locate_vehicle.visibility = View.GONE
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

    fun zoomAroundCurrentLocation(){
        GooleMapUtil.setFixedZoomForSinglePoint(
            mMap,
            LatLng(presenter.currentUserLocation?.latitude!!, presenter.currentUserLocation?.longitude!!),
            100.0,
            100.0
        )
    }

    fun zoomAroundCurrentLocationAndShowMarkers(){
        addCurrentLocationAndVehicleMarker()
        zoomAroundCurrentLocation()
    }

    override fun setUserPosition(location: Location) {
        if(!currentLocationMarkerAdded)addCurrentLocationMarker()
    }

    override fun onLocationPermissionsAvailable() {
        requestLocationUpdates()
    }

    override fun onLocationPermissionsDenied() {

    }
}