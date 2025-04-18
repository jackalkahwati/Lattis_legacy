package io.lattis.operator.presentation.fleet.fragments.map

import io.lattis.operator.presentation.base.BaseView
import io.operator.lattis.presentation.base.fragment.location.BaseLocationFragmentView

interface FleetDetailMapFragmentView : BaseLocationFragmentView{

    fun showMarkers()
    fun onVehiclesInBboxFailure()

    fun onRepositionTimerStart()
    fun onRepositionTimerOver()
}