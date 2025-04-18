package com.lattis.data.database.mapper

import com.lattis.data.database.base.AbstractRealmDataMapper
import com.lattis.data.database.model.RealmLocation
import com.lattis.domain.models.Location
import javax.inject.Inject

class RealmLocationMapper @Inject constructor() :
    AbstractRealmDataMapper<Location, RealmLocation>() {
    override fun mapIn(location: Location): RealmLocation {
        val realmLocation = RealmLocation()
        location.latitude = location.latitude
        location.longitude = location.latitude
        return realmLocation
    }

    override fun mapOut(realmLocation: RealmLocation): Location {
        val location =
            Location()
        location.latitude = realmLocation.latitude
        location.longitude = realmLocation.longitude
        return location
    }
}