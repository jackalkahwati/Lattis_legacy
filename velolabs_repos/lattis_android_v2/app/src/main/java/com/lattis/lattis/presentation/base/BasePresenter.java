package com.lattis.lattis.presentation.base;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public abstract class BasePresenter<T extends BaseView> {

    protected CompositeDisposable subscriptions = new CompositeDisposable();

    protected T view;

    private boolean isLoading;

    private boolean isSetup;

    private boolean isViewUpToDate;

    protected void setup(@Nullable Bundle arguments) {}

    protected void updateViewState(){};

    protected void finish(){
        view = null;
        isSetup = false;
        isViewUpToDate = false;
        isLoading = false;
    }

    public void onClick(@IdRes int id){ }

    public void saveArguments(@NonNull Bundle arguments){ }

    public void onResume(T v) {
        setView(v);
        if(!isViewUpToDate() && !isLoading()){
            updateViewState();
            isViewUpToDate = true;
        }
    }

    public void onDestroy() {
        subscriptions.clear();
    }

    protected boolean isLoading() {
        return isLoading;
    }

    /**
     * Update load status and notify view layer.
     *
     * @param isLoading : true if the view is loading, false else
     */
    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    /**
     * Show loading in the presenter view
     * @param hideContent : true to hide the content below, false else
     */
    protected void showLoading(boolean hideContent){
        setLoading(true);
        if (view != null) {
            view.showLoading(hideContent);
        }
    }

    /**
     * Hide loading in the presenter view
     */
    protected void hideLoading(){
        setLoading(false);
        if (view != null) {
            view.hideLoading();
        }
    }

    protected boolean isSetup() {
        return isSetup;
    }

    public boolean isViewUpToDate() {
        return isViewUpToDate;
    }

    protected void setSetupState(boolean isSetup) {
        this.isSetup = isSetup;
    }

    protected void setView(@NonNull T view) {
        this.view = view;
    }

    protected void clearView() {
        this.view = null;
    }
}
