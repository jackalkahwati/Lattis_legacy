package com.lattis.data.database.store

import com.lattis.data.database.base.RealmObservable
import com.lattis.data.database.mapper.RealmRideMapper
import com.lattis.data.database.model.RealmRide
import com.lattis.domain.models.Ride
import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function
import io.realm.Realm
import io.realm.RealmConfiguration


/**
 * Created by ssd3 on 4/4/17.
 */

class RideRealmDataStore @Inject
constructor(private val realmConfiguration: RealmConfiguration,
            private val rideMapper: RealmRideMapper
) {

    fun getRide(): Observable<Ride> {
        return RealmObservable.`object`<RealmRide>(
            Function { realm ->
                val realmRide = realm.where(RealmRide::class.java)
                    .findFirst()
                if (realmRide != null) realmRide else RealmRide()

            })
            .map { realmRide -> rideMapper.mapOut(realmRide) }
    }

    fun createOrUpdateUser(ride: Ride): Observable<Ride> {
        return RealmObservable.`object`(
                Function<Realm, RealmRide> { realm -> realm.copyToRealmOrUpdate(rideMapper.mapIn(ride)) })
                .map { realmRide -> rideMapper.mapOut(realmRide) }
    }


    fun deleteRide(): Observable<Boolean> {
        return RealmObservable.deleteObject(
                Function{ realm ->
                    val realmRide = realm.where(RealmRide::class.java)
                            .findFirst()
                    if (realmRide != null) {
                        realmRide!!.deleteFromRealm()
                        true
                    }
                    false
                })
    }
}
