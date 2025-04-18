package io.lattis.operator.presentation.fleet.fragments.vehicles

import io.lattis.operator.presentation.base.BaseView

interface FleetDetailVehicleFragmentView :BaseView{

    fun onVehiclesSuccess()
    fun onVehiclesFailure()
}