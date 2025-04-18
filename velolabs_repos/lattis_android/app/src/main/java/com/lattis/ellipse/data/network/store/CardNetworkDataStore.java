package com.lattis.ellipse.data.network.store;

import com.lattis.ellipse.data.network.api.CardApi;
import com.lattis.ellipse.data.network.model.body.card.AddCardBody;
import com.lattis.ellipse.data.network.model.body.card.DeleteCardBody;
import com.lattis.ellipse.data.network.model.mapper.CardResponseMapper;
import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.data.network.model.response.card.AddCardResponse;
import com.lattis.ellipse.data.network.model.response.card.SetUpIntentDataResponse;
import com.lattis.ellipse.data.network.model.response.card.SetUpIntentResponse;
import com.lattis.ellipse.domain.model.Card;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 7/26/17.
 */

public class CardNetworkDataStore {

    private CardApi cardApi;
    private CardResponseMapper cardResponseMapper;


    @Inject
    public CardNetworkDataStore(CardApi cardApi, CardResponseMapper cardResponseMapper) {
        this.cardApi = cardApi;
        this.cardResponseMapper = cardResponseMapper;
    }


    public Observable<AddCardResponse> addCard(String cc_no, int exp_month, int exp_year, String cvc, JSONObject intent) {
        return cardApi.addCard(new AddCardBody(cc_no, exp_month, exp_year, cvc,intent));
    }

    public Observable<List<Card>> getCard() {
        return cardApi.getCard().map(getCardResponse -> {
            List<Card> cards = new ArrayList<>();
            cards.addAll(cardResponseMapper.mapIn(getCardResponse.getCardList()));
            return cards;
        });
    }

    public Observable<BasicResponse> deleteCard(int cardId) {
        return cardApi.deleteCard(new DeleteCardBody(cardId));
    }

    public Observable<BasicResponse> updateCard(int cardId) {
        return cardApi.updateCard(new DeleteCardBody(cardId));

    }

    public Observable<SetUpIntentResponse> getSetUpIntent() {
        return cardApi.getSetUpIntent();

    }
}
