package com.lattis.lattis.presentation.authentication.signup

import com.lattis.lattis.presentation.base.BaseView
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusView

interface SignUpActivityView : BaseUserCurrentStatusView {

    fun onInformationValid()
    fun onInformationInvalid()

    fun onRegistrationSuccess(userId: String?)
    fun onUserNotVerified(userId: String?)
    fun showDuplicateError()
    fun onPasswordInvalid()
    fun onRegistrationFailed()
    fun passwordToggleButton(active:Boolean)

}