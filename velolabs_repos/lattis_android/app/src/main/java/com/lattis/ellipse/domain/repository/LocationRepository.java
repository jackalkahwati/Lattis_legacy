package com.lattis.ellipse.domain.repository;

import com.google.android.libraries.places.api.model.Place;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.domain.model.map.Direction;
import com.lattis.ellipse.domain.model.map.PlaceAutocomplete;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public interface LocationRepository {

    Observable<Location> getLocationUpdates();

    Observable<List<Bike>> getDistanceForBikes(Location currentLocation, List<Bike> bikes);

    Observable<LocationSettingsResult> getLocationSettings();

    Observable<ArrayList<PlaceAutocomplete>> getPlaces(String constraint);

    Observable<Place> getPlaceBuffer(String placeId);
}
