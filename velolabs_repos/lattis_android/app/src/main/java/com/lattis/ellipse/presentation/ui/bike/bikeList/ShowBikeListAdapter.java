package com.lattis.ellipse.presentation.ui.bike.bikeList;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.lattis.ellipse.Utils.CurrencyUtil;
import com.lattis.ellipse.Utils.ResourceUtil;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.presentation.ui.bike.BikeInfoActivity;
import com.lattis.ellipse.presentation.ui.utils.IsRidePaid;
import com.lattis.ellipse.presentation.view.CustomTextView;

import java.util.ArrayList;
import java.util.List;

import io.lattis.ellipse.R;

import static com.lattis.ellipse.presentation.ui.home.HomeActivity.REQUEST_CODE_BIKE_INFO;

class ShowBikeListAdapter extends PagerAdapter {
    Context mContext;
    LayoutInflater mLayoutInflater;
    List<Bike> mBikeList;
    TextView fleetName_TextView, bikeName_textview, bike_tariff, bike_tariff_description;
    ImageView logo_ImageView, cardTypeView;

    CustomTextView ct_bike_terms_condition, cardNumber;
    Card userCard;
    boolean fromQRCode;


    public ShowBikeListAdapter(Context context, List<Bike> bikeList, boolean fromQRCode) {
        this.mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mBikeList = bikeList;
        this.fromQRCode = fromQRCode;
    }

    public void clearList(){
        mBikeList = mBikeList==null ? new ArrayList<>() : mBikeList;
        this.mBikeList.clear();
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.layout_bike_details, container, false);
        fleetName_TextView = (TextView) itemView.findViewById(R.id.tv_fleet_name);
        bikeName_textview = (TextView) itemView.findViewById(R.id.tv_bike_name);
        logo_ImageView = (ImageView) itemView.findViewById(R.id.iv_logo);
        bike_tariff = (TextView) itemView.findViewById(R.id.tv_bike_tariff);
        ct_bike_terms_condition = (CustomTextView) itemView.findViewById(R.id.ct_bike_terms_condition);
        bike_tariff_description = (CustomTextView) itemView.findViewById(R.id.tv_bike_tariff_description);
        cardNumber = (CustomTextView) itemView.findViewById(R.id.tv_card_no);
        cardTypeView = (ImageView) itemView.findViewById(R.id.card_type_icon);
        fleetName_TextView.setText(mBikeList.get(position).getFleet_name());
        bikeName_textview.setText((mBikeList.get(position).getBike_name()));
        //   if (mBikeList.get(position).getPrice_for_membership() != "0")
        if (IsRidePaid.isRidePaidForFleet(mBikeList.get(position).getFleet_type())) {

            if(mBikeList.get(position).getPrice_for_membership()!=null && mBikeList.get(position).getPrice_type_value()!=null){
                bike_tariff.setText(CurrencyUtil.getCurrencySymbolByCode(mBikeList.get(position).getCurrency()) + mBikeList.get(position).getPrice_for_membership());
                bike_tariff_description.setText("/" + " " + mBikeList.get(position).getPrice_type_value() + " " +
                        mBikeList.get(position).getPrice_type() + "*");
            }else{
                bike_tariff.setText("   ");
                bike_tariff_description.setText("        ");
            }

        } else
            bike_tariff.setText(mContext.getString(R.string.ride_cost_status));
        container.addView(itemView);

        RequestOptions requestOptions = new RequestOptions()
            .error(R.mipmap.ic_launcher)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(mContext)
                .load(mBikeList.get(position).getFleet_logo())
                .apply(requestOptions)
                .into(logo_ImageView);


        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gson gson = new Gson();
                String info = gson.toJson(mBikeList.get(position));
                BikeInfoActivity.launchForResult(((Activity) mContext), REQUEST_CODE_BIKE_INFO, info, false);
                ((Activity) mContext).overridePendingTransition(R.anim.slide_in_up, R.anim.no_animation);
                ((Activity) mContext).overridePendingTransition(0, 0);
            }
        });
        if (userCard != null) {
            cardTypeView.setImageResource(ResourceUtil.getResource(userCard.getCc_type().toUpperCase()));
            cardNumber.setText("*" + userCard.getCc_no().substring(userCard.getCc_no().length() - 4));
        }


        ct_bike_terms_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBikeList.get(position).getTerms_condition_url() != null) {
                        FleetTermsConditionActivity.launchActivity(((Activity) mContext), mBikeList.get(position).getTerms_condition_url());
                    }
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(fromQRCode)
                ct_bike_terms_condition.setText(Html.fromHtml(mContext.getString(R.string.find_rite_terms_body_qr), Html.FROM_HTML_MODE_LEGACY));
            else
                ct_bike_terms_condition.setText(Html.fromHtml(mContext.getString(R.string.find_rite_terms_body), Html.FROM_HTML_MODE_LEGACY));

        } else {
            if(fromQRCode)
                ct_bike_terms_condition.setText(Html.fromHtml(mContext.getString(R.string.find_rite_terms_body_qr)));
            else
                ct_bike_terms_condition.setText(Html.fromHtml(mContext.getString(R.string.find_rite_terms_body)));
        }
        ct_bike_terms_condition.setMovementMethod(LinkMovementMethod.getInstance());


        if (mBikeList.get(position).getTerms_condition_url() != null) {
            ct_bike_terms_condition.setVisibility(View.VISIBLE);
        } else {
            ct_bike_terms_condition.setVisibility(View.GONE);
        }
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    @Override
    public int getCount() {
        return mBikeList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == (LinearLayout) object);
    }

    public void setCardList(Card card) {
        this.userCard = card;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


}