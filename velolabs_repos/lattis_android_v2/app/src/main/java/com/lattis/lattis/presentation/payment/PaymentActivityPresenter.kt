package com.lattis.lattis.presentation.payment

import com.lattis.domain.usecase.card.GetCardUseCase
import com.lattis.domain.usecase.card.UpdateCardUseCase
import com.lattis.domain.usecase.user.GetUserUseCase
import com.lattis.domain.models.Card
import com.lattis.domain.models.Promotion
import com.lattis.domain.usecase.promotion.GetPromotionsUseCase
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import javax.inject.Inject

class PaymentActivityPresenter @Inject constructor(
    val getCardUseCase: GetCardUseCase,
    val updateCardUseCase: UpdateCardUseCase,
    val getUserUseCase: GetUserUseCase,
    val getPromotionsUseCase: GetPromotionsUseCase
) : ActivityPresenter<PaymentActivityView>(){

    var cards:List<Card>?=null
    var promotions:List<Promotion>?=null

    fun getCards(){
        subscriptions.add(
            getCardUseCase.execute(object : RxObserver<List<Card>>(view) {
                override fun onNext(newCards: List<Card>) {
                    super.onNext(newCards)
                    view?.hideLoadingForPayment()
                    cards = newCards
                    if(cards==null || cards?.size==0){
                        view?.onNoCardSuccess();
                    }else{
                        view?.onCardListSuccess(newCards);
                    }
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.hideLoadingForPayment()
                    view?.onGetCardFailure();
                }
            })
        )
    }


    fun updateCard(id: Int) {
        subscriptions.add(
            updateCardUseCase
                .setCardId(id)
                .execute(object : RxObserver<Boolean>() {
                    override fun onNext(status:Boolean) {
                        super.onNext(status)
                        getCards()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.hideLoadingForPayment()
                        view.onUpdateCardFailure()
                    }
                })
        )
    }


    fun promotions() {
        subscriptions.add(
            getPromotionsUseCase
                .execute(object : RxObserver<List<Promotion>>() {
                    override fun onNext(newPromotions:List<Promotion>) {
                        super.onNext(newPromotions)
                        promotions = newPromotions
                        view?.onPromotionsSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onPromotionsFailure()
                    }
                })
        )
    }



}