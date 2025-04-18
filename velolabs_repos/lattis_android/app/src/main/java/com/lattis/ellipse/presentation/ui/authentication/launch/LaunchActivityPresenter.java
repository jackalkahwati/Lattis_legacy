package com.lattis.ellipse.presentation.ui.authentication.launch;

import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by ssd3 on 10/3/17.
 */

public class LaunchActivityPresenter extends ActivityPresenter<LaunchActivityView> {

    private Disposable launchScreenTimerSubscription=null;
    private static final int MILLISECONDS_FOR_LAUNCH_SCREEN = 3000;

    @Inject
    public LaunchActivityPresenter() {
    }

    @Override
    protected void updateViewState() {
        super.updateViewState();
    }

    public synchronized void subscribeToLaunchScreenTimer(boolean active) {
        if (active) {
            if (launchScreenTimerSubscription == null) {
                launchScreenTimerSubscription = Observable.timer(MILLISECONDS_FOR_LAUNCH_SCREEN, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Long>() {
                            public void accept(Long aLong) {
                                subscribeToLaunchScreenTimer(false);
                                if(view!=null)
                                    view.onLaunchScreenTimer();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                subscribeToLaunchScreenTimer(false);
                                subscribeToLaunchScreenTimer(true);
                            }
                        });
            }
        } else {
            if (launchScreenTimerSubscription != null) {
                launchScreenTimerSubscription.dispose();
                launchScreenTimerSubscription = null;
            }
        }
    }
}
