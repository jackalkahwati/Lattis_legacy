package com.lattis.data.database.base

import io.reactivex.rxjava3.functions.Function
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.reactivex.rxjava3.core.Observable

object RealmObservable {

    fun <RObject : RealmObject> `object`(function: Function<Realm, RObject>): Observable<RObject> {
        return Observable.create(object : OnSubscribeRealm<RObject>() {
            @Throws(Exception::class)
            override fun get(realm: Realm): RObject {
                return function.apply(realm)
            }
        })
    }

    fun <RObject : Boolean> deleteObject(function: Function<Realm, RObject>): Observable<RObject> {
        return Observable.create(object : OnSubscribeRealm<RObject>() {
            @Throws(Exception::class)
            override fun get(realm: Realm): RObject {
                return function.apply(realm)
            }
        })
    }

    fun <RObject : RealmObject> list(function: Function<Realm, List<RObject>>): Observable<List<RObject>> {
        return Observable.create(object : OnSubscribeRealm<List<RObject>>() {
            @Throws(Exception::class)
            override fun get(realm: Realm): List<RObject> {
                return function.apply(realm)
            }
        })
    }

    fun <RObject : RealmObject> results(function: Function<Realm, RealmResults<RObject>>): Observable<RealmResults<RObject>> {
        return Observable.create(object : OnSubscribeRealm<RealmResults<RObject>>() {
            @Throws(Exception::class)
            override fun get(realm: Realm): RealmResults<RObject> {
                return function.apply(realm)
            }
        })
    }
}


//package com.lattis.ellipse.data.database.base;
//
//import java.util.List;
//
//import io.realm.Realm;
//import io.realm.RealmConfiguration;
//import io.realm.RealmObject;
//import io.realm.RealmResults;
//import io.reactivex.rxjava3.core.Observable;
//import rx.functions.Func1;
//
//public final class RealmObservable {
//
//
//
//    public static <T extends RealmObject> Observable<T> object(final Func1<Realm, T> function) {
//        return Observable.create(new OnSubscribeRealm<T>() {
//            @Override
//            public T get(Realm realm) {
//                return function.call(realm);
//            }
//        });
//    }
//
//    public static <T extends RealmObject> Observable<T> object(RealmConfiguration configuration,
//                                                               final Func1<Realm, T> function) {
//        return Observable.create(new OnSubscribeRealm<T>(configuration) {
//            @Override
//            public T get(Realm realm) {
//                return function.call(realm);
//            }
//        });
//    }
//
//    public static <T extends RealmObject> Observable<List<T>> list(RealmConfiguration configuration,final Func1<Realm, List<T>> function) {
//        return Observable.create(new OnSubscribeRealm<List<T>>(configuration) {
//            @Override
//            public List<T> get(Realm realm) {
//                return function.call(realm);
//            }
//        });
//    }
//
//    public static <T extends RealmObject> Observable<RealmResults<T>> results(RealmConfiguration configuration,
//                                                                              final Func1<Realm, RealmResults<T>> function) {
//        return Observable.create(new OnSubscribeRealm<RealmResults<T>>(configuration) {
//            @Override
//            public RealmResults<T> get(Realm realm) {
//                return function.call(realm);
//            }
//        });
//    }
//
//    public static <T extends Boolean> Observable<T> deleteObject(RealmConfiguration configuration, final Func1<Realm, T> function) {
//        return Observable.create(new OnSubscribeRealm<T>(configuration) {
//            @Override
//            public T get(Realm realm) {
//                return function.call(realm);
//            }
//        });
//    }
//
//}