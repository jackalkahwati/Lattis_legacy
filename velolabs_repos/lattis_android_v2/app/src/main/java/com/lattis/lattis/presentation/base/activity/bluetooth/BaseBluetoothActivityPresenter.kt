package com.lattis.lattis.presentation.base.activity.bluetooth

import com.lattis.lattis.presentation.base.activity.ActivityPresenter

abstract class BaseBluetoothActivityPresenter<View : BaseBluetoothActivityView> :
    ActivityPresenter<View>() {
    open fun onBluetoothEnabled() {}
}