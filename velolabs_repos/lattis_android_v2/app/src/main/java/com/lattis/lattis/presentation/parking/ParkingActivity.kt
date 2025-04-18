package com.lattis.lattis.presentation.parking

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.lattis.domain.models.ParkingZone
import com.lattis.domain.models.ParkingZoneGeometry
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import com.lattis.lattis.presentation.utils.MapboxUtil
import com.lattis.lattis.presentation.utils.MapboxUtil.moveCameraToLeavesBounds
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_parking.*
import java.util.ArrayList
import javax.inject.Inject

class ParkingActivity : BaseActivity<ParkingActivityPresenter, ParkingActivityView>(),
    ParkingActivityView, OnMapReadyCallback,MapboxMap.OnMapClickListener {


    private val REQUEST_CODE_ERROR = 4393

    companion object{
        val LATITUDE = "LATITUDE"
        val LONGITUDE = "LONGITUDE"
        val FLEET_ID = "FLEET_ID"
        val BIKE_ID = "BIKE_ID"

        fun getIntent(context: Context, latitude:Double?,longitude:Double?,fleetId:Int,bikeId:Int): Intent {
            val intent = Intent(context, ParkingActivity::class.java)
            if(latitude!==null && longitude!=null) {
                intent.putExtra(LATITUDE, latitude)
                intent.putExtra(LONGITUDE, longitude)
            }
            intent.putExtra(FLEET_ID,fleetId)
            intent.putExtra(BIKE_ID,bikeId)
            return intent
        }
    }

    @Inject
    override lateinit var presenter: ParkingActivityPresenter
    override val activityLayoutId = R.layout.activity_parking
    override var view: ParkingActivityView = this
    private var mapboxMap: MapboxMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapview_in_parking.onCreate(savedInstanceState)
        mapview_in_parking.getMapAsync(this)

        iv_close_in_parking.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        mapview_in_parking.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapview_in_parking.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapview_in_parking.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapview_in_parking.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapview_in_parking.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapview_in_parking.onDestroy()
    }


    override fun onMapReady(mapboxMapInReady: MapboxMap) {
        mapboxMap = mapboxMapInReady
        mapboxMap?.uiSettings?.isRotateGesturesEnabled = false
        mapboxMap?.uiSettings?.isCompassEnabled = false
        this.mapboxMap?.addOnMapClickListener(this)


        mapboxMap?.setStyle(Style.LIGHT) { style ->

            val cluster_image =
                BitmapFactory.decodeResource(resources, R.drawable.cluster)
            mapboxMap?.style!!.addImage(MapboxUtil.cluster,cluster_image)

            val user_current_location_image = BitmapFactory.decodeResource(
                resources,
                R.drawable.current_location_icon
            )
            mapboxMap?.style!!.addImage(MapboxUtil.user_current_location, user_current_location_image)


            //// parking icons
            val generic_parking_icon =
                BitmapFactory.decodeResource(resources, R.drawable.generic_parking_half)
            mapboxMap?.style!!.addImage(MapboxUtil.generic_parking, generic_parking_icon)

            val parking_meter_icon =
                BitmapFactory.decodeResource(resources, R.drawable.parking_meter_half)
            mapboxMap?.style!!.addImage(MapboxUtil.parking_meter, parking_meter_icon)

            val parking_rack_icon =
                BitmapFactory.decodeResource(resources, R.drawable.parking_rack_half)
            mapboxMap?.style!!.addImage(MapboxUtil.parking_racks, parking_rack_icon)

            val charging_spot_icon =
                BitmapFactory.decodeResource(resources, R.drawable.charging_spot_half)
            mapboxMap?.style!!.addImage(MapboxUtil.charging_spots, charging_spot_icon)

            showLoadingForParking(getString(R.string.loading))
            presenter.getDockHubParking()

        }
    }


    ////////////////////////// code to show parking zone: start ////////////////////////////
    override fun onFindingZoneSuccess(parkingZones: List<ParkingZone>) {
        MapboxUtil.removeLayerAndSourceForPolgon(mapboxMap!!)
        presenter.HOLE_COORDINATES?.clear()
        for (i in parkingZones.indices) {
            val parkingZone: ParkingZone? = parkingZones[i]
            if (parkingZone != null) {
                val parkingZoneGeometries: List<ParkingZoneGeometry>? = parkingZone.parkingZoneGeometry
                if (parkingZoneGeometries != null && parkingZoneGeometries.size > 0) {
                    val polygon: ArrayList<Point> =
                        ArrayList()
                    if (parkingZone.type.equals("circle",true)
                    ) {   //parkingzone is circular
                        for (j in parkingZoneGeometries.indices) {
                            val parkingZoneGeometry: ParkingZoneGeometry =
                                parkingZoneGeometries[j]
                            if (parkingZoneGeometry != null) {
                                val centreLatLng =
                                    LatLng()
                                centreLatLng.longitude = parkingZoneGeometry.longitude
                                centreLatLng.latitude = parkingZoneGeometry.latitude
                                val radius: Double = parkingZoneGeometry.radius
                                val degreesBetweenPoints = 8 //45 sides
                                val numberOfPoints =
                                    Math.floor(360 / degreesBetweenPoints.toDouble()).toInt()
                                val distRadians =
                                    radius / 6371000.0 // earth radius in meters
                                val centerLatRadians =
                                    centreLatLng.latitude * Math.PI / 180
                                val centerLonRadians =
                                    centreLatLng.longitude * Math.PI / 180
                                for (index in 0 until numberOfPoints) {
                                    val degrees =
                                        index * degreesBetweenPoints.toDouble()
                                    val degreeRadians =
                                        degrees * Math.PI / 180
                                    val pointLatRadians = Math.asin(
                                        Math.sin(centerLatRadians) * Math.cos(
                                            distRadians
                                        ) + Math.cos(centerLatRadians) * Math.sin(
                                            distRadians
                                        ) * Math.cos(degreeRadians)
                                    )
                                    val pointLonRadians =
                                        centerLonRadians + Math.atan2(
                                            Math.sin(degreeRadians) * Math.sin(
                                                distRadians
                                            ) * Math.cos(centerLatRadians),
                                            Math.cos(distRadians) - Math.sin(
                                                centerLatRadians
                                            ) * Math.sin(pointLatRadians)
                                        )
                                    val pointLat =
                                        pointLatRadians * 180 / Math.PI
                                    val pointLon =
                                        pointLonRadians * 180 / Math.PI
                                    val latLng =
                                        LatLng(pointLat, pointLon)
                                    presenter.latLngBounds.include(latLng)
                                    presenter.points.add(Point.fromLngLat(latLng.longitude,latLng.latitude))
                                    polygon.add(Point.fromLngLat(latLng.longitude,latLng.latitude))
                                }
                            }
                        }
                    } else {  // parkingzone type is non circular
                        for (j in parkingZoneGeometries.indices) {
                            val parkingZoneGeometry: ParkingZoneGeometry =
                                parkingZoneGeometries[j]
                            if (parkingZoneGeometry != null) {
                                val latLng =
                                    LatLng()
                                latLng.longitude = parkingZoneGeometry?.longitude
                                latLng.latitude = parkingZoneGeometry?.latitude
                                polygon.add(Point.fromLngLat(latLng.longitude,latLng.latitude))
                                presenter.latLngBounds.include(latLng)
                                presenter.points.add(Point.fromLngLat(latLng.longitude,latLng.latitude))
                            }
                        }
                    }
                    if (!polygon.isEmpty()) {
                        presenter.HOLE_COORDINATES.add(polygon)
                    }
                }
            }
        }
        drawPolygon()
    }


    private fun drawPolygon() {
        MapboxUtil.addPolyGons(
            mapboxMap!!, presenter.HOLE_COORDINATES,
            ContextCompat.getColor(this, R.color.parking_zone_fille_color),
            ContextCompat.getColor(this, R.color.parking_zone_out_line_color)
            , 0.1f
        )
    }
    ////////////////////////// code to show parking zone: end ////////////////////////////

    ////////////////////////// code to show parkings: start ////////////////////////////
    override fun onFindingParkingSuccess() {
        hideLoadingForParking()
        if(presenter.currentUserLocation!=null){
            MapboxUtil.showUserCurrentLocation(mapboxMap!!,presenter.currentUserLocation!!)
            presenter.points.add(Point.fromLngLat(presenter.currentUserLocation?.longitude!!,presenter.currentUserLocation?.latitude!!))
            presenter.latLngBounds.include(LatLng(presenter.currentUserLocation?.latitude!!,presenter.currentUserLocation?.longitude!!))
        }


        MapboxUtil.showMarker(
            mapboxMap!!,
            presenter.featureCollection!!,
            presenter.clusterQueryLayerIds,
            presenter.latLngBounds,
            presenter.points
        )
    }

    override fun onFindParkingFailure() {
        hideLoadingForParking()
        if(presenter.currentUserLocation!=null){
            MapboxUtil.showUserCurrentLocation(mapboxMap!!,presenter.currentUserLocation!!)
            presenter.points.add(Point.fromLngLat(presenter.currentUserLocation?.longitude!!,presenter.currentUserLocation?.latitude!!))
            presenter.latLngBounds.include(LatLng(presenter.currentUserLocation?.latitude!!,presenter.currentUserLocation?.longitude!!))
        }

        if(presenter.featureCollection!=null) MapboxUtil.showMarker(
            mapboxMap!!,
            presenter.featureCollection!!,
            presenter.clusterQueryLayerIds,
            presenter.latLngBounds,
            presenter.points
        )


        MapboxUtil.zoomToMarkers(
            mapboxMap!!,
            presenter.latLngBounds,
            presenter.points
        )
    }

    ////////////////////////// code to show parkings: end ////////////////////////////

    ////////////////////////// code for dock hub: start ////////////////////////////
    override fun onDockHubsSuccess() {
        for (dockHub in presenter.dockHubs!!) {
            if(dockHub.ports!=null){
                MapboxUtil.generateAndAddHubDockMarker(
                    mapboxMap!!,
                    this,
                    dockHub?.ports?.size!!,
                    true
                )
            }
        }
        presenter.setDockHubsMarkerData()
    }
    ////////////////////////// code for dock hub: end ////////////////////////////


    override fun onMapClick(point: LatLng): Boolean {
        handleClickIcon(mapboxMap?.projection!!.toScreenLocation(point))
        return true;
    }

    private fun handleClickIcon(screenPoint: PointF): Boolean {
        val features: List<Feature> = mapboxMap!!.queryRenderedFeatures(screenPoint, MapboxUtil.MARKER_LAYER)
         // check if clustered clicked
            val rectF = RectF(screenPoint.x - 10, screenPoint.y - 10, screenPoint.x + 10, screenPoint.y + 10)
            val mapClickFeatureList: List<Feature> = mapboxMap!!.queryRenderedFeatures(rectF, *presenter.clusterQueryLayerIds)

            if (mapClickFeatureList!=null && mapClickFeatureList.size > 0) {
                val clusterLeavesFeatureCollection: FeatureCollection =
                    (mapboxMap!!.style!!.getSource(MapboxUtil.MARKER_SOURCE) as GeoJsonSource).getClusterLeaves(
                        mapClickFeatureList[0],
                        8000, 0
                    )
                moveCameraToLeavesBounds(mapboxMap!!,clusterLeavesFeatureCollection)
            }
            return true

        return false
    }


    fun showLoadingForParking(message:String){
        parking_loading.ct_loading_title.text = message
        parking_loading.visibility = View.VISIBLE
    }

    fun hideLoadingForParking(){
        parking_loading.visibility = View.GONE
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}