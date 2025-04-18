package com.lattis.ellipse.presentation.ui.home.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.gson.Gson;
import com.lattis.ellipse.Utils.MapboxUtil;
import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.fragment.BaseFragment;
import com.lattis.ellipse.presentation.ui.bike.BikeBaseFragment;
import com.lattis.ellipse.presentation.ui.home.HomeActivity;
import com.lattis.ellipse.presentation.ui.parking.FindParkingFragment;
import com.lattis.ellipse.presentation.ui.ride.ActiveRideFragment;
import com.lattis.ellipse.presentation.ui.ride.RideSummaryActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomTextView;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.FillManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;

import javax.inject.Inject;

import butterknife.BindView;
import io.lattis.ellipse.R;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.lattis.ellipse.Utils.ResourceUtil.charging_spots;
import static com.lattis.ellipse.Utils.ResourceUtil.e_bike_selected;
import static com.lattis.ellipse.Utils.ResourceUtil.e_bike_unselected;
import static com.lattis.ellipse.Utils.ResourceUtil.generic_parking;
import static com.lattis.ellipse.Utils.ResourceUtil.ic_pick_location;
import static com.lattis.ellipse.Utils.ResourceUtil.kick_scooter_selected;
import static com.lattis.ellipse.Utils.ResourceUtil.kick_scooter_unselected;
import static com.lattis.ellipse.Utils.ResourceUtil.parking_meter;
import static com.lattis.ellipse.Utils.ResourceUtil.parking_racks;
import static com.lattis.ellipse.Utils.ResourceUtil.regular_selected;
import static com.lattis.ellipse.Utils.ResourceUtil.regular_unselected;
import static com.lattis.ellipse.Utils.ResourceUtil.user_location;
import static com.lattis.ellipse.presentation.ui.bike.BikeInfoActivity.ARGS_BIKE_DETAILS;
import static com.lattis.ellipse.presentation.ui.bike.BikeInfoActivity.ARGS_RESERVE_BIKE;
import static com.lattis.ellipse.presentation.ui.home.HomeActivity.REQUEST_APP_UPDATE_POP;
import static com.lattis.ellipse.presentation.ui.home.HomeActivity.REQUEST_CODE_BIKE_INFO;
import static com.lattis.ellipse.presentation.ui.home.HomeActivity.REQUEST_CODE_PAYMENT_INFO;
import static com.lattis.ellipse.presentation.ui.home.HomeActivity.REQUEST_CODE_REPORT_DAMAGE;
import static com.lattis.ellipse.presentation.ui.home.HomeActivity.REQUEST_CODE_REPORT_THEFT;
import static com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragmentPermissionsDispatcher.getLocationPermissionWithPermissionCheck;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;


