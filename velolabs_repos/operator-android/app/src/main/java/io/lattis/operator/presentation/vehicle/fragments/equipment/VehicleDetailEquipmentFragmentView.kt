package io.lattis.operator.presentation.vehicle.fragments.equipment

import io.lattis.domain.models.ThingStatus
import io.lattis.operator.presentation.base.BaseView

interface VehicleDetailEquipmentFragmentView : BaseView{

    fun startShowingInformation()
    fun onThingStatusSuccess(thingStatus: ThingStatus)
    fun onThingStatusFailure()

    fun showProgressLoading()
    fun hideProgressLoading()

    fun onLockItFailure()
    fun onUnLockItFailure()

}