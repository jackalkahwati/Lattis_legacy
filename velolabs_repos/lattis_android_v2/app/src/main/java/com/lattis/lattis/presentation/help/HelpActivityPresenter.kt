package com.lattis.lattis.presentation.help

import android.text.TextUtils
import com.lattis.domain.models.Help
import com.lattis.domain.usecase.ride.GetRideUseCase
import com.lattis.domain.models.Ride
import com.lattis.domain.usecase.apps.GetHelpInfoUseCase
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import javax.inject.Inject


class HelpActivityPresenter @Inject constructor(
    private val getRideUseCase: GetRideUseCase,
    private val getHelpInfoUseCase: GetHelpInfoUseCase
): ActivityPresenter<HelpActivityView>(){


    var DEFAULT_PHONE_NUMBER = "415-503-9744"
    var phoneNumber:String?=null
    var email:String?=null
    var web_link:String?=null


    fun getHelpInfo(){
        subscriptions.add(
            getHelpInfoUseCase
                .execute(object : RxObserver<Help>(view) {
                    override fun onNext(help: Help) {
                        super.onNext(help)
                        phoneNumber = help.phone_number
                        email = help.email
                        web_link = help.weblink
                        startViewing()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        phoneNumber = DEFAULT_PHONE_NUMBER
                        startViewing()
                    }
                })
        )
    }

    fun startViewing(){
        if(!TextUtils.isEmpty(phoneNumber)) view?.showPhoneNumber(phoneNumber!!)
        if(!TextUtils.isEmpty(email)) view?.showEmail(email!!)
        if(!TextUtils.isEmpty(web_link)) view?.showFaq(web_link!!)
        view?.hideLoadingForAddPromotion()
    }

}