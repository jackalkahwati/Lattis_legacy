package com.lattis.data.mapper

import com.lattis.data.entity.response.card.GetCardDataResponse
import com.lattis.domain.models.Card
import javax.inject.Inject


class CardResponseMapper @Inject
constructor() : AbstractDataMapper<GetCardDataResponse, Card>() {

    override fun mapIn(getCardDataResponse: GetCardDataResponse?): Card {
        val card = Card()
        getCardDataResponse?.let {
            card.id = getCardDataResponse.id
            card.user_id = getCardDataResponse.user_id
            card.stripe_net_profile_id = getCardDataResponse.stripe_net_profile_id
            card.stripe_net_payment_id = getCardDataResponse.stripe_net_payment_id
            card.is_primary = getCardDataResponse.is_primary
            card.type_card = getCardDataResponse.type_card
            card.cc_no = getCardDataResponse.cc_no
            card.exp_month = getCardDataResponse.exp_month
            card.exp_year = getCardDataResponse.exp_year
            card.fingerprint = getCardDataResponse.fingerprint
            card.cc_type = getCardDataResponse.cc_type
            card.created_date = getCardDataResponse.created_date
            card.last_updated = getCardDataResponse.last_updated
            card.card_id = getCardDataResponse.card_id
        }
        return card

    }

    override fun mapOut(card: Card?): GetCardDataResponse? {
        return null
    }
}
