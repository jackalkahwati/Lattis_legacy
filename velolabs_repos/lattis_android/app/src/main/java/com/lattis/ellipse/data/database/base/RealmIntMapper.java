package com.lattis.ellipse.data.database.base;

import androidx.annotation.NonNull;

public class RealmIntMapper extends AbstractRealmDataMapper<Integer,RealmInt> {

    public RealmIntMapper() { }

    @NonNull
    @Override
    public RealmInt mapIn(@NonNull Integer integer) {
        return new RealmInt(integer);
    }

    @NonNull
    @Override
    public Integer mapOut(@NonNull RealmInt realmInt) {
        return realmInt.getValue();
    }
}
