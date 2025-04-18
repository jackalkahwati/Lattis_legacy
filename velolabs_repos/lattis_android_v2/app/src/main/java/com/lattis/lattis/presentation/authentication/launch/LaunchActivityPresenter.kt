package com.lattis.lattis.presentation.authentication.launch

import com.lattis.domain.models.Subscription
import com.lattis.domain.models.UserCurrentStatus
import com.lattis.domain.usecase.membership.GetSubscriptionsUseCase
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LaunchActivityPresenter @Inject constructor(
    val getSubscriptionsUseCase: GetSubscriptionsUseCase
) : BaseUserCurrentStatusPresenter<LaunchActivityView>() {
    private var launchScreenTimerSubscription: Disposable? = null
    var MILLISECONDS_FOR_LAUNCH_SCREEN = 3000
    var subscriptionList:List<Subscription>?=null


    @Synchronized
    fun subscribeToLaunchScreenTimer(active: Boolean) {
        if (active) {
            if (launchScreenTimerSubscription == null) {
                launchScreenTimerSubscription = Observable.timer(MILLISECONDS_FOR_LAUNCH_SCREEN.toLong(), TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        subscribeToLaunchScreenTimer(false)
                        view?.onLaunchScreenTimer()
                    }) {
                        subscribeToLaunchScreenTimer(false)
                        subscribeToLaunchScreenTimer(true)
                    }
            }
        } else {
            if (launchScreenTimerSubscription != null) {
                launchScreenTimerSubscription?.dispose()
                launchScreenTimerSubscription = null
            }
        }
    }

    fun getSubscriptions(){
        subscriptions.add(
            getSubscriptionsUseCase.execute(
                object : RxObserver<List<Subscription>>(view) {
                    override fun onError(e: Throwable) {
                        super.onError(e)
                    }

                    override fun onNext(t: List<Subscription>) {
                        super.onNext(t)
                        subscriptionList=t
                    }
                }
            )
        )
    }



}