@RuntimePermissions
public class HomeMapFragment extends BaseFragment<HomeMapFragmentPresenter> implements HomeMapFragmentView,
        OnMapReadyCallback, MapboxMap.OnMoveListener, MapboxMap.OnFlingListener {

    private final String TAG = HomeMapFragment.class.getName();

    private final int REQUEST_CODE_CANCEL_BIKE_BOOKING = 2000;
    private final int REQUEST_CODE_RIDE_SUMMARY = 2306;
    public static final int REQUEST_CHECK_LOCATION_SETTINGS = 3106;
    public static final int REQUEST_CODE_SYSTEM_LOCATION_PERMISSION_DENIED = 4956;
    private MapboxMap mapboxMap;
    private SupportMapFragment mapFragment;
    private MapView mapview;
    private FindParkingFragment findParkingFragment;
    private ActiveRideFragment activeRideFragment;
    private BikeBaseFragment bikeBaseFragment;
    private BaseFragment currentFragment;
    public boolean isApplicationStarted = true;
    private Ride ride;
    private Bike bike;
    private final int REQUEST_CODE_BIKE_BOOKING = 3000;
    private int REQUEST_CODE_NO_INTERNET = 2242;
    public static int GET_CURRENT_STATUS_POP_UP_REQUEST_CODE = 3245;
    public static int REQUEST_CODE_LOCATION_PERMISSION_DENIED = 3010;
    public static int REQUEST_CODE_FOR_APP_UPDATE = 3011;
    private boolean isInternetConnected =true;
    public static HomeMapFragmentPresenter.CurrentStatus currentStatus;
    private SymbolManager symbolManager;
    private NavigationMapRoute navigationMapRoute;
    private FillManager fillManager;
    private CircleManager circleManager;


    @BindView((R.id.home_fragment_rl_loading_operation))
    View home_fragment_rl_loading_operation;

    @BindView(R.id.home_fragment_label_operation_name)
    CustomTextView home_fragment_label_operation_name;

    @BindView(R.id.greyline)
    ProgressBar greyline;

    @Inject
    HomeMapFragmentPresenter homePresenter;

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
    protected HomeMapFragmentPresenter getPresenter() {
        return homePresenter;
    }

    @Override
    protected int getFragmentLayoutId() {
        return R.layout.home_map_fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.support_map);
        mapview = (MapView) mapFragment.getView();
        mapview.onCreate(savedInstanceState);
        mapview.getMapAsync(this);
    }


    @Override
    protected void configureViews() {
        super.configureViews();

        bikeBaseFragment = (BikeBaseFragment) getChildFragmentManager().findFragmentById(R.id.find_bike_fragment);
        bikeBaseFragment.setParentFragment(this);
        hideFragments(bikeBaseFragment);

        findParkingFragment = (FindParkingFragment) getChildFragmentManager().findFragmentById(R.id.find_parking_fragment);
        findParkingFragment.setParentFragment(this);
        hideFragments(findParkingFragment);

        activeRideFragment = (ActiveRideFragment) getChildFragmentManager().findFragmentById(R.id.active_ride_fragment);
        activeRideFragment.setParentFragment(this);
        hideFragments(activeRideFragment);

        showOperationLoading(getString(R.string.finding_you_label));
    }

    // TODO On Marker click event
//    @Override
//    public boolean onMarkerClick(@NonNull Marker marker) {
//        return currentFragment.setMarkerClicked(marker);
//    }


    @Override
    public void onMapReady(MapboxMap mapboxMap) {

        mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
        mapboxMap.getUiSettings().setCompassEnabled(false);

        this.mapboxMap = mapboxMap;


        // TODO Mapbox style and remove location tracking

        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                symbolManager = new SymbolManager(mapview,mapboxMap, style);
                symbolManager.setIconAllowOverlap(true);  //your choice t/f
                symbolManager.setTextAllowOverlap(true);  //your choice t/f

                fillManager = new FillManager(mapview,mapboxMap,style);
                circleManager = new CircleManager(mapview,mapboxMap,style);

                navigationMapRoute = new NavigationMapRoute(null, mapview, mapboxMap);

                Bitmap ebike_unselected = BitmapFactory.decodeResource(getResources(), R.drawable.ebike_without_shadow_half);
                mapboxMap.getStyle().addImage(e_bike_unselected,ebike_unselected);

                Bitmap ebike_selected = BitmapFactory.decodeResource(getResources(), R.drawable.ebike_with_shadow_half);
                mapboxMap.getStyle().addImage(e_bike_selected,ebike_selected);

                Bitmap regularSelected = BitmapFactory.decodeResource(getResources(), R.drawable.regular_with_shadow_half);
                mapboxMap.getStyle().addImage(regular_selected,regularSelected);

                Bitmap regularUnselected = BitmapFactory.decodeResource(getResources(), R.drawable.regular_without_shadow_half);
                mapboxMap.getStyle().addImage(regular_unselected,regularUnselected);

                Bitmap kickScooter_selected = BitmapFactory.decodeResource(getResources(), R.drawable.kickscooter_with_shadow_half);
                mapboxMap.getStyle().addImage(kick_scooter_selected,kickScooter_selected);

                Bitmap kickScooter_unselected = BitmapFactory.decodeResource(getResources(), R.drawable.kickscooter_without_shadow_half);
                mapboxMap.getStyle().addImage(kick_scooter_unselected,kickScooter_unselected);


                Bitmap ic_pick_location_icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_pick_location);
                mapboxMap.getStyle().addImage(ic_pick_location,ic_pick_location_icon);


                Bitmap generic_parking_icon = BitmapFactory.decodeResource(getResources(), R.drawable.generic_parking_half);
                mapboxMap.getStyle().addImage(generic_parking,generic_parking_icon);

                Bitmap parking_meter_icon = BitmapFactory.decodeResource(getResources(), R.drawable.parking_meter_half);
                mapboxMap.getStyle().addImage(parking_meter,parking_meter_icon);

                Bitmap parking_rack_icon = BitmapFactory.decodeResource(getResources(), R.drawable.parking_rack_half);
                mapboxMap.getStyle().addImage(parking_racks,parking_rack_icon);

                Bitmap charging_spot_icon = BitmapFactory.decodeResource(getResources(), R.drawable.charging_spot_half);
                mapboxMap.getStyle().addImage(charging_spots,charging_spot_icon);

                Bitmap user_location_icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_current_location_small);
                mapboxMap.getStyle().addImage(user_location,user_location_icon);


                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
