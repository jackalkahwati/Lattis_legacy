package com.lattis.ellipse.data.database;

import android.content.Context;

import com.lattis.ellipse.domain.repository.DataBaseManager;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DatabaseManager implements DataBaseManager {

    private Context context;
    private String databaseName;
    private int schemaVersion;

    @Inject
    public DatabaseManager(Context context, String databaseName, int schemaVersion) {
        this.context = context;
        this.databaseName = databaseName;
        this.schemaVersion = schemaVersion;
    }

    @Override
    public Observable<Void> createDataBase() {
        return Observable.create(emitter -> {
            Realm.init(context);
            Realm.setDefaultConfiguration(getConfiguration());
            emitter.onNext(null);
        });
    }

    @Override
    public Observable<Boolean> deleteDataBase() {
        return Observable.create(emitter -> emitter.onNext(Realm.deleteRealm(getConfiguration())));
    }

    private RealmConfiguration getConfiguration(){
        return new RealmConfiguration.Builder()
                .name(databaseName)
                .schemaVersion(schemaVersion)
                .deleteRealmIfMigrationNeeded()
                .build();
    }
}
