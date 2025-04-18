package com.lattis.ellipse.presentation.ui.bike;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.lattis.ellipse.Utils.CurrencyUtil;
import com.lattis.ellipse.Utils.LocaleTranslatorUtils;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.map.Direction;
import com.lattis.ellipse.presentation.ui.base.activity.BaseCloseActivity;
import com.lattis.ellipse.presentation.ui.bike.bikeList.FleetTermsConditionActivity;
import com.lattis.ellipse.presentation.ui.payment.AddCardActivity;
import com.lattis.ellipse.presentation.ui.payment.UserCardListAdapter;
import com.lattis.ellipse.presentation.ui.payment.UserCardListListener;
import com.lattis.ellipse.presentation.ui.utils.IsRidePaid;
import com.lattis.ellipse.presentation.view.CustomButton;
import com.lattis.ellipse.presentation.view.CustomTextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

import static android.view.View.GONE;


public class BikeInfoActivity extends BaseCloseActivity<BikeInfoPresenter> implements BikeInfoView, UserCardListListener {
    private final int REQUEST_CODE_ADD_CARD_ACTIVITY = 7940;
    private final int REQUEST_PARKING_ZONE_ACTIVITY = 5094;
    private final String TAG = BikeInfoActivity.class.getName();
    @Inject
    BikeInfoPresenter bikeInfoPresenter;
    @BindView(R.id.rv_card_list)
    RecyclerView rv_UserCardLit;

    @BindView(R.id.ll_payment_card)
    LinearLayout ll_payment_card;

    private Bike bike;
    private String jsonString;
    public static String ARGS_BIKE_DETAILS = "BIKE_DETAILS";
    public static String ARGS_RESERVE_BIKE = "ARGS_RESERVE_BIKE";
    public static String ARGS_IS_BIKE_RESERVED = "IS_BIKE_RESERVED";
    @BindView(R.id.tv_bike_name)
    CustomTextView bikeName;
    @BindView(R.id.tv_bike_type)
    CustomTextView bikeType;
    @BindView(R.id.tv_bike_charge_status)
    CustomTextView bikeChargeStatus;
//    @BindView(R.id.tv_bike_cost_status)
//    CustomTextView bikeCostStatus;
    @BindView(R.id.tv_bike_description)
    CustomTextView bikeDescription;
    @BindView(R.id.iv_bike)
    ImageView bikeImage;
    @BindView(R.id.tv_pick_up_location)
    CustomTextView bikeLocationOutput;
    @BindView(R.id.tv_timing)
    CustomTextView locationTimingOutput;
    @BindView(R.id.tv_distances)
    CustomTextView toDistanceOutput;
    //    @BindView(R.id.tv_current_location)
//    CustomTextView currentLocationOutput;
    @BindView(R.id.tv_fleet_name)
    CustomTextView fleetNameOutput;
    @BindView(R.id.tv_fleet_tc)
    CustomTextView fleetTermsAndConditions;
    @BindView(R.id.tv_fleet_bikes)
    CustomTextView fleetTotalBikes;
    @BindView(R.id.tv_parking_spots)
    CustomTextView fleetParkingSpots;
    @BindView(R.id.textView2)
    CustomTextView rideCostView;
    @BindView(R.id.tv_ride_exceeding)
    CustomTextView rideExceedingView;
    @BindView((R.id.tv_ride_exceeding_divider))
    LinearLayout tv_ride_exceeding_divider;

    @BindView((R.id.tv_label_fleet))
    CustomTextView tv_label_fleet;


    @BindView((R.id.tv_free))
    TextView tv_free;


    @BindView(R.id.tv_deposit_info)
    CustomTextView rideDepositView;

    @BindView(R.id.rl_deposit_info)
    LinearLayout rl_deposit_info;

    @BindView(R.id.ll_direction)
    LinearLayout ll_direction;


    @BindView(R.id.tv_ride_deposit_description)
    CustomTextView rideDepositDescriptionView;
    @BindView(R.id.view_ride_free)
    LinearLayout rideFreeLayout;
    @BindView(R.id.view_ride_tariff)
    LinearLayout rideTariffLayout;
    @BindView(R.id.button_Reservebike)
    CustomButton button_Reservebike;

