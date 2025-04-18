package com.lattis.lattis.presentation.authentication.forgotpassword

import com.lattis.lattis.presentation.base.BaseView

interface ForgotPasswordActivityView : BaseView{

    fun toggleSendVerificationButton(status:Boolean)
    fun toggleSubmitButton(status:Boolean)
    fun passwordToggleButton(active:Boolean)

    fun onPasswordResetSuccess()
    fun onPasswordResetFailure()

    fun onCodeSentSuccess()
    fun onCodeSentFailure()
}