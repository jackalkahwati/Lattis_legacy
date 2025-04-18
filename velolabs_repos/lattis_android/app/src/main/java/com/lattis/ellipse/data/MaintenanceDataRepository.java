package com.lattis.ellipse.data;

import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.data.network.store.MaintenanceDataStore;
import com.lattis.ellipse.domain.repository.MaintenanceRepository;

import javax.inject.Inject;

import io.reactivex.Observable;



public class MaintenanceDataRepository implements MaintenanceRepository {

    MaintenanceDataStore maintenanceDataStore;
    @Inject
    public MaintenanceDataRepository(MaintenanceDataStore maintenanceDataStore) {
        this.maintenanceDataStore = maintenanceDataStore;
    }
    @Override
    public Observable<BasicResponse> damageBikes(String category,
                                                 String riderNotes,
                                                 int bikeId, String maintenanceImage, int trip_id) {
        return maintenanceDataStore.damageBikes(category,riderNotes,bikeId,maintenanceImage,trip_id);
    }

    @Override
    public Observable<BasicResponse> reportBikeTheft(int bikeId, int trip_id) {
        return maintenanceDataStore.reportBikeTheft(bikeId,trip_id);
    }

}
