package com.lattis.ellipse.data.network.model.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.data.network.model.response.card.GetCardDataResponse;
import com.lattis.ellipse.domain.model.Card;

import javax.inject.Inject;

/**
 * Created by ssd3 on 7/26/17.
 */

public class CardResponseMapper extends AbstractDataMapper<GetCardDataResponse, Card> {

    @Inject
    public CardResponseMapper() {
    }

    @NonNull
    @Override
    public Card mapIn(@NonNull GetCardDataResponse getCardDataResponse) {

        if (getCardDataResponse == null) {
            return new Card();
        }


        Card card = new Card();
        card.setId(getCardDataResponse.getId());
        card.setUser_id(getCardDataResponse.getUser_id());
        card.setStripe_net_profile_id(getCardDataResponse.getStripe_net_profile_id());
        card.setStripe_net_payment_id(getCardDataResponse.getStripe_net_payment_id());
        card.setIs_primary(getCardDataResponse.getIs_primary());
        card.setType_card(getCardDataResponse.getType_card());
        card.setCc_no(getCardDataResponse.getCc_no());
        card.setExp_month(getCardDataResponse.getExp_month());
        card.setExp_year(getCardDataResponse.getExp_year());
        card.setFingerprint(getCardDataResponse.getFingerprint());
        card.setCc_type(getCardDataResponse.getCc_type());
        card.setCreated_date(getCardDataResponse.getCreated_date());
        card.setLast_updated(getCardDataResponse.getLast_updated());
        return card;

    }

    @NonNull
    @Override
    public GetCardDataResponse mapOut(@NonNull Card card) {
        return null;
    }
}
