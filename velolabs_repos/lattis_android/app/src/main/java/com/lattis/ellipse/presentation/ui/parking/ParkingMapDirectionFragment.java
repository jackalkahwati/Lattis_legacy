package com.lattis.ellipse.presentation.ui.parking;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cpiz.android.bubbleview.BubbleTextView;
import com.lattis.ellipse.Utils.ResourceUtil;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Parking;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.domain.model.map.Direction;
import com.lattis.ellipse.domain.model.map.Step;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.ui.base.fragment.bluetooth.BaseBluetoothFragment;
import com.lattis.ellipse.presentation.ui.ride.EndRideFragment;
import com.lattis.ellipse.presentation.ui.ride.service.util.UpdateTripData;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomTextView;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static com.lattis.ellipse.Utils.MapboxUtil.addMarker;
import static com.lattis.ellipse.Utils.MapboxUtil.addUserLocation;
import static com.lattis.ellipse.Utils.MapboxUtil.boundCameraToRoute;
import static com.lattis.ellipse.Utils.MapboxUtil.getUserLocationSymbol;
import static com.lattis.ellipse.Utils.MapboxUtil.selected_size;
import static com.lattis.ellipse.Utils.MapboxUtil.unselected_size;
import static com.lattis.ellipse.Utils.MapboxUtil.zoomToMarkers;


