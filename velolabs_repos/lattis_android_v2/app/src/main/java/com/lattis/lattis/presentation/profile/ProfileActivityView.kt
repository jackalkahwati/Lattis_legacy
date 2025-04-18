package com.lattis.lattis.presentation.profile

import com.lattis.lattis.presentation.base.BaseView

interface ProfileActivityView :BaseView{


    fun setLastName(lastName: String)
    fun setFirstName(firstName: String)
    fun setPhoneNumber(phoneNumber: String)
    fun setEmail(email: String)

    fun onProfileFetchError()

    fun showLoadingForProfile()
    fun hideLoadingForProfile()
    
    fun onCodeSentSuccess()
    fun onCodeSentFailure()
    fun onCodeValidateSuccess()
    fun onCodeValidateFailure()

    fun onUserSaveFailure()

    fun onPasswordChangeSuccess()
    fun onPasswordChangeFailure()

    fun onCodeSentSuccessForUpdateEmail()
    fun onCodeSentFailureForUpdateEmail()

    fun onDeleteAccountSuccess()
    fun onDeleteAccountFailure()
}