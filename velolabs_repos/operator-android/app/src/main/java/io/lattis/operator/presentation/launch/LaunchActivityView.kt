package io.lattis.operator.presentation.authentication.launch

import io.lattis.operator.presentation.base.BaseView


interface LaunchActivityView : BaseView {
    fun onUserSavedFleetSuccess()
    fun onUserSavedFleetFailure()

}