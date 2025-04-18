package com.lattis.lattis.presentation.authentication.verifycode

import androidx.annotation.StringRes
import com.lattis.lattis.presentation.base.BaseView

interface EnterSecretCodeActivityView : BaseView{
    fun showConfirmationCodeError(@StringRes error: Int)
    fun hideConfirmationCodeError()
    fun onSecretCodeFail()
    fun onSecretCodeConfirmed()

    fun onInformationValid()
    fun onInformationInvalid()
}