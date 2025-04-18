package com.lattis.ellipse.data.database.base;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmModel;
import io.realm.RealmResults;

public abstract class AbstractRealmDataMapper<IN,OUT extends RealmModel> {


    @NonNull
    public List<OUT> mapIn(@NonNull List<IN> objects) {
        List<OUT> realmObjects = new ArrayList<>();
        for (IN object : objects) {
            realmObjects.add(mapIn(object));
        }
        return realmObjects;
    }

    @NonNull
    public abstract OUT mapIn(@NonNull IN object);

    @NonNull
    public List<IN> mapOut(@NonNull List<OUT> realmObjectList) {
        List<IN> objects = new ArrayList<>();
        for (OUT realObject : realmObjectList) {
            objects.add(mapOut(realObject));
        }
        return objects;
    }

    @NonNull
    public List<IN> mapOut(@NonNull RealmResults<OUT> realmObjectList) {
        List<IN> objects = new ArrayList<>();
        for (OUT realObject : realmObjectList) {
            objects.add(mapOut(realObject));
        }
        return objects;
    }

    @NonNull
    public abstract IN mapOut(@NonNull OUT realmObject);
}
