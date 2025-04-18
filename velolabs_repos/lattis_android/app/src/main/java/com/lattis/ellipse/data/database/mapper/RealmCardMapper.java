package com.lattis.ellipse.data.database.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.database.base.AbstractRealmDataMapper;
import com.lattis.ellipse.data.database.model.RealmCard;
import com.lattis.ellipse.domain.model.Card;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.RealmResults;

/**
 * Created by ssd3 on 7/26/17.
 */

public class RealmCardMapper  extends AbstractRealmDataMapper<Card,RealmCard> {

    @Inject
    public RealmCardMapper() {}

    @NonNull
    @Override
    public RealmCard mapIn(@NonNull Card card) {
        RealmCard realmCard = new RealmCard();
        realmCard.setId(card.getId());
        realmCard.setUser_id(card.getUser_id());
        realmCard.setStripe_net_profile_id(card.getStripe_net_profile_id());
        realmCard.setStripe_net_payment_id(card.getStripe_net_payment_id());
        realmCard.setIs_primary(card.getIs_primary());
        realmCard.setType_card(card.getType_card());
        realmCard.setCc_no(card.getCc_no());
        realmCard.setExp_month(card.getExp_month());
        realmCard.setExp_year(card.getExp_year());
        realmCard.setFingerprint(card.getFingerprint());
        realmCard.setCc_type(card.getCc_type());
        realmCard.setCreated_date(card.getCreated_date());
        realmCard.setLast_updated(card.getLast_updated());
        return realmCard;
    }

    @NonNull
    @Override
    public Card mapOut(@NonNull RealmCard realmCard) {
        Card card = new Card();
        card.setId(realmCard.getId());
        card.setUser_id(realmCard.getUser_id());
        card.setStripe_net_profile_id(realmCard.getStripe_net_profile_id());
        card.setStripe_net_payment_id(realmCard.getStripe_net_payment_id());
        card.setIs_primary(realmCard.getIs_primary());
        card.setType_card(realmCard.getType_card());
        card.setCc_no(realmCard.getCc_no());
        card.setExp_month(realmCard.getExp_month());
        card.setExp_year(realmCard.getExp_year());
        card.setFingerprint(realmCard.getFingerprint());
        card.setCc_type(realmCard.getCc_type());
        card.setCreated_date(realmCard.getCreated_date());
        card.setLast_updated(realmCard.getLast_updated());
        return card;
    }

    @NonNull
    @Override
    public List<Card> mapOut(@NonNull RealmResults<RealmCard> realmCards) {
        List<Card> contactList = new ArrayList<>();
        for (RealmCard realmCard : realmCards) {
            contactList.add(mapOut(realmCard));
        }
        return contactList;
    }



}
