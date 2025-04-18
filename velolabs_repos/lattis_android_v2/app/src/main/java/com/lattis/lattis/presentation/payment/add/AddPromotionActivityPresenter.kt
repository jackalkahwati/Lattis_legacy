package com.lattis.lattis.presentation.payment.add

import android.text.TextUtils
import com.lattis.domain.usecase.promotion.PromotionRedeemUseCase
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import javax.inject.Inject

class AddPromotionActivityPresenter @Inject constructor(
    val promotionRedeemUseCase: PromotionRedeemUseCase
): ActivityPresenter<AddPromotionActivityView>() {

    var promo_code:String?=null

    fun setPromoCode(promo_code:String?){
        this.promo_code = promo_code
    }

    fun addPromoCode() {

        if(TextUtils.isEmpty(promo_code)){
            return
        }

        subscriptions.add(
            promotionRedeemUseCase
                .withPromoCode(promo_code!!)
                .execute(object : RxObserver<Boolean>() {
                    override fun onNext(status:Boolean) {
                        super.onNext(status)
                        view?.onAddPromotionSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onAddPromotionFailure()
                    }
                })
        )
    }

}