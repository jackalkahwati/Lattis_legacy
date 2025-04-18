package com.lattis.lattis.infrastructure.di.module

import android.content.Context
import com.lattis.data.repository.implementation.api.DataBaseRepositoryImp
import com.lattis.domain.repository.DataBaseRepository
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Named
import javax.inject.Singleton

@Module
class RealmModule {
    @Provides
    @Singleton
    @Named("DatabaseName")
    fun provideDatabaseName(): String {
        return DATABASE_NAME
    }

    @Provides
    @Singleton
    @Named("DatabaseSchemaVersion")
    fun provideDatabaseSchemaVersion(): Int {
        return DATABASE_SCHEMA_VERSION
    }

    @Provides
    @Singleton
    fun provideRealmConfiguration(): RealmConfiguration {
        val realm = Realm.getDefaultInstance()
        val configuration = realm.configuration
        realm.close()
        return configuration
    }

    @Provides
    @Singleton
    @Named("UUID")
    fun provideFleetId(): String {
        return UUID
    }

    @Provides
    @Singleton
    fun provideDataBaseRepository(context: Context, @Named("DatabaseName") databaseName: String, @Named("DatabaseSchemaVersion") schemaVersion: Int): DataBaseRepository{
        return DataBaseRepositoryImp(context!!, databaseName!!, schemaVersion)
    }

    companion object {
        private const val DATABASE_NAME = "lattis.db"
        private const val UUID = "LATTIS_APP"
        private const val DATABASE_SCHEMA_VERSION = 1
    }
}