package com.lattis.data.repository.implementation.api

import com.lattis.data.net.promotion.PromotionApiClient
import com.lattis.domain.models.Promotion
import com.lattis.domain.repository.PromotionRepository
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class PromotionRepositoryImp @Inject constructor(
    val promotionApiClient: PromotionApiClient
) : PromotionRepository {

    override fun redeem(promo_code: String): Observable<Boolean> {
        return promotionApiClient.api.redeem(promo_code)
            .map { true }
    }

    override fun promotions(): Observable<List<Promotion>> {
        return promotionApiClient.api.promotions()
            .map {
                it.promotions
            }
    }
}