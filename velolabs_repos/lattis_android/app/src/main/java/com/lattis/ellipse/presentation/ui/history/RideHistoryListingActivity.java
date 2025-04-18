package com.lattis.ellipse.presentation.ui.history;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.RelativeLayout;

import com.lattis.ellipse.data.network.model.response.history.RideHistoryDataResponse;
import com.lattis.ellipse.presentation.ui.base.activity.BaseBackArrowActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomTextView;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import io.lattis.ellipse.R;


public class RideHistoryListingActivity extends BaseBackArrowActivity<RideHistoryListingActivityPresenter> implements RideHistoryListingActivityView {
    @BindView(R.id.rl_no_trips)
    RelativeLayout noTripeYetView;
    @Inject
    RideHistoryListingActivityPresenter rideHistoryListingActivityPresenter;
    @BindView((R.id.rl_loading_operation))
    View ride_history_loading_operation_view;
    @BindView(R.id.rv_trip_list)
    RecyclerView trioListView;
    public static int REQUEST_CODE_TRIP_HISTORY_FAILURE = 3245;

    @BindView(R.id.label_operation_name)
    CustomTextView loading_operation_name;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.menu_history));

    }

    @NonNull
    @Override
    protected RideHistoryListingActivityPresenter getPresenter() {
        return rideHistoryListingActivityPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_ride_history_listing;
    }

    @Override
    public void onRideHistorySuccess(List<RideHistoryDataResponse> rideHistoryDataResponses) {
        Collections.reverse(rideHistoryDataResponses);

        if(rideHistoryDataResponses!=null && rideHistoryDataResponses.size()>0
                && rideHistoryDataResponses.get(0).getDate_endtrip()==null){
            rideHistoryDataResponses.remove(0);
        }

        hideOperationLoading();
        trioListView.setVisibility(View.VISIBLE);
        trioListView.setLayoutManager(new LinearLayoutManager(this));
        trioListView.setAdapter(new TripsListAdapter(this, rideHistoryDataResponses));

    }

    @Override
    public void onRideHistoryFailure() {
        hideOperationLoading();
        PopUpActivity.launchForResult(this, REQUEST_CODE_TRIP_HISTORY_FAILURE, getString(R.string.alert_error_server_title),
                getString(R.string.alert_error_server_subtitle), "", getString(R.string.ok));

    }

    @Override
    public void onNoRideHistory() {
        hideOperationLoading();
    }

    @Override
    public void showOperationLoading() {
        ride_history_loading_operation_view.setVisibility(View.VISIBLE);
        loading_operation_name.setText(getString(R.string.loading));

    }

    @Override
    public void hideOperationLoading() {
        ride_history_loading_operation_view.setVisibility(View.GONE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TRIP_HISTORY_FAILURE && resultCode == RESULT_OK) {
            finish();
        } else if (requestCode == REQUEST_CODE_TRIP_HISTORY_FAILURE && resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