public class ParkingMapDirectionFragment extends BaseBluetoothFragment<ParkingMapDirectionFragmentPresenter>
        implements ParkingMapDirectionFragmentView, OffRouteListener {


    private final String TAG = ParkingMapDirectionFragment.class.getName();

    private static final int MILLISECONDS_FOR_CONNECTION_TIMEOUT = 60000;
    private static final int MILLISECONDS_FOR_CONNECTING_TIMEOUT = 30000;

    private static final int REQUEST_CONNECTION_TIMEOUT_POP_UP = 1920;
    private int REQUEST_CODE_FOR_GOOGLE_MAP_APP = 283;


    private Direction direction;
    private Parking parking;
    private FindParkingFragment parentFragment;
    private Location fromLocation;
    private Disposable connectionTimerSubscription;
    private Disposable connectingTimerSubscription;
    private EndRideFragment endRideFragment;
    private DialogFragment lockPositionErrorDialog;
    private boolean myVisibility;

    private List<Symbol> symbols = new ArrayList<>();
    List<SymbolOptions> options = new ArrayList<>();
    private DirectionsRoute currentRoute;

    @BindView(R.id.distanceView)
    TextView distanceView;
    @BindView(R.id.durationView)
    TextView durationView;
    @BindView(R.id.tv_parking_name)
    TextView parkingName;
    @BindView(R.id.lock_route_steps)
    TextView lockRouteStepsView;
    DirectionsRoute directionsRoute;

    @BindView(R.id.tv__parking_tooTipForLockUnlock)
    BubbleTextView tv__parking_tooTipForLockUnlock;

    @OnClick(R.id.iv_text_route_layout_close)
    public void onCloseClicked() {
        parentFragment.hideParkingMapDirectionFragment();
    }

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

    private MapboxNavigation navigation = null;

    @OnClick({R.id.iv_lock, R.id.iv_unlock})
    public void setLockPosition(View v) {
        if (lockModel == null) {
            checkForLocation();
            return;
        }

        if (position == null) {
            getPresenter().setPosition(true,false);
            return;
        }

        showPositionChange();

        failurePosition = position;
        if (position == Lock.Hardware.Position.LOCKED) {
            position = null;
            getPresenter().setPosition(false,false);
            Log.e(TAG, "######setLockPosition: UNLOCKING");

        } else if (position == Lock.Hardware.Position.UNLOCKED) {
            position = null;
            getPresenter().setPosition(true,false);
            Log.e(TAG, "######setLockPosition: LOCKING");
        }
    }

    @OnClick({R.id.iv_lock_disconnected})
    public void connectLockAgain(View v) {
        isParkingMapDirectionPreviouslyActivated=true;
        subscribeToSearchAndConnectionTimer(false);
        subscribeToSearchAndConnectionTimer(true);
        showToolTipForLockUnlock(false,false,null);
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
        rl_lock_position_error_popup.setVisibility(View.GONE);
    }


    private String signedMessage;
    private String publicKey;
    LockModel lockModel;
    private Ride ride;
    private Lock.Hardware.Position position = null;
    private Lock.Hardware.Position failurePosition = null;
    private boolean isConnectionFailurePopUpShownPreviously = false;
    private boolean isParkingMapDirectionPreviouslyActivated = false;

    @Inject
    ParkingMapDirectionFragmentPresenter presenter;


    @NonNull
    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected ParkingMapDirectionFragmentPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getFragmentLayoutId() {
        return R.layout.parking_direction_fragment;
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        navigation = new MapboxNavigation(getActivity(), getString(R.string.map_box_access_token));

        endRideFragment = (EndRideFragment) getChildFragmentManager().findFragmentById(R.id.end_ride_fragment);

        hideFragments(endRideFragment);

        pb_locking_unlocking.setVisibility(View.GONE);
        iv_lock.setVisibility(View.GONE);
        iv_unlock.setVisibility(View.GONE);
        iv_lock_connecting.setVisibility(View.GONE);
        iv_lock_connected.setVisibility(View.GONE);
        iv_lock_disconnected.setVisibility(View.GONE);
    }

    public void showRideSummary() {
        unsubscribeAllSubscription();
        parentFragment.showRideSummary();
    }

    public void setParentFragment(FindParkingFragment findParkingFragment) {
        this.parentFragment = findParkingFragment;
    }

    public void setDirectionForParking(Location fromLocation, Parking parking) {
        Log.e(TAG, "Current Longitude: " + fromLocation.getLongitude() + " Latitude: " + fromLocation.getLatitude());
        Log.e(TAG, "Parking Longitude: " + parking.getName() + parking.getLongitude() + " Latitude: " + parking.getLatitude());
        endRideFragment.setParentFragment(this);
        this.parking = parking;
        this.fromLocation = fromLocation;
        showParkingAndCurrentLocation(Point.fromLngLat(fromLocation.getLongitude(),fromLocation.getLatitude()), Point.fromLngLat(parking.getLongitude(), parking.getLatitude()));
        endRideFragment.setParking(parking);
        openGoogleMapApp(parking);
    }

    private void showParkingAndCurrentLocation(Point origin, Point destination){
        addUserLocation(options,origin.latitude(),origin.longitude(), ResourceUtil.getUserLocationResource(),unselected_size);
        addMarker(options,destination.latitude(), destination.longitude(), ResourceUtil.getResourcesByParkingType(parking.getType().toUpperCase()),selected_size);
        symbols = getSymbolManager().create(options);
        if(symbols.size()>1){
            symbols.get(1).setZIndex(25);
        }
        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(new LatLng(destination.latitude(), destination.longitude()));
        latLngs.add(new LatLng(origin.latitude(), origin.longitude()));

        LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
        latLngBounds.includes(latLngs);

        zoomToMarkers(parentFragment.getMapBoxMap(), latLngBounds,latLngs);

    }


    private void getRoute(Point origin, Point destination) {
        addMarker(options,destination.latitude(), destination.longitude(), ResourceUtil.getResourcesByParkingType(parking.getType().toUpperCase()),1.0f);
        symbols = getSymbolManager().create(options);

        NavigationRoute.builder(getActivity())
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            return;
                        } else if (response.body().routes().size() < 1) {
                            return;
                        }

                        currentRoute = response.body().routes().get(0);
                        currentRoute.legs().clear();

                        // Draw the route on the map
                        getNavigationMapRoute().removeRoute();
                        getNavigationMapRoute().addRoute(currentRoute);
                        boundCameraToRoute(currentRoute,new LatLng(fromLocation.getLatitude(),fromLocation.getLongitude()),parentFragment.getMapBoxMap());
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                    }
                });
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


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            hideFragments(endRideFragment);
            isParkingMapDirectionPreviouslyActivated = false;
            unsubscribeAllSubscription();
            setMyVisibility(false);
            getPresenter().requestStopLocationUpdates();
        } else {
            isParkingMapDirectionPreviouslyActivated = true;
            subscribeToSearchAndConnectionTimer(false);
            subscribeToSearchAndConnectionTimer(true);
            showFragments(endRideFragment);
            endRideFragment.showConnectingLabel(true);
            endRideFragment.setParentFragment(this);
            showConnecting();
            getPresenter().getRide();
            setEllipseLockUnlockButton(true);
            setMyVisibility(true);
        }
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

    @Override
    public void onPause() {
        super.onPause();
        subscribeToSearchAndConnectionTimer(false);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unsubscribeAllSubscription();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "######onResume");
        if (isParkingMapDirectionPreviouslyActivated) {
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

    public void setWhetherConnectionRequiredOnResume(boolean state) {
        Log.e(TAG, "######setWhetherConnectionRequiredOnResume-->" + state);
        isParkingMapDirectionPreviouslyActivated = state;
    }

    public void setEllipseLockUnlockButton(boolean enabled){
        iv_unlock.setEnabled(enabled);
        iv_lock.setEnabled(enabled);
    }

    public Integer getLockBattery(){
        return getPresenter().getLock_battery();
    }


    @Override
    public void OnRideSuccess(Ride ride) {
        this.ride = ride;
        endRideFragment.showConnectingLabel(true);
        getPresenter().connectToLastLockedLock();
    }

    @Override
    public void OnRideFailure() {

    }

    @Override
    public void onSignedMessagePublicKeySuccess(String signedMessage, String publicKey) {
        subscribeToSearchAndConnectionTimer(false);
        subscribeToSearchAndConnectionTimer(true);
        endRideFragment.showConnectingLabel(true);
        this.signedMessage = signedMessage;
        this.publicKey = publicKey;
        LockModel lockModel = new LockModel();
        lockModel.setSignedMessage(signedMessage);
        lockModel.setPublicKey(publicKey);
        lockModel.setUserId(ride.getBike_bike_fleet_key());
        lockModel.setMacId(ride.getBike_mac_id());
        getPresenter().setLockModel(lockModel);
        getPresenter().disconnectAllLocks();    //this will disconnect all previous connections and start scanning for required lock
    }

    @Override
    public void onSignedMessagePublicKeyFailure() {
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
    public void onLockConnectionFailed() {
        Log.e(TAG, "######onLockConnectionFailed: ");
        subscribeToSearchAndConnectionTimer(false);
        subscribeToSearchAndConnectionTimer(true);
        showConnecting();
//        getPresenter().getSignedMessagePublicKey();
        checkForLocation();
        endRideFragment.showConnectingLabel(false);
    }

    @Override
    public void onLockConnectionAccessDenied() {
        subscribeToSearchAndConnectionTimer(false);
        showDisconnected();

        Toast.makeText(getActivity(), getString(R.string.ellipse_access_denied_error_label), Toast.LENGTH_LONG).show();
    }

    @Override
    public void showLockPositionSuccess(Lock.Hardware.Position position) {

        pb_locking_unlocking.setVisibility(GONE);
        iv_lock_connecting.setVisibility(GONE);
        iv_lock_connected.setVisibility(GONE);
        iv_lock_disconnected.setVisibility(GONE);


        endRideFragment.showConnectingLabel(false);

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
        subscribeToSearchAndConnectionTimer(false);
        getPresenter().unsubscribeAllSubscription();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showDisconnected();
                showToolTipForLockUnlock(false,true,getString(R.string.tool_tip_reconnection));
                isParkingMapDirectionPreviouslyActivated=false;
                isConnectionFailurePopUpShownPreviously=true;
                PopUpActivity.launchForResult(getActivity(), REQUEST_CONNECTION_TIMEOUT_POP_UP, getString(R.string.bike_booking_end_ride_title),
                        getString(R.string.bike_out_of_range_connection_error), null, getString(R.string.ok_title));
            }
        });


    }


    public synchronized void subscribeToSearchAndConnectionTimer(boolean active){
        if(active){
            if (connectionTimerSubscription == null){
                connectionTimerSubscription = Observable.timer(MILLISECONDS_FOR_CONNECTION_TIMEOUT,TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Long>() {
                            public void accept(Long aLong) {
                                Log.e(TAG,"====================subscribeToLastConnectedTimer::OVER");
                                showConnectionTimeOut();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                            }
                        });
            }
        } else {
            if(connectionTimerSubscription != null){
                connectionTimerSubscription.dispose();
                connectionTimerSubscription = null;
            }
        }
    }


    void startObservingLock() {
        getPresenter().observeConnectionState(lockModel);
    }




    @Override
    public void userOffRoute(android.location.Location location) {

    }

    public void unsubscribeAllSubscription() {
        getPresenter().unsubscribeAllSubscription();
    }

    public void showToolTipForLockUnlock(boolean isLockUnlock,boolean show, String message) {

        if(show) {
            if (isLockUnlock) {
                if (position != null && (position == Lock.Hardware.Position.LOCKED || position == Lock.Hardware.Position.UNLOCKED) && presenter.doesToolTipForUnlockNeedsToBeShown()) {
                    tv__parking_tooTipForLockUnlock.setVisibility(View.VISIBLE);
                    tv__parking_tooTipForLockUnlock.setText(message);
                } else {
                    tv__parking_tooTipForLockUnlock.setVisibility(GONE);
                }
            } else {
                tv__parking_tooTipForLockUnlock.setVisibility(View.VISIBLE);
                tv__parking_tooTipForLockUnlock.setText(message);
            }
        }else{
            tv__parking_tooTipForLockUnlock.setVisibility(GONE);
        }
    }


    public NavigationMapRoute getNavigationMapRoute(){
        return parentFragment.getNavigationMapRoute();
    }
    private SymbolManager getSymbolManager(){
        return parentFragment.getSymbolManager();
    }

    public void clearSymbols(){
        getSymbolManager().deleteAll();
        getSymbolManager().delete(symbols);
        symbols.clear();
        options.clear();
        getNavigationMapRoute().removeRoute();
    }

    public boolean getMyVisibility() {
        return myVisibility;
    }

    public void setMyVisibility(boolean myVisibility) {
        this.myVisibility = myVisibility;
    }


    @Override
    public void setUserPosition(Location location) {
        Symbol userLocationSymbol = getUserLocationSymbol(symbols);
        if(userLocationSymbol==null){
            addUserLocation(options,location.getLatitude(),location.getLongitude(), ResourceUtil.getUserLocationResource(),unselected_size);
        }else{
            userLocationSymbol.setLatLng(new LatLng(location.getLatitude(),location.getLongitude()));
            getSymbolManager().update(userLocationSymbol);
        }
    }

    private void openGoogleMapApp(Parking parking){

        try {
            String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f(%s)&mode=bicycling", parking.getLatitude(), parking.getLongitude(), parking.getName());
            Uri gmmIntentUri = Uri.parse(uri);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) == null) {
                // Google map not installed
            } else {
                startActivityForResult(mapIntent, REQUEST_CODE_FOR_GOOGLE_MAP_APP);
            }
        }catch (ActivityNotFoundException e){

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_FOR_GOOGLE_MAP_APP){
            getPresenter().requestLocationUpdates();
        }
    }


}
