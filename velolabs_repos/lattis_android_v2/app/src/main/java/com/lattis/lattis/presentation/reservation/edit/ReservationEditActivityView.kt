package com.lattis.lattis.presentation.reservation.edit

import com.lattis.domain.models.Bike
import com.lattis.domain.models.Card
import com.lattis.lattis.presentation.base.BaseView
import com.lattis.lattis.presentation.base.activity.location.BaseLocationActivityView

interface ReservationEditActivityView : BaseLocationActivityView {

    fun showTripStart(status:Boolean)

    fun onReservationStartTripSuccess()
    fun onReservationCStartTripFailure()

    fun onReservationCancelSuccess()
    fun onReservationCancelFailure()

    fun onReservationInformationSuccess(bike: Bike)
    fun onReservationInformationFailure()

    fun handleCard(card: Card)
    fun handleNoCard()

}