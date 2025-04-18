package io.lattis.operator.presentation.base.activity.location

import io.lattis.domain.models.Location
import io.lattis.domain.models.LocationSettingsResult
import io.lattis.operator.presentation.base.BaseView


interface BaseLocationActivityView : BaseView {
    fun onLocationSettingsPermissionRequired(locationSettingsResult: LocationSettingsResult)
    fun onLocationSettingsON()
    fun onLocationSettingsNotAvailable()

    fun setUserPosition(location: Location)
}