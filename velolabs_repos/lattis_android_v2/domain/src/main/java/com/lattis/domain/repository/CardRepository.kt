package com.lattis.domain.repository

import com.lattis.domain.models.SetUpIntent
import com.lattis.domain.models.Card
import com.lattis.domain.models.MPPublicKey
import io.reactivex.rxjava3.core.Observable
import org.json.JSONObject

interface CardRepository {
    fun getCard(): Observable<List<Card>>

    fun deleteCard(cardId: Int): Observable<Boolean>

    fun updateCard(cardId: Int): Observable<Boolean>

    fun getSetUpIntent(): Observable<SetUpIntent>

    fun addCard(
        cc_no: String,
        exp_month: Int,
        exp_year: Int,
        cvc: String,
        intent: JSONObject
    ): Observable<Boolean>

    fun getMPPublicKey(
        fleet_id: Int
    ): Observable<MPPublicKey>

    fun addMPCard(
        token:String,
        fleet_id: Int?
    ):Observable<Boolean>

    fun updateCardExpiration(cardId: String,exp_month: Int, exp_year: Int): Observable<Boolean>

}