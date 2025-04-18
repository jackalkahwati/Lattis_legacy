package com.lattis.domain.repository

import com.lattis.domain.models.Promotion
import io.reactivex.rxjava3.core.Observable

interface PromotionRepository {
    fun redeem(promo_code:String): Observable<Boolean>
    fun promotions():Observable<List<Promotion>>
}