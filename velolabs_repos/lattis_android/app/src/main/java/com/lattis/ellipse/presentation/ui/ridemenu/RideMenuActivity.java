package com.lattis.ellipse.presentation.ui.ridemenu;

import android.content.Intent;
import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.activity.BaseAuthenticatedActivity;
import com.lattis.ellipse.presentation.ui.biketheft.ReportBikeTheft;
import com.lattis.ellipse.presentation.ui.damagebike.DamageBikeActivity;

import javax.inject.Inject;

import butterknife.OnClick;
import io.lattis.ellipse.R;

import static com.lattis.ellipse.presentation.ui.ride.EndRideFragment.DAMAGE_REPORT_SUCCESS;
import static com.lattis.ellipse.presentation.ui.ride.EndRideFragment.REPORT_THEFT_SUCCESS;

/**
 * Created by Velo Labs Android on 17-04-2017.
 */

public class RideMenuActivity extends BaseAuthenticatedActivity<RideMenuPresenter> {
    private Ride ride;
    private String jsonString;
    private int REQUEST_CODE_THEFT_REPORT = 9000;
    private int REQUEST_CODE_DAMAGE_REPORT = 9001;


    @Inject
    RideMenuPresenter rideMenuPresenter;

    @OnClick(R.id.iv_cross)
    public void onCloseClicked() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @OnClick(R.id.rl_damage)
    public void onReportDamageClicked() {
        startActivityForResult(new Intent(this, DamageBikeActivity.class).putExtra("BIKE_ID", ride.getBikeId()),REQUEST_CODE_DAMAGE_REPORT);
    }

    @OnClick(R.id.rl_theft)
    public void onReportTheftClicked() {
        startActivityForResult(new Intent(this, ReportBikeTheft.class).putExtra("BIKE_ID", ride.getBikeId()), REQUEST_CODE_THEFT_REPORT);
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        jsonString = getIntent().getStringExtra("RIDE_DETAILS");
        if (!jsonString.isEmpty()) {
            Gson gson = new Gson();
            this.ride = gson.fromJson(jsonString, Ride.class);
        }
    }

    @NonNull
    @Override
    protected RideMenuPresenter getPresenter() {
        return rideMenuPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.layout_ride_settings;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_THEFT_REPORT && resultCode == RESULT_OK) {
            Intent intent = new Intent();
            if(data!=null){
                if(data.hasExtra(REPORT_THEFT_SUCCESS)){
                    if(data.getExtras().getBoolean(REPORT_THEFT_SUCCESS)){
                        intent.putExtra(REPORT_THEFT_SUCCESS,true);
                    }
                }
            }
            setResult(RESULT_OK,intent);
            finish();
        }else if (requestCode == REQUEST_CODE_DAMAGE_REPORT && resultCode == RESULT_OK) {
            Intent intent = new Intent();
            if(data!=null){
                if(data.hasExtra(DAMAGE_REPORT_SUCCESS)){
                    if(data.getExtras().getBoolean(DAMAGE_REPORT_SUCCESS)){
                        intent.putExtra(DAMAGE_REPORT_SUCCESS,true);
                    }
                }
            }
            setResult(RESULT_OK,intent);
            finish();
        }else{
            setResult(RESULT_CANCELED);
            finish();
        }

    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }
}
