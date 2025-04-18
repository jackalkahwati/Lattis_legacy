package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.domain.model.SearchBike;

import io.reactivex.Observable;
/**
 * Created by ssd3 on 3/21/17.
 */

public interface BikeRepository {
    Observable<SearchBike> findBikes(double latitude, double longitude );
    Observable<Ride> bookBike(Bike bike,boolean by_scan,double latitude,double longitude);
    Observable<Boolean> cancelBike(int bike_id, boolean bike_damaged, boolean lockIssue);
    Observable<Bike> bikeDetails(int bike_id, int qr_code_id);
    Observable<Boolean> updateBikeMetaData(int bike_id,int bike_battery_level, int lock_battery_level, String firmware_version, Boolean shackle_jam);
}
