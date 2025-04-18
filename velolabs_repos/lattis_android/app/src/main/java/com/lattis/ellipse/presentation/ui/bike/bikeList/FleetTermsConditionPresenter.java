package com.lattis.ellipse.presentation.ui.bike.bikeList;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;

/**
 * Created by lattis on 01/08/17.
 */

public class FleetTermsConditionPresenter extends ActivityPresenter<FleetTermsConditionView> {
    @Inject
    FleetTermsConditionPresenter() {

    }

    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if (arguments != null) {
            if (arguments.containsKey("URL")) {
                view.loadWebView(arguments.getString("URL"));

            }
        }
    }

}
