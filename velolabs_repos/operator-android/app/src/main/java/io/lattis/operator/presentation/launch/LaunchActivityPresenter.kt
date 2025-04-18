package io.lattis.operator.presentation.authentication.launch

import io.lattis.domain.models.Fleet
import io.lattis.domain.usecase.fleet.GetUserSavedFleetUseCase
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.activity.ActivityPresenter
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LaunchActivityPresenter @Inject constructor(
    private val getUserSavedFleetUseCase: GetUserSavedFleetUseCase
) : ActivityPresenter<LaunchActivityView>() {
    private var launchScreenTimerSubscription: Disposable? = null
    var MILLISECONDS_FOR_LAUNCH_SCREEN = 3000
    var userSavedFleet:Fleet?=null



    @Synchronized
    fun subscribeToLaunchScreenTimer(active: Boolean) {
        if (active) {
            if (launchScreenTimerSubscription == null) {
                launchScreenTimerSubscription = Observable.timer(MILLISECONDS_FOR_LAUNCH_SCREEN.toLong(), TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        subscribeToLaunchScreenTimer(false)
                        getUserSavedFleet()
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

    fun getUserSavedFleet() {
        subscriptions.add(
            getUserSavedFleetUseCase
                .execute(object : RxObserver<Fleet>(view, false) {
                    override fun onNext(newUserSavedFleet:Fleet) {
                        super.onNext(newUserSavedFleet)
                        userSavedFleet = newUserSavedFleet
                        view?.onUserSavedFleetSuccess()
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onUserSavedFleetFailure()
                    }
                })
        )
    }



}