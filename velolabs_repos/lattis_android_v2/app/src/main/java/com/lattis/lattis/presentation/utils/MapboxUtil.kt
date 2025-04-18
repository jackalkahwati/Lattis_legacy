package com.lattis.lattis.presentation.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import com.lattis.domain.models.Location
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdate
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.exceptions.InvalidLatLngBoundsException
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.layout_hub_dock_with_numbers.view.*


object MapboxUtil {

    val regular_selected = "regular_selected"
    val regular_unselected = "regular_unselected"

    val e_bike_selected = "e_bike_selected"
    val e_bike_unselected = "e_bike_unselected"

    val e_bike = "e_bike"
    val e_kick_scooter_25 = "e_kick_scooter_25"
    val e_kick_scooter_50 = "e_kick_scooter_50"
    val e_kick_scooter_75 = "e_kick_scooter_75"
    val e_kick_scooter_100 = "e_kick_scooter_100"

    val regular = "regular"
    val kick_scooter = "kick_scooter"
    val locker = "locker"
    val cluster = "cluster"
    val cart  = "cart"
    val hub_dock = "hub_dock"
    val kayak = "kayak"
    val parking_station = "parking"
    val docking_station = "docking"
    val moped = "moped"

    val kick_scooter_unselected = "kick_scooter_unselected"
    val kick_scooter_selected ="kick_scooter_selected"
    val user_current_location = "user_current_location"
    val nesw = "nesw"

    var generic_parking = "generic_parking"
    var parking_racks = "parking_racks"
    var parking_meter = "parking_meter"
    var charging_spots = "charging_spots"
    var hub_dock_parking = "hub_dock_parking"
    var hub_dock_bike = "hub_dock_bike"
    var hub_parking_bike = "hub_parking_bike"


    val POINT_COUNT = "point_count"

    val MARKER_SOURCE = "marker-source"
    val MARKER_LAYER = "marker-layer"
    val MARKER_POLYGON_SOURCE = "marker-polygon-source"
    val MARKER_POLYGON_LAYER = "marker-polygon-layer"
    val MARKER_LOCATION_SOURCE = "marker-location-source"
    val MARKER_LOCATION_LAYER = "marker-location-layer"
    val CLUSTER_LAYER_NUMBER = "cluster-"
    val COUNT_LAYER = "count"
    val CLUSTER_LAYER_ARRAY = intArrayOf(150, 20, 0)


    var unselected_size = 1.0f
    var selected_size = 1.4f
    val ICON_SELECTED = "selected"

    val MARKER_SELECTED = "MARKER_SELECTED"
    val MARKER_ID = "MARKER_ID"
    val MARKER_TYPE = "MARKER_TYPE"


     val REASONS = arrayOf(
        "REASON_API_GESTURE",
        "REASON_DEVELOPER_ANIMATION",
        "REASON_API_ANIMATION"
    )


