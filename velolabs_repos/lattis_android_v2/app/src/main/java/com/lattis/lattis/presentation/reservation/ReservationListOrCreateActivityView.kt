package com.lattis.lattis.presentation.reservation

import com.lattis.domain.models.Card
import com.lattis.lattis.presentation.base.activity.location.BaseLocationActivityView

interface ReservationListOrCreateActivityView : BaseLocationActivityView {

    fun onReservationsAvailable()
    fun onReservationNotAvailable()

    fun onAvailableTimerValue(time:String)
    fun onAvailableTimerFinish()

    fun handleCard(card: Card)
    fun handleNoCard()

    fun onReservationStartTripSuccess()
    fun onReservationCStartTripFailure()

    fun onReservationCancelSuccess()
    fun onReservationCancelFailure()

    fun onAlreadyOnRideFailure()

    fun onCardMissingFailure()
    fun onNoPrimaryCardFailure()

    fun startReservationTimerOverLocalNotification()
}