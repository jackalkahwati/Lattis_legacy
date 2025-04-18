package com.lattis.lattis.presentation.base.fragment.bluetooth

import com.lattis.domain.models.FirebasePushNotification
import com.lattis.domain.models.UpdateTripData
import com.lattis.domain.models.Lock
import com.lattis.domain.models.Ride
import com.lattis.lattis.presentation.base.fragment.usercurrentlocation.BaseUserCurrentStatusView
import com.lattis.lattis.uimodel.model.LockModel

interface BaseBluetoothFragmentView : BaseUserCurrentStatusView {

    fun onBluetoothEnabled()

    fun requestEnableBluetooth()


    fun onRideSuccess(ride: Ride)
    fun onRideFailure()


    fun onLockConnected(lockModel: LockModel)

    fun showConnectingAsDeviceFound()
    fun showDisconnected()
    fun showConnected()
    fun onLockNotFound()

    fun onLockConnectionFailed()
    fun onLockConnectionAccessDenied()


    fun onSignedMessagePublicKeySuccess(
        signedMessage: String?,
        publicKey: String?
    )

    fun onSignedMessagePublicKeyFailure()

    fun onSetPositionStatus(status: Boolean)
    fun onSetPositionFailure()

    fun showInvalidLockPositionError()
    fun showLockPositionSuccess(position: Lock.Hardware.Position)
    fun handleIoTLockPosition()

    fun showActiveTripData(updateTripData: UpdateTripData)

    fun onLockDisconnectedAfterEndingRide()


    fun isEndRidePossible(status: Boolean)
    fun lockGuidancePopup(status:Boolean)


    fun showDockHubDockingNotification(firebasePushNotification: FirebasePushNotification)

    fun showAdapterScanAndCancelBtns()

    fun lockUnlockPopUp(show:Boolean,locking:Boolean?)

    fun showTapkeyUnlockSuccessFailurePopup(status:Boolean)

    fun showSentinelUnlockUI()
    fun sentinelLockGuidance(active: Boolean)
    fun sentinelTapGuidance(active: Boolean)


    fun showReservationTimerOverPopUp()


    fun checkBLEPermissions()

}