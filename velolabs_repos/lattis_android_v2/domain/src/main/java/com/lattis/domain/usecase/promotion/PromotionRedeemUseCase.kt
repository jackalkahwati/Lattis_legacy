package com.lattis.domain.usecase.promotion

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.PromotionRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class PromotionRedeemUseCase @Inject protected constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val promotionRepository: PromotionRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {

    var promo_code : String?=null

    fun withPromoCode(promo_code: String) : PromotionRedeemUseCase {
        this.promo_code = promo_code
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return promotionRepository.redeem(promo_code!!)
    }
}