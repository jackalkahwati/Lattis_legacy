package com.lattis.lattis.presentation.base.activity.location

import com.lattis.domain.models.LocationSettingsResult
import com.lattis.domain.models.Location
import com.lattis.lattis.presentation.base.BaseView

interface BaseLocationActivityView : BaseView{
    fun onLocationSettingsPermissionRequired(locationSettingsResult: LocationSettingsResult)
    fun onLocationSettingsON()
    fun onLocationSettingsNotAvailable()

    fun setUserPosition(location:Location)
}