package com.lattis.ellipse.domain.model;

import androidx.annotation.DrawableRes;

import io.lattis.ellipse.R;

public enum Pin {
    UP("01", R.drawable.icon_input_top),
    RIGHT("02", R.drawable.icon_input_right),
    BOTTOM("04", R.drawable.icon_input_down),
    LEFT("08", R.drawable.icon_input_left);

    private String value;
    private int drawable;

    Pin(String value, @DrawableRes int drawable) {
        this.value = value;
        this.drawable = drawable;
    }

    public String getPin() {
        return this.value;
    }

    public int getDrawable() {
        return this.drawable;
    }

}