    @SuppressLint("MissingPermission")
    fun activateLocationComponent(
        context: Context,
        mapboxMap: MapboxMap
    ) {

        val locationComponentOptions = LocationComponentOptions.builder(context)
            .elevation(2f)
            .build()

        val locationComponentActivationOptions = LocationComponentActivationOptions
            .builder(context, mapboxMap?.style!!)
            .locationComponentOptions(locationComponentOptions)
            .build()

        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            var locationComponent = mapboxMap.locationComponent

            // Activate with a built LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            if(!locationComponent.isLocationComponentEnabled()) {
                // Enable to make component visible
                locationComponent.setLocationComponentEnabled(true);

                // Set the component's camera mode
                locationComponent.setCameraMode(CameraMode.TRACKING);

                // Set the component's render mode
                locationComponent.setRenderMode(RenderMode.NORMAL);

                locationComponent.zoomWhileTracking(14.0,7000)

            }

        }
    }

    @SuppressLint("MissingPermission")
    fun disableLocationComponent(
        context: Context,
        mapboxMap: MapboxMap
    ) { // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            var locationComponent = mapboxMap.locationComponent
            locationComponent.setLocationComponentEnabled(false);
        }
    }


    fun setFixedZoomForSinglePoint(
        mapboxMap: MapboxMap,
        latLng: LatLng,
        topPadding:Double,
        bottomPadding:Double
    ) {
        if (latLng!=null) {
            val cameraPosition =
                CameraPosition.Builder()
                    .target(latLng)
                    .padding(0.0,topPadding,0.0, bottomPadding)
                    .zoom(14.0)
                    .build()
            mapboxMap.animateCamera(
                CameraUpdateFactory
                    .newCameraPosition(cameraPosition), 100
            )
        }
    }

    fun zoomToMarkers(
        mapboxMap: MapboxMap,
        builder: LatLngBounds.Builder,
        points: List<Point>
    ) {
        if (points != null && points.size > 1) { // build works for more than 1 item
            var bounds: LatLngBounds? = null
            bounds = try {
                builder.build()
            } catch (ex: InvalidLatLngBoundsException) {
                setFixedZoomForSinglePoint(mapboxMap, LatLng(points.get(0).latitude(),points.get(0).longitude()),Math.round(convertDpToPixel(220.0)).toDouble(),Math.round(convertDpToPixel(20.0)).toDouble())
                return
            }

            // Calculate distance between northeast and southwest
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                bounds?.northEast?.latitude!!, bounds.northEast.longitude,
                bounds?.southWest?.latitude!!, bounds.southWest.longitude, results
            )
            var cu: CameraUpdate? = null
            cu = if (results[0] < 1000) { // distance is less than 1 km -> set to zoom level 15
                CameraUpdateFactory.newLatLngZoom(
                    bounds.center,
                    15.0
                )
            } else {
                val bottomPadding =
                    Math.round(convertDpToPixel(220.0))
                        .toInt() // offset from edges of the map in pixels
                val topPadding = Math.round(convertDpToPixel(80.0)).toInt()
                val sidePadding = Math.round(convertDpToPixel(40.0)).toInt()
                CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    sidePadding,
                    topPadding,
                    sidePadding,
                    bottomPadding
                )
            }
            if (cu != null) {
                mapboxMap.animateCamera(cu, 3000)
            }
        } else if(points!=null && points.size>0){
            setFixedZoomForSinglePoint(mapboxMap, LatLng(points.get(0).latitude(),points.get(0).longitude()),Math.round(convertDpToPixel(220.0)).toDouble(),Math.round(convertDpToPixel(20.0)).toDouble())
        }
    }


    fun convertDpToPixel(dp: Double): Double {
        val metrics = Resources.getSystem().displayMetrics
        val px = dp * (metrics.densityDpi / 160)
        return Math.round(px).toDouble()
    }


    fun showUserCurrentLocation(mapboxMap: MapboxMap,userCurrentLocation: Location){


//        val markerSource: GeoJsonSource? = mapboxMap.getStyle()!!.getSourceAs(MARKER_LOCATION_SOURCE)
//        if(markerSource!=null) {
//
//            markerSource?.setGeoJson(
//                FeatureCollection.fromFeature(
//                    Feature.fromGeometry(
//                        Point.fromLngLat(
//                            userCurrentLocation.longitude,
//                            userCurrentLocation.latitude
//                        )
//                    )
//                )
//            )
//
//        }else{
            removeLayerAndSourceForLocation(mapboxMap)
            setUpSourceForLocation(mapboxMap,getFeatureCollectionForCurrentLocation(userCurrentLocation))
            setUpLayerForLocation(mapboxMap)
//        }
    }



    fun getFeatureCollectionForCurrentLocation(userCurrentLocation: Location) : FeatureCollection{
        val markerCoordinates: ArrayList<Feature> = ArrayList()

        if (userCurrentLocation != null) {
            val feature =
                Feature.fromGeometry(
                    Point.fromLngLat(
                        userCurrentLocation.longitude,
                        userCurrentLocation.latitude
                    )
                )

            feature.addStringProperty("poi", user_current_location)
            markerCoordinates.add(feature)
        }

        return FeatureCollection.fromFeatures(markerCoordinates);
    }

    fun setUpSourceForLocation(mapboxMap: MapboxMap,featureCollection: FeatureCollection){

        val geoJsonSource= GeoJsonSource(
            MARKER_LOCATION_SOURCE, featureCollection,
            GeoJsonOptions()
        )

        mapboxMap?.style!!.addSource(geoJsonSource!!)
    }


    private fun setUpLayerForLocation(mapboxMap: MapboxMap) {

        val locationLayer = SymbolLayer(MARKER_LOCATION_LAYER, MARKER_LOCATION_SOURCE)
            .withProperties(
                PropertyFactory.iconImage("{poi}"),  /* allows show all icons */
                PropertyFactory.iconAllowOverlap(true), /* when feature is in selected state, grow icon */
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconOffset(
                    arrayOf(0f, -9f)
                ),
                PropertyFactory.iconSize(
                    Expression.match(
                        Expression.toString(Expression.get(ICON_SELECTED)), // property selected is a number
                        Expression.literal(1.0f),        // default value
                        Expression.stop("false", 1.0),
                        Expression.stop(
                            "true",
                            1.4
                        )
                    )
                )
            )


        val markerLayer: Layer? = mapboxMap.getStyle()!!.getLayer(MARKER_LAYER)
        if(markerLayer==null) {
            mapboxMap.style?.addLayer(locationLayer)
        }else{
            mapboxMap.style?.addLayerBelow(locationLayer, MARKER_LAYER)
        }
    }


    fun removeLayerAndSourceForLocation(mapboxMap: MapboxMap){
        mapboxMap.style?.removeLayer(MARKER_LOCATION_LAYER)
        mapboxMap.style?.removeSource(MARKER_LOCATION_SOURCE)
    }

    fun removeLayerAndSourceForPolgon(mapboxMap: MapboxMap){
        mapboxMap.style?.removeLayer(MARKER_POLYGON_LAYER)
        mapboxMap.style?.removeSource(MARKER_POLYGON_SOURCE)
    }


    fun addPolyGons(mapboxMap: MapboxMap, polyGonList:List<List<Point>>,
                    fillOutlineColorRequired:Int,
                    fillColorRequired:Int,
                    fillOpacityRequired:Float
    ){

        mapboxMap.style?.addSource(GeoJsonSource(MARKER_POLYGON_SOURCE, Polygon.fromLngLats(polyGonList)))
        mapboxMap.style?.addLayer(
            FillLayer(MARKER_POLYGON_LAYER, MARKER_POLYGON_SOURCE).withProperties(
                fillColor(fillColorRequired),
                fillOutlineColor(fillOutlineColorRequired),
                fillOpacity(
                    Expression.match(
                        Expression.literal(fillOpacityRequired)
                    )
                )
            )
        )

    }



    fun showSingleMarker(mapboxMap: MapboxMap,location:Location,resourceString:String,uniqueId:Int,currentLocation:Location){
        val markerCoordinates: ArrayList<Feature> = ArrayList()
        var latLngBounds = LatLngBounds.Builder()
        var points: java.util.ArrayList<Point> = ArrayList()
        val clusterQueryLayerIds:Array<String?> = arrayOfNulls<String>(CLUSTER_LAYER_ARRAY.size)
        val feature =
            Feature.fromGeometry(
                Point.fromLngLat(
                    location?.longitude!!,
                    location?.latitude!!
                )
            )

        feature.addBooleanProperty("BIKE",true)
        feature.addStringProperty("poi",resourceString)
        feature.addNumberProperty(MARKER_ID,uniqueId)
        feature.addBooleanProperty(MARKER_SELECTED,false)
        markerCoordinates.add(feature)


        points.add(Point.fromLngLat(location.longitude!!,location.latitude!!))
        points.add(Point.fromLngLat(currentLocation.longitude!!,currentLocation.latitude!!))
        latLngBounds.include(LatLng(location.latitude!!, location.longitude!!))
        latLngBounds.include(LatLng(currentLocation.latitude!!, currentLocation.longitude!!))
        showMarker(mapboxMap,FeatureCollection.fromFeatures(markerCoordinates),clusterQueryLayerIds,latLngBounds,points)
    }


    fun showMarker(mapboxMap: MapboxMap,
                   featureCollection: FeatureCollection?,
                   clusterQueryLayerIds:Array<String?>,
                   builder: LatLngBounds.Builder,
                   latLngs: List<Point>

    ){
        if(featureCollection==null)return
        setUpSource(mapboxMap,featureCollection)
        setUpLayer(mapboxMap.style)
        setUpClusteredSource(mapboxMap,clusterQueryLayerIds)
        zoomToMarkers(mapboxMap,builder,latLngs)
    }


    fun setUpSource(mapboxMap: MapboxMap,featureCollection: FeatureCollection){
        removeLayerAndSourceForMarker(mapboxMap)
        val geoJsonSource= GeoJsonSource(MARKER_SOURCE, featureCollection,
            GeoJsonOptions().withCluster(true).withClusterRadius(50).withClusterMaxZoom(14)
        )
        mapboxMap.style?.addSource(geoJsonSource!!)
    }


    fun removeLayerAndSourceForMarker(mapboxMap: MapboxMap){
        mapboxMap.style?.removeLayer(MARKER_LAYER)

        for (i in CLUSTER_LAYER_ARRAY.indices) {
            mapboxMap.style?.removeLayer(CLUSTER_LAYER_NUMBER+"$i")
        }
        mapboxMap.style?.removeLayer(COUNT_LAYER)
        mapboxMap.style?.removeSource(MARKER_SOURCE)
    }


    private fun setUpLayer(loadedMapStyle: Style?) {

        loadedMapStyle?.addLayer(
            SymbolLayer(MARKER_LAYER, MARKER_SOURCE)
                .withProperties(
                    iconImage("{poi}"),  /* allows show all icons */
                    iconAllowOverlap(true), /* when feature is in selected state, grow icon */
                    iconIgnorePlacement(true),
                    iconOffset(
                        arrayOf(0f, -9f)),
                    iconSize(
                        match(
                            Expression.toString(get(MARKER_SELECTED)), // property selected is a number
                            literal(1.0f),        // default value
                            stop("false", 1.0),
                            stop("true", 1.4)         // if selected set it to original size
                        )
                    )
                )
        )
    }

    fun setUpClusteredSource(mapboxMap: MapboxMap,clusterQueryLayerIds:Array<String?>){
        // Use the earthquakes GeoJSON source to create three point ranges.
        // Use the earthquakes GeoJSON source to create three point ranges.


        for (i in CLUSTER_LAYER_ARRAY.indices) {

            clusterQueryLayerIds[i] = CLUSTER_LAYER_NUMBER+"$i"
            //Add clusters' SymbolLayers images
            val symbolLayer = SymbolLayer(CLUSTER_LAYER_NUMBER+"$i", MARKER_SOURCE)
            symbolLayer.setProperties(
                iconImage(cluster)
            )
            val pointCount =
                toNumber(get(POINT_COUNT))

            // Add a filter to the cluster layer that hides the icons based on "point_count"
            symbolLayer.setFilter(
                if (i == 0) all(
                    has(POINT_COUNT),
                    gte(pointCount, literal(CLUSTER_LAYER_ARRAY[i]))
                ) else all(
                    has(POINT_COUNT),
                    gte(pointCount, literal(CLUSTER_LAYER_ARRAY[i])),
                    lt(pointCount, literal(CLUSTER_LAYER_ARRAY[i - 1]))
                )
            )
            mapboxMap.style?.addLayer(symbolLayer)
        }

        //Add a SymbolLayer for the cluster data number point count

        //Add a SymbolLayer for the cluster data number point count
        mapboxMap.style?.addLayer(
            SymbolLayer(COUNT_LAYER, MARKER_SOURCE).withProperties(
                textField(toString(get(POINT_COUNT))),
                textSize(16f),
                textColor(Color.WHITE),
                textIgnorePlacement(true),
//                textOffset(arrayOf(0f, .5f)),
                textAllowOverlap(true)
            )
        )
    }

    fun selectFeature(mapboxMap: MapboxMap,featureCollection: FeatureCollection,feature: Feature) {
        feature.properties()!!.addProperty(MARKER_SELECTED, true)
        resetSource(mapboxMap,featureCollection)
    }

    fun resetPreviousSelected(featureCollection: FeatureCollection){
        var feature =getSelectedFeature(featureCollection)
        feature?.properties()?.addProperty(MARKER_SELECTED, false)
    }


    fun getSelectedFeature(featureCollection: FeatureCollection): Feature? {
        if (featureCollection != null) {
            for (feature in featureCollection.features()!!) {
                if (feature.getBooleanProperty(MARKER_SELECTED)) {
                    return feature
                }
            }
        }
        return null
    }

    fun resetSource(mapboxMap: MapboxMap,featureCollection: FeatureCollection){
        (mapboxMap.style?.getSource(MARKER_SOURCE) as GeoJsonSource).setGeoJson(featureCollection)
    }

    fun deselectAll(mapboxMap: MapboxMap,featureCollection: FeatureCollection?) {
        if(featureCollection==null)return
        for (feature in featureCollection.features()!!) {
            feature.properties()!!.addProperty(MARKER_SELECTED, false)
        }
        resetSource(mapboxMap,featureCollection)
    }



    fun moveCameraToLeavesBounds(mapboxMap: MapboxMap,featureCollectionToInspect: FeatureCollection) {
        val latLngList: java.util.ArrayList<LatLng> = java.util.ArrayList()
        if (featureCollectionToInspect.features() != null) {
            for (singleClusterFeature in featureCollectionToInspect.features()!!) {
                val clusterPoint: Point = singleClusterFeature.geometry() as Point
                if (clusterPoint != null) {
                    latLngList.add(
                        LatLng(
                            clusterPoint.latitude(),
                            clusterPoint.longitude()
                        )
                    )
                }
            }
            if (latLngList.size > 1) {
                val latLngBounds =
                    LatLngBounds.Builder()
                        .includes(latLngList)
                        .build()

                mapboxMap.easeCamera(
                    CameraUpdateFactory.newLatLngBounds(latLngBounds, convertDpToPixel(20.0).toInt(),convertDpToPixel(50.00).toInt(),convertDpToPixel(20.0).toInt(), convertDpToPixel(350.00).toInt()),
                    1000
                )


            }
        }
    }



    fun generateAndAddHubDockMarker(mapboxMap: MapboxMap, activity: Activity, number:Int,isParking: Boolean){
        if(isParking && mapboxMap.style?.getImage(hub_dock_parking+"_"+number)==null) {
            var layout= LayoutInflater.from(activity).inflate(R.layout.layout_hub_dock_with_numbers,null)
            layout.tv_hub_dock_number.setText(""+number)
            layout.iv_hub_dock_main.setImageResource(R.drawable.hub_dock_parking_half)
            layout.iv_hub_dock_number.setImageResource(R.drawable.hub_dock_parking_number)
            layout.tv_hub_dock_number.setTextColor(ContextCompat.getColor(activity,R.color.hub_dock_parking))
            val bitmap = SymbolGenerator.generate(layout)
            mapboxMap.style?.addImage(hub_dock_parking + "_" + number, bitmap)
        }else if(!isParking && mapboxMap.style?.getImage(hub_dock_bike+"_"+number)==null){
            var layout= LayoutInflater.from(activity).inflate(R.layout.layout_hub_dock_with_numbers,null)
            layout.tv_hub_dock_number.setText(""+number)
            layout.iv_hub_dock_main.setImageResource(R.drawable.hub_dock_bike)
            layout.iv_hub_dock_number.setImageResource(R.drawable.hub_dock_bike_number)
            layout.tv_hub_dock_number.setTextColor(ContextCompat.getColor(activity,R.color.hub_dock_bike))
            val bitmap = SymbolGenerator.generate(layout)
            mapboxMap.style?.addImage(hub_dock_bike + "_" + number, bitmap)
        }
    }

    fun generateHubParkingMarker(mapboxMap: MapboxMap, activity: Activity, number:Int){
        if(mapboxMap.style?.getImage(hub_parking_bike+"_"+number)==null) {
            var layout =
                LayoutInflater.from(activity).inflate(R.layout.layout_hub_dock_with_numbers, null)
            layout.tv_hub_dock_number.setText("" + number)
            layout.iv_hub_dock_main.setImageResource(R.drawable.parking_hub)
            layout.iv_hub_dock_number.setImageResource(R.drawable.hub_dock_bike_number)
            layout.tv_hub_dock_number.setTextColor(
                ContextCompat.getColor(
                    activity,
                    R.color.hub_dock_bike
                )
            )
            val bitmap = SymbolGenerator.generate(layout)
            mapboxMap.style?.addImage(hub_parking_bike + "_" + number, bitmap)
        }
    }

}