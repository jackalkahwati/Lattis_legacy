package com.lattis.lattis.presentation.parking

import android.os.Bundle
import com.lattis.domain.models.DockHub
import com.lattis.domain.usecase.parking.GetParkingZoneUseCase
import com.lattis.domain.usecase.parking.GetParkingsForFleetUseCase
import com.lattis.domain.models.Location
import com.lattis.domain.models.Parking
import com.lattis.domain.models.ParkingZone
import com.lattis.domain.usecase.parking.GetDockHubUseCase
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.parking.ParkingActivity.Companion.BIKE_ID
import com.lattis.lattis.presentation.parking.ParkingActivity.Companion.FLEET_ID
import com.lattis.lattis.presentation.parking.ParkingActivity.Companion.LATITUDE
import com.lattis.lattis.presentation.parking.ParkingActivity.Companion.LONGITUDE
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.presentation.utils.MapboxUtil
import com.lattis.lattis.utils.ResourceHelper.getResourcesByParkingType
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import javax.inject.Inject

class ParkingActivityPresenter @Inject constructor(
    val getParkingZoneUseCase: GetParkingZoneUseCase,
    val getParkingsForFleetUseCase: GetParkingsForFleetUseCase,
    val getDockHubUseCase: GetDockHubUseCase
) : ActivityPresenter<ParkingActivityView>(){


    var HOLE_COORDINATES: java.util.ArrayList<java.util.ArrayList<Point>> = java.util.ArrayList()
    var latLngBounds = LatLngBounds.Builder()
    var points: java.util.ArrayList<Point> = ArrayList()
    var featureCollection: FeatureCollection?=null
    val clusterQueryLayerIds:Array<String?> = arrayOfNulls<String>(MapboxUtil.CLUSTER_LAYER_ARRAY.size)
    var parkings:List<Parking>?=null
    var currentUserLocation: Location? = null
    var fleetId:Int?=null
    var bikeId:Int?=null
    var dockHubs:List<DockHub>?=null
    var parkingMarkerCoordinates: ArrayList<Feature> = ArrayList()
    private var isDockHubCheckRequiredAfterLocationUpdate = false


    //// parking spot and zone :start
    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null) {
            var latitude:Double?=null
            if (arguments.containsKey(LATITUDE)) {
                 latitude= arguments.getDouble(LATITUDE)
            }

            var longitude:Double?=null
            if (arguments.containsKey(LONGITUDE)) {
                longitude= arguments.getDouble(LONGITUDE)
            }

            if(latitude!=null && longitude!=null){
                currentUserLocation = Location(latitude,longitude)
            }

            if (arguments.containsKey(FLEET_ID)) {
                fleetId= arguments.getInt(FLEET_ID)
            }
            if (arguments.containsKey(BIKE_ID)) {
                bikeId= arguments.getInt(BIKE_ID)
            }
        }
    }

    fun findParkingsFromFleetId() {
        subscriptions.add(
            getParkingsForFleetUseCase
                .withFleetId(fleetId!!)
                .execute(object : RxObserver<List<Parking>>() {
                    override fun onNext(newParkings: List<Parking>) {
                        if (newParkings != null && newParkings.size>0) {
                            parkings = newParkings
                            setParkingMarkerData(parkings!!)
                            view?.onFindingParkingSuccess()
                        }else{
                            createParkingFeatureCollection()
                            view?.onFindParkingFailure()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        addUserCurrentLocationToLatLng()
                        createParkingFeatureCollection()
                        view?.onFindParkingFailure()

                    }
                })
        )
    }

    fun getParkingZones() {
        latLngBounds = LatLngBounds.Builder()
        HOLE_COORDINATES.clear()
        points.clear()

        subscriptions.add(
            getParkingZoneUseCase
                .withFleetID(fleetId!!)
                .execute(object : RxObserver<List<ParkingZone>>() {
                    override fun onNext(parkingZone: List<ParkingZone>) {
                        if (parkingZone != null && parkingZone.size>0) {
                            view.onFindingZoneSuccess(parkingZone)
                        }
                        findParkingsFromFleetId()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        findParkingsFromFleetId()
                    }
                })
        )
    }

    fun setParkingMarkerData(parkingList: List<Parking>){
        for(parking in parkingList){
            val feature =
                Feature.fromGeometry(
                    Point.fromLngLat(
                        parking?.longitude!!,
                        parking?.latitude!!
                    )
                )

            feature.addStringProperty("poi",getResourcesByParkingType(parking.type))
            feature.addNumberProperty(MapboxUtil.MARKER_ID,parking.parking_spot_id)
            feature.addBooleanProperty(MapboxUtil.MARKER_SELECTED,false)
            parkingMarkerCoordinates.add(feature)

            val parkingLatLng =
                LatLng(parking.latitude!!, parking.longitude!!)

            points.add(Point.fromLngLat(parking.longitude!!,parking.latitude!!))
            latLngBounds.include(parkingLatLng)
        }

//        if(currentUserLocation!=null){
//            points.add(Point.fromLngLat(currentUserLocation?.longitude!!,currentUserLocation?.latitude!!))
//            latLngBounds.include(LatLng(currentUserLocation?.latitude!!,currentUserLocation?.longitude!!))
//        }

        createParkingFeatureCollection()

    }

    fun addUserCurrentLocationToLatLng(){
        if(currentUserLocation!=null){
            latLngBounds.include(LatLng(currentUserLocation?.latitude!!,currentUserLocation?.longitude!!))
        }
    }


    fun getDockHubParking(){
        latLngBounds = LatLngBounds.Builder()
        HOLE_COORDINATES.clear()
        points.clear()
        dockHubs=null
        isDockHubCheckRequiredAfterLocationUpdate=false
        parkingMarkerCoordinates= ArrayList()
        featureCollection=null

        if(currentUserLocation!=null) {
            subscriptions.add(
                getDockHubUseCase
                    .withBikeId(bikeId!!)
                    .withLocation(currentUserLocation!!)
                    .execute(object : RxObserver<List<DockHub>>() {
                        override fun onNext(newDockHubs: List<DockHub>) {
                            if (newDockHubs != null && newDockHubs.size > 0) {
                                dockHubs = newDockHubs
                                view?.onDockHubsSuccess()
                            } else {
                                getParkingZones()
                            }
                        }

                        override fun onError(e: Throwable) {
                            super.onError(e)
                            getParkingZones()
                        }
                    })
            )
        }else {
            getParkingZones()
        }

    }

    fun setDockHubsMarkerData(){
        for(dockHub in dockHubs!!){
            val feature =
                Feature.fromGeometry(
                    Point.fromLngLat(
                        dockHub.longitude!!,
                        dockHub.latitude!!
                    )
                )

            feature.addStringProperty("poi", MapboxUtil.hub_dock_parking +"_"+dockHub.ports?.size!!)
            feature.addNumberProperty(MapboxUtil.MARKER_ID,dockHub.hub_id)
            feature.addStringProperty(MapboxUtil.MARKER_TYPE, MapboxUtil.hub_dock_parking)
            feature.addBooleanProperty(MapboxUtil.MARKER_SELECTED,false)
            parkingMarkerCoordinates.add(feature)

            val parkingLatLng =
                LatLng(dockHub.latitude!!, dockHub.longitude!!)
            points.add(Point.fromLngLat(dockHub.longitude!!,dockHub.latitude!!))
            latLngBounds.include(parkingLatLng)
        }
//        if(currentUserLocation!=null){
//            latLngBounds.include(LatLng(currentUserLocation?.latitude!!,currentUserLocation?.longitude!!))
//        }
        getParkingZones()
    }

    fun createParkingFeatureCollection(){
        featureCollection = FeatureCollection.fromFeatures(parkingMarkerCoordinates);
    }





}