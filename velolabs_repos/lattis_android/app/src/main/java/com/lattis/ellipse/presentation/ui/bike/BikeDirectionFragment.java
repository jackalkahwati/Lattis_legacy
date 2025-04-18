package com.lattis.ellipse.presentation.ui.bike;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.lattis.ellipse.Utils.CurrencyUtil;
import com.lattis.ellipse.Utils.ResourceUtil;
import com.lattis.ellipse.Utils.UtilHelper;
import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.data.network.model.response.ride.UpdateTripResponse;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.ui.base.fragment.bluetooth.BaseBluetoothFragment;
import com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragment;
import com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragmentPresenter;
import com.lattis.ellipse.presentation.ui.payment.AddCardActivity;
import com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity;
import com.lattis.ellipse.presentation.ui.ride.service.util.UpdateTripData;
import com.lattis.ellipse.presentation.ui.utils.IsRidePaid;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity2;
import com.lattis.ellipse.presentation.view.CustomTextView;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.lattis.ellipse.Utils.MapboxUtil.addMarker;
import static com.lattis.ellipse.Utils.MapboxUtil.addUserLocation;
import static com.lattis.ellipse.Utils.MapboxUtil.selected_size;
import static com.lattis.ellipse.Utils.MapboxUtil.unselected_size;
import static com.lattis.ellipse.Utils.MapboxUtil.zoomToMarkers;
import static com.lattis.ellipse.presentation.ui.bike.BikeInfoActivity.ARGS_IS_BIKE_RESERVED;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.END_RIDE_ID_LOADING_STRING;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.END_RIDE_PAYMENT_FAILURE;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.FORCE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LATITUDE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LONGITUDE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.TRIP_ID;
import static com.lattis.ellipse.presentation.ui.utils.PopUpActivity.ACTIONBTN_POP_UP;
import static com.lattis.ellipse.presentation.ui.utils.PopUpActivity.SUBTITLE1_POP_UP;
import static com.lattis.ellipse.presentation.ui.utils.PopUpActivity.TITLE_POP_UP;

//public class BikeDirectionFragment extends BaseBluetoothFragment<BikeDirectionFragmentPresenter> implements BikeDirectionFragmentView, ProgressChangeListener, NavigationEventListener, AlertLevelChangeListener,
//        OffRouteListener {

public class BikeDirectionFragment extends BaseBluetoothFragment<BikeDirectionFragmentPresenter> implements BikeDirectionFragmentView{
    private static final String TAG = BikeDirectionFragment.class.getName();

    private CountDownTimer bikeBookTimer;
//    private Bike bike;
    private Location fromLocation;
    private BikeBaseFragment parentFragment;
    private LatLng[] direction;
    private boolean isDirectionLoaded = false;
    private final static int REQUEST_CODE_CANCEL_BIKE_BOOKING = 2000;
    private String signedMessage;
    private String publicKey;
    private boolean isBikeConnectedThruBluetooth = false;
    private boolean isBikeDirectionPreviouslyActive = false;
    private MapboxNavigation navigation = null;

    private List<Symbol> symbols = new ArrayList<>();
    private List<SymbolOptions> options = new ArrayList<>();

//    private boolean isRideStarted;
    private boolean isBeginTripClicked;
    private Timer tripCostTimerSubscription;
    private int TIMER_TRIP_COST_INTERVAL_VALUE_MILLISECONDS = 1000;
    private Ride ride;
    private static final int REQUEST_END_RIDE_CHECKLIST = 492;
    private static final int REQUEST_CODE_ADD_CARD_ACTIVITY = 5192;
    private static final int REQUEST_CODE_RIDE_INFO_ACTIVITY = 5196;
    private static final int REQUEST_CODE_WHY_BEGIN_TRIP_GREY_OUT_ACTIVITY = 5197;
    private static final int REQUEST_CODE_START_RIDE_FAIL_ACTIVITY = 5198;
    private static final int REQUEST_CODE_BIKE_END_RIDE_ACTIVITY = 5199;
    private static final int REQUEST_CODE_LOCK_ACCESS_DENIED=5201;
    private static final int REQUEST_CODE_CANCEL_BIKE_RESERVATION_FAIL=5202;
    private static final int REQUEST_CODE_GET_CURRENT_STATUS_FAIL_ACTIVITY = 5200;
    public static HomeMapFragmentPresenter.CurrentStatus currentStatus;
    private Disposable getCurrentStatusTimerSubscription;
    private static final int MILLISECONDS_FOR_GET_CURRENT_STATUS = 2000;
    private int updateTripDataCounter = -1;
    ValueAnimator beginTripValueAnimator=null;
    private boolean lockIssue=false;

