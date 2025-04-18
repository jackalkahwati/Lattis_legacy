package com.lattis.ellipse.presentation.ui.history;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lattis.ellipse.Utils.AddressUtils;
import com.lattis.ellipse.Utils.CurrencyUtil;
import com.lattis.ellipse.Utils.ResourceUtil;
import com.lattis.ellipse.Utils.UtilHelper;
import com.lattis.ellipse.data.network.model.response.history.RideHistoryDataResponse;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.presentation.ui.base.activity.BaseBackArrowActivity;
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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import io.lattis.ellipse.R;

import static com.lattis.ellipse.Utils.MapboxUtil.addMarker;
import static com.lattis.ellipse.Utils.UtilHelper.getTimeFromDuration;

public class TripDetailsActivity extends BaseBackArrowActivity<TripDetailsPresenter> implements
            TripDetailsView, OnMapReadyCallback {
    @Inject
    TripDetailsPresenter tripDetailsPresenter;
    @BindView(R.id.tv_trip_time)
    CustomTextView tripTimeStarted;
    @BindView(R.id.tv_fleet_name)
    CustomTextView fleetName;
    @BindView(R.id.tv_trip_duration)
    CustomTextView tripDuration;
    @BindView(R.id.tv_penalty_surcharge)
    CustomTextView tv_penalty_surcharge;
    @BindView(R.id.tv_metered_charge)
    CustomTextView meterCharged;
    @BindView(R.id.tv_total_charge)
    CustomTextView totalCharge;
    @BindView(R.id.tv_source_address)
    CustomTextView startAddress;
    @BindView(R.id.tv_destination_address)
    CustomTextView endAddress;
    @BindView(R.id.view_ride_cost)
    LinearLayout linearLayoutRideCostView;
    private RideHistoryDataResponse rideHistoryDataResponse;
    @BindView(R.id.card_type_icon)
    ImageView iv_CardType;
    @BindView(R.id.tv_cardNumber)
    CustomTextView cardNumber;
    @BindView(R.id.tv_deposit)
    CustomTextView tv_deposit;
    @BindView(R.id.tv_over_usage_fees)
    CustomTextView tv_over_usage_fees;


    @BindView(R.id.mapView)
    MapView mapView;

    private MapboxMap mapboxMap = null;
    private List<Symbol> symbols = new ArrayList<>();
    private List<SymbolOptions> options = new ArrayList<>();
    private SymbolManager symbolManager;


    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.ride_details_title));
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected TripDetailsPresenter getPresenter() {
        return tripDetailsPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_trip_details;
    }

    private void displayStartAndEndAddresses(){
        if (rideHistoryDataResponse.getSteps() != null && rideHistoryDataResponse.getSteps().length > 0) {
            if(rideHistoryDataResponse.getStart_address()!=null && !rideHistoryDataResponse.getStart_address().isEmpty()) {
                startAddress.setText(rideHistoryDataResponse.getStart_address());
            }
            if (rideHistoryDataResponse.getEnd_address()==null || !rideHistoryDataResponse.getEnd_address().isEmpty()){
                endAddress.setText(rideHistoryDataResponse.getEnd_address());
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
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


                if (getPresenter().getRideHistoryDataResponse()!=null && getPresenter().getRideHistoryDataResponse().getSteps() != null) {
                    double[][] steps = getPresenter().getRideHistoryDataResponse().getSteps();
                    if (steps != null) {
                        if (steps.length > 0) {
                            LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
                            addMarker(options, steps[0][0], steps[0][1], "icon_start_drawable", 1.0f);
                            latLngBounds.include(new LatLng((double)steps[0][0],(double)steps[0][1]));
                            addMarker(options, steps[steps.length - 1][0], steps[steps.length - 1][1], "icon_end_drawable", 1.0f);
                            latLngBounds.include(new LatLng((double)steps[steps.length - 1][0],(double)steps[steps.length - 1][1]));
                            symbols = symbolManager.create(options);
                            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 100));
                        }
                    }
                }

            }
        });
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

    @Override
    public void setTripDetails(RideHistoryDataResponse rideHistoryDataResponse) {
        this.rideHistoryDataResponse = rideHistoryDataResponse;
        tripTimeStarted.setText(UtilHelper.getDateCurrentTimeZone(
                this,Long.parseLong(rideHistoryDataResponse.getDate_created())));
        totalCharge.setText(CurrencyUtil.getCurrencySymbolByCode(rideHistoryDataResponse.getCurrency()) + UtilHelper.getDotAfterNumber(rideHistoryDataResponse.getTotal()));
        meterCharged.setText(CurrencyUtil.getCurrencySymbolByCode(rideHistoryDataResponse.getCurrency()) + UtilHelper.getDotAfterNumber(rideHistoryDataResponse.getCharge_for_duration()));
        tripDuration.setText(getTimeFromDuration(rideHistoryDataResponse.getDuration()));
        tv_penalty_surcharge.setText(CurrencyUtil.getCurrencySymbolByCode(rideHistoryDataResponse.getCurrency()) + UtilHelper.getDotAfterNumber(rideHistoryDataResponse.getPenalty_fees()));
        tv_deposit.setText(CurrencyUtil.getCurrencySymbolByCode(rideHistoryDataResponse.getCurrency()) + UtilHelper.getDotAfterNumber(rideHistoryDataResponse.getDeposit()));
        tv_over_usage_fees.setText(CurrencyUtil.getCurrencySymbolByCode(rideHistoryDataResponse.getCurrency()) + UtilHelper.getDotAfterNumber(rideHistoryDataResponse.getOver_usage_fees()));

        fleetName.setText(rideHistoryDataResponse.getFleet_name());
        if (rideHistoryDataResponse.getTotal() != null)
            linearLayoutRideCostView.setVisibility(View.VISIBLE);
        else
            linearLayoutRideCostView.setVisibility(View.GONE);

        if (rideHistoryDataResponse.getCc_type() != null && !rideHistoryDataResponse.getCc_type().equals(""))
            iv_CardType.setImageResource(ResourceUtil.getResource(rideHistoryDataResponse.getCc_type().toUpperCase()));

        if (rideHistoryDataResponse.getCc_no() != null && !rideHistoryDataResponse.getCc_no().equals("") && rideHistoryDataResponse.getCc_no().length()>0)
            cardNumber.setText("*" + rideHistoryDataResponse.getCc_no()
                    .substring(rideHistoryDataResponse.getCc_no().length() - 4));

        displayStartAndEndAddresses();


    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }



    @Override
    public void onGetCardSuccess(List<Card> cards) {
        if (cards != null && cards.size()>0) {
            for (Card card : cards) {
                if (card.getIs_primary()) {
                    iv_CardType.setImageResource(ResourceUtil.getResource(card.getCc_type().toUpperCase()));
                    cardNumber.setText("*" + card.getCc_no().substring(card.getCc_no().length() - 4));
                }
            }
        }

    }

    @Override
    public void onGetCardFailure() {

    }
}
