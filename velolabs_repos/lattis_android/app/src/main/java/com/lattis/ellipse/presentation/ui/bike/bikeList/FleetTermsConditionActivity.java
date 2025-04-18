package com.lattis.ellipse.presentation.ui.bike.bikeList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.NonNull;

import com.lattis.ellipse.presentation.ui.base.activity.BaseCloseActivity;
import com.lattis.ellipse.presentation.ui.base.fragment.WebViewFragment;

import javax.inject.Inject;

import io.lattis.ellipse.R;

/**
 * Created by lattis on 01/08/17.
 */

public class FleetTermsConditionActivity extends BaseCloseActivity<FleetTermsConditionPresenter> implements FleetTermsConditionView {

    @Inject
    FleetTermsConditionPresenter fleetTermsConditionPresenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected FleetTermsConditionPresenter getPresenter() {
        return fleetTermsConditionPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_fleet_t_c;
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.find_rite_terms_title));
        setToolBarBackGround(Color.WHITE);

    }

    @Override
    public void loadWebView(String url) {
        addFragment(R.id.fragment_container, WebViewFragment.newInstance(url),false);

    }

    public static void launchActivity(Activity activity, String url) {
        activity.startActivity(new Intent(activity, FleetTermsConditionActivity.class)
                .putExtra("URL", url));

    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
