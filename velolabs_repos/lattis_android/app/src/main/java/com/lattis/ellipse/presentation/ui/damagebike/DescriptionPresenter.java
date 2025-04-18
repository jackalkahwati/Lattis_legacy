package com.lattis.ellipse.presentation.ui.damagebike;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;


public class DescriptionPresenter extends ActivityPresenter<DescriptionView> {
    private final String DESCRIPTION_TAG = "DESCRIPTION";

    @Inject
    DescriptionPresenter() {

    }

    @Override
    protected void updateViewState() {

    }

    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if (arguments != null) {
            if (arguments.containsKey(DESCRIPTION_TAG)) {
                view.setDescriptionInfo(arguments.getString(DESCRIPTION_TAG));

            }
        }
    }

}
