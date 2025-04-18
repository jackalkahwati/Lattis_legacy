package com.lattis.ellipse.presentation.ui.base.activity;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import io.lattis.ellipse.R;


public class DrawerMenu {

    public static List<DrawerMenu> getMenusWithRide(){
        List<DrawerMenu>drawerMenus = new ArrayList<>();
        drawerMenus.add(new DrawerMenu(R.id.nav_home, R.string.title_activity_home, R.mipmap.home));
        drawerMenus.add(new DrawerMenu(R.id.nav_billing, R.string.menu_payment, R.mipmap.payment));
        drawerMenus.add(new DrawerMenu(R.id.profile, R.string.action_profile_settings, R.mipmap.ic_profile));
        drawerMenus.add(new DrawerMenu(R.id.nav_ride_history, R.string.menu_history, R.mipmap.ride_history));
        drawerMenus.add(new DrawerMenu(R.id.nav_report_damage, R.string.report_damage, R.mipmap.damage));
        drawerMenus.add(new DrawerMenu(R.id.nav_report_theft, R.string.report_theft_small, R.mipmap.report_theft));
        return drawerMenus;
    }

    public static List<DrawerMenu> getMenusWithoutRide(){
        List<DrawerMenu>drawerMenus = new ArrayList<>();
        drawerMenus.add(new DrawerMenu(R.id.nav_home, R.string.title_activity_home, R.mipmap.home));
        drawerMenus.add(new DrawerMenu(R.id.nav_billing, R.string.menu_payment, R.mipmap.payment));
        drawerMenus.add(new DrawerMenu(R.id.profile, R.string.action_profile_settings, R.mipmap.ic_profile));
        drawerMenus.add(new DrawerMenu(R.id.nav_ride_history, R.string.menu_history, R.mipmap.ride_history));
        return drawerMenus;
    }

    private int itemId;
    private int titleId;
    private int imageId;
    private boolean checked;

    public DrawerMenu(@IdRes int itemId, @StringRes int titleId, @DrawableRes int imageId) {
        this.itemId = itemId;
        this.titleId = titleId;
        this.imageId = imageId;
    }

    public int getItemId() {
        return this.itemId;
    }

    public int getTitleId() {
        return this.titleId;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getImageId() {
        return imageId;
    }

    public boolean isChecked() {
        return this.checked;
    }

}
