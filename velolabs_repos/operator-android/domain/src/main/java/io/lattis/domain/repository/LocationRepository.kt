package io.lattis.domain.repository

import io.lattis.domain.models.Location
import io.lattis.domain.models.LocationSettingsResult
import io.reactivex.Observable
import java.util.*

interface LocationRepository {
    fun getLocationUpdates(freshLocationData:Boolean): Observable<Location>
    fun getLocationSettings(): Observable<LocationSettingsResult>
}