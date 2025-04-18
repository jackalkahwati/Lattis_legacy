package com.lattis.ellipse.data.database.base;

import java.util.List;

import io.reactivex.functions.Function;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.reactivex.Observable;

public final class RealmObservable {

    public static <RObject extends RealmObject> Observable<RObject> object(final Function<Realm, RObject> function) {
        return Observable.create(new OnSubscribeRealm<RObject>() {
            @Override
            public RObject get(Realm realm) throws Exception {
                return function.apply(realm);
            }
        });
    }

    public static <RObject extends Boolean> Observable<RObject> deleteObject(final Function<Realm, RObject> function) {
        return Observable.create(new OnSubscribeRealm<RObject>() {
            @Override
            public RObject get(Realm realm) throws Exception {
                return function.apply(realm);
            }
        });
    }

    public static <RObject extends RealmObject> Observable<List<RObject>> list(final Function<Realm, List<RObject>> function) {
        return Observable.create(new OnSubscribeRealm<List<RObject>>() {
            @Override
            public List<RObject> get(Realm realm) throws Exception {
                return function.apply(realm);
            }
        });
    }

    public static <RObject extends RealmObject> Observable<RealmResults<RObject>> results(final Function<Realm, RealmResults<RObject>> function) {
        return Observable.create(new OnSubscribeRealm<RealmResults<RObject>>() {
            @Override
            public RealmResults<RObject> get(Realm realm) throws Exception {
                return function.apply(realm);
            }
        });
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
//import io.reactivex.Observable;
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