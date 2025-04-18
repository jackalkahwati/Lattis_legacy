package io.lattis.operator.presentation.vehicle.fragments.vehicle

import io.lattis.operator.model.ChangeStatus
import io.lattis.operator.presentation.base.BaseView

interface VehicleDetailVehicleFragmentView : BaseView{

    fun startShowingInformation()

    fun onChangeStatusSuccess(changeStatus: ChangeStatus)
    fun onChangeStatusFailure()
}