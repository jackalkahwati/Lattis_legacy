package cc.skylock.skylock.adapter;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.WalkThroughActivity;

public class WalkThroughPagerAdapter extends PagerAdapter {
    WalkThroughActivity activity;
    LayoutInflater inflater;


    public WalkThroughPagerAdapter(WalkThroughActivity context) {
        this.activity = context;
    }

    int NumberOfPages = 4;
    int[] image = {
            R.drawable.page_one,
            R.drawable.page_two,
            R.drawable.page_three,
            R.drawable.page4
    };


    @Override
    public int getCount() {
        return NumberOfPages;
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        RelativeLayout walkthroughImage;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.walkthrough_items, container, false);
        walkthroughImage = (RelativeLayout) itemView.findViewById(R.id.walkthroughImage);
        walkthroughImage.setBackground(ResourcesCompat.getDrawable(activity.getResources(), image[position], null));
        (container).addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

}
