package com.lattis.lattis.presentation.authentication.signup

import android.text.TextUtils
import com.lattis.domain.error.SignUpValidationError
import com.lattis.domain.usecase.authentication.SignUpUseCase
import com.lattis.domain.utils.StringUtils
import com.lattis.domain.models.VerificationBundle
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusPresenter
import com.lattis.lattis.presentation.home.activity.HomeActivityPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.presentation.utils.FirebaseUtil
import io.lattis.lattis.R
import retrofit2.HttpException
import javax.inject.Inject

class SignUpActivityPresenter @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : BaseUserCurrentStatusPresenter<SignUpActivityView>() {

    private var email: String? = null
    private var password: String? = null
    private var firstName:String?=null
    private var lastName:String?=null


    fun setEmail(email: String?) {
        this.email = email
        checkIfSignInDetailsComplete()
    }

    fun setPassword(password: String?) {
        this.password = password
        checkIfSignInDetailsComplete()
        if(TextUtils.isEmpty(password)){
            view?.passwordToggleButton(false)
        }else{
            view?.passwordToggleButton(true)
        }
    }

    fun setFirstname(firstName: String?) {
        this.firstName = firstName
        checkIfSignInDetailsComplete()
    }

    fun setLastName(lastName: String?) {
        this.lastName = lastName
        checkIfSignInDetailsComplete()
    }

    fun checkIfSignInDetailsComplete(){
        if(!TextUtils.isEmpty(email) &&
            !TextUtils.isEmpty(password) &&
            !TextUtils.isEmpty(firstName) &&
            !TextUtils.isEmpty(lastName) &&
            !TextUtils.isEmpty(password) &&
            StringUtils.isValidEmail(email)){
            view?.onInformationValid()
        }else{
            view?.onInformationInvalid()
        }
    }


    fun getPassword():String?{
        return password
    }


    fun trySignUp() {
        subscriptions.add(
            signUpUseCase.withValue(
                    email!!,
                    firstName!!,
                    lastName!!,
                    password!!
                )
                .execute(object : RxObserver<VerificationBundle>(view) {
                    override fun onNext(verificationBundle: VerificationBundle) {
                        super.onNext(verificationBundle)
                        if (verificationBundle?.isVerified?:false) {
                            view?.onRegistrationSuccess(verificationBundle.userId)
                        } else {
                            view?.onUserNotVerified(verificationBundle.userId)
                        }
                        FirebaseUtil.instance
                            ?.addSignUpEvent(verificationBundle.userId!!, email!!)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        if (e != null && e is SignUpValidationError) {
                            view?.onPasswordInvalid()
                        } else if (e is HttpException) {
                            if (e.code() == 401) {
                                view?.showDuplicateError()
                            } else {
                                view?.onRegistrationFailed()
                            }
                        } else {
                            view?.onRegistrationFailed()
                        }
                    }
                })
        )
    }

}