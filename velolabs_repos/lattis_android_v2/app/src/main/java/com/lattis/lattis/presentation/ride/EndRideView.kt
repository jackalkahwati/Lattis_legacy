package com.lattis.lattis.presentation.ride

import com.lattis.lattis.presentation.base.activity.location.BaseLocationActivityView

interface EndRideView : BaseLocationActivityView {

    fun onUploadImageSuccess()

    fun onUploadImageFailure()


    fun onEndTripSuccess()

    fun onEndTripFailure()

    fun onEndTripPaymentFailure()

    fun onEndTripStripeConnectFailure()

    fun onEndTripEnforeParkingFailure()

    fun onActiveTripStopped()

    fun handleUINotForceEndRide()
    fun handleUIForceEndRide()

}