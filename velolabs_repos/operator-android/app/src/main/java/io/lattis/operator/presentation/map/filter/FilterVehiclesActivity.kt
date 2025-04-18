package io.lattis.operator.presentation.map.filter

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.ClusterManager.OnClusterClickListener
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener
import io.lattis.domain.models.Fleet
import io.lattis.domain.models.Location
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.base.activity.location.BaseLocationActivity
import io.lattis.operator.presentation.fleet.FleetDetailActivity.Companion.FLEET
import io.lattis.operator.presentation.map.base.MarkerClusterRenderer
import io.lattis.operator.presentation.map.uimodels.GoogleMapMarker
import io.lattis.operator.presentation.utils.GooleMapUtil.setFixedZoomForSinglePoint
import io.lattis.operator.presentation.utils.GooleMapUtil.zoomToMakers
import io.lattis.operator.presentation.utils.GooleMapUtil.zoomToMarkers
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.lattis.operator.utils.GeneralUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.layout_headers.*
import kotlinx.android.synthetic.main.view_toolbar.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class FilterVehiclesActivity : BaseLocationActivity<FilterVehiclesActivityPresenter, FilterVehiclesActivityView>(),
    FilterVehiclesActivityView, OnMapReadyCallback {

    @Inject
    override lateinit var presenter: FilterVehiclesActivityPresenter
    override val activityLayoutId = R.layout.activity_map
    override var view: FilterVehiclesActivityView = this

    private lateinit var mMap: GoogleMap
    private lateinit var mClusterManager : ClusterManager<GoogleMapMarker>
    private var fetchVehiclesForBbox = false
    private var MILLISECONDS_FOR_MAP_MOVE_BUFFER = 1000
    private var mapDelayTimerDisposable: Disposable?=null
    private val REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY = 345

    companion object {
        fun getIntent(context: Context, fleet: Fleet): Intent {
            val intent = Intent(context, FilterVehiclesActivity::class.java)
            intent.putExtra(FLEET, Gson().toJson(fleet))
            return intent
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        fetchLocation()
    }

    fun setMapListeners(){
        mMap.setOnCameraIdleListener {
            val curScreen: LatLngBounds =  mMap!!.getProjection().getVisibleRegion().latLngBounds
            var northeast=curScreen.northeast
            var southwest=curScreen.southwest
            var center=curScreen.center
            Log.v("northeast LatLng", "-:" + northeast)
            Log.v("southwest LatLng", "-:" + southwest)
            Log.v("center LatLng", "-:" + center)
//            mClusterManager.addItem(GoogleMapMarker(northeast.latitude,northeast.longitude,null))
//            mClusterManager.addItem(GoogleMapMarker(southwest.latitude,southwest.longitude,null))
            mClusterManager.onCameraIdle()
            checkIfVehicleCallIsRequired(southwest,northeast)
        }

        mMap.setOnCameraMoveStartedListener(GoogleMap.OnCameraMoveStartedListener { reason ->
            if (reason == REASON_GESTURE) {
                // The user gestured on the map.
                fetchVehiclesForBbox = true
            }
        })

    }

    fun checkIfVehicleCallIsRequired(southwest:LatLng,northeast:LatLng){

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
                }) { throwable ->

                }
    }

    fun initialiseCluster(){
        mClusterManager = ClusterManager(this, mMap)
        mClusterManager.renderer = MarkerClusterRenderer(this, mMap, mClusterManager)


        mMap.setOnMarkerClickListener(mClusterManager);

        mClusterManager
            .setOnClusterClickListener(OnClusterClickListener<GoogleMapMarker> { cluster ->
                zoomToMarkers(mMap, cluster)
                true
            })


        mClusterManager.setOnClusterItemClickListener(OnClusterItemClickListener<GoogleMapMarker> {
            if(!it.isLocation()) {
                startVehicleDetailActivity(it.getVehicle()!!)
            }
            false
        })
    }

    override fun showMarkers(){
        mClusterManager.clearItems()
        mClusterManager.cluster()
        mClusterManager.addItems(presenter.getMarkers())
        mClusterManager.cluster()
        zoomToMakers(mMap, presenter.markers)
    }





    override fun configureViews() {
        super.configureViews()
        (map_fragment as SupportMapFragment).getMapAsync(this)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.marginEnd = GeneralUtils.dpToPx(this, 72)
        toolbar.layoutParams =params
    }

    override fun setTitle(title: String) {
        toolbar_title.text = title
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

    override fun onLocationPermissionsAvailable() {
        requestLocationUpdates()
        initialiseCluster()
        setMapListeners()
    }

    override fun setUserPosition(location: Location) {
        presenter.requestStopLocationUpdates()
        mClusterManager.addItem(GoogleMapMarker(location.latitude, location.longitude, null, true))
        setFixedZoomForSinglePoint(mMap, LatLng(location.latitude, location.longitude), 100.0, 100.0)
        fetchVehiclesForBbox = true
    }

    fun startVehicleDetailActivity(vehicle: Vehicle){
        startActivityForResult(VehicleDetailActivity.getIntent(this,vehicle),REQUEST_CODE_VEHICLE_DETAIL_ACTIVITY)
    }

    /// handle failure here

    override fun onLocationPermissionsDenied() {

    }

    override fun onVehiclesInBboxFailure() {

    }
}