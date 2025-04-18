package com.lattis.ellipse.data.database;

import com.lattis.ellipse.data.database.base.RealmObservable;
import com.lattis.ellipse.data.database.mapper.RealmUserMapper;
import com.lattis.ellipse.data.database.model.RealmUser;
import com.lattis.ellipse.domain.model.User;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.realm.Realm;
import io.realm.RealmConfiguration;


public class UserRealmDataStore {

    private RealmConfiguration realmConfiguration;
    private RealmUserMapper userMapper;

    @Inject
    public UserRealmDataStore(RealmConfiguration realmConfiguration,
                              RealmUserMapper userMapper) {
        this.realmConfiguration = realmConfiguration;
        this.userMapper = userMapper;
    }

    public Observable<User> getUser() {
        return RealmObservable.object(
                realm -> {
                    RealmUser realmUser = realm.where(RealmUser.class)
                        .findFirst();
                    return realmUser!=null ? realmUser : new RealmUser();
                })
                .map(realmUser -> userMapper.mapOut(realmUser));
    }

    public Observable<User> createOrUpdateUser(User user) {
        return RealmObservable.object(
                new Function<Realm, RealmUser>() {
                    @Override
                    public RealmUser apply(Realm realm) {
                        return realm.copyToRealmOrUpdate(userMapper.mapIn(user));
                    }
                })
                .map(new Function<RealmUser, User>() {
                    @Override
                    public User apply(RealmUser realmUser) {
                        return userMapper.mapOut(realmUser);
                    }
                });
    }
}
