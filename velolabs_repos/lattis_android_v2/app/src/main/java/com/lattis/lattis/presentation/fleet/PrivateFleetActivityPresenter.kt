package com.lattis.lattis.presentation.fleet

import com.lattis.domain.usecase.user.GetUserUseCase
import com.lattis.domain.models.User
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import javax.inject.Inject

class PrivateFleetActivityPresenter @Inject constructor(
    private val getUserUseCase: GetUserUseCase
) :ActivityPresenter<PrivateFleetActivityView>(){
    var user:User?=null
    var attemptToAddPrivateFleet:Boolean = false

    fun getUserProfile() {
        subscriptions.add(getUserUseCase.execute(object : RxObserver<User>(view, false) {
            override fun onNext(currUser: User) {
                view?.hideLoadingForPrivateFleet()
                if (currUser != null) {
                    user= currUser
                    if (user?.privateNetworks != null && user?.privateNetworks?.size?:0 > 0) {
                        view?.showPrivateFleetListView()
                    } else {
                        view?.showNoPrivateFleetView()
                    }

                }else{
                    view?.onProfileFetchError()
                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                view?.hideLoadingForPrivateFleet()
                view?.onProfileFetchError()
            }
        }))
    }


    fun fleetPresent():Boolean{
        return if(user!=null &&
                    user?.privateNetworks!=null &&
                    user?.privateNetworks?.size!! > 0
                ) true else false
    }

}