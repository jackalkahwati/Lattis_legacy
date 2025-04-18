package com.lattis.lattis.presentation.qrscan.dummy

import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DummyLoadingPresenter @Inject constructor(

) : ActivityPresenter<DummyLoadingView>(){


    private var dummyScreenTimerSubscription: Disposable? = null
    var MILLISECONDS_FOR_DUMMY_SCREEN = 5000


    @Synchronized
    fun subscribeToDummyScreenTimer(active: Boolean) {
        if (active) {
            if (dummyScreenTimerSubscription == null) {
                dummyScreenTimerSubscription = Observable.timer(MILLISECONDS_FOR_DUMMY_SCREEN.toLong(), TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        subscribeToDummyScreenTimer(false)
                        view?.onDummyScreenTimer()
                    }) {
                        subscribeToDummyScreenTimer(false)
                        subscribeToDummyScreenTimer(true)
                    }
            }
        } else {
            if (dummyScreenTimerSubscription != null) {
                dummyScreenTimerSubscription?.dispose()
                dummyScreenTimerSubscription = null
            }
        }
    }

}