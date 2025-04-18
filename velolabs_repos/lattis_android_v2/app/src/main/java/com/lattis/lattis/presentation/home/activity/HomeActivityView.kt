package com.lattis.lattis.presentation.home.activity

import com.lattis.domain.models.Ride
import com.lattis.domain.models.User
import com.lattis.lattis.presentation.base.activity.location.BaseLocationActivityView

interface HomeActivityView : BaseLocationActivityView {
    fun startShowingBikeListFragment()
    fun startShowingBikeBookedFragment()
    fun startShowingBikeBookedWithActiveTrip()
    fun startShowingActiveTripFragment(fromQrCode:Boolean)
    fun showServerError()
    fun onRideSuccess(ride: Ride)
    fun onRideFailure()

    fun handleUser(user:User)

    fun onLogOutSuccessfull()
    fun onLogOutFailure()

    fun onSubscriptionSuccess()

    fun onReservationsAvailable()
    fun onReservationNotAvailable()
}