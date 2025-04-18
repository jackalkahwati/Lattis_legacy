package com.lattis.ellipse.presentation.ui.bike;

import androidx.annotation.NonNull;

import com.lattis.ellipse.presentation.ui.base.activity.BaseAuthenticatedActivity;

import javax.inject.Inject;

import butterknife.OnClick;
import io.lattis.ellipse.R;

/**
 * Created by ssd3 on 8/25/17.
 */

public class WhyBeginTripGreyOutActivity extends BaseAuthenticatedActivity<WhyBeginTripGreyOutActivityPresenter> implements WhyBeginTripGreyOutActivityView {

    @Inject
    WhyBeginTripGreyOutActivityPresenter presenter;


    @Override
    protected void inject() {
        getComponent().inject(this);
    }


    @NonNull
    @Override
    protected WhyBeginTripGreyOutActivityPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_why_begin_trip_grey_out;
    }


    @OnClick({R.id.iv_close_why_begin_trip_grey_out_pop_up,R.id.cb_ok_why_begin_trip_grey_out_pop_up})
    public void closeThisActivity(){
        finish();
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}