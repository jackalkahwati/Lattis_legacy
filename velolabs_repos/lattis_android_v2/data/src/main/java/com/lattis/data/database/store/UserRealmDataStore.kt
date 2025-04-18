package com.lattis.data.database.store

import com.lattis.data.database.base.RealmObservable
import com.lattis.data.database.mapper.RealmUserMapper
import com.lattis.data.database.model.RealmUser
import com.lattis.domain.models.User
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject
class UserRealmDataStore @Inject
constructor(private val userMapper: RealmUserMapper) {

    val user: Observable<User>
        get() = RealmObservable.`object`<RealmUser>(
                Function{ realm ->
                    val realmUser = realm.where(RealmUser::class.java)
                        .findFirst()
                    if (realmUser != null) realmUser else RealmUser()
                })
            .map { realmUser -> userMapper.mapOut(realmUser) }

    fun createOrUpdateUser(user: User): Observable<User> {
        return RealmObservable.`object`(
                Function<Realm, RealmUser> { realm -> realm.copyToRealmOrUpdate(userMapper.mapIn(user)) })
            .map { realmUser -> userMapper.mapOut(realmUser) }
    }
}