package com.lattis.ellipse.presentation.ui.bike;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.lattis.ellipse.Utils.MapboxUtil;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.fragment.BaseFragment;
import com.lattis.ellipse.presentation.ui.bike.bikeList.BikeListFragment;
import com.lattis.ellipse.presentation.ui.home.HomeActivity;
import com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragment;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomTextView;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;



@RuntimePermissions
public class BikeBaseFragment extends BaseFragment<BikeBaseFragmentPresenter> implements BikeBaseFragmentView {

    private static final String TAG = BikeBaseFragment.class.getName();
    private HomeMapFragment parentFragment;
    private MapboxMap mapboxMap = null;
    private Polyline polyline = null;
    private BikeDirectionFragment bikeDirectionFragment;
    private BikeListFragment bikeListFragment;
    IconFactory iconFactory;
    Location currentLocation;
    private Bike bike;
    boolean isDirectionLoaded = false;
    private Icon currentLocationIcon;
    public static final int RESERVE_BIKE_REQUEST_CODE = 3000;
    private final int REQUEST_RESERVE_BIKE_FAIL = 3001;
    @Inject
    BikeBaseFragmentPresenter homePresenter;
    @BindView(R.id.rl_bluetooth_popup)
    RelativeLayout rl_bluetooth_popup;
    @BindView(R.id.label_operation_name)
    CustomTextView loading_operation_name;
    private Bike selectedBike;
    private final int REQUEST_CODE_SEARCH_ADDRESS = 4646;
    private Disposable findCurrentLocationSubscription =null;
    private static final int MILLISECONDS_FOR_CURRENT_LOCATION_TIMER = 10000;



    @OnClick(R.id.ct_bluetooth_popup_will_do)
    public void closeBluetoothPop() {
        rl_bluetooth_popup.setVisibility(View.GONE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void setParentFragment(HomeMapFragment homeMapFragment) {
        this.parentFragment = homeMapFragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().invalidateOptionsMenu();
    }

    public void setMapboxMap(MapboxMap mapboxMap) {
        if (mapboxMap == null) return;
        this.mapboxMap = mapboxMap;
    }

    public MapboxMap getMapboxMap() {
        return mapboxMap;
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

    public static BikeBaseFragment newInstance() {
        return new BikeBaseFragment();
    }

    @NonNull
    @Override
    protected BikeBaseFragmentPresenter getPresenter() {
        return homePresenter;
    }

    @Override
    protected int getFragmentLayoutId() {
        return R.layout.find_bike_layout;
    }


    @Override
    protected void configureViews() {
        super.configureViews();
        bikeListFragment = (BikeListFragment) getChildFragmentManager().findFragmentById(R.id.bike_list_fragment);
        bikeListFragment.setParentFragment(this);
        bikeDirectionFragment = (BikeDirectionFragment) getChildFragmentManager().findFragmentById(R.id.bike_direction_fragment);
        bikeDirectionFragment.setParentFragment(this);
        hideFragments(bikeDirectionFragment);
        iconFactory = IconFactory.getInstance(getActivity());
        Drawable icon_current_location_drawable = ContextCompat.getDrawable(getActivity(), R.drawable.icon_current_location);
        currentLocationIcon = iconFactory.fromBitmap(resize(icon_current_location_drawable, 150));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == HomeActivity.REQUEST_CODE_SEARCH_ADDRESS){
            getBikeListFragment().onActivityResult(requestCode, resultCode, data);
        }
    }


    public void showOperationLoading(String message) {
        parentFragment.showOperationLoading(message);
    }

    private Bitmap resize(Drawable image, int size) {
        Bitmap b = ((BitmapDrawable) image).getBitmap();
        return Bitmap.createScaledBitmap(b, size, size, false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            hideFragments(getBikeDirectionFragment());
            hideFragments(getBikeListFragment());
            requestStopLocationUpdates();
        } else {
            showFragments(getBikeListFragment());
        }
    }


    public void requestLocationUpdates() {
        this.currentLocation = null;
        Log.e(TAG,"requestLocationUpdates::------->");
        subscribeToFindCurrentLocationTimer(false);
        subscribeToFindCurrentLocationTimer(true);
        getPresenter().requestLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        subscribeToFindCurrentLocationTimer(false);
    }

    public void requestStopLocationUpdates() {
        subscribeToFindCurrentLocationTimer(false);
        getPresenter().requestStopLocationUpdates();
    }


    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void getLocationPermission() {
    }


    public void start() {
        Log.e(TAG,"start::------->");
        isDirectionLoaded = false;
        hideFragments(getBikeDirectionFragment());
        requestLocationUpdates();
        //checkBluetooth();
    }

    public void refreshCard(){
        getBikeListFragment().refreshCard();
    }


    public void stop() {
        requestStopLocationUpdates();
    }

    private void checkBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                rl_bluetooth_popup.setVisibility(View.VISIBLE);

            } else {
                rl_bluetooth_popup.setVisibility(View.GONE);
            }
        }
    }


