package com.lattis.ellipse.presentation.ui.base.activity.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

import com.lattis.ellipse.presentation.ui.base.activity.BaseCloseActivity;

/**
 * Created by ssd3 on 9/21/17.
 */

public abstract class BaseBluetoothActivity <Presenter extends BaseBluetoothActivityPresenter> extends BaseCloseActivity<Presenter>
        implements BaseBluetoothActivityView {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 5647;

    @Override
    public void requestEnableBluetooth() {
        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                REQUEST_CODE_ENABLE_BLUETOOTH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQUEST_CODE_ENABLE_BLUETOOTH){
                getPresenter().onBluetoothEnabled();
                onBluetoothEnabled();
            }
        }
    }

    @Override
    public void onBluetoothEnabled() {

    }
}

