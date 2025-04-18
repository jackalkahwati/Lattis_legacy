package com.lattis.lattis.presentation.payment.add

import com.lattis.lattis.presentation.base.BaseView

interface AddPaymentCardActivityView : BaseView{

    fun showEditCardView()

    fun showLoadingForAddPaymentCard(message: String?)
    fun hideLoadingForAddPaymentCard()

    //// add card
    open fun showError()
    fun onCardAddSuccess()
    fun onCardAddFailure()

    fun onUpdateCardExpirationSuccess()
    fun onUpdateCardExpirationFailure()

    fun onCardInvalid()
    fun onCardAlreadyExists()

    //// delete card
    open fun onDeleteCardSuccess()
    fun onDeleteCardFailure()


    fun confirmSetupIntentForStripe()
//    fun confirmSetupIntentForMercadoPago()

    fun showSingleCardDeleteError()
    fun showPrimaryCardDeleteError()
    fun showDeleteCardWarning()


}