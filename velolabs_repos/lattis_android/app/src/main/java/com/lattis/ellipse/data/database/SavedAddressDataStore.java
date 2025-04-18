package com.lattis.ellipse.data.database;

import com.lattis.ellipse.data.database.base.RealmObservable;
import com.lattis.ellipse.data.database.mapper.RealmSavedAddressMapper;
import com.lattis.ellipse.data.database.model.RealmSavedAddress;
import com.lattis.ellipse.domain.model.SavedAddress;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class SavedAddressDataStore {

    private RealmConfiguration realmConfiguration;
    private RealmSavedAddressMapper savedAddressMapper;

    @Inject
    public SavedAddressDataStore(io.realm.RealmConfiguration realmConfiguration,
                                 RealmSavedAddressMapper savedAddressMapper) {
        this.realmConfiguration = realmConfiguration;
        this.savedAddressMapper = savedAddressMapper;
    }

    public Observable<List<SavedAddress>> getSavedAddress() {
        return RealmObservable.results(
                realm -> realm.where(RealmSavedAddress.class).findAll())
                .map(realmSavedAddresses -> {
                    if(realmSavedAddresses!=null) {
                        return savedAddressMapper.mapOut(realmSavedAddresses);
                    }else{
                        return new ArrayList<>();
                    }
                });
    }

    public Observable<Boolean> saveSavedAddresses(List<SavedAddress> savedAddresses) {
        return RealmObservable.list(
                        realm -> realm.copyToRealmOrUpdate(savedAddressMapper.mapIn(savedAddresses)))
                .flatMap(realmSavedAddresses-> Observable.just(true));
    }


    public Observable<Boolean> deleteSavedAddress() {
        return RealmObservable.deleteObject(
                realm -> {
                    RealmSavedAddress realmSavedAddress = realm.where(RealmSavedAddress.class)
                            .findFirst();
                    if(realmSavedAddress != null){
                        realmSavedAddress.deleteFromRealm();
                        return true;
                    }
                    return false;
                });
    }
}
