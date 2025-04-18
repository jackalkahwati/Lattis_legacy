package com.lattis.ellipse.presentation.ui.parking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.cpiz.android.bubbleview.BubbleTextView;
import com.lattis.ellipse.Utils.ResourceUtil;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Parking;
import com.lattis.ellipse.domain.model.ParkingZone;
import com.lattis.ellipse.domain.model.ParkingZoneGeometry;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.ui.base.fragment.BaseFragment;
import com.lattis.ellipse.presentation.ui.base.fragment.bluetooth.BaseBluetoothFragment;
import com.lattis.ellipse.presentation.ui.home.fragment.HomeMapFragment;
import com.lattis.ellipse.presentation.ui.payment.AddCardActivity;
import com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity;
import com.lattis.ellipse.presentation.ui.ride.fee.ParkingFeeActivity;
import com.lattis.ellipse.presentation.ui.ride.service.util.UpdateTripData;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomButton;
import com.lattis.ellipse.presentation.view.CustomTextView;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.annotation.FillManager;
import com.mapbox.mapboxsdk.plugins.annotation.FillOptions;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static com.lattis.ellipse.Utils.MapboxUtil.addMarker;
import static com.lattis.ellipse.Utils.MapboxUtil.addUserLocation;
import static com.lattis.ellipse.Utils.MapboxUtil.getUserLocationSymbol;
import static com.lattis.ellipse.Utils.MapboxUtil.isNotSymbolOfuserLocation;
import static com.lattis.ellipse.Utils.MapboxUtil.selected_size;
import static com.lattis.ellipse.Utils.MapboxUtil.unselected_size;
import static com.lattis.ellipse.Utils.MapboxUtil.zoomToMarkers;
import static com.lattis.ellipse.Utils.ResourceUtil.getResourcesByParkingType;
import static com.lattis.ellipse.Utils.ResourceUtil.user_location;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.END_RIDE_PAYMENT_FAILURE;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.FLEET_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.FLEET_TYPE;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.FORCE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LATITUDE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LOCK_BATTERY;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LONGITUDE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.TRIP_ID;
import static com.lattis.ellipse.presentation.ui.ride.fee.ParkingFeeActivityPresenter.ARGS_END_RIDE;
import static com.lattis.ellipse.presentation.ui.ride.fee.ParkingFeeActivityPresenter.ARGS_FIND_ZONES;

public class FindParkingFragment extends BaseBluetoothFragment<FindParkingFragmentPresenter> implements FindParkingFragmentView {

    private final String TAG = FindParkingFragment.class.getName();
    private static final int REQUEST_PARKING_FEE_ACTIVITY =6838;
    private static final int REQUEST_END_RIDE_CHECKLIST = 889;
    private static final int REQUEST_CODE_ADD_CARD_ACTIVITY = 5092;

    private ParkingDetailFragment parkingDetailFragment;
    private ParkingMapDirectionFragment parkingMapDirectionFragment;
    private Parking selectedParking = null;
    private List<Symbol> symbols = new ArrayList<>();
    private List<SymbolOptions> options = new ArrayList<>();
    private OnSymbolClickListener symbolClickListener;

    List<Parking> parkings;
    private HomeMapFragment parentFragment;
    private MapboxMap mapboxMap = null;
    private Polyline polyline = null;
    private int fleetId;
    private IconFactory iconFactory;
    private List<LatLng> POLYGON_COORDINATES;
    List<List<LatLng>> HOLE_COORDINATES = new ArrayList<List<LatLng>>();
    private Ride ride;
    private boolean isParkingRestricted=false;
    private Integer lock_battery;
    private LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
    private List<LatLng> latLngs = new ArrayList<>();

    @BindView(R.id.rl_parking_info)
    RelativeLayout rl_parking_info;

    @BindView(R.id.cv_no_restriction_parking_spot)
    CustomTextView cv_no_restriction_parking_spot;


    @BindView(R.id.parking_end_ride_btn)
    CustomButton parking_end_ride_btn;

    @BindView(R.id.rl_parking_lock_position_error_popup)
    RelativeLayout rl_parking_lock_position_error_popup;

    @BindView(R.id.ct_parking_lock_position_error_popup_title)
    CustomTextView ct_parking_lock_position_error_popup_title;

    @BindView(R.id.ct_parking_lock_position_error_popup_subtitle1)
    CustomTextView ct_parking_lock_position_error_popup_subtitle1;


