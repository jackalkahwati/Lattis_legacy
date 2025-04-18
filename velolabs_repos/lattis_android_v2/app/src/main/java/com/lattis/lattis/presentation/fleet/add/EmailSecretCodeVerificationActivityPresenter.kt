package com.lattis.lattis.presentation.fleet.add

import android.os.Bundle
import android.text.TextUtils
import com.lattis.domain.usecase.user.AddPrivateNetworkUseCase
import com.lattis.domain.usecase.user.ConfirmVerificationForPrivateNetworkUseCase
import com.lattis.domain.usecase.user.GetLocalUserUseCase
import com.lattis.domain.models.PrivateNetwork
import com.lattis.domain.models.User
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.fleet.add.EmailSecretCodeVerificationActivity.Companion.ARG_USER_FLEET_PRESENT
import com.lattis.lattis.presentation.ui.base.RxObserver
import javax.inject.Inject


class EmailSecretCodeVerificationActivityPresenter @Inject constructor(
    val confirmVerificationForPrivateNetworkUseCase: ConfirmVerificationForPrivateNetworkUseCase,
    val addPrivateNetworkUseCase: AddPrivateNetworkUseCase,
    val getLocalUserUseCase: GetLocalUserUseCase
) : ActivityPresenter<EmailSecretCodeVerificationActivityView>() {


    private var confirmationCode: String? = null
    val account_type: String = "private_account"
    var user:User?=null
    private var email: String? = null
    private var userFleetPresent = false


    fun setEmail(email: String?) {
        this.email = email
    }

    fun setConfirmationCode(confirmationCode:String?){
        this.confirmationCode=confirmationCode
        checkIfConfirmationCodeDetailsComplete()
    }

    override fun setup(arguments: Bundle?) {
        super.setup(arguments)

        if (arguments!!.containsKey(ARG_USER_FLEET_PRESENT)) {
            userFleetPresent =
                arguments.getBoolean(ARG_USER_FLEET_PRESENT)
        }
    }

    fun checkIfConfirmationCodeDetailsComplete(){
        if(TextUtils.isEmpty(confirmationCode)){
            view?.onInformationInvalid()
        }else{
            view?.onInformationValid()
        }
    }

    override fun updateViewState() {
    }

    fun submitConfirmationCode() {
        subscriptions.add(
            confirmVerificationForPrivateNetworkUseCase
                .forUser(user?.id)
                .forAccountType(account_type)
                .withConfirmationCode(confirmationCode)
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status:Boolean) {
                        view?.onSecretCodeConfirmed()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.hideLoadingForEmailSecretCode()
                        view?.onSecretCodeFail()
                    }
                })
        )
    }


    fun addPrivateNetwork(){
        subscriptions.add(
            addPrivateNetworkUseCase.withEmail(email)
                .execute(object : RxObserver<List<PrivateNetwork>>() {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        view?.hideLoadingForEmailSecretCode()
                        view?.onCodeSentFail()
                    }

                    override fun onNext(privateNetworks:List<PrivateNetwork>) {
                        view?.hideLoadingForEmailSecretCode()
                            if (privateNetworks.size > 0
                            ) {
                                    view?.onCodeSentSuccess()

                            } else {
                                if (view != null && userFleetPresent) {
                                    view?.onNoNewFleetWithCurrentFleetPresent()
                                } else if (view != null && !userFleetPresent) {
                                    view?.onNoNewFleetWithNoCurrentFleetPresent()
                                }
                            }

                    }
                })
        )
    }


    ////// Get user: start //////////////
    val getUser: Unit
        get() {
            subscriptions.add(getLocalUserUseCase.execute(object : RxObserver<User>(view, false) {
                override fun onNext(currUser: User) {

                    if (currUser != null) {
                        user = currUser
                    }

                }
                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            }))
        }



}