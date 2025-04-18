package com.lattis.lattis.presentation.ride

import com.lattis.domain.models.ParkingZone
import com.lattis.lattis.presentation.base.fragment.bluetooth.BaseBluetoothFragmentView

interface BikeBookedOrActiveRideView : BaseBluetoothFragmentView {


    fun showBikeOnly()
    fun showBikeBookingWithTrip(confirm:Boolean)
    fun showActiveTrip()

    fun showBikeBookedTime(time:String)
    fun showActiveTripTime(time:String)

    fun startDisconnectedAnimation()
    fun startConnectingAnimation(isActiveTripAnimationRequired:Boolean)
    fun showConnectionTimeOut()
    fun onStartRideFail()

    fun onCancelBikeSuccess()
    fun onCancelBikeFail()

    fun showServerErrorForParkingFee()
    fun showRestrictedParking()
    fun showOutOfBound(fee:Float,currency:String)
    fun showOutOfZone()
    fun startEndRide()
    fun enableLockUnlockAndHideProgressAfterParkingFeeCheck()


    fun hideProgressLoading()


    fun onFindingParkingSuccess()
    fun onFindParkingFailure()
    fun onDockHubsSuccess()
    fun onFindingZoneSuccess(parkingZone: List<ParkingZone>, isGeoFence:Boolean)

    fun onReportTheftSuccess()
    fun onReportTheftFailure()

    fun showIotScreen()
    fun showManualLockScreen()
    fun showManualLockPopScreen()

    fun onNoLockAvailableForBike()

    fun applyGeoFenceRestrictionIfApplicable()


    fun hideIotHintPopupDueToTimer()
    fun cancelRideReservation()

}