//                if(isApplicationStarted)
//                    getLocationPermissionWithPermissionCheck(HomeMapFragment.this);
                if(isApplicationStarted)
                    getPresenter().checkForNewVersion();

            }
        });

        // TODO Marker click, scroll listener for Mapbox
        this.mapboxMap.addOnMoveListener(this);
        this.mapboxMap.addOnFlingListener(this);


        getFindParkingFragment().setMapboxMap(this.mapboxMap);
        getActiveRideFragment().setMapboxMap(this.mapboxMap);
        getBikeBaseFragment().setMapboxMap(this.mapboxMap);
    }



    @Override
    public void onAppUpdateAvailable(AppUpdateManager appUpdateManager, AppUpdateInfo appUpdateInfo){

//        try {
//            appUpdateManager.startUpdateFlowForResult(
//                    appUpdateInfo,
//                    AppUpdateType.IMMEDIATE,
//                    getActivity(),
//                    REQUEST_CODE_FOR_APP_UPDATE);
//        } catch (IntentSender.SendIntentException e) {
//            appUpdateFailed();
//        }


        new MaterialAlertDialogBuilder(getActivity(),R.style.AlertDialogTheme)
                .setTitle(getString(R.string.app_update_avaliable_title))
                .setMessage(getString(R.string.app_update_avaliable_text))
                .setPositiveButton(getString(R.string.app_update_avaliable_action), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openGooglePlayStore();
                        getActivity().finish();
                    }
                })
                .setNeutralButton(getString(R.string.app_update_avaliable_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onAppUpdateNotAvailable();
                    }
                })
                .show();





    }
    @Override
    public void onAppUpdateNotAvailable(){
        startFetchingLocation();
    }




    private void startFetchingLocation(){
        if(isApplicationStarted)
            getLocationPermissionWithPermissionCheck(HomeMapFragment.this);
    }

    private void appUpdateFailed(){
        startFetchingLocation();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresenter().getLocalUser();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().clearAllSubscriptions();
        mapview.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapview.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapview.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapview.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapview.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapview.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapview.onSaveInstanceState(outState);
    }


    private void hideFragments(Fragment... fragments) {
        try {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            for (Fragment fragment : fragments) {
                ft.hide(fragment);
            }
            ft.commit();
        } catch (IllegalStateException e) {
            if(meVisible) {
                getPresenter().getCurrentUserStatus();
            }
        }
    }

    private void showFragments(Fragment... fragments) {
        try {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            for (Fragment fragment : fragments) {
                currentFragment = (BaseFragment) fragment;
                ft.show(fragment);
                if (mapboxMap != null && currentFragment != null)
                    currentFragment.setMapBox(mapboxMap);
            }
            ft.commit();
        } catch (IllegalStateException e) {
            if(meVisible) {
                getPresenter().getCurrentUserStatus();
            }
        }
    }

    @Override
    public void onGetCurrentUserStatusSuccess(GetCurrentUserStatusResponse getCurrentUserStatusResponse) {
        isApplicationStarted = false;
        currentStatus = null;

        Log.e(TAG,"onGetCurrentUserStatusSuccess::------->");

        if (getCurrentUserStatusResponse != null) {
            if (getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse() == null && getCurrentUserStatusResponse.getCurrentUserStatusTripResponse() == null) {
                // No bike book and no trip
                currentStatus = HomeMapFragmentPresenter.CurrentStatus.NO_BIKE_NO_TRIP;
            } else if (getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse() != null && getCurrentUserStatusResponse.getCurrentUserStatusTripResponse() == null) {
                // no trip but active bike booking
                currentStatus = HomeMapFragmentPresenter.CurrentStatus.NO_TRIP_BUT_ACTIVE_BIKE_BOOKING;
            } else if (getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse() == null && getCurrentUserStatusResponse.getCurrentUserStatusTripResponse() != null) {
                //  active trip
                currentStatus = HomeMapFragmentPresenter.CurrentStatus.ACTIVE_TRIP;
            } else if (getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse() != null && getCurrentUserStatusResponse.getCurrentUserStatusTripResponse() != null) {
                //  server fault
                currentStatus = HomeMapFragmentPresenter.CurrentStatus.INVALID;
            }

            if (currentStatus == HomeMapFragmentPresenter.CurrentStatus.NO_BIKE_NO_TRIP) {   // No bike book and no trip
                ride = new Ride();
                ride.setId(getPresenter().getFleetId());
                ride.setBike_on_call_operator(getCurrentUserStatusResponse.getOnCallOperator());
                ride.setSupport_phone(getCurrentUserStatusResponse.getSupportPhone());
                getPresenter().saveRide(ride);
            } else if (currentStatus == HomeMapFragmentPresenter.CurrentStatus.NO_TRIP_BUT_ACTIVE_BIKE_BOOKING) {    // no trip but active bike booking
                ride = new Ride();
                ride.setId(getPresenter().getFleetId());
                ride.setBikeId(getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse().getBike_id());
                ride.setBike_booked_on(getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse().getBooked_on());
                ride.setBike_expires_in((int) (getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse().getTill() - getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse().getBooked_on()));
                ride.setBike_on_call_operator(getCurrentUserStatusResponse.getOnCallOperator());
                ride.setSupport_phone(getCurrentUserStatusResponse.getSupportPhone());
                //getPresenter().saveRide(ride);
                getPresenter().getBikeDetails(ride.getBikeId());
            } else if (currentStatus == HomeMapFragmentPresenter.CurrentStatus.ACTIVE_TRIP) { //  active trip
                ride = new Ride();
                ride.setBike_on_call_operator(getCurrentUserStatusResponse.getOnCallOperator());
                ride.setSupport_phone(getCurrentUserStatusResponse.getSupportPhone());
                getPresenter().getRideSummary(getCurrentUserStatusResponse.getCurrentUserStatusTripResponse().getTrip_id());
            }
        }
    }


    public void stopUdateTripService(boolean disconnect) {
        if(disconnect) {
            getPresenter().disconnectAllLocks();
        }
        getPresenter().stopUpdateTripService();
    }

    @Override
    public void onSaveRideSuccess(Ride ride) {
        boolean disconnect= true;
        if (currentStatus == HomeMapFragmentPresenter.CurrentStatus.NO_BIKE_NO_TRIP) {
            showBikeListFragment();
        } else if (currentStatus == HomeMapFragmentPresenter.CurrentStatus.NO_TRIP_BUT_ACTIVE_BIKE_BOOKING) {
            showBikeBookingFragment(null);
        } else if (currentStatus == HomeMapFragmentPresenter.CurrentStatus.ACTIVE_TRIP) {
            this.ride = ride;
            if (this.ride.isFirst_lock_connect()) {
                disconnect=false;
                showActiveRideFragment();
            } else {
                showBikeBookingFragment(ride);
            }
        }
        stopUdateTripService(disconnect);
    }

    @Override
    public void onBikeDetailsSuccess(Bike bike) {
        this.bike = bike;
        if (currentStatus == HomeMapFragmentPresenter.CurrentStatus.NO_TRIP_BUT_ACTIVE_BIKE_BOOKING) {
            bike.setBooked_on(ride.getBike_booked_on());
            bike.setExpires_in(ride.getBike_expires_in());
            Ride ride = getPresenter().getBikeModelMapper().mapIn(bike);
            ride.setId(getPresenter().getFleetId());
            ride.setBikeId(this.ride.getBikeId());
            ride.setBike_booked_on(this.ride.getBike_booked_on());
            ride.setBike_expires_in(this.ride.getBike_expires_in());
            ride.setBike_on_call_operator(this.ride.getBike_on_call_operator());
            ride.setSupport_phone(this.ride.getSupport_phone());
            getPresenter().saveRide(ride);
        } else if (currentStatus == HomeMapFragmentPresenter.CurrentStatus.ACTIVE_TRIP) {
            Ride ride = getPresenter().getBikeModelMapper().mapIn(bike);
            ride.setId(getPresenter().getFleetId());
            ride.setBikeId(this.ride.getBikeId());
            ride.setRideId(this.ride.getRideId());
            ride.setRide_booked_on(this.ride.getRide_booked_on());
            ride.setBike_on_call_operator(this.ride.getBike_on_call_operator());
            ride.setSupport_phone(this.ride.getSupport_phone());
            ride.setFirst_lock_connect(this.ride.isFirst_lock_connect());
            ride.setDo_not_track_trip(this.ride.getDo_not_track_trip());
            getPresenter().saveRide(ride);
        }
    }

    @Override
    public void onGetRideSummarySuccess(RideSummaryResponse rideSummaryResponse) {
        ride.setId(getPresenter().getFleetId());
        ride.setBikeId(rideSummaryResponse.getRideSummaryResponse().getBike_id());
        ride.setRideId(rideSummaryResponse.getRideSummaryResponse().getTrip_id());
        ride.setRide_booked_on(rideSummaryResponse.getRideSummaryResponse().getDate_created());
        ride.setDo_not_track_trip(rideSummaryResponse.getRideSummaryResponse().getDo_not_track_trip());
        ride.setFirst_lock_connect(rideSummaryResponse.getRideSummaryResponse().isFirst_lock_connect());
        getPresenter().getBikeDetails(ride.getBikeId());
    }


    @Override
    public void onGetRideSummaryFailure() {
        hideOperationLoading();
        PopUpActivity.launchForResult(getActivity(), GET_CURRENT_STATUS_POP_UP_REQUEST_CODE, getString(R.string.get_current_status_failure_title),
                getString(R.string.get_current_status_failure_subtitle), "", getString(R.string.please_try_again));
    }

    @Override
    public void onBikeDetailsFailure() {
        hideOperationLoading();
        PopUpActivity.launchForResult(getActivity(), GET_CURRENT_STATUS_POP_UP_REQUEST_CODE, getString(R.string.get_current_status_failure_title),
                getString(R.string.get_current_status_failure_subtitle), "", getString(R.string.please_try_again));
    }

    @Override
    public void onSaveRideFailure() {
        hideOperationLoading();
    }


    @Override
    public void onGetCurrentUserStatusFailure() {
        hideOperationLoading();
        if(isInternetConnected) {
            PopUpActivity.launchForResult(getActivity(), GET_CURRENT_STATUS_POP_UP_REQUEST_CODE, getString(R.string.get_current_status_failure_title),
                    getString(R.string.get_current_status_failure_subtitle), "", getString(R.string.please_try_again));

        }

    }


    @Override
    public void onRideDeleted() {
        hideFragments(getBikeBaseFragment(), getFindParkingFragment(), getActiveRideFragment());
        getLocationPermissionWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION})
    public void getLocationPermission() {
        MapboxUtil.activateLocationComponent(getContext(),mapboxMap,this.mapboxMap.getStyle());
        getPresenter().getLocationSetting();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            hideOperationLoading();
            PopUpActivity.launchForResult(getActivity(), REQUEST_CODE_LOCATION_PERMISSION_DENIED, getString(R.string.notice),
                    getString(R.string.privacy_location_explanation), "", getString(R.string.privacy_location_submit_button));

            if(getPresenter().getUser()!=null) {
                logCustomException(new Throwable("Location Permission denied: " + getPresenter().getUser().getEmail()));
            }else{
                logCustomException(new Throwable("Location Permission denied: " ));
            }
        }
        HomeMapFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }



    public void showBikeListFragment() {
        currentStatus = HomeMapFragmentPresenter.CurrentStatus.NO_BIKE_NO_TRIP;
        hideFragments(getFindParkingFragment(), getActiveRideFragment());
        stopUdateTripService(true);
        showFragments(getBikeBaseFragment());
        getBikeBaseFragment().start();
        currentFragment = getBikeBaseFragment();
        ((HomeActivity) getActivity()).setDrawerMenuWithoutRide();
    }

    public void showParkingFragment(int bike_fleet_id, Location location, Lock.Hardware.Position lockPosition, Integer lock_battery) {
        currentStatus = HomeMapFragmentPresenter.CurrentStatus.ACTIVE_TRIP;
        hideFragments(getActiveRideFragment(), getBikeBaseFragment(), getFindParkingFragment());
        showFragments(getFindParkingFragment());
        getFindParkingFragment().startShowingParkingSpotAndZones(bike_fleet_id, location, lockPosition, lock_battery);
        currentFragment = getFindParkingFragment();
        showOperationLoading("Loading...");
    }


    public void showBikeBookingFragment(Ride currentRide) {
        currentStatus = HomeMapFragmentPresenter.CurrentStatus.NO_TRIP_BUT_ACTIVE_BIKE_BOOKING;
        hideFragments(getFindParkingFragment(), getBikeBaseFragment(), getActiveRideFragment());
        stopUdateTripService(true);
        showFragments(getBikeBaseFragment());
        getBikeBaseFragment().stop();
        getBikeBaseFragment().reserveBike(bike, currentRide);
        currentFragment = getBikeBaseFragment();
        ((HomeActivity) getActivity()).setDrawerMenuWithRide();
    }

    public void showActiveRideFragment() {
        currentStatus = HomeMapFragmentPresenter.CurrentStatus.ACTIVE_TRIP;
        hideToolbar();
        hideFragments(getFindParkingFragment(), getBikeBaseFragment(), getActiveRideFragment());
        showFragments(getActiveRideFragment());
        getActiveRideFragment().setInternetStatus(isInternetConnected);
        currentFragment = getActiveRideFragment();
        ((HomeActivity) getActivity()).setDrawerMenuWithRide();
    }


    public void showRideSummaryFragment() {
        currentStatus = HomeMapFragmentPresenter.CurrentStatus.NO_BIKE_NO_TRIP;
        Intent intent = new Intent(getActivity(), RideSummaryActivity.class);
        startActivityForResult(intent, REQUEST_CODE_RIDE_SUMMARY);
    }


    public void showOperationLoading(String operationName) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                home_fragment_rl_loading_operation.setVisibility(View.VISIBLE);
                home_fragment_label_operation_name.setText(operationName);
            }
        });
    }

    public void hideOperationLoading() {
        home_fragment_rl_loading_operation.setVisibility(View.GONE);
    }


    private void openGooglePlayStore() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=io.lattis.lattis"));
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "onActivityResult: " + requestCode);

        if (requestCode == REQUEST_CODE_NO_INTERNET) {
            getActivity().finish();
        } else if (requestCode == REQUEST_CODE_REPORT_DAMAGE && resultCode == RESULT_OK) {
            getActiveRideFragment().showEndSummary();
        } else if (requestCode == REQUEST_CODE_RIDE_SUMMARY) {
            deleteRide();
        } else if (requestCode == GET_CURRENT_STATUS_POP_UP_REQUEST_CODE && resultCode == RESULT_OK) {
            showOperationLoading(getString(R.string.finding_you_label));
            getLocationPermissionWithPermissionCheck(this);
        } else if (requestCode == GET_CURRENT_STATUS_POP_UP_REQUEST_CODE && resultCode == RESULT_CANCELED) {
            getActivity().finish();
        } else if (requestCode == REQUEST_APP_UPDATE_POP && resultCode == RESULT_OK) {
            openGooglePlayStore();
        } else if (requestCode == HomeActivity.REQUEST_CODE_SEARCH_ADDRESS) {
            getBikeBaseFragment().onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == REQUEST_CODE_BIKE_BOOKING && resultCode == RESULT_OK) {
            if (data != null) {
                this.bike = new Gson().fromJson(data.getStringExtra(ARGS_BIKE_DETAILS), Bike.class);
                getBikeBaseFragment().bookBikeAfterAcceptance(bike);
            }
        } else if (requestCode==REQUEST_CODE_LOCATION_PERMISSION_DENIED ){
            showOperationLoading(getString(R.string.finding_you_label));
            getLocationPermissionWithPermissionCheck(this);
        }else if (requestCode==REQUEST_CODE_SYSTEM_LOCATION_PERMISSION_DENIED ){
            showOperationLoading(getString(R.string.finding_you_label));
            getPresenter().getLocationSetting();
        }else if(requestCode == REQUEST_CODE_PAYMENT_INFO ){
            if(currentStatus == HomeMapFragmentPresenter.CurrentStatus.NO_BIKE_NO_TRIP){
                getBikeBaseFragment().refreshCard();
            }
        }else if(requestCode == REQUEST_CODE_REPORT_THEFT ){
            getLocationPermissionWithPermissionCheck(this);
        } else if(requestCode == REQUEST_CODE_BIKE_INFO){
            if(data!=null){
                if(data.hasExtra(ARGS_RESERVE_BIKE) && data.hasExtra(ARGS_BIKE_DETAILS)){
                    if(data.getExtras().getBoolean(ARGS_RESERVE_BIKE)){
                        this.bike = new Gson().fromJson(data.getStringExtra(ARGS_BIKE_DETAILS), Bike.class);
                        getBikeBaseFragment().bookBikeAfterAcceptance(bike);
                    }else{
                        if(currentStatus == HomeMapFragmentPresenter.CurrentStatus.NO_BIKE_NO_TRIP){
                            getBikeBaseFragment().refreshCard();
                        }
                    }
                }else{
                    if(currentStatus == HomeMapFragmentPresenter.CurrentStatus.NO_BIKE_NO_TRIP){
                        getBikeBaseFragment().refreshCard();
                    }
                }
            }else{
                if(currentStatus == HomeMapFragmentPresenter.CurrentStatus.NO_BIKE_NO_TRIP){
                    getBikeBaseFragment().refreshCard();
                }
            }
        }else if(requestCode == REQUEST_CHECK_LOCATION_SETTINGS){
            if (resultCode == RESULT_OK) {
                startFetchingCurrentUserStatus();
            } else {
                hideOperationLoading();
                PopUpActivity.launchForResult(getActivity(), REQUEST_CODE_SYSTEM_LOCATION_PERMISSION_DENIED, getString(R.string.notice),
                        getString(R.string.privacy_location_explanation), "", getString(R.string.privacy_location_submit_button));
            }
        }else if (requestCode == REQUEST_CODE_REPORT_DAMAGE && resultCode == RESULT_CANCELED){
            getBikeBaseFragment().cancelRideAfterDamage();
        }
