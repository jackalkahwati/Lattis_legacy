package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.data.network.model.response.card.AddCardResponse;
import com.lattis.ellipse.data.network.model.response.card.SetUpIntentDataResponse;
import com.lattis.ellipse.data.network.model.response.card.SetUpIntentResponse;
import com.lattis.ellipse.domain.model.Card;

import org.json.JSONObject;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 7/26/17.
 */

public interface CardRepository {
    Observable<AddCardResponse> addCard(String cc_no, int exp_month, int exp_year, String cvc, JSONObject intent);
    Observable<List<Card>> getCard();

    Observable<BasicResponse> deleteCard(int cardId);

    Observable<BasicResponse> updateCard(int cardId);

    Observable<SetUpIntentResponse> getSetUpIntent();
}
