package com.lattis.ellipse;

import android.app.Application;

import com.lattis.ellipse.Utils.FirebaseUtil;
import com.lattis.ellipse.presentation.dagger.component.ApplicationComponent;
import com.lattis.ellipse.presentation.dagger.component.DaggerApplicationComponent;
import com.lattis.ellipse.presentation.dagger.module.ApplicationModule;
import com.lattis.ellipse.presentation.dagger.module.AuthenticationModule;
import com.lattis.ellipse.presentation.dagger.module.BluetoothModule;
import com.lattis.ellipse.presentation.dagger.module.DeviceModule;
import com.lattis.ellipse.presentation.dagger.module.LocationModule;
import com.lattis.ellipse.presentation.dagger.module.NetworkModule;
import com.lattis.ellipse.presentation.dagger.module.RealmModule;
import com.lattis.ellipse.presentation.dagger.module.RepositoryModule;
import com.lattis.ellipse.presentation.dagger.module.SettingModule;
import com.lattis.ellipse.presentation.dagger.module.StripeModule;
import com.lattis.ellipse.presentation.dagger.module.UpdateTripServiceModule;
import com.lattis.ellipse.presentation.dagger.qualifier.DatabaseName;
import com.lattis.ellipse.presentation.dagger.qualifier.DatabaseSchemaVersion;
import com.mapbox.mapboxsdk.Mapbox;

import javax.inject.Inject;

import io.lattis.ellipse.R;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class Lattis extends Application {

    private ApplicationComponent applicationComponent;
    @Inject @DatabaseName
    String mDataBaseName;
    @Inject @DatabaseSchemaVersion
    int mDataSchemaVersion;

    @Override
    public void onCreate() {
        super.onCreate();
        initApplicationComponent();
        getApplicationComponent().inject(this);
        FirebaseUtil.getInstance().instantiateSDK(this);
        setupRealmDatabase();
        Mapbox.getInstance(this, getString(R.string.map_box_access_token));
    }

    private void initApplicationComponent(){
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .authenticationModule(new AuthenticationModule())
                .deviceModule(new DeviceModule())
                .realmModule(new RealmModule())
                .networkModule(new NetworkModule())
                .repositoryModule(new RepositoryModule())
                .settingModule(new SettingModule())
                .locationModule(new LocationModule())
                .stripeModule(new StripeModule())
                .bluetoothModule(new BluetoothModule(this))
                .updateTripServiceModule(new UpdateTripServiceModule(this))
                .build();
    }

    private void setupRealmDatabase() {
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name(mDataBaseName)
                .schemaVersion(mDataSchemaVersion)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(configuration);
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

}
