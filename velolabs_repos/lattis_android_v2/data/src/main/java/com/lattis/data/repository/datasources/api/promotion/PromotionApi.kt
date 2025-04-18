package com.lattis.data.repository.datasources.api.promotion

import com.lattis.data.entity.response.BasicResponse
import com.lattis.data.entity.response.promotion.PromotionsResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface PromotionApi {

    @PATCH("api/promotions/{promo_code}/redeem")
    fun redeem(@Path("promo_code") promo_code:String): Observable<BasicResponse>

    @GET("api/promotions")
    fun promotions(): Observable<PromotionsResponse>
}