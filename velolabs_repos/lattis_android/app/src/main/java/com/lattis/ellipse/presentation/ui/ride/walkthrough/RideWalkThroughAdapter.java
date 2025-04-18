package com.lattis.ellipse.presentation.ui.ride.walkthrough;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import io.lattis.ellipse.R;

/**
 * Created by ssd3 on 7/24/17.
 */

public class RideWalkThroughAdapter extends PagerAdapter {

    @Override
    public int getCount() {
        return RideWalkThroughPage.values().length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Context context = container.getContext();

        int layoutId = R.layout.ride_walkthrough_items;

        View view = LayoutInflater.from(context)
                .inflate(layoutId, container, false);

        TextView titleView = (TextView) view.findViewById(R.id.ride_walkthrough_item_title);
        TextView descriptionView = (TextView) view.findViewById(R.id.ride_walkthrough_item_subtitle);
        ImageView imageView = (ImageView) view.findViewById(R.id.ride_walkthrough_item_image);

        RideWalkThroughPage page = RideWalkThroughPage.values()[position];

        titleView.setText(context.getString(page.getTitle()));
        descriptionView.setText(context.getString(page.getDescription()));
        imageView.setImageResource(page.getImage());

        if(position==2){
            descriptionView.setTextSize(18);
        }else{
            descriptionView.setTextSize(22);
        }

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}