    private Animation connectingAnimation;
    private Animation connectedAnimation;

    private DirectionsRoute currentRoute;


    @BindView(R.id.tv_bikeName)
    CustomTextView bikeNameTextview;

    @BindView(R.id.tv_bikeName_trip_starting)
    CustomTextView tv_bikeName_trip_starting;

    @BindView(R.id.tv_bike_booking_timer)
    CustomTextView tv_bike_booking_timer;


    @BindView(R.id.cv_bike_booking_timer_text_payment_free)
    CustomTextView cv_bike_booking_timer_text_payment_free;


    @BindView(R.id.cv_begin_trip_help)
    CustomTextView cv_begin_trip_help;

    @BindView(R.id.cv_see_ride_info)
    CustomTextView cv_see_ride_info;


    @BindView(R.id.ll_ui_before_trip_starting)
    LinearLayout ll_ui_before_trip_starting;

    @BindView(R.id.ll_ui_after_trip_starting)
    LinearLayout ll_ui_after_trip_starting;


    @BindView(R.id.cv_trip_cost_value_label)
    CustomTextView cv_trip_cost_value_label;


    @BindView(R.id.cv_trip_cost_currency_unit_label)
    CustomTextView cv_trip_cost_currency_unit_label;


    @BindView(R.id.cv_trip_timer_value_label)
    CustomTextView cv_trip_timer_value_label;


    @BindView(R.id.rl_trip_timer_cost)
    RelativeLayout rl_trip_timer_cost;


    @Inject
    BikeDirectionFragmentPresenter bikeDirectionFragmentPresenter;

    @BindView(R.id.text_view_begin_trip)
    CustomTextView beginTripTextView;

    @BindView(R.id.view_animating_begin_trip)
    View view_animating_begin_trip;


    @BindView(R.id.rl_bike_reservation_begin_trip)
    RelativeLayout rl_bike_reservation_begin_trip;

    @OnClick(R.id.rl_bike_reservation_begin_trip)
    public void startActiveRide() {
        if (isBikeConnectedThruBluetooth) {
            parentFragment.showOperationLoading(getString(R.string.loading));
            getPresenter().startRide();
        } else {
            startActivityForResult(new Intent(getActivity(), WhyBeginTripGreyOutActivity.class), REQUEST_CODE_WHY_BEGIN_TRIP_GREY_OUT_ACTIVITY);
        }
    }


    @OnClick({R.id.tv_bikeName, R.id.cv_see_ride_info})
    public void seeRideInfo() {
        String info = new Gson().toJson(getPresenter().getBike());
        startActivityForResult(new Intent(getActivity(), BikeInfoActivity.class)
                .putExtra(ARGS_IS_BIKE_RESERVED, true)
                .putExtra(BikeInfoActivity.ARGS_BIKE_DETAILS, info), REQUEST_CODE_RIDE_INFO_ACTIVITY);
    }

    public void cancelRideAfterDamage(){
        cancelRideOrShowBillingStarted(true);
    }

    @OnClick(R.id.iv_cancel_ride)
    public void cancelBikeReservation() {
        cancelRideOrShowBillingStarted(false);
    }

