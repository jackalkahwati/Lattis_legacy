package io.lattis.operator.presentation.authentication

import io.lattis.operator.presentation.base.BaseView

interface SignInActivityView : BaseView {

    fun onEmailPasswordValid()
    fun onEmailPasswordInvalid()
    fun passwordToggleButton(active:Boolean)

    fun onAuthenticationFailed()
    fun onAuthenticationSuccess()

}