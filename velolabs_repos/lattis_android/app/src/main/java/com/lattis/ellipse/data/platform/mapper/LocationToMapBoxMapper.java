package com.lattis.ellipse.data.platform.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.domain.model.Location;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ssd3 on 3/29/17.
 */

public class LocationToMapBoxMapper extends AbstractDataMapper<Location,LatLng> {

    @NonNull
    @Override
    public List<LatLng> mapIn(@NonNull List<Location> locations) {
        List<LatLng> latLngs = new ArrayList<>() ;
        for(Location location : locations){
            latLngs.add(new LatLng(location.getLatitude(),location.getLongitude()));
        }
        return  latLngs;
    }

    @NonNull
    @Override
    public LatLng mapIn(@NonNull Location location) {
        return null;
    }

    @NonNull
    @Override
    public List<LatLng> mapIn(@NonNull Location[] locations) {
        return super.mapIn(locations);
    }

    @NonNull
    @Override
    public Location mapOut(@NonNull LatLng latLng) {
        return null;
    }

    @NonNull
    @Override
    public List<Location> mapOut(@NonNull LatLng[] latLngs) {
        return super.mapOut(latLngs);
    }

    @NonNull
    @Override
    public List<Location> mapOut(@NonNull List<LatLng> latLngs) {
        return null;
    }

}
