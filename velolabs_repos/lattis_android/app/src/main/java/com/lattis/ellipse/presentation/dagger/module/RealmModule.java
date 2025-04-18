package com.lattis.ellipse.presentation.dagger.module;

import android.content.Context;

import com.lattis.ellipse.data.database.DatabaseManager;
import com.lattis.ellipse.domain.repository.DataBaseManager;
import com.lattis.ellipse.presentation.dagger.qualifier.DatabaseName;
import com.lattis.ellipse.presentation.dagger.qualifier.DatabaseSchemaVersion;
import com.lattis.ellipse.presentation.dagger.qualifier.FleetId;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;

@Module
public class RealmModule {

    private static final String DATABASE_NAME = "ellipse.db";
    private static final String FLEET_ID = "9";
    private static final int DATABASE_SCHEMA_VERSION = 1;

    @Provides
    @Singleton
    @DatabaseName
    String provideDatabaseName() {
        return DATABASE_NAME;
    }

    @Provides
    @Singleton
    @DatabaseSchemaVersion
    int provideDatabaseSchemaVersion() {
        return DATABASE_SCHEMA_VERSION;
    }

    @Provides
    @Singleton
    RealmConfiguration provideRealmConfiguration() {
        Realm realm = Realm.getDefaultInstance();
        RealmConfiguration configuration = realm.getConfiguration();
        realm.close();
        return configuration;
    }

    @Provides
    @Singleton
    @FleetId
    String provideFleetId(){
        return FLEET_ID;
    }

    @Provides
    @Singleton
    DataBaseManager provideDatabaseManager(Context context, @DatabaseName String databaseName, @DatabaseSchemaVersion int schemaVersion){
        return new DatabaseManager(context, databaseName, schemaVersion);
    }


}