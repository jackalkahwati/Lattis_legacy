package com.lattis.ellipse.data.database.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.database.base.AbstractRealmDataMapper;
import com.lattis.ellipse.data.database.model.RealmPrivateNetwork;
import com.lattis.ellipse.domain.model.PrivateNetwork;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.RealmResults;

/**
 * Created by ssd3 on 5/8/17.
 */

public class RealmPrivateNetworkMapper extends AbstractRealmDataMapper<PrivateNetwork,RealmPrivateNetwork> {

    @Inject
    public RealmPrivateNetworkMapper() {}

    @NonNull
    @Override
    public RealmPrivateNetwork mapIn(@NonNull PrivateNetwork  privateNetwork) {
        RealmPrivateNetwork realmPrivateNetwork = new RealmPrivateNetwork();
        realmPrivateNetwork.setPrivate_fleet_user_id(privateNetwork.getPrivate_fleet_user_id());
        realmPrivateNetwork.setUserId(privateNetwork.getUserId());
        realmPrivateNetwork.setEmail(privateNetwork.getEmail());
        realmPrivateNetwork.setFleet_id(privateNetwork.getFleet_id());
        realmPrivateNetwork.setVerified(privateNetwork.getVerified());
        realmPrivateNetwork.setFleet_name(privateNetwork.getFleet_name());
        realmPrivateNetwork.setType(privateNetwork.getType());
        realmPrivateNetwork.setLogo(privateNetwork.getLogo());
        return realmPrivateNetwork;
    }

    @NonNull
    @Override
    public PrivateNetwork mapOut(@NonNull RealmPrivateNetwork realmPrivateNetwork) {
        PrivateNetwork privateNetwork = new PrivateNetwork();
        privateNetwork.setPrivate_fleet_user_id(realmPrivateNetwork.getPrivate_fleet_user_id());
        privateNetwork.setUserId(realmPrivateNetwork.getUserId());
        privateNetwork.setEmail(realmPrivateNetwork.getEmail());
        privateNetwork.setFleet_id(realmPrivateNetwork.getFleet_id());
        privateNetwork.setVerified(realmPrivateNetwork.getVerified());
        privateNetwork.setFleet_name(realmPrivateNetwork.getFleet_name());
        privateNetwork.setType(realmPrivateNetwork.getType());
        privateNetwork.setLogo(realmPrivateNetwork.getLogo());
        return privateNetwork;
    }


    @NonNull
    @Override
    public List<PrivateNetwork> mapOut(@NonNull RealmResults<RealmPrivateNetwork> realmPrivateNetworks) {
        List<PrivateNetwork> privateNetworks = new ArrayList<>();
        for (RealmPrivateNetwork realmPrivateNetwork : realmPrivateNetworks) {
            privateNetworks.add(mapOut(realmPrivateNetwork));
        }
        return privateNetworks;
    }
}
