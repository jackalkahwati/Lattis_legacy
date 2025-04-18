package com.lattis.lattis.presentation.payment

import com.lattis.domain.models.Card
import com.lattis.lattis.presentation.base.BaseView

interface PaymentActivityView : BaseView{

    fun onCardListSuccess(cards: List<Card>)
    fun onNoCardSuccess()
    fun onGetCardFailure()

    fun onUpdateCardFailure()

    fun showLoadingForPayment(message: String?)
    fun hideLoadingForPayment()


    fun onPromotionsSuccess()
    fun onPromotionsFailure()

}