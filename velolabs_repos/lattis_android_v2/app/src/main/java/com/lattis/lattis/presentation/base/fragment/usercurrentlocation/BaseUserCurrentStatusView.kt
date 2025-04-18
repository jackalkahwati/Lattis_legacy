package com.lattis.lattis.presentation.base.fragment.usercurrentlocation

import com.lattis.domain.models.RideSummary
import com.lattis.domain.models.UserCurrentStatus
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Ride
import com.lattis.lattis.presentation.base.fragment.location.BaseLocationFragmentView

interface BaseUserCurrentStatusView : BaseLocationFragmentView{
    fun onGetCurrentUserStatusFailure()
    fun onNoInternet()

    fun doAllRequiredWhenNoBikeBookingAndNoTrip(userCurrentStatus: UserCurrentStatus)
    fun doAllRequiredWhenNoTripButBikeBooking(userCurrentStatus: UserCurrentStatus)
    fun doAllRequiredTripPresent(userCurrentStatus: UserCurrentStatus)

    fun onSaveRideSuccess(ride: Ride)
    fun onSaveRideFailure()

    fun onBikeDetailsSuccess(bike: Bike)
    fun onBikeDetailsFailure()

    fun onGetRideSummarySuccess(rideSummary: RideSummary)
    fun onGetRideSummaryFailure()


    fun onGetRideSummarySuccessForEndingRide(rideSummary: RideSummary)
    fun onGetRideSummaryFailureForEndingRide()
}