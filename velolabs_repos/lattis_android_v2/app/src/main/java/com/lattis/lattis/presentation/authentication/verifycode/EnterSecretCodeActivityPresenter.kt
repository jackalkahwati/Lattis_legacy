package com.lattis.lattis.presentation.authentication.verifycode

import android.os.Bundle
import android.text.TextUtils
import com.lattis.domain.error.ConfirmCodeValidationError
import com.lattis.domain.usecase.authentication.ConfirmVerificationCodeUseCase
import com.lattis.domain.usecase.authentication.SendVerificationCodeUseCase
import com.lattis.domain.models.User
import com.lattis.lattis.presentation.authentication.signin.SignInActivityView
import com.lattis.lattis.presentation.authentication.verifycode.EnterSecretCodeActivity.Companion.ARG_PASSWORD
import com.lattis.lattis.presentation.authentication.verifycode.EnterSecretCodeActivity.Companion.ARG_USER_ACCOUNT_TYPE
import com.lattis.lattis.presentation.authentication.verifycode.EnterSecretCodeActivity.Companion.ARG_USER_ID
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import io.lattis.lattis.R
import javax.inject.Inject


class EnterSecretCodeActivityPresenter @Inject constructor(
    val sendVerificationCodeUseCase: SendVerificationCodeUseCase,
    val confirmVerificationCodeUseCase: ConfirmVerificationCodeUseCase
) : ActivityPresenter<EnterSecretCodeActivityView>() {


    private var confirmationCode: String? = null
    var account_type: String? = null
    var userId: String? = null
    var password: String? = null


    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(ARG_USER_ID)) {
            this.userId = arguments.getString(ARG_USER_ID)
        }
        if (arguments != null && arguments.containsKey(ARG_USER_ACCOUNT_TYPE)) {
            this.account_type = arguments.getString(ARG_USER_ACCOUNT_TYPE)
        }
        if (arguments != null && arguments.containsKey(ARG_PASSWORD)) {
            this.password = arguments.getString(ARG_PASSWORD)
        }
    }

    fun setConfirmationCode(confirmationCode:String){
        this.confirmationCode = confirmationCode
        checkIfConfirmationCodeDetailsComplete()
    }

    fun checkIfConfirmationCodeDetailsComplete(){
        if(TextUtils.isEmpty(confirmationCode)){
            view?.onInformationInvalid()
        }else{
            view?.onInformationValid()
        }
    }

    override fun updateViewState() {
        sendConfirmationCode()
    }

    fun submitConfirmationCode() {
        subscriptions.add(
            confirmVerificationCodeUseCase
                .forUser(userId)
                .forAccountType(account_type)
                .withConfirmationCode(confirmationCode)
                .withPassword(password)
                .execute(object : RxObserver<User>(view) {
                    override fun onNext(user: User) {
                        view.onSecretCodeConfirmed()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        if (view != null) view.onSecretCodeFail()
                        if (e != null && e is ConfirmCodeValidationError) {
                            onValidationError(e as ConfirmCodeValidationError)
                        }
                    }
                })
        )
    }

    fun sendConfirmationCode() {
        subscriptions.add(
            sendVerificationCodeUseCase
                .forUser(userId)
                .forAccountType(account_type)
                .execute(object :RxObserver<Boolean>(view){
                    override fun onError(e: Throwable) {
                        super.onError(e)
                    }

                    override fun onNext(t: Boolean) {
                        super.onNext(t)
                    }
                })
        )
    }

    private fun onValidationError(error: ConfirmCodeValidationError) {
        val status: List<ConfirmCodeValidationError.Status> = error.status
        if (status.contains(ConfirmCodeValidationError.Status.INVALID_CONFIRMATION_CODE)) {
            view.showConfirmationCodeError(R.string.error_confirmation_code)
        } else {
            view.hideConfirmationCodeError()
        }
    }



}