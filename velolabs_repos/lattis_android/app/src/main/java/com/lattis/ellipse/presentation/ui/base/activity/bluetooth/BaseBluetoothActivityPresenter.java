package com.lattis.ellipse.presentation.ui.base.activity.bluetooth;

import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

/**
 * Created by ssd3 on 9/21/17.
 */

public abstract class BaseBluetoothActivityPresenter <View extends BaseBluetoothActivityView> extends ActivityPresenter<View> {

    protected void onBluetoothEnabled(){ }
}