//    public void resetView() {
//        isDirectionLoaded = false;
//        clearMapView();
//        parentFragment.currentStatus = HomeMapFragmentPresenter.CurrentStatus.NO_BIKE_NO_TRIP;
//        hideFragments(bikeDirectionFragment);
//        showFragments(bikeListFragment);
//        setToolbarHeader(getString(R.string.label_find_ride));
//        hideOperationLoading();
//        getPresenter().resetCurrentUserLocation();
//        requestLocationUpdates();
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BikeBaseFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void setUserPosition(Location location) {

        Log.e(TAG,"setUserPosition::------->");
        requestStopLocationUpdates();

        showFragments(getBikeListFragment());
        hideFragments(getBikeDirectionFragment());

        getBikeListFragment().setUserPosition(location);


        if (currentLocation == null) {
            this.currentLocation = location;
            if (mapboxMap == null) return;
            MapboxUtil.enableLocationComponent(getActivity(),mapboxMap,mapboxMap.getStyle());
        }

    }





    @Override
    public void OnReserveBikeSuccess(long startTime, int countDownTime) {
        selectedBike.setBooked_on(startTime);
        selectedBike.setExpires_in(countDownTime);
        ((HomeActivity)getActivity()).setDrawerMenuWithRide();
        reserveBike(selectedBike, null);
    }

    @Override
    public void OnReserveBikeFail() {
        getBikeListFragment().setReserveButtonStatus(true);
        PopUpActivity.launchForResult(getActivity(), REQUEST_RESERVE_BIKE_FAIL, getString(R.string.alert_error_server_title),
                getString(R.string.alert_error_server_subtitle), null, getString(R.string.ok));
    }

    @Override
    public void OnReserveBikeNotFound() {
        getBikeListFragment().setReserveButtonStatus(true);
        PopUpActivity.launchForResult(getActivity(), REQUEST_RESERVE_BIKE_FAIL, getString(R.string.route_to_bike_booked_alert_title),
                getString(R.string.route_to_bike_booked_alert_text), null, getString(R.string.ok));
    }

    public void clearMapView() {
        if (polyline != null) {
            polyline.remove();
        }
        if (mapboxMap != null)
            mapboxMap.clear();

        getBikeListFragment().clearSymbols();
        getBikeDirectionFragment().clearSymbols();
        getNavigationMapRoute().removeRoute();
    }

    public void refreshMapBikes(LatLngBounds latLngBounds) {
        mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100));
    }

    public void setMarkerCenter(double latitude, double longitude, double zoomlevel) {
        if (zoomlevel != 0) {
            mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(latitude, longitude), zoomlevel));
        } else {
            mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(latitude, longitude), mapboxMap.getCameraPosition().zoom));
        }
    }

    public Marker addMarker(double latitude, double longitude, Icon markerIcon) {
        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .icon(markerIcon);
        return mapboxMap.addMarker(options);
    }

    public boolean getInternetStatus() {
        return parentFragment.getInternetStatus();
    }

