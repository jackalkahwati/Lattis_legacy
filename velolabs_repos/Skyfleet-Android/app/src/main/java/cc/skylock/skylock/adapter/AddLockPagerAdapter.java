package cc.skylock.skylock.adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import cc.skylock.skylock.ui.fragment.AddLockHome;
import cc.skylock.skylock.ui.fragment.LockStepOne;
import cc.skylock.skylock.ui.fragment.LockStepThree;
import cc.skylock.skylock.ui.fragment.LockStepTwo;

/**
 * Created by Velo Labs Android on 18-05-2016.
 */
public class AddLockPagerAdapter extends FragmentPagerAdapter {
    Activity activity;

    public AddLockPagerAdapter(Activity activity, FragmentManager fm) {
        super(fm);
        this.activity = activity;
    }

    @Override
    public Fragment getItem(int pos) {
        switch (pos) {


            case 0:
                return AddLockHome.newInstance();
            case 1:
                return LockStepOne.newInstance();
            case 2:
                return LockStepTwo.newInstance();
            case 3:
                return LockStepThree.newInstance();
            default:
                return LockStepOne.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}
