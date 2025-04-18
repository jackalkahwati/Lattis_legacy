package com.lattis.ellipse.data.database.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.database.base.AbstractRealmDataMapper;
import com.lattis.ellipse.data.database.model.RealmLocation;
import com.lattis.ellipse.domain.model.Location;

import javax.inject.Inject;

public class RealmLocationMapper extends AbstractRealmDataMapper<Location,RealmLocation> {

    @Inject
    public RealmLocationMapper() {}

    @NonNull
    @Override
    public RealmLocation mapIn(@NonNull Location location) {
        RealmLocation realmLocation = new RealmLocation();
        location.setLatitude(location.getLatitude());
        location.setLongitude(location.getLatitude());
        return realmLocation;
    }

    @NonNull
    @Override
    public Location mapOut(@NonNull RealmLocation realmLocation) {
        Location location = new Location();
        location.setLatitude(realmLocation.getLatitude());
        location.setLongitude(realmLocation.getLongitude());
        return location;
    }
}
