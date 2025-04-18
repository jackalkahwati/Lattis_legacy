package com.lattis.lattis.presentation.profile

import com.lattis.domain.usecase.user.*
import com.lattis.domain.models.User
import com.lattis.domain.usecase.logout.LogOutUseCase
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.presentation.utils.FirebaseUtil
import javax.inject.Inject
import javax.inject.Named

class ProfileActivityPresenter @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    @param:Named("ISDCode") val countryCode: Int,
    @param:Named("ISO31662Code") val countryCodeString: String,
    val sendCodeToPhoneNumberUseCase: SendCodeToPhoneNumberUseCase,
    val validateCodeForChangePhoneNumberUseCase: ValidateCodeForChangePhoneNumberUseCase,
    val saveUserUseCase: SaveUserUseCase,
    val changePasswordUseCase: ChangePasswordUseCase,
    val validateCodeForChangeMailUseCase: ValidateCodeForChangeMailUseCase,
    val updateEmailUseCase: UpdateEmailUseCase,
    val deleteAccountUseCase: DeleteAccountUseCase,
    val logOutUseCase: LogOutUseCase
): ActivityPresenter<ProfileActivityView>(){

    var user:User?=null
    var phoneNumber:String?=null
    var code:String?=null
    var newEmail:String?=null
    var codeForEmail:String?=null


    fun getUserProfile() {
        subscriptions.add(getUserUseCase.execute(object : RxObserver<User>(view, false) {
            override fun onNext(currUser: User) {
                view?.hideLoadingForProfile()
                if (currUser != null) {
                    user = currUser
                    view.setFirstName(user?.firstName?:"")
                    view.setLastName(user?.lastName?:"")
                    view.setPhoneNumber(user?.phoneNumber?:"")
                    view.setEmail(user?.email?:"")
                }else{
                    view?.onProfileFetchError()
                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                view?.hideLoadingForProfile()
                view?.onProfileFetchError()
            }
        }))
    }

    fun sendCodeToUpdatePhoneNumber() {
        subscriptions.add(
            sendCodeToPhoneNumberUseCase
                .withPhoneNumber(phoneNumber)
                .withCountryCode(countryCodeString)
                .execute(object : RxObserver<Boolean>(view, false) {
                    override fun onNext(status: Boolean) {
                        view?.onCodeSentSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onCodeSentFailure()
                    }
                })
        )
    }

    fun validateCodeForUpdatePhoneNumber() {
        subscriptions.add(
            validateCodeForChangePhoneNumberUseCase
                .withCode(code)
                .withPhoneNumber(phoneNumber)
                .execute(object : RxObserver<Boolean>(view, false) {
                    override fun onNext(status: Boolean) {
                        view?.onCodeValidateSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onCodeValidateFailure()
                    }
                })
        )
    }

    fun saveUser() {
        subscriptions.add(
            saveUserUseCase
                .withUser(user!!)
                .execute(object : RxObserver<User>(view, false) {
                    override fun onNext(user: User) {
                        getUserProfile()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onUserSaveFailure()
                    }
                })
        )
    }

    fun changePassword(oldPassword:String,newPassword:String) {
            subscriptions.add(
                changePasswordUseCase
                    .withPassword(oldPassword)
                    .withNewPassword(newPassword).execute(object : RxObserver<Boolean>() {
                        override fun onNext(status: Boolean) {
                            super.onNext(status)
                            view?.onPasswordChangeSuccess()
                        }

                        override fun onError(e: Throwable) {
                            super.onError(e!!)
                            view?.onPasswordChangeFailure()
                        }
                    })
            )
        }

    fun validateCodeForUpdateEmail() {
        subscriptions.add(
            validateCodeForChangeMailUseCase
                .withCode(codeForEmail)
                .withEmail(newEmail)
                .execute(object : RxObserver<Boolean>(view, false) {
                    override fun onNext(status: Boolean) {
                        getUserProfile()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view.onCodeValidateFailure()
                    }
                })
        )
    }

    fun sendCodeToUpdateEmail(){
        subscriptions.add(
            updateEmailUseCase.withEmail(newEmail).execute(object : RxObserver<Boolean>() {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    view.onCodeSentFailureForUpdateEmail()
                }

                override fun onNext(status: Boolean) {
                    view.onCodeSentSuccessForUpdateEmail()
                }
            })
        )
    }


    fun deleteAccount(){
        subscriptions.add(
            deleteAccountUseCase.execute(object : RxObserver<Boolean>() {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    view.onDeleteAccountFailure()
                }

                override fun onNext(status: Boolean) {
                    logOut()
                }
            })
        )
    }

    open fun logOut(): Unit {
        subscriptions.add(logOutUseCase.execute(object : RxObserver<Boolean>(view, false) {
            override fun onNext(success: Boolean) {
                view.onDeleteAccountSuccess()

            }
            override fun onError(e: Throwable) {
                super.onError(e)
                view.onDeleteAccountSuccess()
            }
        }))
    }



}