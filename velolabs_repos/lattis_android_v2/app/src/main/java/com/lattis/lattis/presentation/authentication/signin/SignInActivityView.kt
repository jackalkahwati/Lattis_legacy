package com.lattis.lattis.presentation.authentication.signin

import androidx.annotation.StringRes
import com.lattis.lattis.presentation.base.BaseView
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusView

interface SignInActivityView : BaseUserCurrentStatusView {

    fun onUserVerified(email: String?)
    fun onUserNotVerified(email: String?)
    fun onAuthenticationFailed()
    fun onUserNotExists()

    fun onEmailPasswordValid()
    fun onEmailPasswordInvalid()

    fun passwordToggleButton(active:Boolean)



}