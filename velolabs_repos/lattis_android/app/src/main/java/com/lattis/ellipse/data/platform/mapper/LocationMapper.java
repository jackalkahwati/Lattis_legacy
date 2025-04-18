package com.lattis.ellipse.data.platform.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.domain.model.Location;

/**
 * Created by raverat on 2/23/17.
 */

public class LocationMapper extends AbstractDataMapper<android.location.Location,Location> {

    @NonNull
    @Override
    public Location mapIn(@NonNull android.location.Location object) {
        Location location = null;
        if (object != null) {
            location = new Location();
            location.setLatitude(object.getLatitude());
            location.setLongitude(object.getLongitude());
            location.setAccuracy(object.getAccuracy());
            location.setTime(object.getTime());
            location.setHasAccuracy(object.hasAccuracy());
            location.setHasSpeed(object.hasSpeed());
            location.setSpeed(object.getSpeed());
            location.setProvider(object.getProvider());
        }
        return location;
    }

    @NonNull
    @Override
    public android.location.Location mapOut(@NonNull Location location) {
        return null;
    }

}
