package com.lattis.lattis.presentation.base.fragment.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.lattis.lattis.presentation.base.fragment.usercurrentlocation.BaseUserCurrentStatusFragment
import io.lattis.lattis.R
import io.reactivex.rxjava3.annotations.NonNull
import permissions.dispatcher.NeedsPermission


abstract class BaseBluetoothFragment<Presenter : BaseBluetoothFragmentPresenter<V>,V: BaseBluetoothFragmentView> :
    BaseUserCurrentStatusFragment<Presenter, V>(), BaseBluetoothFragmentView {


    val REQUEST_CODE_ENABLE_BLUETOOTH = 5647
    val REQUEST_CODE_BLUETOOTH_AFTER_DENY = 5648
    val REQUEST_CODE_BLUETOOTH_SCAN_CONNECT_AFTER_DENY = 5649


    private fun checkPermission(@NonNull permission: String): Boolean {
        return (ActivityCompat.checkSelfPermission(requireContext(), permission)
                === PackageManager.PERMISSION_GRANTED)
    }

    override fun checkBLEPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val deniedPermissions: ArrayList<String> = ArrayList()
            if(!checkPermission(Manifest.permission.BLUETOOTH_SCAN))
                deniedPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            if(!checkPermission(Manifest.permission.BLUETOOTH_CONNECT))
                deniedPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            if(deniedPermissions.isEmpty()){
                presenter.startLockConnectionForBLE()
            }else{
                requestMultiplePermissions.launch(deniedPermissions.toTypedArray())
            }
        }else{
            presenter.startLockConnectionForBLE()
        }
    }

    override fun requestEnableBluetooth() {
        startActivityForResult(
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
            REQUEST_CODE_ENABLE_BLUETOOTH
        )
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var anyPermissionNotGranted = false
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
                if(!it.value) {
                    anyPermissionNotGranted = true
                }
            }

            if(anyPermissionNotGranted){
                showBluetoothRequiredPop(REQUEST_CODE_BLUETOOTH_SCAN_CONNECT_AFTER_DENY)
            }else{
                presenter.startLockConnectionForBLE()
            }
        }

    override fun onBluetoothEnabled() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_OK) {
            presenter.onBluetoothEnabled()
            onBluetoothEnabled()
        }else if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_CANCELED) {
            showBluetoothRequiredPop(REQUEST_CODE_BLUETOOTH_AFTER_DENY)
        }else if( requestCode == REQUEST_CODE_BLUETOOTH_AFTER_DENY){
            requestEnableBluetooth()
        }else if( requestCode == REQUEST_CODE_BLUETOOTH_SCAN_CONNECT_AFTER_DENY){
            checkBLEPermissions()
        }
    }

    fun showBluetoothRequiredPop(requestCode:Int){
        launchPopUpActivity(
            requestCode,
            getString(R.string.bluetooth_access_alert_message),
            null,
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )
    }
}