    @BindView(R.id.ll_batter_level)
    LinearLayout ll_batter_level;


    @BindView(R.id.ll_network)
    LinearLayout ll_network;

    List<Card> cardList;

    @OnClick(R.id.tv_fleet_tc)
    public void termsConditionClicked() {
        FleetTermsConditionActivity.launchActivity(this, bike.getTerms_condition_url());
    }

    @OnClick(R.id.button_Reservebike)
    public void buttonReserveBikeClicked(View v) {

        Intent intent = new Intent();
        intent.putExtra(ARGS_BIKE_DETAILS, new Gson().toJson(bike));
        intent.putExtra(ARGS_RESERVE_BIKE, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    @OnClick(R.id.tv_parking_zones)
    public void parkingZoneViewClicked() {
        FleetParkingActivity.launchForResult(this, bike.getFleet_id(), REQUEST_PARKING_ZONE_ACTIVITY);
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @OnClick(R.id.tv_add_card)
    public void addCreditCard() {
        startActivityForResult(new Intent(BikeInfoActivity.this, AddCardActivity.class), REQUEST_CODE_ADD_CARD_ACTIVITY);
    }


    @NonNull
    @Override
    protected BikeInfoPresenter getPresenter() {
        return bikeInfoPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_bike_info;
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.label_about_ride));
        setToolBarBackGround(Color.WHITE);
        if(getIntent()!=null){
            if(getIntent().hasExtra(ARGS_IS_BIKE_RESERVED)){
                if(getIntent().getBooleanExtra(ARGS_IS_BIKE_RESERVED,false)){
                    button_Reservebike.setVisibility(GONE);
                }
            }
        }

        jsonString = getIntent().getStringExtra(ARGS_BIKE_DETAILS);
        if (!jsonString.isEmpty()) {
            Gson gson = new Gson();
            bike = gson.fromJson(jsonString, Bike.class);
            bikeName.setText(bike.getBike_name());

            if(bike.getType().equalsIgnoreCase("electric") && bike.getBike_battery_level()!=null && !TextUtils.isEmpty(bike.getBike_battery_level())) {
                bikeChargeStatus.setText(bike.getBike_battery_level() + " %");
            }else{
                bikeChargeStatus.setText("N/A");
            }

            if(bike.getType().equalsIgnoreCase("electric")){
                bikeType.setText(getString(R.string.bike_type_e_bike));
            }else{
                bikeType.setText(getString(R.string.bike_type_regular));
            }
//            bikeCostStatus.setText(bike.getTariff());
            bikeDescription.setText(bike.getDescription());
            if(bike.getFleet_name()!=null){
                fleetNameOutput.setText(bike.getFleet_name());
            }else{
                fleetNameOutput.setVisibility(GONE);
            }

            Log.e(TAG, "Bike Terms and Condition url:::" + bike.getTerms_condition_url());
            if (bike.getTerms_condition_url() != null) {
                fleetTermsAndConditions.setVisibility(View.VISIBLE);
            } else {
                fleetTermsAndConditions.setVisibility(GONE);
            }

            if(bike.getFleet_bikes()==null && bike.getFleet_parking_spots()==null){
                tv_label_fleet.setVisibility(GONE);
                fleetTotalBikes.setVisibility(GONE);
                fleetParkingSpots.setVisibility(GONE);
            }else{
                if(bike.getFleet_bikes()!=null)
                    fleetTotalBikes.setText(bike.getFleet_bikes() + " " + "bikes");
                if(bike.getFleet_parking_spots()!=null)
                    fleetParkingSpots.setText(bike.getFleet_parking_spots() + " " + "parking spots");
            }


            ll_network.setVisibility(View.VISIBLE);
            fleetTermsAndConditions.setText(getString(R.string.fleet_terms_condition));


            if (IsRidePaid.isRidePaidForFleet(bike.getFleet_type())) {
                rideFreeLayout.setVisibility(GONE);
                rideTariffLayout.setVisibility(View.VISIBLE);
                showRideCostDetails();
            } else {
                ll_payment_card.setVisibility(GONE);
                String freeText = "<font color=#00AAD1>"+getString(R.string.label_cost_ride_free_default)+"</font>";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tv_free.setText(Html.fromHtml(freeText, Html.FROM_HTML_MODE_LEGACY));
                }else{
                    tv_free.setText(Html.fromHtml(freeText));
                }
                rideFreeLayout.setVisibility(View.VISIBLE);
                rideTariffLayout.setVisibility(GONE);
            }


            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.bike_default);
            requestOptions.error(R.drawable.bike_default);
            requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
            requestOptions.dontAnimate();

            Glide.with(this)
                    .load(bike.getPic())
                    .apply(requestOptions)
                    .into(bikeImage);
            final Location bikeLocation = new Location();
            bikeLocation.setLatitude(bike.getLatitude());
            bikeLocation.setLongitude(bike.getLongitude());
            getPresenter().setBikeLocation(bikeLocation);
            getPresenter().requestLocationUpdates();

        }
    }

    private void showRideCostDetails() {
        String rideCost = "";
        try {
            if (bike.getPrice_type() != null && !bike.getPrice_type().equals("")) {
                rideCost = "<font color=#00AAD1>" + CurrencyUtil.getCurrencySymbolByCode(bike.getCurrency()) + bike.getPrice_for_membership() + " "
                        + " </font><font color=#818181>" + getString(R.string.label_ride_cost) + " " +
                        bike.getPrice_type_value() + " " + LocaleTranslatorUtils.getLocaleString(this, bike.getPrice_type()) + "</font>";
            }
        } catch (Exception e) {

        }

        String rideExceeding = "";
        try {
            if (bike.getExcess_usage_type() != null && !bike.getExcess_usage_type().equals("")
                    && bike.getUsage_surcharge()!=null && bike.getUsage_surcharge().equalsIgnoreCase("yes")) {

                rideExceeding = "<font color=#818181>" + getString(R.string.label_ride_exceeding_1) +
                        "</font>" + " " + "<font color=#00AAD1>" + bike.getExcess_usage_type_after_value() + "</font>" + " " +
                        "<font color=#818181>" + LocaleTranslatorUtils.getLocaleString(this,bike.getExcess_usage_type_after_type().toLowerCase())+ " " + getString(R.string.label_ride_exceeding_2) + "</font>" + " " +
                        "<font color=#00AAD1>" + CurrencyUtil.getCurrencySymbolByCode(bike.getCurrency()) + String.format("%.2f", Float.parseFloat(bike.getExcess_usage_fees())) + " "
                        + "</font>" + "<font color=#818181>" + getString(R.string.label_ride_exceeding_3) + "</font>" + " " +
                        "</font>" + "<font color=#00AAD1>" + bike.getExcess_usage_type_value()+ " " +LocaleTranslatorUtils.getLocaleString(this,bike.getExcess_usage_type().toLowerCase()) + "</font>";
            }
        } catch (Exception e) {

        }

        String rideDeposit = "";
        try {
            if (bike.getPrice_for_ride_deposit_type() != null && !bike.getPrice_for_ride_deposit_type().equals("")
                    && bike.getRide_deposit()!=null && bike.getRide_deposit().equalsIgnoreCase("yes")) {
                rideDeposit = "<font color=#00AAD1>" + CurrencyUtil.getCurrencySymbolByCode(bike.getCurrency()) + bike.getPrice_for_ride_deposit() + " "
                        + " </font><font color=#818181>" + getString(R.string.label_ride_deposit_info) + " " +
                        bike.getPrice_for_ride_deposit_type().toLowerCase() + "</font>";
            }
        } catch (Exception e) {

        }

        String rideDescription = "";
        try {
            if (bike.getExcess_usage_type_after_type() != null && !bike.getExcess_usage_type_after_type().equals("")
                    && bike.getRide_deposit()!=null && bike.getRide_deposit().equalsIgnoreCase("yes")) {
                rideDescription = "<font color=#818181>" + getString(R.string.label_ride_deposit_description_1) + "</font>"
                        + " " + "<font color=#00AAD1>" + CurrencyUtil.getCurrencySymbolByCode(bike.getCurrency()) + bike.getPrice_for_ride_deposit() + "</font>" + " " +
                        "<font color=#818181>" + getString(R.string.label_ride_deposit_description_2) + "</font>" + " "
                        + "<font color=#00AAD1>" + bike.getExcess_usage_type_after_value() + "</font>" + " " +
                        "<font color=#818181>" + LocaleTranslatorUtils.getLocaleString(this,bike.getExcess_usage_type_after_type()).toLowerCase() + "</font>";
            }
        } catch (Exception e) {

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            rideCostView.setText(Html.fromHtml(rideCost, Html.FROM_HTML_MODE_LEGACY));
//            fleetTermsAndConditions.setText(Html.fromHtml(getString(R.string.fleet_terms_condition),
//                    Html.FROM_HTML_MODE_LEGACY));

            if(rideDeposit.equals("")) {
                rl_deposit_info.setVisibility(GONE);
            }else {
                rideDepositView.setText(Html.fromHtml(rideDeposit, Html.FROM_HTML_MODE_LEGACY));
            }

            if(rideExceeding.equals("")) {
                rideExceedingView.setVisibility(GONE);
                tv_ride_exceeding_divider.setVisibility(GONE);
            }else {
                rideExceedingView.setText(Html.fromHtml(rideExceeding, Html.FROM_HTML_MODE_LEGACY));
            }

            if(rideDescription.equals("")) {
                rideDepositDescriptionView.setVisibility(GONE);
            }else {
                rideDepositDescriptionView.setText(Html.fromHtml(rideDescription, Html.FROM_HTML_MODE_LEGACY));
            }

        } else {

            if(rideDeposit.equals("")) {
                rl_deposit_info.setVisibility(GONE);
            }else {
                rideDepositView.setText(Html.fromHtml(rideDeposit));
            }

            if(rideExceeding.equals("")) {
                rideExceedingView.setVisibility(GONE);
                tv_ride_exceeding_divider.setVisibility(GONE);
            }else {
                rideExceedingView.setText(Html.fromHtml(rideExceeding));
            }

            if(rideDescription.equals("")) {
                rideDepositDescriptionView.setVisibility(GONE);
            }else {
                rideDepositDescriptionView.setText(Html.fromHtml(rideDescription));
            }
            rideCostView.setText(Html.fromHtml(rideCost));
//            fleetTermsAndConditions.setText(Html.fromHtml(getString(R.string.fleet_terms_condition)));
        }
    }



    @Override
    public void onDirectionLoaded(Direction direction) {
        ll_direction.setVisibility(View.VISIBLE);
        getPresenter().requestStopLocationUpdates();
        bikeLocationOutput.setText(direction.getEndAddress());
        locationTimingOutput.setText(direction.getDuration() + " "
                + getString(R.string.location_description));
        toDistanceOutput.setText(direction.getDistance());
        //currentLocationOutput.setText(direction.getStartAddress());
    }


    @Override
    public void onDirectionFailed() {
        getPresenter().requestStopLocationUpdates();
        ll_direction.setVisibility(GONE);
    }

    public static void launchForResult(Activity activity, int reserveBikeRequestCode, String info, Boolean isBikeReserved) {
        activity.startActivityForResult(new Intent(activity, BikeInfoActivity.class)
                //.putParcelableArrayListExtra("CARD_DETAILS", (ArrayList<? extends Parcelable>) cards)
                .putExtra(ARGS_IS_BIKE_RESERVED,isBikeReserved)
                .putExtra(BikeInfoActivity.ARGS_BIKE_DETAILS, info), reserveBikeRequestCode);
    }

    @Override
    public void setUserPosition(Location location) {
        getPresenter().requestStopLocationUpdates();
    }

    @Override
    public void showCardDetailsList(List<Card> cardList) {
        if (cardList != null) {
            this.cardList = cardList;
            rv_UserCardLit.setVisibility(View.VISIBLE);
            rv_UserCardLit.setLayoutManager(new LinearLayoutManager(this));
            rv_UserCardLit.setAdapter(new UserCardListAdapter(this, cardList, false, this));
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ADD_CARD_ACTIVITY && resultCode == RESULT_OK) {
            getPresenter().getCards();
        }
    }

    @Override
    public void onclickCheckBox(int position) {
        getPresenter().updateCard(cardList.get(position).getId());
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }
}


