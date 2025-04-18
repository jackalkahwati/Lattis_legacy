package com.lattis.lattis.presentation.authentication.forgotpassword

import android.text.TextUtils
import com.lattis.domain.usecase.authentication.ResetPasswordUseCase
import com.lattis.domain.usecase.user.SendForgotPasswordCodeUseCase
import com.lattis.domain.utils.StringUtils
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import javax.inject.Inject

class ForgotPasswordActivityPresenter @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val sendForgotPasswordCodeUseCase: SendForgotPasswordCodeUseCase
):ActivityPresenter<ForgotPasswordActivityView>(){


    private var resetPasswordEmail:String?=null
    private var resetPasswordCode:String?=null
    private var resetPassword:String?=null

    fun setEmail(email:String?){
        this.resetPasswordEmail = email
        view?.toggleSendVerificationButton(!TextUtils.isEmpty(resetPasswordEmail) && StringUtils.isValidEmail(resetPasswordEmail))
    }


    fun getEmail():String{
        return resetPasswordEmail!!
    }



    fun setCode(code:String?){
        this.resetPasswordCode = code
        checkForSubmitButton()
    }

    fun setPassword(password:String?){
        this.resetPassword = password
        checkForSubmitButton()

        if(TextUtils.isEmpty(resetPassword)){
            view?.passwordToggleButton(false)
        }else{
            view?.passwordToggleButton(true)
        }
    }

    fun checkForSubmitButton(){
        view?.toggleSubmitButton(!TextUtils.isEmpty(resetPassword) && !TextUtils.isEmpty(resetPasswordCode) && StringUtils.isLongerThanMinLength(resetPassword) && StringUtils.isShorterThanMaxLength(resetPassword))
    }





    fun sendConfirmationCodeForResetPassword() {
        subscriptions.add(
            sendForgotPasswordCodeUseCase
                .toEmail(resetPasswordEmail)
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        view.onCodeSentSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onCodeSentFailure()
                    }
                })
        )
    }


    fun resetPassword(){
        subscriptions.add(
            resetPasswordUseCase
                .withValues(resetPasswordEmail!!, resetPasswordCode!!, resetPassword!!)
                .execute(object : RxObserver<Boolean>(view, false) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        view.onPasswordResetSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onPasswordResetFailure()
                    }
                })
        )
    }
}