package com.lattis.ellipse.presentation.ui.ride.walkthrough;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import io.lattis.ellipse.R;

/**
 * Created by ssd3 on 7/24/17.
 */

public enum  RideWalkThroughPage {

    HOW_TO_UNLOCK(R.string.ride_walkthough_page1_title_label, R.string.ride_walkthough_page1_subtitle_label, R.drawable.icon_lock),
    PULL_THE_LOCK_OPEN(R.string.ride_walkthough_page2_title_label, R.string.ride_walkthough_page2_subtitle_label, R.drawable.pull_lock),
    PUSH_THE_LOCK_CLOSED(R.string.ride_walkthough_page3_title_label, R.string.ride_walkthough_page3_subtitle_label, R.drawable.push_lock),
    HOW_TO_LOCK(R.string.ride_walkthough_page4_title_label, R.string.ride_walkthough_page4_subtitle_label, R.drawable.icon_unlock),
    LOCK_AND_STOW(R.string.ride_walkthough_page5_title_label, R.string.ride_walkthough_page5_subtitle_label, R.drawable.lock_stow);

    private int title;
    private int description;
    private int image;

    private RideWalkThroughPage(@StringRes int title, @StringRes int description, @DrawableRes int image) {
        this.title = title;
        this.description = description;
        this.image = image;
    }

    public int getTitle() {
        return title;
    }

    public int getDescription() {
        return description;
    }

    public int getImage() {
        return image;
    }

}
