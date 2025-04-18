package com.lattis.ellipse.data.network.store;

import com.lattis.ellipse.data.network.api.MaintenanceApi;
import com.lattis.ellipse.data.network.model.body.bike.BikeDetailBody;
import com.lattis.ellipse.data.network.model.body.maintenance.DamageBikeBody;
import com.lattis.ellipse.data.network.model.response.BasicResponse;

import javax.inject.Inject;

import io.reactivex.Observable;



public class MaintenanceDataStore {
    private MaintenanceApi maintenanceApi;
    @Inject
    MaintenanceDataStore(MaintenanceApi maintenanceApi)
    {

        this.maintenanceApi = maintenanceApi;
    }
    public Observable<BasicResponse> damageBikes(String category,
                                                 String riderNotes,
                                                 int bikeId, String maintenanceImage, int trip_id){
        return this.maintenanceApi.damageBikes(new DamageBikeBody(category,riderNotes,bikeId,maintenanceImage,trip_id));
    }
    public Observable<BasicResponse> reportBikeTheft(int bikeId,int trip_id){
        return this.maintenanceApi.reportBikeTheft(new BikeDetailBody(bikeId,-1, trip_id));
    }

}