//    private void setShowBikeListFragment() {
//        isDirectionLoaded = false;
//        clearMapView();
//        mapboxMap.setMyLocationEnabled(true);
//        requestStopLocationUpdates();
//    }

    public void reserveBike(Bike bike, Ride ride) {
        this.bike = bike;
        requestStopLocationUpdates();
        clearMapView();
        hideToolbar();
        hideFragments(getBikeListFragment());
        showFragments(getBikeDirectionFragment());
//        TODO enable location tracking
        MapboxUtil.enableLocationComponent(getActivity(),mapboxMap,mapboxMap.getStyle());
//        mapboxMap.setMyLocationEnabled(true);
        getBikeDirectionFragment().setDirectionForBike(bike, ride);
    }


    public void showRouteToBike(LatLng[] latLngs) {
        // TODO changed for mapbox
//        LatLng[] points = new LatLng[latLngs.length];
//        int index = 0;
//        LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
//        for (LatLng latLng : latLngs) {
//            points[index] = new LatLng(latLng.getLatitude(), latLng.getLongitude());
//            latLngBounds.include(points[index]);
//            index++;
//        }
//        Icon unSelectedIcon = ResourceUtil.getBikeResource(getActivity(), false, bike.getType().toUpperCase());
//        addMarker(bike.getLatitude(), bike.getLongitude(), unSelectedIcon);
//        // Draw Points on MapView
//        polyline = mapboxMap.addPolyline(new PolylineOptions()
//                .add(points)
//                .color(Color.parseColor("#57D8FF"))
//                .width(3));
//        if (index > 2)
//            refreshMapBikes(latLngBounds.build());
//
//        mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLngs[0].getLatitude(), latLngs[0].getLongitude()), 12));
    }


    public void showRouteMatcher(List<LatLng> latLngs) {
        // TODO change for mapbox
//        clearMapView();
//        isDirectionLoaded = true;
//        LatLng[] points = new LatLng[latLngs.size()];
//        int index = 0;
//        LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
//        for (LatLng latLng : latLngs) {
//            points[index] = new LatLng(latLng.getLatitude(), latLng.getLongitude());
//            latLngBounds.include(points[index]);
//            index++;
//        }
//        Icon unSelectedIcon = ResourceUtil.getBikeResource(getActivity(), false, bike.getType().toUpperCase());
//        addMarker(bike.getLatitude(), bike.getLongitude(), unSelectedIcon);
//        // Draw Points on MapView
//        polyline = mapboxMap.addPolyline(new PolylineOptions()
//                .add(points)
//                .color(Color.parseColor("#57D8FF"))
//                .width(3));
//
//
//        if (index > 2)
//            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 200, 200, 200, 700));
//        else
//            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(bike.getLatitude(), bike.getLongitude()), 14));
//

    }

    public void hideOperationLoading() {
        parentFragment.hideOperationLoading();
    }


    public void showActiveRideFragment() {
        clearMapView();
        hideFragments(getBikeDirectionFragment());
        parentFragment.showActiveRideFragment();
    }

    public void showBikeBaseFragment() {
        clearMapView();
        parentFragment.deleteRide();
    }

    public void showRideSummaryFragment(){
        parentFragment.showRideSummaryFragment();
    }


    private void hideFragments(Fragment... fragments) {
        try {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            for (Fragment fragment : fragments) {
                ft.hide(fragment);
            }
            ft.commit();
        }catch (IllegalStateException e){

        }
    }

    private void showFragments(Fragment... fragments) {
        try{
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            for (Fragment fragment : fragments) {
                ft.show(fragment);
            }
            ft.commit();
        }catch (IllegalStateException e){

        }
    }

    public void bookBikeAfterAcceptance(Bike bike) {
        if (bike == null) {
            return;
        }
        this.selectedBike = bike;

        getPresenter().reserveBike(bike);
    }

    @Override
    public void setMapBox(MapboxMap mapBox) {
        super.setMapBox(mapBox);
        getBikeListFragment().setMapBox(mapBox);
    }

    @Override
    public void setScrollListener() {
        super.setScrollListener();
        getBikeListFragment().setScrollListener();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);  // Use filter.xml from step 1
        Log.e(TAG, "onCreateOptionsMenu");
    }


    private BikeListFragment getBikeListFragment() {
        if (bikeListFragment == null) {
            bikeListFragment = (BikeListFragment) getChildFragmentManager().findFragmentById(R.id.bike_list_fragment);
            bikeListFragment.setParentFragment(this);
        }
        return bikeListFragment;
    }

    private BikeDirectionFragment getBikeDirectionFragment() {
        if (bikeDirectionFragment == null) {
            bikeDirectionFragment = (BikeDirectionFragment) getChildFragmentManager().findFragmentById(R.id.bike_direction_fragment);
            bikeDirectionFragment.setParentFragment(this);
        }
        return bikeDirectionFragment;
    }


    public synchronized void subscribeToFindCurrentLocationTimer(boolean active) {
        Log.e(TAG,"subscribeToFindCurrentLocationTimer::"+active);
        if (active) {
            if (findCurrentLocationSubscription == null) {
                findCurrentLocationSubscription = Observable.timer(MILLISECONDS_FOR_CURRENT_LOCATION_TIMER, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Long>() {
                            public void accept(Long aLong) {
                                Log.e(TAG,"subscribeToFindCurrentLocationTimer::"+active+" call");
                                subscribeToFindCurrentLocationTimer(false);
                                requestLocationUpdates();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {

                            }
                        });
            }
        } else {
            if (findCurrentLocationSubscription != null) {
                findCurrentLocationSubscription.dispose();
                findCurrentLocationSubscription = null;
            }
        }
    }

/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bikeListFragment.onActivityResult(requestCode, resultCode, data);
    }
*/


    public SymbolManager getSymbolManager(){
        return parentFragment.getSymbolManager();
    }


    public NavigationMapRoute getNavigationMapRoute(){
        return parentFragment.getNavigationMapRoute();
    }

    public void cancelRideAfterDamage(){
        bikeDirectionFragment.cancelRideAfterDamage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().requestStopLocationUpdates();
    }
}
