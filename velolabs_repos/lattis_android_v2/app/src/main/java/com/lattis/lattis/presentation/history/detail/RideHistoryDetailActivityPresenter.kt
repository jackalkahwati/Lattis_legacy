package com.lattis.lattis.presentation.history.detail

import android.os.Bundle
import com.google.gson.Gson
import com.lattis.domain.models.RideSummary
import com.lattis.domain.models.RideHistory
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.history.detail.RideHistoryDetailActivity.Companion.RIDE_HISTORY_DATA
import com.lattis.lattis.presentation.ride.BikeBookedOrActiveRideFragment
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import javax.inject.Inject


class RideHistoryDetailActivityPresenter @Inject constructor(

) : ActivityPresenter<RideHistoryDetailActivityView>(){

    var rideHistoryData:RideHistory.RideHistoryData?=null
    lateinit var featureCollection: FeatureCollection

    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null) {
            if (arguments.containsKey(RIDE_HISTORY_DATA)) {
                rideHistoryData = Gson().fromJson(arguments.getString(RIDE_HISTORY_DATA) , RideHistory.RideHistoryData::class.java)
            }
        }
    }

    fun setMarkerData(startLocation: LatLng?, endLocation: LatLng?){
        val markerCoordinates: ArrayList<Feature> = ArrayList()

        if(startLocation!=null){
            val feature =
                Feature.fromGeometry(
                    Point.fromLngLat(
                        startLocation.longitude,
                        startLocation.latitude
                    )
                )

            feature.addStringProperty("poi","icon_start_drawable")
            markerCoordinates.add(feature)
        }

        if(endLocation!=null){
            val feature =
                Feature.fromGeometry(
                    Point.fromLngLat(
                        endLocation.longitude,
                        endLocation.latitude
                    )
                )

            feature.addStringProperty("poi","icon_end_drawable")
            markerCoordinates.add(feature)
        }


        featureCollection = FeatureCollection.fromFeatures(markerCoordinates);

    }

}