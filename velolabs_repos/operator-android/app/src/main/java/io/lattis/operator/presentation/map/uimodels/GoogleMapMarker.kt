package io.lattis.operator.presentation.map.uimodels

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import io.lattis.domain.models.Vehicle

class GoogleMapMarker (lat: Double,
                       lng: Double,
                       vehicle: Vehicle?,
                       isLocation:Boolean=false
) : ClusterItem {

    private val position: LatLng
    private val vehicle: Vehicle?
    private val isLocation:Boolean

    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String? {
        return vehicle?.name
    }

    override fun getSnippet(): String? {
        return null
    }

    fun getVehicle(): Vehicle? {
        return vehicle
    }

    fun isLocation():Boolean{
        return isLocation
    }

    init {
        position = LatLng(lat, lng)
        this.vehicle = vehicle
        this.isLocation = isLocation
    }
}