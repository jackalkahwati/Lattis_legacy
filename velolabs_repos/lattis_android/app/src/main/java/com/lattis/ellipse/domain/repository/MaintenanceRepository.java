package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.data.network.model.response.BasicResponse;

import io.reactivex.Observable;


public interface MaintenanceRepository {
    Observable<BasicResponse> damageBikes(String category,
                                          String riderNotes,
                                          int bikeId, String maintenanceImage, int trip_id);
    Observable<BasicResponse> reportBikeTheft(int bikeId, int trip_id);

}
