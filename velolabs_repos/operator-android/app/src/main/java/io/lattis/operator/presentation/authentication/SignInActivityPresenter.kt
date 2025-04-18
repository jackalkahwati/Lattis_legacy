package io.lattis.operator.presentation.authentication

import android.os.Bundle
import android.text.TextUtils
import io.lattis.domain.models.User
import io.lattis.domain.usecase.authentication.SignInUseCase
import io.lattis.operator.presentation.base.BasePresenter
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.activity.ActivityPresenter
import io.lattis.operator.utils.StringUtils
import retrofit2.adapter.rxjava2.HttpException
import javax.inject.Inject

class SignInActivityPresenter @Inject constructor(
    private val signInUseCase: SignInUseCase
) : ActivityPresenter<SignInActivityView>() {


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
                        view?.onAuthenticationSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onAuthenticationFailed()

                    }
                })
        )
    }

}