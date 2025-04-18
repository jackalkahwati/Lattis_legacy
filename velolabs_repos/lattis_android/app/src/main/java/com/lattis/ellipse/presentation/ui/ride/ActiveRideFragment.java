package com.lattis.ellipse.presentation.ui.ride;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cpiz.android.bubbleview.BubbleTextView;
import com.lattis.ellipse.Utils.MapboxUtil;
import com.lattis.ellipse.Utils.ResourceUtil;
import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.ui.base.fragment.bluetooth.BaseBluetoothFragment;
import com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragment;
import com.lattis.ellipse.presentation.ui.ride.service.util.UpdateTripData;
import com.lattis.ellipse.presentation.ui.ride.walkthrough.RideWalkThroughAdapter;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomTextView;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import io.lattis.ellipse.R;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.relex.circleindicator.CircleIndicator;
import permissions.dispatcher.NeedsPermission;

import static android.view.View.GONE;
import static com.lattis.ellipse.Utils.MapboxUtil.addUserLocation;

public class ActiveRideFragment extends BaseBluetoothFragment<ActiveRideFragmentPresenter> implements
        ActiveRideFragmentView {

    private final String TAG = ActiveRideFragment.class.getName();

    private static final int REQUEST_CONNECTION_TIMEOUT_POP_UP = 1920;
    private HomeMapFragment parentFragment;
    private EndRideFragment endRideFragment;
    private MapboxMap mapboxMap = null;
    private List<Symbol> symbols = new ArrayList<>();
    private List<SymbolOptions> options = new ArrayList<>();

    @BindView(R.id.iv_lock)
    ImageView iv_lock;

    @BindView(R.id.iv_unlock)
    ImageView iv_unlock;

    @BindView(R.id.iv_lock_connecting)
    ImageView iv_lock_connecting;

    @BindView(R.id.iv_lock_disconnected)
    ImageView iv_lock_disconnected;

    @BindView(R.id.iv_lock_connected)
    ImageView iv_lock_connected;

    @BindView(R.id.pb_locking_unlocking)
    ProgressBar pb_locking_unlocking;


    @BindView(R.id.viewPager)
    ViewPager viewPager;

    @BindView(R.id.indicator)
    CircleIndicator circleIndicator;

    @BindView(R.id.rl_ride_walkthrough)
    RelativeLayout rl_ride_walkthrough;

    @BindView(R.id.rl_lock_unlock)
    RelativeLayout rl_lock_unlock;

    @BindView(R.id.tv_tooTipForLockUnlock)
    BubbleTextView tv_tooTipForLockUnlock;

    @Nullable
    @BindView(R.id.no_internet_active_ride_layout)
    RelativeLayout noInternetViewInActiveRide;

    @OnClick(R.id.cv_ride_walkthrough_skip)
    public void skipRideWalkThrough(View v){
        viewPager.setCurrentItem(0);
        rl_ride_walkthrough.setVisibility(GONE);
    }

    @OnClick({R.id.iv_lock, R.id.iv_unlock})
    public void setLockPosition(View v) {
        if (lockModel == null) {
            checkForLocation();
            return;
        }

        if (position == null) {
            getPresenter().setPosition(true, false);
            return;
        }

        showPositionChange();

        failurePosition = position;
        if (position == Lock.Hardware.Position.LOCKED) {
            setEllipseLockUnlockButton(false);
            position = null;
            getPresenter().setPosition(false, false);
            Log.e(TAG, "######setLockPosition: UNLOCKING");

        } else if (position == Lock.Hardware.Position.UNLOCKED) {
            setEllipseLockUnlockButton(false);
            position = null;
            getPresenter().setPosition(true, false);
            Log.e(TAG, "######setLockPosition: LOCKING");
        }
    }

    @OnClick({R.id.iv_lock_disconnected})
    public void connectLockAgain(View v) {
       showToolTipForLockUnlock(false,false,null);
        isActiveModePreviouslyActivated=true;
        showConnecting();
        checkForLocation();
    }


    @BindView(R.id.rl_lock_position_error_popup)
    RelativeLayout rl_lock_position_error_popup;

    @BindView(R.id.ct_lock_position_error_popup_title)
    CustomTextView ct_lock_position_error_popup_title;

    @BindView(R.id.ct_lock_position_error_popup_subtitle1)
    CustomTextView ct_lock_position_error_popup_subtitle1;





    @OnClick(R.id.ct_lock_position_error_popup_ok)
    public void closeLockPositionErrorPopup() {
        rl_lock_position_error_popup.setVisibility(GONE);
    }

    private String signedMessage;
    private String publicKey;
    LockModel lockModel;
    private Ride ride;
    private Lock.Hardware.Position position = null;
    private Lock.Hardware.Position failurePosition = null;
    private boolean isActiveModePreviouslyActivated = false;
    private boolean isConnectionFailurePopUpShownPreviously = false;

    @Inject
    ActiveRideFragmentPresenter presenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @NonNull
    @Override
    protected ActiveRideFragmentPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getFragmentLayoutId() {
        return R.layout.active_ride_fragment;
    }

    @Override
    protected void configureViews() {
        super.configureViews();

        endRideFragment = (EndRideFragment) getChildFragmentManager().findFragmentById(R.id.end_ride_fragment);
        endRideFragment.setParentFragment(this);
        endRideFragment.setParking(null);
        hideFragments(endRideFragment);

        pb_locking_unlocking.setVisibility(GONE);
        iv_lock.setVisibility(GONE);
        iv_unlock.setVisibility(GONE);
        iv_lock_connecting.setVisibility(GONE);
        iv_lock_connected.setVisibility(GONE);
        iv_lock_disconnected.setVisibility(GONE);

        RideWalkThroughAdapter adapter = new RideWalkThroughAdapter();
        viewPager.setAdapter(adapter);
        circleIndicator.setViewPager(viewPager);

    }


    @Override
    public void setUserPosition(Location location) {
        parentFragment.hideOperationLoading();
        if (mapboxMap != null) {
            MapboxUtil.enableLocationComponent(getActivity(),mapboxMap,mapboxMap.getStyle());
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), 16),3000);
            if(symbols.size()==0){
                addUserLocation(options,location.getLatitude(),location.getLongitude(), ResourceUtil.getUserLocationResource(),1.0f);
                symbols = getSymbolManager().create(options);
            }else{
                Symbol symbol = symbols.get(0);
                symbol.setLatLng(new LatLng(location.getLatitude(),location.getLongitude()));
                getSymbolManager().update(symbol);
            }
        }
    }

    @Override
    public void OnRideSuccess(Ride ride) {
        this.ride = ride;
        Log.e(TAG, "######OnRideSuccess: " + ride.toString());
        endRideFragment.showConnectingLabel(true);
        getPresenter().startActiveTripService();
        getPresenter().connectToLastLockedLock(); // this is real, uncomment while commiting
    }

    @Override
    public void OnRideFailure() {
        Log.e(TAG, "######OnRideFailure: ");
    }

    @Override
    public void onSignedMessagePublicKeySuccess(String signedMessage, String publicKey) {
        endRideFragment.showConnectingLabel(true);
        this.signedMessage = signedMessage;
        this.publicKey = publicKey;
        LockModel lockModel = new LockModel();
        lockModel.setSignedMessage(signedMessage);
        lockModel.setPublicKey(publicKey);
        lockModel.setUserId(ride.getBike_bike_fleet_key());
        lockModel.setMacId(ride.getBike_mac_id());
        getPresenter().setLockModel(lockModel);
        this.lockModel = lockModel;
        getPresenter().disconnectAllLocks();    //this will disconnect all previous connections and start scanning for required lock
    }

    @Override
    public void onSignedMessagePublicKeyFailure() {
        Log.e(TAG, "######OnSignedMessagePublicKeyFailure: ");
        endRideFragment.showConnectingLabel(false);
        showDisconnected();
        showToolTipForLockUnlock(false,true,getString(R.string.tool_tip_reconnection));
    }

    @Override
    public void onSetPositionStatus(Boolean status) {
        Log.e(TAG, "######OnSetPositionStatus: " + status);
    }

    @Override
    public void requestEnableBluetooth() {
        super.requestEnableBluetooth();
    }


    @Override
    public void onLockConnected(LockModel lockModel) {
        Log.e(TAG, "######onLockConnected: " + lockModel.toString());
        showConnected();
        this.lockModel = lockModel;


        Observable.timer(2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    public void accept(Long aLong) {
                        if(ride.getDo_not_track_trip()==null || !ride.getDo_not_track_trip()){
                            getPresenter().startLocationTrackInActiveTripService();
                        }else{
                            getPresenter().stopLocationTrackInActiveTripService();
                        }
                    }
                });


        startObservingLock();
    }


    public void showPositionChange(){
        pb_locking_unlocking.setVisibility(View.VISIBLE);
    }


    @Override
    public void showDisconnected() {
        showToolTipForLockUnlock(false,false,null);
        pb_locking_unlocking.setVisibility(GONE);
        iv_lock.setVisibility(GONE);
        iv_unlock.setVisibility(GONE);
        iv_lock_connecting.setVisibility(GONE);
        iv_lock_connected.setVisibility(GONE);
        iv_lock_disconnected.setVisibility(View.VISIBLE);
        endRideFragment.showConnectingLabel(false);
    }

    @Override
    public void showConnected() {
        endRideFragment.showConnectingLabel(false);
    }

    @Override
    public void showConnecting() {
        showToolTipForLockUnlock(false,false,null);
        Log.e(TAG, "######showConnecting:");
        pb_locking_unlocking.setVisibility(View.VISIBLE);
        iv_lock.setVisibility(GONE);
        iv_unlock.setVisibility(GONE);
        iv_lock_connecting.setVisibility(View.VISIBLE);
        iv_lock_connected.setVisibility(GONE);
        iv_lock_disconnected.setVisibility(GONE);
        endRideFragment.showConnectingLabel(true);

    }

    @Override
    public void onLockConnectionFailed() {
        Log.e(TAG, "######onLockConnectionFailed: ");
        showConnecting();
        checkForLocation();
        endRideFragment.showConnectingLabel(false);
        position=null;
    }

    @Override
    public void onLockConnectionAccessDenied() {
        showDisconnected();
        Toast.makeText(getActivity(), getString(R.string.ellipse_access_denided_text), Toast.LENGTH_LONG).show();
    }

    @Override
    public void showLockPositionError() {
        Log.e(TAG, "######showLockPositionError: ");
        pb_locking_unlocking.setVisibility(GONE);
        iv_lock.setVisibility(GONE);
        iv_unlock.setVisibility(GONE);
        iv_lock_connecting.setVisibility(GONE);
        iv_lock_connected.setVisibility(View.VISIBLE);
        iv_lock_disconnected.setVisibility(GONE);

        showLockPositionErrorPopUp(getString(R.string.active_ride_jamming_title),getString(R.string.lock_jamming_subtitle));
    }

    @Override
    public void showLockPositionSuccess(Lock.Hardware.Position position) {


        pb_locking_unlocking.setVisibility(GONE);
        iv_lock_connecting.setVisibility(GONE);
        iv_lock_connected.setVisibility(GONE);
        iv_lock_disconnected.setVisibility(GONE);


        endRideFragment.showConnectingLabel(false);
        setEllipseLockUnlockButton(true);

        this.position = position;
        if (position == Lock.Hardware.Position.LOCKED) {
            iv_lock.setVisibility(View.VISIBLE);
            iv_unlock.setVisibility(GONE);




        } else if (position == Lock.Hardware.Position.UNLOCKED) {
            iv_unlock.setVisibility(View.VISIBLE);
            iv_lock.setVisibility(GONE);

        }

        if(position!=null) {
            String message = position == Lock.Hardware.Position.LOCKED ? getString(R.string.tool_tip_unlock) : getString(R.string.tool_tip_lock);
            showToolTipForLockUnlock(true,true,message);
        }

    }

    @Override
    public void onSetPositionFailure() {
        if(failurePosition!=null){
            showLockPositionSuccess(failurePosition);
        }
    }

    @Override
    public void setRideDurationAndCost(UpdateTripData updateTripData) {
        if(updateTripData!=null){
            if(endRideFragment!=null){
                endRideFragment.setRideDurationAndCost(updateTripData.getCost(),updateTripData.getCurrency());
            }
        }
    }

    @Override
    public void showConnectionTimeOut(){
        getPresenter().unsubscribeAllSubscription();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showDisconnected();
                showToolTipForLockUnlock(false,true,getString(R.string.tool_tip_reconnection));
                isActiveModePreviouslyActivated=false;
                isConnectionFailurePopUpShownPreviously=true;
                PopUpActivity.launchForResult(getActivity(), REQUEST_CONNECTION_TIMEOUT_POP_UP, getString(R.string.bike_booking_end_ride_title),
                        getString(R.string.bike_out_of_range_connection_error), null, getString(R.string.ok_title));
            }
        });


    }


    void startObservingLock() {
        getPresenter().observeConnectionState(lockModel);
    }

    @OnClick(R.id.iv_select_parking_fragment)
    public void onParkingClicked() {
        parentFragment.showParkingFragment(ride.getBike_fleet_id(),getPresenter().getCurrentUserLocation(),position, getLockBattery());
    }


    public void setParentFragment(HomeMapFragment homeMapFragment) {
        this.parentFragment = homeMapFragment;
    }

    public void setMapboxMap(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
    }

    private void setCurrentPosition() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return;
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            hideFragments(endRideFragment);
            requestStopLocationUpdates();
            unsubscribeAllSubscription();
            isActiveModePreviouslyActivated = false;
            position=null;
        } else {
            position=null;
            isActiveModePreviouslyActivated = true;
            showFragments(endRideFragment);
            endRideFragment.showConnectingLabel(true);
            endRideFragment.setParentFragment(this);
            showConnecting();
            getPresenter().getRide();
            setCurrentPosition();
            requestLocationUpdates();
            setEllipseLockUnlockButton(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "######onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void showEndSummary() {
        endRideFragment.launchRideSummary();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "######onResume");
        if (isActiveModePreviouslyActivated) {
            Log.e(TAG, "######onResume:isActiveModePreviouslyActivated-->true");
            getPresenter().connectToLastLockedLock();
            endRideFragment.showConnectingLabel(true);
            hideFragments(endRideFragment);
            showFragments(endRideFragment);
        } else if (isConnectionFailurePopUpShownPreviously) {
            isConnectionFailurePopUpShownPreviously=false;
            hideFragments(endRideFragment);
            showFragments(endRideFragment);
        } else {
            Log.e(TAG, "######onResume:isActiveModePreviouslyActivated-->false");
        }

        setEllipseLockUnlockButton(true);

    }

    public void showRideSummary() {
        getPresenter().stopActiveTripService();
        unsubscribeAllSubscription();
        parentFragment.showRideSummaryFragment();
    }

    public void showParkingFragment() {
        parentFragment.showParkingFragment(ride.getBike_fleet_id(),getPresenter().getCurrentUserLocation(), position,getLockBattery());
    }

    public void setWhetherConnectionRequiredOnResume(boolean state) {
        Log.e(TAG, "######setWhetherConnectionRequiredOnResume-->" + state);
        isActiveModePreviouslyActivated = state;
    }

    public void setEllipseLockUnlockButton(boolean enabled){
        iv_unlock.setEnabled(enabled);
        iv_lock.setEnabled(enabled);
    }

    public Integer getLockBattery(){
        return getPresenter().getLock_battery();
    }

    public Lock.Hardware.Position getLockStatus() {
        return position;
    }

    public void showLockPositionErrorPopUp(String title, String message) {
        ct_lock_position_error_popup_title.setText(title);
        ct_lock_position_error_popup_subtitle1.setText(message);
        rl_lock_position_error_popup.setVisibility(View.VISIBLE);
        return;
    }



    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void requestLocationUpdates() {
        getPresenter().requestLocationUpdates();
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void requestStopLocationUpdates() {
        getPresenter().requestStopLocationUpdates();
    }

    public void unsubscribeAllSubscription() {
        clearSymbols();
        getPresenter().unsubscribeAllSubscription();
    }




    private void hideFragments(Fragment... fragments) {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        for (Fragment fragment : fragments) {
            ft.hide(fragment);
        }
        ft.commit();
    }

    private void showFragments(Fragment... fragments) {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        for (Fragment fragment : fragments) {
            ft.show(fragment);
        }
        ft.commit();
    }


    public void setInternetStatus(boolean connectivity){
        if(noInternetViewInActiveRide!=null){
            if(connectivity){
                noInternetViewInActiveRide.setVisibility(GONE);
            }else{
                noInternetViewInActiveRide.setVisibility(View.VISIBLE);
            }
        }
    }

    public void showToolTipForLockUnlock(boolean isLockUnlock,boolean show, String message) {

        if(show) {
            if (isLockUnlock) {
                if (position != null && (position == Lock.Hardware.Position.LOCKED || position == Lock.Hardware.Position.UNLOCKED) && presenter.doesToolTipForUnlockNeedsToBeShown()) {
                    tv_tooTipForLockUnlock.setVisibility(View.VISIBLE);
                    tv_tooTipForLockUnlock.setText(message);
                } else {
                    tv_tooTipForLockUnlock.setVisibility(GONE);
                }
            } else {
                tv_tooTipForLockUnlock.setVisibility(View.VISIBLE);
                tv_tooTipForLockUnlock.setText(message);
            }
        }else{
            tv_tooTipForLockUnlock.setVisibility(GONE);
        }
    }


    private SymbolManager getSymbolManager(){
        return parentFragment.getSymbolManager();
    }

    public void clearSymbols(){
        if(getSymbolManager()!=null)
            getSymbolManager().delete(symbols);
        symbols.clear();
        options.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().unsubscribeAllSubscription();
    }
}
