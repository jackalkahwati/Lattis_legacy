package io.lattis.data.mapper


import io.lattis.domain.models.Location


class LocationMapper : AbstractDataMapper<android.location.Location, Location>() {

    override fun mapIn(`object`: android.location.Location?): Location {
        return Location(`object`?.latitude!!,`object`?.longitude!!)
    }

    override fun mapOut(location: Location?): android.location.Location? {
        return null
    }

}