//        else if(requestCode == REQUEST_CODE_FOR_APP_UPDATE ){
//            if(resultCode != RESULT_OK) {
//                startFetchingLocation();
//            }else{
//                getPresenter().checkIfNewVersionGettingInstalled();
//            }
//        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

    }

    // TODO Add the code for mapbox scroll
//    @Override
//    public void onScroll() {
//        if (currentFragment != null)
//            currentFragment.setScrollListener();
//    }




    @Override
    public void onFling() {
//        Toast.makeText(getActivity(), "onFling", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMoveBegin(MoveGestureDetector detector) {
        // user started moving the map
        if (currentFragment != null)
            currentFragment.setScrollListener();
    }
    @Override
    public void onMove(MoveGestureDetector detector) {
        // user is moving the map
    }
    @Override
    public void onMoveEnd(MoveGestureDetector detector) {
        // user stopped moving the map
    }

    public void noAccountAdded() {
        isApplicationStarted = false;
        getPresenter().clearGetCurrentStatusSubscription();
    }

    public void setInternetStatus(boolean isInternetConnected) {
        this.isInternetConnected = isInternetConnected;
        if(currentFragment!=null && currentFragment == getActiveRideFragment()){
            getActiveRideFragment().setInternetStatus(isInternetConnected);
            ((HomeActivity)getActivity()).hideNoInternetView(false);
        }
    }

    public boolean getInternetStatus() {
        return isInternetConnected;
    }

    public void checkToRestoreApp() {
        if(isApplicationStarted){
            getLocationPermissionWithPermissionCheck(this);
        }
    }

    public void deleteRide() {
        showOperationLoading(getString(R.string.finding_you_label));
        getPresenter().deleteRide();
    }


