package com.lattis.domain.repository

import com.google.android.libraries.places.api.model.Place
import com.lattis.domain.models.LocationSettingsResult
import com.lattis.domain.models.map.PlaceAutocomplete
import com.lattis.domain.models.Location
import io.reactivex.rxjava3.core.Observable
import java.util.*

interface LocationRepository {
    fun getLocationUpdates(freshLocationData:Boolean): Observable<Location>
    fun getLocationSettings(): Observable<LocationSettingsResult>
    fun getPlaces(constraint: String): Observable<ArrayList<PlaceAutocomplete>>
    fun getPlaceBuffer(placeId: String): Observable<Place>
}