package com.lattis.ellipse.presentation;

import com.lattis.ellipse.domain.executor.PostExecutionThread;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;


@Singleton
public class UIThread implements PostExecutionThread {

    @Inject
    public UIThread() {
    }

    @Override public Scheduler getScheduler() {
        return AndroidSchedulers.mainThread();
    }
}