//    private SupportMapFragment getMapFragment() {
//        if (mapFragment == null) {
//            mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.support_map);
//            mapFragment.getMapAsync(this);
//        }
//        return mapFragment;
//    }

    private BikeBaseFragment getBikeBaseFragment() {
        if (bikeBaseFragment == null) {
            bikeBaseFragment = (BikeBaseFragment) getChildFragmentManager().findFragmentById(R.id.find_bike_fragment);
            bikeBaseFragment.setParentFragment(this);
        }
        return bikeBaseFragment;
    }

    private FindParkingFragment getFindParkingFragment() {
        if (findParkingFragment == null) {
            findParkingFragment = (FindParkingFragment) getChildFragmentManager().findFragmentById(R.id.find_parking_fragment);
            findParkingFragment.setParentFragment(this);
        }
        return findParkingFragment;
    }

    private ActiveRideFragment getActiveRideFragment() {
        if (activeRideFragment == null) {
            activeRideFragment = (ActiveRideFragment) getChildFragmentManager().findFragmentById(R.id.active_ride_fragment);
            activeRideFragment.setParentFragment(this);
        }
        return activeRideFragment;
    }


    @Override
    public void onRideSuccess(Ride ride) {
        currentStatus = HomeMapFragmentPresenter.CurrentStatus.ACTIVE_TRIP;
        isInternetConnected =false;
        ((HomeActivity)getActivity()).hideNoInternetView(false);
        onSaveRideSuccess(ride);
    }

    @Override
    public void onRideFailure() {
        hideOperationLoading();
        PopUpActivity.launchForResult(getActivity(), GET_CURRENT_STATUS_POP_UP_REQUEST_CODE, getString(R.string.get_current_status_failure_title),
                getString(R.string.get_current_status_failure_subtitle), "", getString(R.string.please_try_again));
    }


    @Override
    public void onLocationSettingsPermissionRequired(LocationSettingsResult locationSettingsResult) {
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            ResolvableApiException resolvable = (ResolvableApiException) locationSettingsResult.getApiException();
            resolvable.startResolutionForResult(getActivity(), REQUEST_CHECK_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error.
            onLocationSettingsNotAvailable();
        }
    }

    @Override
    public void onLocationSettingsON() {
        MapboxUtil.disableLocationComponent(getActivity(),mapboxMap,mapboxMap.getStyle());
        startFetchingCurrentUserStatus();
    }

    @Override
    public void onLocationSettingsNotAvailable() {
        hideOperationLoading();
        startFetchingCurrentUserStatus();
    }


    private void startFetchingCurrentUserStatus(){
        getPresenter().getCurrentUserStatus();
    }

    public SymbolManager getSymbolManager(){
       return symbolManager;
    }

    public NavigationMapRoute getNavigationMapRoute(){
        return navigationMapRoute;
    }

    public FillManager getFillManager(){
        return fillManager;
    }

    public CircleManager getCircleManager(){
        return circleManager;
    }

}
