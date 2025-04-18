package com.lattis.ellipse.data.database;

import com.lattis.ellipse.data.database.base.RealmObservable;
import com.lattis.ellipse.data.database.mapper.RealmRideMapper;
import com.lattis.ellipse.data.database.model.RealmRide;
import com.lattis.ellipse.domain.model.Ride;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by ssd3 on 4/4/17.
 */

public class RideRealmDataStore {

    private RealmConfiguration realmConfiguration;
    private RealmRideMapper rideMapper;

    @Inject
    public RideRealmDataStore(RealmConfiguration realmConfiguration,
                              RealmRideMapper rideMapper) {
        this.realmConfiguration = realmConfiguration;
        this.rideMapper = rideMapper;
    }

    public Observable<Ride> getRide() {
        return RealmObservable.object(
                realm -> {
                    RealmRide realmRide = realm.where(RealmRide.class)
                        .findFirst();
                    return realmRide!=null ? realmRide : new RealmRide();

                })
                .map(realmRide -> rideMapper.mapOut(realmRide));
    }

    public Observable<Ride> createOrUpdateUser(Ride ride) {
        return RealmObservable.object(
                new Function<Realm, RealmRide>() {
                    @Override
                    public RealmRide apply(Realm realm) {
                        return realm.copyToRealmOrUpdate(rideMapper.mapIn(ride));
                    }
                })
                .map(new Function<RealmRide, Ride>() {
                    @Override
                    public Ride apply(RealmRide realmRide) {
                        return rideMapper.mapOut(realmRide);
                    }
                });
    }


    public Observable<Boolean> deleteRide() {
        return RealmObservable.deleteObject(
                realm -> {
                    RealmRide realmRide = realm.where(RealmRide.class)
                            .findFirst();
                    if(realmRide != null){
                        realmRide.deleteFromRealm();
                        return true;
                    }
                    return false;
                });
    }
}
