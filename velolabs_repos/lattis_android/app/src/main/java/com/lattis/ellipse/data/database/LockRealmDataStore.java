package com.lattis.ellipse.data.database;

import com.lattis.ellipse.data.database.base.RealmObservable;
import com.lattis.ellipse.data.database.mapper.RealmLockMapper;
import com.lattis.ellipse.data.database.model.RealmLock;
import com.lattis.ellipse.domain.model.Lock;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class LockRealmDataStore {

    private RealmLockMapper lockMapper;
    private RealmConfiguration realmConfiguration;

    @Inject
    LockRealmDataStore(RealmConfiguration realmConfiguration, RealmLockMapper lockMapper) {
        this.lockMapper = lockMapper;
        this.realmConfiguration = realmConfiguration;
    }


    public Observable<Lock> createOrUpdateLock(Lock lock) {
        return RealmObservable.object(
                new Function<Realm, RealmLock>() {
                    @Override
                    public RealmLock apply(Realm realm) {
                        return realm.copyToRealmOrUpdate(lockMapper.mapIn(lock));
                    }
                })
                .map(new Function<RealmLock, Lock>() {
                    @Override
                    public Lock apply(RealmLock realmlock) {
                        return lockMapper.mapOut(realmlock);
                    }
                });
    }

    public Observable<Lock> getLock() {
        return RealmObservable.object(
                realm -> realm.where(RealmLock.class)
                        .findFirst())
                .map(realmLock -> lockMapper.mapOut(realmLock));
    }


    public Observable<Boolean> deleteLock() {
        return RealmObservable.deleteObject(
                realm -> {
                    RealmLock realmLock = realm.where(RealmLock.class)
                            .findFirst();
                    if(realmLock != null){
                        realmLock.deleteFromRealm();
                        return true;
                    }
                    return false;
                });
    }



}
