package com.lattis.ellipse.presentation.ui.ride;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.lattis.ellipse.Utils.CurrencyUtil;
import com.lattis.ellipse.Utils.UtilHelper;
import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.activity.BaseActivity;
import com.lattis.ellipse.presentation.ui.utils.IsRidePaid;
import com.lattis.ellipse.presentation.view.CustomTextView;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

import static com.lattis.ellipse.Utils.MapboxUtil.addMarker;

/**
 * Created by ssd3 on 7/18/17.
 */

public class RideSummaryActivity extends BaseActivity<RideSummaryActivityPresenter> implements RideSummaryActivityView, OnMapReadyCallback {
//public class RideSummaryActivity extends BaseActivity<RideSummaryActivityPresenter> implements RideSummaryActivityView{


    private final String TAG = RideSummaryActivity.class.getName();
    private Ride ride;
    private MapboxMap mapboxMap = null;
    private List<Symbol> symbols = new ArrayList<>();
    List<SymbolOptions> options = new ArrayList<>();
    private SymbolManager symbolManager;

    private int ratings = 0;



    @BindView(R.id.mapView)
    MapView mapView;


    @BindView(R.id.dateTextView)
    CustomTextView dateTextView;

    @BindView(R.id.rate1)
    ImageView rate1;

    @BindView(R.id.rate2)
    ImageView rate2;

    @BindView(R.id.rate3)
    ImageView rate3;

    @BindView(R.id.rate4)
    ImageView rate4;

    @BindView(R.id.rate5)
    ImageView rate5;

    @BindView(R.id.tv_ride_duration)
    CustomTextView tv_ride_duration;

    @BindView(R.id.tv_ride_cost)
    CustomTextView tv_ride_cost;

    @BindView(R.id.tv_ride_cost_unit)
    CustomTextView tv_ride_cost_unit;



    @OnClick({R.id.rate1, R.id.rate2, R.id.rate3, R.id.rate4, R.id.rate5})
    public void onRatingClicked(View view) {

        ratings = 0;
        if (view == rate1) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                rate1.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
                rate2.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));
                rate3.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));
                rate4.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));
                rate5.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));

            } else {
                rate1.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));
                rate2.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));
                rate3.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));
                rate4.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));
                rate5.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));

            }
            ratings = 1;
        } else if (view == rate2) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                rate1.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
                rate2.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
                rate3.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));
                rate4.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));
                rate5.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));
            } else {
                rate1.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));
                rate2.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));
                rate3.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));
                rate4.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));
                rate5.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));

            }
            ratings = 2;
        } else if (view == rate3) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                rate1.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
                rate2.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
                rate3.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
                rate4.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));
                rate5.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));

            } else {
                rate1.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));
                rate2.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));
                rate3.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));
                rate4.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));
                rate5.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));

            }
            ratings = 3;
        } else if (view == rate4) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                rate1.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
                rate2.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
                rate3.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
                rate4.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
                rate5.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));

            } else {
                rate1.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));
                rate2.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));
                rate3.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));
                rate4.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));
                rate5.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));

            }
            ratings = 4;
        } else if (view == rate5) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                rate1.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
                rate2.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
                rate3.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
                rate4.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
                rate5.setImageDrawable(getDrawable(R.drawable.icon_selected_star));
            } else {
                rate1.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));
                rate2.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));
                rate3.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));
                rate4.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));
                rate5.setImageDrawable(getResources().getDrawable(R.drawable.icon_selected_star));

            }
            ratings = 5;
        }

    }

    @OnClick(R.id.button_submit)
    public void onSubmitButtonClicked() {
        getPresenter().rideRating(ride.getRideId(), ratings);
    }

    @Inject
    RideSummaryActivityPresenter presenter;



    @NonNull
    @Override
    protected RideSummaryActivityPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.ride_summary_activity;
    }


    @Override
    public void onGetRideSuccess(Ride ride) {
        this.ride = ride;
        getPresenter().getRideSummary(ride.getRideId());
    }

    @Override
    public void onGetRideFailure() {

    }


    @Override
    public void onRideRatingSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onRideRatingFailure() {
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        dateTextView.setText(getCurrentDateTime());



        getPresenter().disconnectAllLocks();
        resetViews();
//        getPresenter().getRide();
    }

    private Bitmap resize(Drawable image, int size) {
        Bitmap b = ((BitmapDrawable) image).getBitmap();
        return Bitmap.createScaledBitmap(b, size, size, false);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        symbolManager = new SymbolManager(mapView,mapboxMap, style);
                        symbolManager.setIconAllowOverlap(true);  //your choice t/f
                        symbolManager.setTextAllowOverlap(true);  //your choice t/f

                        Bitmap icon_start_drawable = BitmapFactory.decodeResource(getResources(), R.drawable.icon_current_location);
                        mapboxMap.getStyle().addImage("icon_start_drawable",icon_start_drawable);

                        Bitmap icon_end_drawable = BitmapFactory.decodeResource(getResources(), R.drawable.ride_summary_flag);
                        mapboxMap.getStyle().addImage("icon_end_drawable",icon_end_drawable);

                        getPresenter().disconnectAllLocks();
                        resetViews();
                        getPresenter().getRide();
                    }
                });
        // TODO changed for mapbox
