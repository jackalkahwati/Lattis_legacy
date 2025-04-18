package com.lattis.ellipse.presentation.ui.base.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lattis.ellipse.Utils.FirebaseUtil;
import com.lattis.ellipse.presentation.ui.base.BasePresenter;
import com.lattis.ellipse.presentation.ui.base.BaseView;

public abstract class ActivityPresenter<View extends BaseView> extends BasePresenter<View> {

    void onCreate(@Nullable Bundle arguments, View view) {
        setView(view);
        setup(arguments);
        setSetupState(true);
    }

    void onReCreate(@NonNull Bundle savedInstanceState, View view){
        setView(view);
        if(!isSetup()){
            setup(savedInstanceState);
            setSetupState(true);
        }
    }

    void onDestroy(boolean isFinishing) {
        clearView();

        if (isFinishing) {
            finish();
        } else {
            onTempDestroy();
        }
    }

    protected void onTempDestroy() { }

    protected void onReenter() { }

    protected void logCustomException(Throwable e){
        FirebaseUtil.getInstance().logException(e);
    }

}