    private void cancelRideOrShowBillingStarted(boolean isBikeDamaged){
        isBikeDirectionPreviouslyActive = false;

        if (!getPresenter().isRideStarted() && !isBikeDamaged) {
            Gson gson = new Gson();
            String json = gson.toJson(getPresenter().getBike());
            startActivityForResult(new Intent(getActivity(), CancelRideActivity.class)
                            .putExtra("BIKE_DETAILS", json)
                            .putExtra("BIKE_DAMAGE", false)
                            .putExtra("LOCK_ISSUE", lockIssue),
                    REQUEST_CODE_CANCEL_BIKE_BOOKING);
        }else if(!getPresenter().isRideStarted() && isBikeDamaged) {
            getPresenter().cancelBikeReservation(getPresenter().getBike(),true,false);
        }else {
            Intent intent = new Intent(getActivity(), PopUpActivity2.class);
            intent.putExtra(TITLE_POP_UP, getString(R.string.bike_booking_end_ride_title));
            intent.putExtra(SUBTITLE1_POP_UP, getString(R.string.bike_booking_end_ride_text));
            intent.putExtra(ACTIONBTN_POP_UP, getString(R.string.bike_booking_end_ride_action));
            startActivityForResult(intent, REQUEST_CODE_BIKE_END_RIDE_ACTIVITY);

        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.e(TAG, "Hidden position: " + hidden);
        if (hidden) {
            isBikeConnectedThruBluetooth = false;
            requestStopLocationUpdates();
            isBikeDirectionPreviouslyActive = false;
            cancelAllSubscriptionAndTimer();
        } else {
            //beginTripTextView.setTag("NOT_CONNECTED");
            getPresenter().setRideStartingRequiredAfterLocationUpdate(false);
            isBikeConnectedThruBluetooth = false;
        }
    }

    public void requestStopLocationUpdates() {
        getPresenter().requestStopLocationUpdates();
    }

    @Override
    public void onStartRideSuccess() {
        getNavigationMapRoute().removeRoute();
        clearSymbols();
        showActiveTrip();
    }

    @Override
    public void onStartRideFail() {
        parentFragment.hideOperationLoading();
        isBikeDirectionPreviouslyActive = false;
        Intent intent = new Intent(getActivity(), PopUpActivity.class);
        intent.putExtra(TITLE_POP_UP, getString(R.string.alert_error_server_title));
        intent.putExtra(SUBTITLE1_POP_UP, getString(R.string.alert_error_server_subtitle));
        intent.putExtra(ACTIONBTN_POP_UP, getString(R.string.ok));
        startActivityForResult(intent, REQUEST_CODE_START_RIDE_FAIL_ACTIVITY);
    }


    @Override
    public void OnDisconnectSuccess(boolean endRide) {
        getNavigationMapRoute().removeRoute();
        if(endRide) {
            launchRideSummaryFragment();
        }else{
            launchBikeBaseFragment();
        }

    }

    @Override
    public void OnDisconnectFailure(boolean endRide) {
        if(endRide) {
            launchRideSummaryFragment();
        }else{
            launchBikeBaseFragment();
        }
    }


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
    protected BikeDirectionFragmentPresenter getPresenter() {
        return bikeDirectionFragmentPresenter;
    }

    @Override
    protected int getFragmentLayoutId() {
        return R.layout.bike_direction_fragment;
    }

    @Override
    public void setTime(String time) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tv_bike_booking_timer.setText(Html.fromHtml(String.format(getString(R.string.bike_booking_timer_text_payment_2), time), Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_bike_booking_timer.setText(Html.fromHtml(String.format(getString(R.string.bike_booking_timer_text_payment_2), time)));
        }

    }

    public void setParentFragment(BikeBaseFragment bikeBaseFragment) {
        this.parentFragment = bikeBaseFragment;
    }

    @Override
    protected void configureViews() {
        super.configureViews();

    }

    private void disconnectLock(boolean endRide) {
        getPresenter().setDisconnectRequiredForApp(true);
        getPresenter().disconnectAllLocks(endRide);
    }

    public void setDirectionForBike(Bike bike, Ride ride) {

        isBikeDirectionPreviouslyActive = true;
        getPresenter().setBike(bike);
        isDirectionLoaded = false;
        isBeginTripClicked = false;
        getPresenter().setDisconnectRequiredForApp(false);
        tv_bikeName_trip_starting.setText(bike.getBike_name());
        bikeNameTextview.setPaintFlags(bikeNameTextview.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        bikeNameTextview.setText(bike.getBike_name());
        navigation = new MapboxNavigation(getActivity(), getString(R.string.map_box_access_token));
        beginTripTextView.setText(getString(R.string.bike_booking_begin_trip));
        showBeginTripGreyOut();
        isBikeConnectedThruBluetooth = false;
        getPresenter().deleteLock();
        lockIssue=false;


        if (ride == null) {
            getPresenter().setRideStarted(false);
            showBikeReservationUI();
        } else {
            getPresenter().setRideStarted( true);
            this.ride = ride;
            this.ride.setBike_price_for_membership(""); // We dont want to show wrong price.
            updateTripDataCounter = -1;
            showTripTimerAndCostUI();
        }

        checkForLocation(); // check if location is ON

        parentFragment.hideOperationLoading();
    }



    @Override
    public void setUserPosition(Location location) {
        getPresenter().requestStopLocationUpdates();
        if (isDirectionLoaded == false) {
            this.fromLocation = location;
            parentFragment.clearMapView();
            showBikeAndCurrentLocation(Point.fromLngLat(fromLocation.getLongitude(),fromLocation.getLatitude()), Point.fromLngLat(getPresenter().getBike().getLongitude(), getPresenter().getBike().getLatitude()));
        }
    }





    private void showBikeAndCurrentLocation(Point origin, Point destination){
        addUserLocation(options,origin.latitude(),origin.longitude(), ResourceUtil.getUserLocationResource(),unselected_size);
        addMarker(options,destination.latitude(), destination.longitude(), ResourceUtil.getBikeResource(getPresenter().getBike().getType().toUpperCase(),true),selected_size);
        symbols = getSymbolManager().create(options);
        if(symbols.size()>1){
            symbols.get(1).setZIndex(25);
        }

        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(new LatLng(destination.latitude(), destination.longitude()));
        latLngs.add(new LatLng(origin.latitude(), origin.longitude()));

        LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
        latLngBounds.includes(latLngs);

        zoomToMarkers(getMapBoxMap(), latLngBounds,latLngs);

    }



    @Override
    public void showConnecting() {
        startBeginTripAnimation();
    }


    @Override
    public void onLockConnectionStatus(Boolean connected) {
        if(!connected){
            showConnectionError();
        }
    }

    @Override
    public void onLockConnected(LockModel lockModel) {
        Log.e(TAG, "######onLockConnected: " + lockModel.toString());
        lockIssue=false;
        stopBeginTripAnimationAsConnected();
        isBikeConnectedThruBluetooth = true;
        getPresenter().saveLock(getPresenter().getLockModel());

    }


    @Override
    public void onLockConnectionFailed() {
        Log.e(TAG, "######onLockConnectionFailed:");
        showBeginTripGreyOut();
        isBikeConnectedThruBluetooth = false;
        getPresenter().getSignedMessagePublicKey(getPresenter().getBike());
    }

    @Override
    public void onLockConnectionAccessDenied() {
        lockIssue=true;
        showConnectionError();
    }

    private void showConnectionError(){
        showBeginTripGreyOut();
        isBikeConnectedThruBluetooth = false;
        isBikeDirectionPreviouslyActive = false;
        Intent intent = new Intent(getActivity(), PopUpActivity.class);
        intent.putExtra(TITLE_POP_UP, getString(R.string.ellipse_access_denided_title));
        intent.putExtra(SUBTITLE1_POP_UP, getString(R.string.ellipse_access_denided_text));
        intent.putExtra(ACTIONBTN_POP_UP, getString(R.string.ok));
        startActivityForResult(intent, REQUEST_CODE_LOCK_ACCESS_DENIED);
    }

    @Override
    public void requestEnableBluetooth() {
        super.requestEnableBluetooth();
    }

    @Override
    public void onSaveLockSuccess(Lock lock) {

    }

    @Override
    public void onSaveLockFailure() {

    }

    @Override
    public void OnSignedMessagePublicKeySuccess(String signedMessage, String publicKey) {
        this.signedMessage = signedMessage;
        this.publicKey = publicKey;

        LockModel lockModel = new LockModel();
        lockModel.setSignedMessage(signedMessage);
        lockModel.setPublicKey(publicKey);
        lockModel.setUserId(getPresenter().getBike().getFleet_key());
        lockModel.setMacId(getPresenter().getBike().getMac_id());
        getPresenter().setLockModel(lockModel);
        getPresenter().disconnectAllLocks(false);    //this will disconnect all previous connections and start scanning for required lock
    }

    @Override
    public void OnSignedMessagePublicKeyFailure() {

    }

    @Override
    public void onGetCurrentUserStatusSuccess(GetCurrentUserStatusResponse getCurrentUserStatusResponse) {
        currentStatus = null;
        if (getCurrentUserStatusResponse != null) {
            if (getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse() == null && getCurrentUserStatusResponse.getCurrentUserStatusTripResponse() == null) {
                currentStatus = HomeMapFragmentPresenter.CurrentStatus.NO_BIKE_NO_TRIP;
            } else if (getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse() != null && getCurrentUserStatusResponse.getCurrentUserStatusTripResponse() == null) {
                currentStatus = HomeMapFragmentPresenter.CurrentStatus.NO_TRIP_BUT_ACTIVE_BIKE_BOOKING;
            } else if (getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse() == null && getCurrentUserStatusResponse.getCurrentUserStatusTripResponse() != null) {
                currentStatus = HomeMapFragmentPresenter.CurrentStatus.ACTIVE_TRIP;
            }
        }

        if (currentStatus != null && currentStatus == HomeMapFragmentPresenter.CurrentStatus.ACTIVE_TRIP) { //  active trip
            ride = new Ride();
            ride.setBike_on_call_operator(getCurrentUserStatusResponse.getOnCallOperator());
            ride.setSupport_phone(getCurrentUserStatusResponse.getSupportPhone());
            getPresenter().getRideSummary(getCurrentUserStatusResponse.getCurrentUserStatusTripResponse().getTrip_id());
        } else {
            subscribeToGetCurrentStatusTimer(true);
        }
    }

    @Override
    public void onGetRideSummarySuccess(RideSummaryResponse rideSummaryResponse) {
        if (currentStatus != null && currentStatus == HomeMapFragmentPresenter.CurrentStatus.ACTIVE_TRIP) { //  active trip
            ride.setId(getPresenter().getFleetId());
            ride.setBikeId(rideSummaryResponse.getRideSummaryResponse().getBike_id());
            ride.setRideId(rideSummaryResponse.getRideSummaryResponse().getTrip_id());
            ride.setRide_booked_on(rideSummaryResponse.getRideSummaryResponse().getDate_created());
            getPresenter().getBikeDetails(ride.getBikeId());
        }
    }

    @Override
    public void onBikeDetailsSuccess(Bike bike) {
        if (currentStatus != null && currentStatus == HomeMapFragmentPresenter.CurrentStatus.ACTIVE_TRIP) {
            Ride ride = getPresenter().getBikeModelMapper().mapIn(bike);
            ride.setId(getPresenter().getFleetId());
            ride.setBikeId(this.ride.getBikeId());
            ride.setRideId(this.ride.getRideId());
            ride.setRide_booked_on(this.ride.getRide_booked_on());
            ride.setBike_on_call_operator(this.ride.getBike_on_call_operator());
            ride.setSupport_phone(this.ride.getSupport_phone());
            getPresenter().saveRide(ride);
        }
    }

    @Override
    public void onBikeUnAuthorised() {
        showFailurePopupForPaidRide(getString(R.string.alert_error_server_title), getString(R.string.qr_error_fleet_access_denied));
    }

    @Override
    public void onBikeAlreadyRented() {
        showFailurePopupForPaidRide(getString(R.string.alert_error_server_title), getString(R.string.qr_error_bike_is_rent));
    }

    @Override
    public void onBikeNotAvailable() {
        showFailurePopupForPaidRide(getString(R.string.alert_error_server_title), getString(R.string.qr_error_bike_not_live));
    }

    @Override
    public void onBikeNotFound() {
        showFailurePopupForPaidRide(getString(R.string.alert_error_server_title), getString(R.string.alert_error_server_subtitle));
    }


    @Override
    public void onSaveRideSuccess(Ride ride) {
        this.ride = ride;
        getPresenter().setRideStarted(true);
        showTripTimerAndCostUI();
    }

    @Override
    public void onTripDataSuccess(UpdateTripResponse updateTripResponse) {

        if (updateTripResponse != null && updateTripResponse.getUpdateTripDataResponse() != null) {
            cv_trip_cost_currency_unit_label.setText(CurrencyUtil.getCurrencySymbolByCode(updateTripResponse.getUpdateTripDataResponse().getCurrency()));
            cv_trip_cost_value_label.setText(UtilHelper.getDotAfterNumber("" + updateTripResponse.getUpdateTripDataResponse().getCharge_for_duration()));
        }
    }

    @Override
    public void onTripDataFailure() {

    }

    @Override
    public void onSaveRideFailure() {
        showFailurePopupForPaidRide(getString(R.string.alert_error_server_title), getString(R.string.alert_error_server_subtitle));
    }

    @Override
    public void onBikeDetailsFailure() {
        showFailurePopupForPaidRide(getString(R.string.alert_error_server_title), getString(R.string.alert_error_server_subtitle));
    }

    @Override
    public void onGetRideSummaryFailure() {
        showFailurePopupForPaidRide(getString(R.string.alert_error_server_title), getString(R.string.alert_error_server_subtitle));
    }

    @Override
    public void onGetCurrentUserStatusFailure() {
        showFailurePopupForPaidRide(getString(R.string.alert_error_server_title), getString(R.string.alert_error_server_subtitle));
    }

    private void showFailurePopupForPaidRide(String title, String message) {
        Intent intent = new Intent(getActivity(), PopUpActivity.class);
        intent.putExtra(TITLE_POP_UP, title);
        intent.putExtra(SUBTITLE1_POP_UP, message);
        intent.putExtra(ACTIONBTN_POP_UP, getString(R.string.ok));
        startActivityForResult(intent, REQUEST_CODE_GET_CURRENT_STATUS_FAIL_ACTIVITY);
    }

    public synchronized void subscribeToGetCurrentStatusTimer(boolean active) {
        if (active) {
            if (getCurrentStatusTimerSubscription == null) {
                getCurrentStatusTimerSubscription = Observable.timer(MILLISECONDS_FOR_GET_CURRENT_STATUS, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Long>() {
                            public void accept(Long aLong) {
                                subscribeToGetCurrentStatusTimer(false);
                                getPresenter().getCurrentUserStatus();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {

                            }
                        });
            }
        } else {
            if (getCurrentStatusTimerSubscription != null) {
                getCurrentStatusTimerSubscription.dispose();
                getCurrentStatusTimerSubscription = null;
            }
        }
    }


    private void showBikeReservationUI() {
        ll_ui_after_trip_starting.setVisibility(GONE);
        cv_see_ride_info.setVisibility(GONE);
        ll_ui_before_trip_starting.setVisibility(VISIBLE);
        rl_trip_timer_cost.setVisibility(GONE);
        cv_begin_trip_help.setVisibility(VISIBLE);
        tv_bike_booking_timer.setVisibility(VISIBLE);
        cv_bike_booking_timer_text_payment_free.setVisibility(VISIBLE);
        if (IsRidePaid.isRidePaidForFleet(getPresenter().getBike().getFleet_type())) {
            cv_bike_booking_timer_text_payment_free.setText(getString(R.string.bike_booking_timer_text_payment_1));
        } else {
            cv_bike_booking_timer_text_payment_free.setText(getString(R.string.bike_booking_timer_text_free));
        }
        startBikeReservationTimer();
    }

    private void stopBeginTripAnimationAsConnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (beginTripValueAnimator != null) {
                    beginTripValueAnimator.cancel();
                    beginTripValueAnimator = null;
                }
                view_animating_begin_trip.startAnimation(showConnectedAnimation());
            }
        });
    }

    private void showBeginTripGreyOut() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (beginTripValueAnimator != null) {
                    beginTripValueAnimator.cancel();
                    beginTripValueAnimator = null;
                }

                if(connectingAnimation !=null){
                    connectingAnimation.cancel();
                    connectingAnimation.reset();
                    connectingAnimation =null;
                }

                if(connectedAnimation !=null){
                    connectedAnimation.cancel();
                    connectedAnimation.reset();
                    connectedAnimation =null;
                }
                view_animating_begin_trip.setBackgroundColor(Color.parseColor("#B7C1CD"));
                rl_bike_reservation_begin_trip.setBackgroundColor(Color.parseColor("#B7C1CD"));
            }
        });
    }

    private void startBeginTripAnimation() {
        view_animating_begin_trip.startAnimation(showConnectingAnimation());
    }

    private Animation showConnectingAnimation() {

        if(connectedAnimation !=null){
            connectedAnimation.cancel();
            connectedAnimation.reset();
            connectedAnimation =null;
        }

        if(connectingAnimation ==null || connectingAnimation.hasEnded()) {
            connectingAnimation = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, -1.0f,
                    Animation.RELATIVE_TO_PARENT, 1.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f);
            view_animating_begin_trip.setBackgroundColor(Color.parseColor("#00AAD1"));
            connectingAnimation.setRepeatCount(Animation.INFINITE);
            connectingAnimation.setDuration(1500);
            connectingAnimation.setInterpolator(new AccelerateInterpolator());
        }
        return connectingAnimation;
    }

    private Animation showConnectedAnimation(){

        if(connectingAnimation !=null){
            connectingAnimation.cancel();
            connectingAnimation.reset();
            connectingAnimation =null;
        }

        if(connectedAnimation ==null || connectedAnimation.hasEnded()) {
            connectedAnimation = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 1.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f);

            view_animating_begin_trip.setBackgroundColor(Color.parseColor("#00AAD1"));
            connectedAnimation.setDuration(1500);
            connectedAnimation.setInterpolator(new AccelerateInterpolator());
        }

        connectedAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view_animating_begin_trip.setBackgroundColor(Color.parseColor("#00AAD1"));
                rl_bike_reservation_begin_trip.setBackgroundColor(Color.parseColor("#00AAD1"));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        return connectedAnimation;
    }



    private void showActiveTrip() {
        parentFragment.hideOperationLoading();
        cancelAllSubscriptionAndTimer();
        parentFragment.showActiveRideFragment();
    }

    private void showTripTimerAndCostUI() {
        ll_ui_after_trip_starting.setVisibility(VISIBLE);
        cv_see_ride_info.setVisibility(VISIBLE);
        ll_ui_before_trip_starting.setVisibility(GONE);
        cv_begin_trip_help.setVisibility(GONE);
        tv_bike_booking_timer.setVisibility(GONE);
        cv_bike_booking_timer_text_payment_free.setVisibility(GONE);
        rl_trip_timer_cost.setVisibility(VISIBLE);
        cv_trip_cost_value_label.setText(ride.getBike_price_for_membership());
        Log.e(TAG, "ride.getBike_price_for_membership()-->" + ride.getBike_price_for_membership());
        subscribeToTripCostTimer(true);
    }

    public synchronized void subscribeToTripCostTimer(boolean active) {
        if (active) {

            if (tripCostTimerSubscription != null) {
                tripCostTimerSubscription.cancel();
            }

            tripCostTimerSubscription = new Timer();
            tripCostTimerSubscription.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cv_trip_timer_value_label.setText(UtilHelper.getDurationBreakdown(getActivity(),UtilHelper.getTime() - (long) ride.getRide_booked_on()));

                                if (updateTripDataCounter > 15 || updateTripDataCounter == -1) {
                                    updateTripDataCounter = 0;
                                    getPresenter().updateTrip(ride);
                                } else {
                                    updateTripDataCounter++;
                                }

                            }
                        });
                    } catch (Exception e) {

                    }
                }
            }, 0, TIMER_TRIP_COST_INTERVAL_VALUE_MILLISECONDS);

        } else {
            if (tripCostTimerSubscription != null) {
                tripCostTimerSubscription.cancel();
                tripCostTimerSubscription = null;
            }
        }
    }


    private void startBikeReservationTimer() {

        int timeRemaining = calculateTimeRemaining();

        if (timeRemaining == 0 || timeRemaining < 0) {
            launchNextScreenDepeningUponFleetType();
            return;
        }


        if (bikeBookTimer != null) {
            bikeBookTimer.cancel();
        }

        bikeBookTimer = new CountDownTimer(timeRemaining * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;

                String time = String.format("%02d", seconds / 60)
                        + ":" + String.format("%02d", seconds % 60);

                setTime(time);
            }

            public void onFinish() {
                launchNextScreenDepeningUponFleetType();
            }
        }.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelAllSubscriptionAndTimer();
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "######onResume:");
        if (isBikeDirectionPreviouslyActive == true) {
            Log.e(TAG, "######onResume:isBikeDirectionPreviouslyActive-->true");
            if (getPresenter().isRideStarted()) {
                subscribeToTripCostTimer(true);
            } else {
                startBikeReservationTimer();
            }
            if (isBikeConnectedThruBluetooth == false) {
                showBeginTripGreyOut();
                getPresenter().disconnectAllLocks(false);
            }else{
                getPresenter().observeConnectionState();
            }

        } else {
            Log.e(TAG, "######onResume:isBikeDirectionPreviouslyActive-->false");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void launchNextScreenDepeningUponFleetType() {
        if (IsRidePaid.isRidePaidForFleet(getPresenter().getBike().getFleet_type())) {
            showRideFareAndCost();
        } else {
            disconnectLock(false);
        }
    }

    private void showRideFareAndCost() {
        getPresenter().getCurrentUserStatus();
    }

    private int calculateTimeRemaining() {
        long currentTime = UtilHelper.getTime();
        long diffTime = currentTime - getPresenter().getBike().getBooked_on();
        long seconds = diffTime;
        if (seconds > getPresenter().getBike().getExpires_in()) {
            return 0;
        } else {
            return (int) ((getPresenter().getBike().getExpires_in() + getPresenter().getBike().getBooked_on()) - currentTime);
        }
    }

    private void launchRideSummaryFragment(){
        cancelAllSubscriptionAndTimer();
        parentFragment.showRideSummaryFragment();
    }


    private void launchBikeBaseFragment() {
        cancelAllSubscriptionAndTimer();
        parentFragment.showBikeBaseFragment();
    }


    void launchEndRideCheckListActivity(boolean isForceEndRide) {
        Log.e(TAG, "launchEndRideCheckListActivity::" + isForceEndRide);
        Intent intent = new Intent(getActivity(), EndRideCheckListActivity.class);
        intent.putExtra(TRIP_ID, this.ride.getRideId());


        Location location = null;

        if (location != null) {
            double lat = location.getLatitude();
            double longitude = location.getLongitude();
            intent.putExtra(LONGITUDE_END_RIDE_ID, longitude);
            intent.putExtra(LATITUDE_END_RIDE_ID, lat);
        }

        if (isForceEndRide) {
            intent.putExtra(FORCE_END_RIDE_ID, isForceEndRide);
        }

        intent.putExtra(END_RIDE_ID_LOADING_STRING, getString(R.string.bike_booking_cancelling_loader));

        startActivityForResult(intent, REQUEST_END_RIDE_CHECKLIST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult" + requestCode);
        if (requestCode == REQUEST_CODE_CANCEL_BIKE_BOOKING) {
            if (resultCode == RESULT_OK) {
                HomeMapFragment.currentStatus = HomeMapFragmentPresenter.CurrentStatus.NO_BIKE_NO_TRIP;
                disconnectLock(false);
            } else if (resultCode == RESULT_CANCELED) {
                isBikeDirectionPreviouslyActive = true;
            }
        } else if (requestCode == REQUEST_END_RIDE_CHECKLIST && resultCode == RESULT_OK) {
            disconnectLock(true);
        } else if (requestCode == REQUEST_END_RIDE_CHECKLIST && resultCode == RESULT_CANCELED) {
            if (data != null) {
                if (data.hasExtra(END_RIDE_PAYMENT_FAILURE)) {
                    Log.e(TAG, "onActivityResult::Request_end_Ride_payment failure::true");
                    if (data.getExtras().getBoolean(END_RIDE_PAYMENT_FAILURE)) {
                        startActivityForResult(new Intent(getActivity(), AddCardActivity.class), REQUEST_CODE_ADD_CARD_ACTIVITY);
                        return;
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_START_RIDE_FAIL_ACTIVITY) {
            disconnectLock(false);
        } else if (requestCode == REQUEST_CODE_BIKE_END_RIDE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                launchEndRideCheckListActivity(true);
            } else if (resultCode == RESULT_CANCELED) {
                // do nothing
            }
        }else if (requestCode == REQUEST_CODE_LOCK_ACCESS_DENIED) {
            parentFragment.showOperationLoading(getString(R.string.bike_booking_cancelling_loader));
            getPresenter().cancelBikeReservation(getPresenter().getBike(),false,true);
        }
    }


    private void cancelAllSubscriptionAndTimer() {
        if (bikeBookTimer != null) {
            bikeBookTimer.cancel();
            bikeBookTimer = null;
        }

        subscribeToTripCostTimer(false);
        getPresenter().cancelConnectionSubscription();
        getPresenter().requestStopLocationUpdates();
        subscribeToGetCurrentStatusTimer(false);
        subscribeToTripCostTimer(false);


        if (beginTripValueAnimator != null) {
            beginTripValueAnimator.cancel();
            beginTripValueAnimator = null;
        }

        if(connectedAnimation!=null){
            connectedAnimation.cancel();
            connectedAnimation.reset();
            connectedAnimation =null;
        }

        if(connectingAnimation!=null){
            connectingAnimation.cancel();
            connectingAnimation.reset();
            connectingAnimation =null;
        }

    }


    @Override
    public void onCancelBikeSuccess() {
        parentFragment.hideOperationLoading();
        disconnectLock(false);
    }

    @Override
    public void onCancelBikeFail() {
        parentFragment.hideOperationLoading();
        PopUpActivity.launchForResult(getActivity(), REQUEST_CODE_CANCEL_BIKE_RESERVATION_FAIL, getString(R.string.alert_error_server_title),
                getString(R.string.alert_error_server_subtitle), null, getString(R.string.ok));
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
    }

    private MapboxMap getMapBoxMap(){
        return  parentFragment.getMapboxMap();
    }


    @Override
    public void OnRideSuccess(Ride ride) {

    }

    @Override
    public void OnRideFailure() {

    }

    @Override
    public void showDisconnected() {

    }

    @Override
    public void showConnected() {

    }

    @Override
    public void showConnectionTimeOut() {

    }

    @Override
    public void onSignedMessagePublicKeySuccess(String signedMessage, String publicKey) {

    }

    @Override
    public void onSignedMessagePublicKeyFailure() {

    }

    @Override
    public void onSetPositionStatus(Boolean status) {

    }

    @Override
    public void onSetPositionFailure() {

    }

    @Override
    public void showLockPositionError() {

    }

    @Override
    public void showLockPositionSuccess(Lock.Hardware.Position position) {

    }

    @Override
    public void setRideDurationAndCost(UpdateTripData updateTripData) {

    }


}
