package com.lattis.data.repository.implementation.api

import com.lattis.data.database.store.CardNetworkDataStore
import com.lattis.data.entity.body.card.AddCardBody
import com.lattis.data.entity.response.BasicResponse
import com.lattis.data.mapper.CardSetUpIntentMapper
import com.lattis.domain.models.SetUpIntent
import com.lattis.domain.repository.CardRepository
import com.lattis.domain.models.Card
import com.lattis.domain.models.MPPublicKey
import io.reactivex.rxjava3.core.Observable
import org.json.JSONObject
import javax.inject.Inject

class CardRepositoryImp @Inject
constructor(
    private val cardNetworkDataStore: CardNetworkDataStore,
    private val cardSetUpIntentMapper: CardSetUpIntentMapper
) : CardRepository {
    override fun getCard(): Observable<List<Card>> {
        return cardNetworkDataStore.card
    }


    override fun deleteCard(cardId: Int): Observable<Boolean> {
        return cardNetworkDataStore.deleteCard(cardId).map {
            true
        }
    }

    override fun updateCard(cardId: Int): Observable<Boolean> {
        return cardNetworkDataStore.updateCard(cardId).map {
            true
        }
    }

    override fun updateCardExpiration(cardId: String,exp_month: Int, exp_year: Int): Observable<Boolean>{
        return cardNetworkDataStore.updateCardExpiration(cardId,exp_month,exp_year).map {
            true
        }
    }

    override fun getSetUpIntent(): Observable<SetUpIntent> {
        return cardNetworkDataStore.setUpIntent().map {
            cardSetUpIntentMapper.mapIn(it)
        }
    }

    override fun addCard(
        cc_no: String,
        exp_month: Int,
        exp_year: Int,
        cvc: String,
        intent: JSONObject
    ): Observable<Boolean> {
        return cardNetworkDataStore.addCard(cc_no, exp_month, exp_year, cvc, intent).map {
            true
        }
    }

    override fun getMPPublicKey(
        fleet_id: Int
    ): Observable<MPPublicKey> {
        return cardNetworkDataStore.getMPPublicKey(fleet_id)
    }

    override fun addMPCard(
        token:String,
        fleet_id: Int?
    ):Observable<Boolean>{
        return cardNetworkDataStore.addMPCard(token,fleet_id)
    }
}