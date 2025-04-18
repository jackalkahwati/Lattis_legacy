package io.lattis.operator.presentation.map.filter

import io.lattis.operator.presentation.base.BaseView
import io.lattis.operator.presentation.base.activity.location.BaseLocationActivityView

interface FilterVehiclesActivityView : BaseLocationActivityView{
    fun setTitle(title: String)
    fun showMarkers()
    fun onVehiclesInBboxFailure()
}