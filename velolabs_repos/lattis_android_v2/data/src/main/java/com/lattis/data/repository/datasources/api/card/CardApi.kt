package com.lattis.data.repository.datasources.api.card


import com.lattis.data.entity.body.card.AddCardBody
import com.lattis.data.entity.body.card.AddMPCardBody
import com.lattis.data.entity.body.card.CardBody
import com.lattis.data.entity.body.card.UpdateCardExpirationBody
import com.lattis.data.entity.response.BasicResponse
import com.lattis.data.entity.response.card.AddCardResponse
import com.lattis.data.entity.response.card.GetCardResponse
import com.lattis.data.entity.response.card.MPPublicKeyResponse
import com.lattis.data.entity.response.card.SetUpIntentResponse
import com.lattis.domain.models.MPPublicKey
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface CardApi {

    @get:POST("api/users/get-cards/")
    val card: Observable<GetCardResponse>

    @POST("api/users/add-cards/")
    fun addCard(@Body addCardBody: AddCardBody): Observable<AddCardResponse>

    @POST("api/users/delete-card/")
    fun deleteCard(@Body deleteCardBody: CardBody): Observable<BasicResponse>

    @POST("api/users/set-card-primary/")
    fun updateCard(@Body deleteCardBody: CardBody?): Observable<BasicResponse>

    @POST("api/users/add-cards?action=setup_intent&payment_gateway=mercadopago")
    fun getMPPublicKey(@Query("fleet_id")fleet_id:Int): Observable<MPPublicKeyResponse>

    @POST("api/users/add-cards")
    fun addMPCard(@Body addMPCardBody: AddMPCardBody): Observable<AddCardResponse>

    @POST("api/users/add-cards?action=setup_intent")
    fun getSetUpIntent(): Observable<SetUpIntentResponse>

    @POST("api/users/update-card/")
    fun updateCardExpiration(@Body updateCardExpirationBody: UpdateCardExpirationBody): Observable<BasicResponse>

}
