package io.lattis.operator.presentation.map.base

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import io.lattis.operator.R
import io.lattis.operator.presentation.map.uimodels.GoogleMapMarker
import io.lattis.operator.utils.MarkerImageUtil.Companion.getBikeResource
import io.lattis.operator.utils.MarkerImageUtil.Companion.getLocationMarker


class MarkerClusterRenderer constructor(
    val context: Context,
    mMap: GoogleMap,
    clusterManager: ClusterManager<GoogleMapMarker>
) : DefaultClusterRenderer<GoogleMapMarker>(context, mMap, clusterManager) {

    override fun onBeforeClusterItemRendered(item: GoogleMapMarker, markerOptions: MarkerOptions) {
        // use this to make your change to the marker option
        // for the marker before it gets render on the map
        if(item.isLocation()){
            markerOptions.icon(getLocationMarker())
        }else{
            markerOptions.icon(getBikeResource(item.getVehicle()?.group?.type!!,item.getVehicle()?.batteryLevel))
        }
    }
}