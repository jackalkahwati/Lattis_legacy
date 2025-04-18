package com.lattis.lattis.presentation.ride

import android.os.Bundle
import com.lattis.domain.models.RideSummary
import com.lattis.domain.usecase.ride.RideRatingUseCase
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import javax.inject.Inject

class RideSummaryPresenter @Inject constructor(
    val rideRatingUseCase: RideRatingUseCase
):ActivityPresenter<RideSummaryView>(){


    lateinit var featureCollection: FeatureCollection
    var rideSummary:RideSummary?=null


    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null) {
            if (arguments.containsKey(BikeBookedOrActiveRideFragment.RIDE_SUMMARY_DATA)) {
                rideSummary = arguments.getSerializable(BikeBookedOrActiveRideFragment.RIDE_SUMMARY_DATA) as RideSummary
            }
        }
    }


    fun setMarkerData(startLocation:LatLng?,endLocation: LatLng?){
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

    fun rateTheRide(rating: Int) {
        subscriptions.add(
            rideRatingUseCase
                .withTripId(rideSummary?.trip_id!!)
                .withRating(rating)
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        view.onRideRatingSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view.onRideRatingFailure()
                    }
                })
        )
    }

}