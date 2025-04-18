package com.lattis.lattis.presentation.authentication.signin

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import com.lattis.domain.usecase.authentication.SignInUseCase
import com.lattis.domain.utils.StringUtils
import com.lattis.domain.models.User
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusPresenter
import com.lattis.lattis.presentation.home.activity.HomeActivityPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.presentation.utils.FirebaseUtil
import io.lattis.lattis.R
import retrofit2.HttpException
import javax.inject.Inject

class SignInActivityPresenter @Inject constructor(
    private val signInUseCase: SignInUseCase
) : BaseUserCurrentStatusPresenter<SignInActivityView>() {

    private var email: String? = null
    private var password: String? = null


    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
    }

    override fun updateViewState() {}


    fun setEmail(email: String?) {
        this.email = email
        checkIfSignInDetailsComplete()
    }

    fun setPassword(password: String?) {
        this.password = password
        checkIfSignInDetailsComplete()
    }

    fun getPassword():String?{
        return password
    }

    fun checkIfSignInDetailsComplete(){
        if(email!=null && password!=null && StringUtils.isLongerThanMinLength(password) && StringUtils.isValidEmail(email)){
            view?.onEmailPasswordValid()
        }else{
            view?.onEmailPasswordInvalid()
        }

        if(TextUtils.isEmpty(password)){
            view?.passwordToggleButton(false)
        }else{
            view?.passwordToggleButton(true)
        }
    }


    fun trySignIn() {
        subscriptions.add(
            signInUseCase.withValues(email!!, password!!)
                .execute(object : RxObserver<User>(view, false) {
                    override fun onNext(user: User) {
                        super.onNext(user)
                        FirebaseUtil.instance?.addSignInEvent(user.id!!, email!!)
                        if (user.isVerified?:false) {
                            view?.onUserVerified(user.id)
                        } else {
                            view?.onUserNotVerified(user.id)
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        if (view != null && e != null && e is HttpException) {
                            if (e.code() == 404) {
                                if (view != null) view.onUserNotExists()
                            } else {
                                if (view != null) view.onAuthenticationFailed()
                            }
                        } else {
                            if (view != null) {
                                view.onAuthenticationFailed()
                            }
                        }
                    }
                })
        )
    }

}