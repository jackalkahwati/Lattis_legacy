package io.lattis.operator.presentation.map.locate

import io.lattis.operator.presentation.base.BaseView
import io.lattis.operator.presentation.base.activity.location.BaseLocationActivityView

interface LocateVehicleActivityView :BaseLocationActivityView {

    fun startShowingMap()
    fun updateVehicleLocation()
    fun hideProgressLoading()

    fun onRepositionTimerStart()
    fun onRepositionTimerOver()
}