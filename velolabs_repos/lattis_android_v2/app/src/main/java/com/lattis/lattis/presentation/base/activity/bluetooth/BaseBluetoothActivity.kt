package com.lattis.lattis.presentation.base.activity.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.core.app.ActivityCompat.startActivityForResult
import com.lattis.lattis.presentation.base.activity.BaseCloseActivity

abstract class BaseBluetoothActivity<Presenter : BaseBluetoothActivityPresenter<V>,V:BaseBluetoothActivityView> :
    BaseCloseActivity<Presenter,V>(), BaseBluetoothActivityView {
    override fun requestEnableBluetooth() {
        startActivityForResult(
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
            REQUEST_CODE_ENABLE_BLUETOOTH
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
                presenter.onBluetoothEnabled()
                onBluetoothEnabled()
            }
        }
    }

    override fun onBluetoothEnabled() {}

    companion object {
        private const val REQUEST_CODE_ENABLE_BLUETOOTH = 5647
    }
}