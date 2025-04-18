package com.lattis.lattis.presentation.base.activity.usercurrentstatus

import com.lattis.domain.models.RideSummary
import com.lattis.domain.models.UserCurrentStatus
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Ride
import com.lattis.lattis.presentation.base.BaseView

interface BaseUserCurrentStatusView :BaseView{
    fun onNoInternet()
    fun onGetCurrentUserStatusFailure()

    fun doAllRequiredWhenNoBikeBookingAndNoTrip(userCurrentStatus: UserCurrentStatus)
    fun doAllRequiredWhenNoTripButBikeBooking(userCurrentStatus: UserCurrentStatus)
    fun doAllRequiredTripPresent(userCurrentStatus: UserCurrentStatus)


    fun onSaveRideSuccess(ride: Ride)
    fun onSaveRideFailure()

    fun onBikeDetailsSuccess(bike: Bike)
    fun onBikeDetailsFailure()

    fun onGetRideSummarySuccess(rideSummary: RideSummary)
    fun onGetRideSummaryFailure()
}