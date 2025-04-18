package com.lattis.ellipse.presentation.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.presentation.ui.base.activity.BaseDrawerActivity;
import com.lattis.ellipse.presentation.ui.base.activity.DrawerMenu;
import com.lattis.ellipse.presentation.ui.base.fragment.BaseFragment;
import com.lattis.ellipse.presentation.ui.bike.SearchPlacesActivity;
import com.lattis.ellipse.presentation.ui.biketheft.ReportBikeTheft;
import com.lattis.ellipse.presentation.ui.damagebike.DamageBikeActivity;
import com.lattis.ellipse.presentation.ui.history.RideHistoryListingActivity;
import com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragment;
import com.lattis.ellipse.presentation.ui.payment.PaymentInfoActivity;
import com.lattis.ellipse.presentation.ui.profile.ProfileActivity;
import com.lattis.ellipse.presentation.ui.profile.TermsAndConditionsActivity;
import com.lattis.ellipse.presentation.ui.profile.help.HelpActivity;
import com.lattis.ellipse.presentation.ui.profile.logout.LogOutActivity;
import com.lattis.ellipse.presentation.ui.profile.logout.LogOutAfterEndingRideActivity;

import javax.inject.Inject;

import butterknife.OnClick;
import io.lattis.ellipse.R;

import static com.lattis.ellipse.presentation.ui.damagebike.DamageBikeActivity.DAMAGE_REPORT_CANCEL_BOOKING;
import static com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragment.GET_CURRENT_STATUS_POP_UP_REQUEST_CODE;
import static com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragment.REQUEST_CHECK_LOCATION_SETTINGS;
import static com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragment.REQUEST_CODE_FOR_APP_UPDATE;
import static com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragment.REQUEST_CODE_LOCATION_PERMISSION_DENIED;
import static com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragment.REQUEST_CODE_SYSTEM_LOCATION_PERMISSION_DENIED;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.TRIP_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideFragment.DAMAGE_REPORT_SUCCESS;
import static com.lattis.ellipse.presentation.ui.ride.EndRideFragment.REPORT_THEFT_SUCCESS;

public class HomeActivity extends BaseDrawerActivity<HomePresenter> implements HomeView {

    private final String TAG = HomeActivity.class.getName();
    private final static int TERMS_CONDITIONS_REQUEST = 1000;
    private final static int REQUEST_CODE_TERMS_CONDITIONS_REQUEST = 1000;
    private final static int REQUEST_CODE_CANCEL_BIKE_BOOKING = 2000;
    private final int REQUEST_CODE_BIKE_BOOKING = 3000;
    private final int REQUEST_CODE_LOG_OUT_AFTER_END_RIDE = 2022;
    public static final int REQUEST_CODE_BIKE_INFO = 7483;
    public static final int REQUEST_CODE_REPORT_DAMAGE = 1013;
    public static final int REQUEST_CODE_REPORT_THEFT = 1014;
    public static final int REQUEST_APP_UPDATE_POP = 1274;
    public static final int REQUEST_CODE_SEARCH_ADDRESS = 4646;
    public static final int REQUEST_CODE_PAYMENT_INFO = 4647;
    private boolean isInternetConnected = true;

    public void hideNoInternetView(boolean visibility){
        if(noInternetView!=null){
            if(visibility)
                noInternetView.setVisibility(View.VISIBLE);
            else
                noInternetView.setVisibility(View.GONE);
        }
    }


    DrawerMenu menuItem;
    @Inject
    HomePresenter homePresenter;
    public Toolbar mToolbar;


    @OnClick(R.id.nav_help)
    public void onHelpButtonclicked() {
        closeDrawer();
        startActivity(new Intent(HomeActivity.this, HelpActivity.class));
    }


    @OnClick(R.id.nav_logout)
    public void onLogoutButtonclicked() {
        closeDrawer();
        getPresenter().getCurrentUserStatus();
    }

    public void setDrawerMenuWithRide() {
        setBaseDrawerMenuWithRide();
    }

