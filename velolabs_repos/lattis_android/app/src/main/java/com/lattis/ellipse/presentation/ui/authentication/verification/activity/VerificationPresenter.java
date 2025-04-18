package com.lattis.ellipse.presentation.ui.authentication.verification.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;

import static com.lattis.ellipse.presentation.ui.authentication.verification.activity.VerificationActivity.ARG_USER_ID;

public class VerificationPresenter extends ActivityPresenter<VerificationView> {

    private long userId;

    @Inject
    VerificationPresenter() {}

    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if(arguments.containsKey(ARG_USER_ID)){
            this.userId = arguments.getLong(ARG_USER_ID);
        }
    }

    @Override
    protected void updateViewState() {
    }

    public long getUserId() {
        return userId;
    }
}
