package io.lattis.operator.presentation.fleet

import io.lattis.operator.presentation.base.BaseView

interface FleetDetailActivityView : BaseView{

    fun onUserSuccess()
    fun onLogOutSuccessfull()
    fun onLogOutFailure()
}