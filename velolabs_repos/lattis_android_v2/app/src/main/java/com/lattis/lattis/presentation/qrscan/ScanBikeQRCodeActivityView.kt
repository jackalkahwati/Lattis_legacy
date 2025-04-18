package com.lattis.lattis.presentation.qrscan

import com.lattis.domain.models.Bike
import com.lattis.domain.models.Card
import com.lattis.domain.models.Lock
import com.lattis.lattis.presentation.base.activity.bluetooth.BaseBluetoothActivityView
import com.lattis.lattis.uimodel.model.LockModel

interface ScanBikeQRCodeActivityView : BaseBluetoothActivityView {
    fun handleCard(card: Card)
    fun handleNoCard()
    fun onInvalidQRCode()
    fun restartScanner()
    fun onBikeDetailsSuccess(bike: Bike)
    fun onBikeUnAuthorised()
    fun onBikeAlreadyRented()
    fun onBikeNotFound()
    fun onBikeNotAvailable()
    fun onBikeDetailsFailure()
    fun onBikeNotLive()
    fun startRide()
    fun onStartRideSuccess()
    fun onStartRideFail()
    fun OnReserveBikeSuccess()
    fun OnReserveBikeFail()
    fun OnReserveBikeNotFound()
    fun onMissingUserCard()
    fun onCancelBikeSuccess()
    fun onCancelBikeFail()
    fun hideProgressbar()
    fun showProgressbar()

    ////////////////////////////////// LOCK CONNECTION CODE : START //////////////////////////////////////////////////
    fun onLockScanned(lockModel: LockModel?)
    fun onScanStart()
    fun onScanStop()
    fun onLockConnected(lockModel: LockModel)
    fun showConnecting(requiresReconnection: Boolean)
    fun onLockConnectionFailed()
    fun onLockConnectionAccessDenied()
    fun OnSignedMessagePublicKeySuccess(
        signedMessage: String,
        publicKey: String
    )

    fun OnSignedMessagePublicKeyFailure()
    fun onSaveLockSuccess(lock: Lock?)
    fun onSaveLockFailure()
    fun OnSetPositionStatus(status: Boolean)
    fun onGetUserProfile()

    fun onCodeSentSuccess()
    fun onCodeSentFailure()
    fun onCodeValidateFailure()


    fun onIotScanSuccess()
    fun onIotScanFailure()
    fun setIotScanSubTitle()


    //// rentalfare :start
    fun showPayPerUse()
    fun showRentalFare()
    fun onBikeReserveFailureDuePricingOptionSelectionRemaining()
    //// rentalfare :end


    ////closehub :start
    fun showClosedHubPorts()
    fun onClosedHubPortSelectionFailure()
    fun onClosedHubPortSelectionSuccess()
    ////closehub :end
}