package com.lattis.ellipse.data.database.base;

import androidx.annotation.NonNull;

public class RealmStringMapper extends AbstractRealmDataMapper<String,RealmString> {

    public RealmStringMapper() { }

    @NonNull
    @Override
    public RealmString mapIn(@NonNull String string) {
        return new RealmString(string);
    }

    @NonNull
    @Override
    public String mapOut(@NonNull RealmString realmString) {
        return realmString.getValue();
    }
}
