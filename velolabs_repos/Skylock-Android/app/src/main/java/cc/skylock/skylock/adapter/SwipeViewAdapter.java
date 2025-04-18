package cc.skylock.skylock.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import cc.skylock.skylock.cardswipe.CardStackView;
import cc.skylock.skylock.cardswipe.FeedItemView;
import cc.skylock.skylock.ui.fragment.LockStepFive;
import cc.skylock.skylock.ui.fragment.LockStepFour;
import cc.skylock.skylock.ui.fragment.LockStepThree;
import cc.skylock.skylock.ui.fragment.LockStepTwo;

/**
 * Created by prabhu on 1/19/16.
 */
public class SwipeViewAdapter extends BaseAdapter {
    List<Integer> mItems;
    Context mContext;
    CardStackView fragmentManager;
    int stepToDisplay;
    public SwipeViewAdapter(Context context,CardStackView fragmentManager,int stepToDispaly){
        this.mContext =  context;
        this. fragmentManager = fragmentManager;
        this.stepToDisplay = stepToDispaly;
        mItems = new ArrayList<Integer>();
        for(int i=stepToDispaly;i<=5;i++){
            mItems.add(i);
        }
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Integer getItem(int position) {
        return  mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FeedItemView personItemView;
        if (convertView == null) {
            personItemView = new FeedItemView(mContext);
        } else {
            personItemView = (FeedItemView) convertView;
        }
        personItemView.bind(getItem(position),fragmentManager);
        return personItemView;
    }
}