    private String signedMessage;
    private String publicKey;
    LockModel lockModel;
    private Lock.Hardware.Position position = null;
    private Lock.Hardware.Position failurePosition = null;
    private static final int REQUEST_CONNECTION_TIMEOUT_POP_UP = 1902;

    @BindView(R.id.iv_lock_parking)
    ImageView iv_lock;

    @BindView(R.id.iv_unlock_parking)
    ImageView iv_unlock;

    @BindView(R.id.iv_lock_connecting_parking)
    ImageView iv_lock_connecting;

    @BindView(R.id.iv_lock_disconnected_parking)
    ImageView iv_lock_disconnected;

    @BindView(R.id.iv_lock_connected_parking)
    ImageView iv_lock_connected;

    @BindView(R.id.pb_locking_unlocking_parking)
    ProgressBar pb_locking_unlocking;

    @BindView(R.id.rl_lock_unlock_parking)
    RelativeLayout rl_lock_unlock;

    @BindView(R.id.tv_tooTipForLockUnlock_parking)
    BubbleTextView tv_tooTipForLockUnlock;

    @OnClick(R.id.ct_parking_lock_position_error_popup_ok)
    public void closeLockPositionErrorPopup() {
        rl_parking_lock_position_error_popup.setVisibility(GONE);
    }

    @OnClick(R.id.parking_end_ride_btn)
    public void endRideClicked() {
        if (position == null || (position != Lock.Hardware.Position.LOCKED)) {
            showLockPositionErrorPopUp(getString(R.string.lock_position_error_pop_title), getString(R.string.lock_position_error_pop_subtitle1));
            return;
        }
        launchParkingFeeActivity();
    }


    @Inject
    FindParkingFragmentPresenter presenter;

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
    protected FindParkingFragmentPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getFragmentLayoutId() {
        return R.layout.start_parking_layout;
    }


    public void setParentFragment(HomeMapFragment homeMapFragment) {
        this.parentFragment = homeMapFragment;
    }

    public void setMapboxMap(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    protected void configureViews() {
        super.configureViews();
        parkingDetailFragment = (ParkingDetailFragment) getChildFragmentManager().findFragmentById(R.id.praking_detail_fragment);
        parkingDetailFragment.setParentFragment(this);
        hideFragments(parkingDetailFragment);
        parkingMapDirectionFragment = (ParkingMapDirectionFragment) getChildFragmentManager().findFragmentById(R.id.parking_direction_fragment);
        parkingMapDirectionFragment.setParentFragment(this);
        hideFragments(parkingMapDirectionFragment);
        iconFactory = IconFactory.getInstance(getActivity());


        pb_locking_unlocking.setVisibility(GONE);
        iv_lock.setVisibility(GONE);
        iv_unlock.setVisibility(GONE);
        iv_lock_connecting.setVisibility(GONE);
        iv_lock_connected.setVisibility(GONE);
        iv_lock_disconnected.setVisibility(GONE);
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            getPresenter().requestStopLocationUpdates();
            hideFragments(parkingDetailFragment, parkingMapDirectionFragment);
            removeListeningToSymbolClick();
            deleteAllPreviousPolygons();
            getPresenter().setCurrentUserLocation(null);
        }else{
            getPresenter().getRide();
            startListeningToSymbolClick();
        }
    }

    @OnClick(R.id.iv_cancel_parking_info)
    public void closeParkingInfo() {
        rl_parking_info.setVisibility(GONE);
    }

    @OnClick(R.id.iv_unselect_parking_fragment)
    public void onParkingClicked() {
        if(parkingMapDirectionFragment.getMyVisibility()){
            hideFragments(parkingDetailFragment,parkingMapDirectionFragment);
            clearMapView();
            startShowingParkingSpotAndZones(fleetId,getPresenter().getCurrentUserLocation(),position,lock_battery);
        }else{
            clearMapView();
            parentFragment.showActiveRideFragment();
        }
    }

    @Override
    public void setUserPosition(Location location) {
        getPresenter().setCurrentUserLocation(location);
        Symbol userLocationSymbol = getUserLocationSymbol(symbols);
        if(userLocationSymbol==null){
            Symbol symbol =getSymbolManager().create(addUserLocation(getPresenter().getCurrentUserLocation().getLatitude(), getPresenter().getCurrentUserLocation().getLongitude(), ResourceUtil.getUserLocationResource(), unselected_size));
            symbols.add(symbol);
        }else{
            userLocationSymbol.setLatLng(new LatLng(location.getLatitude(),location.getLongitude()));
            getSymbolManager().update(userLocationSymbol);
        }
    }

