package io.lattis.operator.presentation.utils

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.Cluster
import io.lattis.operator.presentation.map.uimodels.GoogleMapMarker
import java.lang.Exception

object GooleMapUtil {

    fun setFixedZoomForSinglePoint(
        googleMap: GoogleMap,
        latLng: LatLng,
        topPadding:Double,
        bottomPadding:Double
    ) {
        if (latLng!=null) {
            val cameraPosition =
                CameraPosition.Builder()
                    .target(latLng)
                    .zoom(14.0F)
                    .build()
            googleMap.animateCamera(
                CameraUpdateFactory
                    .newCameraPosition(cameraPosition)
            )
        }
    }

    fun zoomToMakers(googleMap: GoogleMap,markers:ArrayList<GoogleMapMarker>){
        //Calculate the markers to get their position
        //Calculate the markers to get their position
        try {
            val b = LatLngBounds.Builder()
            for (m in markers) {
                b.include(m.getPosition())
            }
            val bounds = b.build()

            val cu = CameraUpdateFactory.newLatLngBounds(bounds, 350)
            googleMap.animateCamera(cu)
        }catch (e:Exception){

        }
    }

    fun zoomToMarkers(googleMap: GoogleMap,cluster: Cluster<GoogleMapMarker>){
        try {
            val builder = LatLngBounds.builder()
            for (item in cluster.items) {
                builder.include(item.position)
            }
            val bounds = builder.build()
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 350))
        }catch (e:Exception){

        }
    }
}