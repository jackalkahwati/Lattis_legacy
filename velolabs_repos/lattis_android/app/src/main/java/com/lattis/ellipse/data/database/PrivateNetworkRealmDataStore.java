package com.lattis.ellipse.data.database;

import com.lattis.ellipse.data.database.base.RealmObservable;
import com.lattis.ellipse.data.database.mapper.RealmPrivateNetworkMapper;
import com.lattis.ellipse.data.database.model.RealmPrivateNetwork;
import com.lattis.ellipse.domain.model.PrivateNetwork;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;


public class PrivateNetworkRealmDataStore {

    private RealmPrivateNetworkMapper realmPrivateNetworkMapper;

    @Inject
    public PrivateNetworkRealmDataStore(RealmPrivateNetworkMapper realmPrivateNetworkMapper) {
        this.realmPrivateNetworkMapper = realmPrivateNetworkMapper;
    }

    public Observable<PrivateNetwork> getPrivateNetwork(String id) {
        return RealmObservable.object(
                realm -> {
                    RealmPrivateNetwork realmPrivateNetwork = realm.where(RealmPrivateNetwork.class)
                        .equalTo(RealmPrivateNetwork.COLUMN_NAME_PRIVATE_FLEET_USER_ID, id)
                        .findFirst();

                    return realmPrivateNetwork != null ? realmPrivateNetwork : new RealmPrivateNetwork();
                })
                .map(realmPrivateNetwork -> realmPrivateNetworkMapper.mapOut(realmPrivateNetwork));
    }

    public Observable<List<PrivateNetwork>> savePrivateNetworkList(List<PrivateNetwork> privateNetworks) {
        if(privateNetworks==null){
            return Observable.just(new ArrayList<>());
        }
        return RealmObservable.list(
                realm -> realm.copyToRealmOrUpdate(realmPrivateNetworkMapper.mapIn(privateNetworks)))
                .map(realmPrivateNetwork -> realmPrivateNetworkMapper.mapOut(realmPrivateNetwork));
    }

    public Observable<List<PrivateNetwork>> getPrivateNetworks() {
        return RealmObservable.results(
                realm -> realm.where(RealmPrivateNetwork.class)
                        .findAll())
                .map(realmPrivateNetworks -> realmPrivateNetworkMapper.mapOut(realmPrivateNetworks));
    }

    public Observable<Boolean> removePrivateNetwork(PrivateNetwork privateNetwork){
        return RealmObservable.deleteObject(
                realm -> {
                    RealmPrivateNetwork realmPrivateNetwork = realm.where(RealmPrivateNetwork.class)
                            .equalTo(RealmPrivateNetwork.COLUMN_NAME_PRIVATE_FLEET_USER_ID, privateNetwork.getPrivate_fleet_user_id())
                            .findFirst();
                    if (realmPrivateNetwork != null) {
                        realmPrivateNetwork.deleteFromRealm();
                        return true;
                    }
                    return false;
                });
    }

}