    @Override
    public void onFindingParkingFailure() {
        hideOperationLoading();
        if(mapboxMap!=null) {
            showParkings();
        }
        showAppropiateParkingInfoAndAdjustZoom();
    }


    public void startShowingParkingSpotAndZones(int fleetId, Location location, Lock.Hardware.Position lockPosition, Integer lock_battery) {
        this.fleetId = fleetId;
        this.lock_battery=lock_battery;
        this.isParkingRestricted=false;
        latLngBounds = new LatLngBounds.Builder();
        latLngs = new ArrayList<>();
        if(location!=null){
            getPresenter().setCurrentUserLocation(location);
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rl_parking_lock_position_error_popup.setVisibility(GONE);
                parking_end_ride_btn.setVisibility(View.VISIBLE);
                rl_parking_info.setVisibility(GONE);
                getPresenter().getParkingZone(fleetId);
            }
        });

    }

    @Override
    public void onFindingParkingSuccess(List<Parking> parkings) {
        hideOperationLoading();
        if(parkings.size()>0){
            isParkingRestricted=true;
        }

        this.parkings = parkings;
        if (mapboxMap != null) {
            showParkings();
        }
        showAppropiateParkingInfoAndAdjustZoom();
    }


    private void launchParkingFeeActivity(){

        if(ride==null){
            return;
        }


        Intent intent = new Intent(getActivity(), ParkingFeeActivity.class);
        intent.putExtra(FLEET_ID, ride.getBike_fleet_id());
        intent.putExtra(FLEET_TYPE, ride.getBike_fleet_type());
        if (getPresenter().getCurrentUserLocation() != null) {
            double lat = getPresenter().getCurrentUserLocation().getLatitude();
            double longitude = getPresenter().getCurrentUserLocation().getLongitude();
            intent.putExtra(LONGITUDE_END_RIDE_ID, longitude);
            intent.putExtra(LATITUDE_END_RIDE_ID, lat);
        }
        startActivityForResult(intent, REQUEST_PARKING_FEE_ACTIVITY);
    }


    void launchEndRideCheckListActivity(boolean isForceEndRide) {
        Intent intent = new Intent(getActivity(), EndRideCheckListActivity.class);
        intent.putExtra(TRIP_ID, this.ride.getRideId());
        if (getPresenter().getCurrentUserLocation() != null) {
            double lat = getPresenter().getCurrentUserLocation().getLatitude();
            double longitude = getPresenter().getCurrentUserLocation().getLongitude();
            intent.putExtra(LONGITUDE_END_RIDE_ID, longitude);
            intent.putExtra(LATITUDE_END_RIDE_ID, lat);
        }
        if(lock_battery!=null)
            intent.putExtra(LOCK_BATTERY, lock_battery);

        if (isForceEndRide) {
            intent.putExtra(FORCE_END_RIDE_ID, isForceEndRide);
        }
        startActivityForResult(intent, REQUEST_END_RIDE_CHECKLIST);
    }


    ////////////////////////// code to show parking zone: start ////////////////////////////

    @Override
    public void onFindingZoneSuccess(List<ParkingZone> parkingZones) {
        mapboxMap.clear();
        HOLE_COORDINATES.clear();
        POLYGON_COORDINATES = new ArrayList<LatLng>() {
            {
                add(new LatLng(-90, -180));
                add(new LatLng(90, -180));
                add(new LatLng(90, 180));
                add(new LatLng(-90, 180));
                add(new LatLng(-90, -180));
            }
        };

        for (int i = 0; i < parkingZones.size(); i++) {
            ParkingZone parkingZone = parkingZones.get(i);
            if(parkingZone!=null ){
                List<ParkingZoneGeometry> parkingZoneGeometries = parkingZone.getParkingZoneGeometry();
                if(parkingZoneGeometries!=null && parkingZoneGeometries.size()>0){
                    List<LatLng> polygon = new ArrayList<>();

                    if(parkingZone.getType().equalsIgnoreCase("circle")){   //parkingzone is circular

                        for (int j = 0; j < parkingZoneGeometries.size(); j++) {
                            ParkingZoneGeometry parkingZoneGeometry = parkingZoneGeometries.get(j);
                            if (parkingZoneGeometry != null) {

                                LatLng centreLatLng = new LatLng();
                                centreLatLng.setLongitude(parkingZoneGeometry.getLongitude());
                                centreLatLng.setLatitude(parkingZoneGeometry.getLatitude());
                                double radius = parkingZoneGeometry.getRadius();

                                int degreesBetweenPoints = 8; //45 sides
                                int numberOfPoints = (int) Math.floor(360 / degreesBetweenPoints);
                                double distRadians = radius / 6371000.0; // earth radius in meters
                                double centerLatRadians = centreLatLng.getLatitude() * Math.PI / 180;
                                double centerLonRadians = centreLatLng.getLongitude() * Math.PI / 180;
                                for (int index = 0; index < numberOfPoints; index++) {
                                    double degrees = index * degreesBetweenPoints;
                                    double degreeRadians = degrees * Math.PI / 180;
                                    double pointLatRadians = Math.asin(Math.sin(centerLatRadians) * Math.cos(distRadians) + Math.cos(centerLatRadians) * Math.sin(distRadians) * Math.cos(degreeRadians));
                                    double pointLonRadians = centerLonRadians + Math.atan2(Math.sin(degreeRadians) * Math.sin(distRadians) * Math.cos(centerLatRadians),
                                            Math.cos(distRadians) - Math.sin(centerLatRadians) * Math.sin(pointLatRadians));
                                    double pointLat = pointLatRadians * 180 / Math.PI;
                                    double pointLon = pointLonRadians * 180 / Math.PI;
                                    LatLng latLng = new LatLng(pointLat, pointLon);


                                    latLngBounds.include(latLng);
                                    latLngs.add(latLng);
                                    polygon.add(latLng);
                                }


                            }
                        }

                    }else{  // parkingzone type is non circular

                        for (int j = 0; j < parkingZoneGeometries.size(); j++) {
                            ParkingZoneGeometry parkingZoneGeometry = parkingZoneGeometries.get(j);
                            if(parkingZoneGeometry!=null){
                                LatLng latLng = new LatLng();
                                latLng.setLongitude(parkingZoneGeometry.getLongitude());
                                latLng.setLatitude(parkingZoneGeometry.getLatitude());
                                polygon.add(latLng);
                                latLngBounds.include(latLng);
                                latLngs.add(latLng);
                            }
                        }
                    }

                    if(!polygon.isEmpty()){
                        HOLE_COORDINATES.add(polygon);
                    }
                }
            }
        }

        if(HOLE_COORDINATES.size()>0){
            isParkingRestricted=true;
        }

        drawPolygon();
    }


    private void drawPolygon() {
        deleteAllPreviousPolygons();
        List<FillOptions> options = new ArrayList<>();
//        List<List<LatLng>> earthLatLngs = new ArrayList<>();
//        earthLatLngs.add(POLYGON_COORDINATES);
//        FillOptions earthFillOptions = new FillOptions()
//                .withLatLngs(earthLatLngs)
//                .withFillColor("#000000")
//                .withFillOpacity(0.5f);
//
//        options.add(earthFillOptions);

        for (List<LatLng> latLngList : HOLE_COORDINATES){
            List<List<LatLng>> latLngs = new ArrayList<>();
            latLngs.add(latLngList);
            FillOptions fillOptions = new FillOptions()
                    .withLatLngs(latLngs)
                    .withFillOutlineColor("#FF0000")
                    .withFillColor("#FF0000")
                    .withFillOpacity(0.1f);
            options.add(fillOptions);
        }

        getFillManager().create(options);

    }



    ////////////////////////// code to show parking zone: end ////////////////////////////



    private void showParkings() {
        clearMapView();
        if(parkings!=null && !parkings.isEmpty()) {
            for (Parking parking : parkings) {
                if (this.fleetId == parking.getFleet_id()) {
                    addMarker(options, parking.getLatitude(), parking.getLongitude(), getResourcesByParkingType(parking.getType().toUpperCase()), 1.0f);
                    LatLng parkingLatLng = new LatLng(parking.getLatitude(), parking.getLongitude());
                    latLngBounds.include(parkingLatLng);
                    latLngs.add(parkingLatLng);
                }
            }
        }
        if(getPresenter().getCurrentUserLocation()!=null) {
            addUserLocation(options, getPresenter().getCurrentUserLocation().getLatitude(), getPresenter().getCurrentUserLocation().getLongitude(), ResourceUtil.getUserLocationResource(), unselected_size);
            LatLng currentLatLng = new LatLng(getPresenter().getCurrentUserLocation().getLatitude(),getPresenter().getCurrentUserLocation().getLongitude());
            latLngBounds.include(currentLatLng);
            latLngs.add(currentLatLng);
        }
        symbols = getSymbolManager().create(options);
        getPresenter().requestLocationUpdates();
    }


    private void showAppropiateParkingInfoAndAdjustZoom(){
        if(!isParkingRestricted){
            cv_no_restriction_parking_spot.setVisibility(View.VISIBLE);
        }else{
            cv_no_restriction_parking_spot.setVisibility(View.GONE);
        }
        rl_parking_info.setVisibility(View.VISIBLE);
        zoomToMarkers(mapboxMap,latLngBounds,latLngs);
    }


    private void setSelectedParking(Parking parking, Symbol selectedSymbol) {
        for(Symbol symbol : symbols){
            if(isNotSymbolOfuserLocation(symbol) && symbol != selectedSymbol) {
                symbol.setIconSize(unselected_size);
                symbol.setZIndex(10);
                getSymbolManager().update(symbol);
            }
        }
        selectedSymbol.setIconSize(selected_size);
        selectedSymbol.setZIndex(25);
        getSymbolManager().update(selectedSymbol);

//        rl_parking_info.setVisibility(GONE);
//        parking_end_ride_btn.setVisibility(GONE);
        rl_parking_lock_position_error_popup.setVisibility(GONE);



        selectedParking = parking;
        if (selectedParking == null) {
            hideFragments(parkingDetailFragment);
        } else {
            parkingDetailFragment.setParking(selectedParking);
            showFragments(parkingDetailFragment);
        }
    }

    public void getDirectionForParking(Parking parking) {
        hideParkingDetailFragment();
        getPresenter().requestStopLocationUpdates();
        showFragments(parkingMapDirectionFragment);
        rl_parking_info.setVisibility(View.GONE);
        parking_end_ride_btn.setVisibility(GONE);
        clearMapView();
        deleteAllPreviousPolygons();
        parkingMapDirectionFragment.setDirectionForParking(getPresenter().getCurrentUserLocation(), parking);
    }



    public void clearMapView() {
        if (polyline != null) {
            polyline.remove();
        }
        if (mapboxMap != null)
            mapboxMap.clear();

        clearSymbols();
    }

    public void showRideSummary() {
        clearMapView();
        parentFragment.showRideSummaryFragment();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PARKING_FEE_ACTIVITY && resultCode == RESULT_OK) {
            Log.e(TAG,"onActivityResult::REQUEST_PARKING_FEE_ACTIVITY");
            if(data!=null){
                if(data.hasExtra(ARGS_END_RIDE)){
                    Log.e(TAG,"onActivityResult::ARGS_END_RIDE");
                    if(data.getExtras().getBoolean(ARGS_END_RIDE)){
                        Log.e(TAG,"onActivityResult::ARGS_END_RIDE::::::"+ride.getBike_skip_parking_image());
                        if(ride.getBike_skip_parking_image()){
                            launchEndRideCheckListActivity(true);
                        }else{
                            launchEndRideCheckListActivity(false);
                        }
                    }
                }else if(data.hasExtra(ARGS_FIND_ZONES)){
                    Log.e(TAG,"onActivityResult::ARGS_END_RIDE");
                    if(data.getExtras().getBoolean(ARGS_FIND_ZONES)){
//
                    }
                }
            }
        }if (requestCode == REQUEST_END_RIDE_CHECKLIST && resultCode == RESULT_OK) {
            showRideSummary();
        } else if (requestCode == REQUEST_END_RIDE_CHECKLIST && resultCode == RESULT_CANCELED) {
            if(data!=null){
                if(data.hasExtra(END_RIDE_PAYMENT_FAILURE)){
                    Log.e(TAG,"onActivityResult::Request_end_Ride_payment failure::true");
                    if(data.getExtras().getBoolean(END_RIDE_PAYMENT_FAILURE)){
                        startActivityForResult(new Intent(getActivity(), AddCardActivity.class), REQUEST_CODE_ADD_CARD_ACTIVITY);
                        return;
                    }
                }
            }
        }
    }

    public void showLockPositionErrorPopUp(String title, String message) {
        ct_parking_lock_position_error_popup_title.setText(title);
        ct_parking_lock_position_error_popup_subtitle1.setText(message);
        rl_parking_lock_position_error_popup.setVisibility(View.VISIBLE);
        return;
    }

    public void hideParkingDetailFragment() {
        rl_parking_info.setVisibility(View.VISIBLE);
        parking_end_ride_btn.setVisibility(View.VISIBLE);
        hideFragments(parkingDetailFragment);
        unSelectAllParkingSymbols();
    }

    private void unSelectAllParkingSymbols(){
        for(Symbol symbol : symbols){
            if(isNotSymbolOfuserLocation(symbol)) {
                symbol.setIconSize(unselected_size);
                symbol.setZIndex(10);
                getSymbolManager().update(symbol);
            }
        }
    }

    public void hideParkingMapDirectionFragment() {
        hideFragments(parkingMapDirectionFragment);
        showParkings();
    }

    public void showOperationLoading(String operationName) {
        if (parentFragment != null)
            parentFragment.showOperationLoading(operationName);
    }

    public void hideOperationLoading() {
        parentFragment.hideOperationLoading();
    }

    public static FindParkingFragment newInstance() {
        return new FindParkingFragment();
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


    public NavigationMapRoute getNavigationMapRoute(){
        return parentFragment.getNavigationMapRoute();
    }
    public SymbolManager getSymbolManager(){
        return parentFragment.getSymbolManager();
    }

    public void clearSymbols(){
        getSymbolManager().deleteAll();
        getSymbolManager().delete(symbols);
        symbols.clear();
        options.clear();

        parkingMapDirectionFragment.clearSymbols();
    }


    private void startListeningToSymbolClick(){
        getSymbolManager().addClickListener(symbolClickListener = new OnSymbolClickListener() {
            @Override
            public void onAnnotationClick(Symbol symbol) {
                if(symbol.getIconImage()!=user_location) {
                    for(int i=0 ;i<symbols.size();i++){
                        if(symbols.get(i) == symbol){
                            setSelectedParking(parkings.get(i), symbol);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void removeListeningToSymbolClick(){
        if(symbolClickListener!=null) {
            getSymbolManager().removeClickListener(symbolClickListener);
            symbolClickListener = null;
        }
    }

    public MapboxMap getMapBoxMap(){
        return  mapboxMap;
    }

    private FillManager getFillManager() {
        return parentFragment.getFillManager();
    }

    private void deleteAllPreviousPolygons() {
        if(getFillManager()!=null) {
            getFillManager().deleteAll();
        }
    }


/////////////////////////



    @OnClick({R.id.iv_lock_parking, R.id.iv_unlock_parking})
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
        showConnecting();
        checkForLocation();
    }

    @Override
    public void OnRideSuccess(Ride ride) {
        this.ride = ride;
        parking_end_ride_btn.setVisibility(View.VISIBLE);
        getPresenter().startActiveTripService();
        getPresenter().connectToLastLockedLock(); // this is real, uncomment while commiting
    }

    @Override
    public void OnRideFailure() {
        Log.e(TAG, "######OnRideFailure: ");
    }

    @Override
    public void onSignedMessagePublicKeySuccess(String signedMessage, String publicKey) {
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

    }

    @Override
    public void showConnected() {

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
    }

    @Override
    public void onLockConnectionFailed() {
        Log.e(TAG, "######onLockConnectionFailed: ");
        showConnecting();
        checkForLocation();
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

    }

    @Override
    public void showConnectionTimeOut(){
        getPresenter().unsubscribeAllSubscription();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showDisconnected();
                showToolTipForLockUnlock(false,true,getString(R.string.tool_tip_reconnection));
                PopUpActivity.launchForResult(getActivity(), REQUEST_CONNECTION_TIMEOUT_POP_UP, getString(R.string.bike_booking_end_ride_title),
                        getString(R.string.bike_out_of_range_connection_error), null, getString(R.string.ok_title));
            }
        });


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

    public void setEllipseLockUnlockButton(boolean enabled){
        iv_unlock.setEnabled(enabled);
        iv_lock.setEnabled(enabled);
    }

    void startObservingLock() {
        getPresenter().observeConnectionState(lockModel);
    }


}
