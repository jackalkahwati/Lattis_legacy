package com.lattis.ellipse.data;

import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.data.network.model.response.card.AddCardResponse;
import com.lattis.ellipse.data.network.model.response.card.SetUpIntentDataResponse;
import com.lattis.ellipse.data.network.model.response.card.SetUpIntentResponse;
import com.lattis.ellipse.data.network.store.CardNetworkDataStore;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.domain.repository.CardRepository;

import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;


public class CardDataRepository implements CardRepository{

    private CardNetworkDataStore cardNetworkDataStore;

    @Inject
    public CardDataRepository(CardNetworkDataStore cardNetworkDataStore){
        this.cardNetworkDataStore = cardNetworkDataStore;
    }

    @Override
    public Observable<AddCardResponse> addCard(String cc_no, int exp_month, int exp_year, String cvc, JSONObject intent) {
        return cardNetworkDataStore.addCard(cc_no,exp_month,exp_year,cvc, intent);
    }

    @Override
    public Observable<List<Card>> getCard() {
        return cardNetworkDataStore.getCard();
    }

    @Override
    public Observable<BasicResponse> deleteCard(int cardId) {
        return cardNetworkDataStore.deleteCard(cardId);
    }

    @Override
    public Observable<BasicResponse> updateCard(int cardId) {
        return cardNetworkDataStore.updateCard(cardId);
    }

    @Override
    public Observable<SetUpIntentResponse> getSetUpIntent() {
        return cardNetworkDataStore.getSetUpIntent();
    }
}
