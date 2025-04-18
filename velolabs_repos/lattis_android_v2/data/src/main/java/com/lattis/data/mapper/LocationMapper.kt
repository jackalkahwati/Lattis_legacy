package com.lattis.data.mapper


import com.lattis.domain.models.Location

/**
 * Created by raverat on 2/23/17.
 */

class LocationMapper : AbstractDataMapper<android.location.Location, Location>() {

    override fun mapIn(`object`: android.location.Location?): Location {
        var location=Location()
        if (`object` != null) {
            location.latitude = `object`.latitude
            location.longitude = `object`.longitude
            location.accuracy = `object`.accuracy
            location.time = `object`.time
            location.setHasAccuracy(`object`.hasAccuracy())
            location.setHasSpeed(`object`.hasSpeed())
            location.speed = `object`.speed
            location.provider = `object`.provider
        }
        return location
    }

    override fun mapOut(location: Location?): android.location.Location? {
        return null
    }

}