//        mapboxMap.setStyleUrl(Style.LIGHT);
//        mapboxMap.setMyLocationEnabled(false);
//        mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
//        mapboxMap.getTrackingSettings().setDismissAllTrackingOnGesture(true);
//        this.mapboxMap.getMyLocationViewSettings().setForegroundTintColor(Color.parseColor("#00AAD1"));
//        this.mapboxMap.getMyLocationViewSettings().setAccuracyTintColor(Color.parseColor("#A0C8E0"));



    }

    @Override
    public void onGetRideSummarySuccess(RideSummaryResponse rideSummaryResponse) {
        if (mapboxMap == null) return;

        // TODO changed for mapbox
//        mapboxMap.setMyLocationEnabled(false);


        if (rideSummaryResponse.getRideSummaryResponse() != null) {

            long ride_booked_on = rideSummaryResponse.getRideSummaryResponse().getDate_created();
            long currentTime = getTime();
            tv_ride_duration.setText(getDurationBreakdown(currentTime - ride_booked_on));

            if(IsRidePaid.isRidePaidForFleet(ride.getBike_fleet_type())){
                tv_ride_cost_unit.setText(CurrencyUtil.getCurrencySymbolByCode(rideSummaryResponse.getRideSummaryResponse().getCurrency()));
                tv_ride_cost_unit.setVisibility(View.VISIBLE);
                tv_ride_cost.setText(UtilHelper.getDotAfterNumber(rideSummaryResponse.getRideSummaryResponse().getTotal()));
            }else{
                tv_ride_cost_unit.setVisibility(View.GONE);
                tv_ride_cost.setText("");
            }


            if (rideSummaryResponse.getRideSummaryResponse().getSteps() != null) {
                double[][] steps = rideSummaryResponse.getRideSummaryResponse().getSteps();

                if(steps!=null) {

                    if(steps.length>0){
                        addMarker(options,steps[0][0],steps[0][1],"icon_start_drawable",1.0f);
                        addMarker(options,steps[steps.length-1][0],steps[steps.length-1][1],"icon_end_drawable",1.0f);
                    }

                    symbols = symbolManager.create(options);


                    ArrayList<Location> locationArrayList = new ArrayList();
                    for (int i = 0; i < steps.length; i++) {
                        if (steps[i] != null && steps[i].length > 2) {
                            double latitude = steps[i][0];
                            double longitude = steps[i][1];
                            locationArrayList.add(new Location(latitude, longitude));
                        }
                    }

                    int index = 0;
                    LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
                    LatLng[] points = new LatLng[locationArrayList.size()];
                    for (int i = 0; i < locationArrayList.size(); i++) {
                        points[index] = new LatLng(locationArrayList.get(i).getLatitude(), locationArrayList.get(i).getLongitude());
                        latLngBounds.include(points[index]);
                        index++;
                    }



//                    // Draw Points on MapView
//                    polyline = mapboxMap.addPolyline(new PolylineOptions()
//                            .add(points)
//                            .color(Color.parseColor("#57D8FF"))
//                            .width(3));
                    //mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(steps[0][0], steps[0][1]), 100));
                    mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 100));
                    // mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 50), 5000);
                }
            }
        }

    }

    @Override
    public void onGetRideSummaryFailure() {

    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void resetViews() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            rate1.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));
            rate2.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));
            rate3.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));
            rate4.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));
            rate5.setImageDrawable(getDrawable(R.drawable.icon_unselected_star));
        } else {
            rate1.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));
            rate2.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));
            rate3.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));
            rate4.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));
            rate5.setImageDrawable(getResources().getDrawable(R.drawable.icon_unselected_star));
        }
    }


    String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
        String date = (dateFormat.format(calendar.getTime()).toString()).toUpperCase();

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        String time = (timeFormat.format(calendar.getTime()).toString()).toUpperCase();

        return date + " " + getString(R.string.general_at_label).toUpperCase() + " " + time;
    }

    long getTime() {
        Date date = new Date();
        return date.getTime() / 1000;
    }

    private String getDurationBreakdown(long millis) {

        millis = millis * 1000;

        if (millis < 0) {
            //throw new IllegalArgumentException("Duration must be greater than zero!");
            return "";
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if (days != 0) {
            sb.append(days);
            sb.append(" " + getString(R.string.days) + " ");
        }

        //if (hours != 0) {
            if (hours < 10) {
                sb.append("0" + hours);
            } else {
                sb.append(hours);
            }

            sb.append(":");
        //}

        if (minutes < 10) {
            sb.append("0" + minutes);
        } else {
            sb.append(minutes);
        }
        sb.append(":");

        if (seconds < 10) {
            sb.append("0" + seconds);
        } else {
            sb.append(seconds);
        }
        return sb.toString();
    }


    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
