package com.lattis.lattis.presentation.bikelist
import com.lattis.domain.models.Card
import com.lattis.domain.models.User
import com.lattis.lattis.presentation.base.fragment.location.BaseLocationFragmentView

interface BikeListFragmentView : BaseLocationFragmentView{
    fun handleBikesAndDockHubs()
    fun handleNoBikes()
    fun handleError()

    fun handleDockHubs()
    fun handleParkingHubs()

    fun handleUser(user: User?)
    fun handleCard(card: Card)
    fun handleNoCard()


    fun OnReserveBikeSuccess(startTime: Long, countDownTime: Int)
    fun OnReserveBikeFail()
    fun OnReserveBikeNotFound()
    fun onPreAuthFailure()
    fun onMissingUserCard()

    fun onBikeAlreadyRented()



    fun onCodeSentSuccess()
    fun onCodeSentFailure()
    fun onCodeValidateFailure()

    fun onUserProfileSuccess()


    fun showPayPerUse()
    fun showRentalFare()
    fun onBikeReserveFailureDuePricingOptionSelectionRemaining()

}