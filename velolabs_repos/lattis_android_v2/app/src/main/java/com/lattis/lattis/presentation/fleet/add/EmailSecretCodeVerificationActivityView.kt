package com.lattis.lattis.presentation.fleet.add

import androidx.annotation.StringRes
import com.lattis.lattis.presentation.base.BaseView

interface EmailSecretCodeVerificationActivityView : BaseView{
    fun showConfirmationCodeError(@StringRes error: Int)
    fun hideConfirmationCodeError()
    fun onSecretCodeFail()
    fun onSecretCodeConfirmed()

    fun onInformationValid()
    fun onInformationInvalid()

    fun onCodeSentFail()
    fun onCodeSentSuccess()
    fun onNoNewFleetWithCurrentFleetPresent()
    fun onNoNewFleetWithNoCurrentFleetPresent()

    fun showLoadingForEmailSecretCode(message: String?)
    fun hideLoadingForEmailSecretCode()
}