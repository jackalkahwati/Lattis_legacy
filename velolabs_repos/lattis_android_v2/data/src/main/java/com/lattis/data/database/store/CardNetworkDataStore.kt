package com.lattis.data.database.store

import com.lattis.data.entity.body.card.AddCardBody
import com.lattis.data.entity.body.card.AddMPCardBody
import com.lattis.data.entity.body.card.CardBody
import com.lattis.data.entity.body.card.UpdateCardExpirationBody
import com.lattis.data.entity.response.BasicResponse
import com.lattis.data.entity.response.card.AddCardResponse
import com.lattis.data.entity.response.card.SetUpIntentResponse
import com.lattis.data.mapper.CardResponseMapper
import com.lattis.data.net.card.CardApiClient
import com.lattis.domain.models.Card
import com.lattis.domain.models.MPPublicKey

import org.json.JSONObject

import java.util.ArrayList

import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable

/**
 * Created by ssd3 on 7/26/17.
 */

class CardNetworkDataStore @Inject
constructor(private val cardApiClient: CardApiClient,
            private val cardResponseMapper: CardResponseMapper) {

    val card: Observable<List<Card>>
        get() = cardApiClient.api.card.map { getCardResponse ->
            val cards = ArrayList<Card>()
            if(getCardResponse!=null && getCardResponse.cardList!=null) {
                cards.addAll(cardResponseMapper.mapIn(getCardResponse.cardList!!))
            }
            cards
        }

    fun setUpIntent(): Observable<SetUpIntentResponse>{
        return cardApiClient.api.getSetUpIntent()
    }


    fun addCard(cc_no: String, exp_month: Int, exp_year: Int, cvc: String, intent: JSONObject): Observable<AddCardResponse> {
        return cardApiClient.api.addCard(AddCardBody(cc_no, exp_month, exp_year, cvc, intent))
    }

    fun deleteCard(cardId: Int): Observable<BasicResponse> {
        return cardApiClient.api.deleteCard(CardBody(cardId))
    }

    fun updateCard(cardId: Int): Observable<BasicResponse> {
        return cardApiClient.api.updateCard(CardBody(cardId))

    }

    fun updateCardExpiration(cardId: String,exp_month: Int, exp_year: Int): Observable<BasicResponse>{
        return cardApiClient.api.updateCardExpiration(UpdateCardExpirationBody(cardId,exp_month,exp_year))
    }

    fun getMPPublicKey(
        fleet_id: Int
    ):Observable<MPPublicKey>{
        return cardApiClient.api.getMPPublicKey(fleet_id)
            .map {
                it.mpPublicKey
            }
    }

    fun addMPCard(
        token:String,
        fleet_id: Int?
    ):Observable<Boolean>{
        return cardApiClient.api.addMPCard(AddMPCardBody(
            token,
            "mercadopago",
            fleet_id)
        ).map {
            true
        }
    }
}
