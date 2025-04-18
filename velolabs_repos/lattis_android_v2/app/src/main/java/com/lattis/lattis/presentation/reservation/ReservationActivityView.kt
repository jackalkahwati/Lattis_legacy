package com.lattis.lattis.presentation.reservation

import com.lattis.domain.models.Card
import com.lattis.lattis.presentation.base.BaseView
import com.lattis.lattis.presentation.base.activity.location.BaseLocationActivityView

interface ReservationActivityView :BaseLocationActivityView{

    fun showLoadingForReservation(message:String)
    fun hideLoadingForReservation()

    fun resetReturnState(active:Boolean)
    fun resetPricingOptionsState(active: Boolean)
    fun resetVehicleState(active: Boolean)

    fun onAvailableVehiclesSuccess()
    fun onAvailableVehiclesFailure()

    fun handleCard(card: Card)
    fun handleNoCard()

    fun onCostEstimationSuccess()
    fun onCostEstimationFailure()

    fun onReservenSuccess()
    fun onReserveFailure()


    fun onCodeSentSuccess()
    fun onCodeSentFailure()
    fun onCodeValidateFailure()
    fun onUserProfileSuccess()

    //// rentalfare :start
    fun setPricingOptionsUI(state:Boolean)
    fun showPayPerUse()
    fun showRentalFare()
    fun onBikeReserveFailureDuePricingOptionSelectionRemaining()
    fun enableCalculateCostEstimate()
    //// rentalfare :end

}