    public void setDrawerMenuWithoutRide() {
        setBaseDrawerMenuWithoutRide();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @NonNull
    @Override
    protected HomePresenter getPresenter() {
        return homePresenter;
    }

    @Override
    protected int getActivityContentLayoutId() {
        return R.layout.content_layout_home;
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @Override
    protected int getDefaultSelectedItemId() {
        return R.id.nav_home;
    }

    @Override
    protected void onDrawerItemClicked(DrawerMenu menuItem) {
        super.onDrawerItemClicked(menuItem);
    }


    @Override
    protected void configureViews() {
        super.configureViews();
        invalidateOptionsMenu();
    }


    @Override
    protected void onDrawerItemSelected(DrawerMenu menuItem) {
        this.menuItem = menuItem;
        replaceDrawerFragment(menuItem.getItemId());
    }

    private void replaceDrawerFragment(int menuItemId) {
        getSupportFragmentManager().popBackStack();
        switch (menuItemId) {
            case R.id.nav_billing:
                startActivityForResult(new Intent(this, PaymentInfoActivity.class),REQUEST_CODE_PAYMENT_INFO);
                break;
            case R.id.profile:
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(getEnterAnimation(), R.anim.no_animation);
                break;
            case R.id.nav_emergencyContacts:
//                replaceDrawerFragment(EmergencyContactActivity.newInstance());
                break;
            case R.id.nav_report_damage:
                startActivityForResult(new Intent(this, DamageBikeActivity.class), REQUEST_CODE_REPORT_DAMAGE);
                break;
            case R.id.nav_ride_history:
                startActivity(new Intent(this, RideHistoryListingActivity.class));
                break;
            case R.id.nav_report_theft:
                startActivityForResult(new Intent(this, ReportBikeTheft.class),REQUEST_CODE_REPORT_THEFT);
                break;
        }
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            //setCheckedItem(DrawerMenu.HOME);
            setHomeForBackPressed();
        }
        super.onBackPressed();
    }

    @Override
    public void onGetCurrentUserStatusFailure() {
        startActivity(new Intent(this, LogOutActivity.class));
    }

    @Override
    public void onGetCurrentUserStatusSuccess(GetCurrentUserStatusResponse getCurrentUserStatusResponse) {
        if (getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse() == null && getCurrentUserStatusResponse.getCurrentUserStatusTripResponse() != null) {
            //active ride
            Intent intent = new Intent(this, LogOutAfterEndingRideActivity.class);
            intent.putExtra(TRIP_ID, getCurrentUserStatusResponse.getCurrentUserStatusTripResponse().getTrip_id());
            startActivityForResult(intent, REQUEST_CODE_LOG_OUT_AFTER_END_RIDE);
        } else { // no active ride
            startActivity(new Intent(this, LogOutActivity.class));
        }
    }


    @Override
    public void showTermsAndConditions() {
        TermsAndConditionsActivity.launchForResult(this, REQUEST_CODE_TERMS_CONDITIONS_REQUEST);
    }

    private void replaceDrawerFragment(BaseFragment fragment) {
        replaceFragment(R.id.fragment_container, fragment);
    }


    private void replaceDrawerFragment(Fragment fragment) {
        replaceFragment(R.id.fragment_container, fragment);
    }

    @Override
    public void showOnBoardingFlow() {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult: " + requestCode);
        if (requestCode == TERMS_CONDITIONS_REQUEST && resultCode == RESULT_OK) {
            getPresenter().onTermsAndConditionAccepted();
            finish();
        } else if (requestCode == REQUEST_CODE_CANCEL_BIKE_BOOKING && resultCode == RESULT_OK) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
            fragment.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == REQUEST_CODE_BIKE_BOOKING && resultCode == RESULT_OK) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
            fragment.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == REQUEST_CODE_LOG_OUT_AFTER_END_RIDE && resultCode == RESULT_OK) {
        } else if (requestCode == REQUEST_CODE_REPORT_DAMAGE && resultCode == RESULT_OK) {
            if(data!=null) {
                if (data.hasExtra(DAMAGE_REPORT_SUCCESS)) {
                    if (data.getExtras().getBoolean(DAMAGE_REPORT_SUCCESS)) {
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
                        fragment.onActivityResult(requestCode, resultCode, data);
                    }
                }
            }
        }else if (requestCode == REQUEST_CODE_REPORT_THEFT && resultCode == RESULT_OK) {
            Intent intent = new Intent();
            if(data!=null){
                if(data.hasExtra(REPORT_THEFT_SUCCESS)){
                    if(data.getExtras().getBoolean(REPORT_THEFT_SUCCESS)){
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
                        fragment.onActivityResult(requestCode, resultCode, data);
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_REPORT_DAMAGE && resultCode == RESULT_CANCELED) {
            if (data != null) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == GET_CURRENT_STATUS_POP_UP_REQUEST_CODE) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
            fragment.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == REQUEST_APP_UPDATE_POP) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
            fragment.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == REQUEST_CODE_SEARCH_ADDRESS) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
            fragment.onActivityResult(requestCode, resultCode, data);
        }else if (requestCode == REQUEST_CODE_LOCATION_PERMISSION_DENIED) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
            fragment.onActivityResult(requestCode, resultCode, data);
        }else if(requestCode == REQUEST_CODE_PAYMENT_INFO){
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
            fragment.onActivityResult(requestCode, resultCode, data);
        }else if(requestCode == REQUEST_CODE_BIKE_INFO){
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
            fragment.onActivityResult(requestCode, resultCode, data);
        }else if(requestCode == REQUEST_CHECK_LOCATION_SETTINGS){
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
            fragment.onActivityResult(requestCode, resultCode, data);
        }else if (requestCode == REQUEST_CODE_SYSTEM_LOCATION_PERMISSION_DENIED) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
            fragment.onActivityResult(requestCode, resultCode, data);
        }else if (requestCode == REQUEST_CODE_FOR_APP_UPDATE) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
            fragment.onActivityResult(requestCode, resultCode, data);
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void getToolbar(Toolbar toolbar) {
        super.getToolbar(toolbar);
        this.mToolbar = toolbar;
        toolbar.findViewById(R.id.toolbar_subtitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInternetConnected)
                    startActivityForResult(new Intent(HomeActivity.this, SearchPlacesActivity.class), REQUEST_CODE_SEARCH_ADDRESS);
            }
        });

        toolbar.findViewById(R.id.iv_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInternetConnected)
                    startActivityForResult(new Intent(HomeActivity.this, SearchPlacesActivity.class), REQUEST_CODE_SEARCH_ADDRESS);
            }
        });

    }

    @Override
    protected void noAccountAdded() {
        super.noAccountAdded();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
        if (fragment != null) {
            ((HomeMapFragment) fragment).noAccountAdded();
        }
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {
        super.onInternetConnectionChanged(isConnected);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
        if(noInternetView!=null){
            if(isConnected)
                noInternetView.setVisibility(View.GONE);
            else
                noInternetView.setVisibility(View.VISIBLE);
        }
        if(fragment!=null){
            ((HomeMapFragment) fragment).setInternetStatus(isConnected);
        }
        if(!isInternetConnected && isConnected && fragment != null){
            ((HomeMapFragment) fragment).checkToRestoreApp();
        }
        isInternetConnected = isConnected;
    }
}
