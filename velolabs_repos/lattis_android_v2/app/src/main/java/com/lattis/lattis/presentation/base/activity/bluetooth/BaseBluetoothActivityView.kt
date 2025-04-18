package com.lattis.lattis.presentation.base.activity.bluetooth

import com.lattis.lattis.presentation.base.BaseView

interface BaseBluetoothActivityView : BaseView {
    fun onBluetoothEnabled()
    fun requestEnableBluetooth()
}