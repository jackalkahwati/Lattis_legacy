package com.lattis.ellipse.data.network.api;


import com.lattis.ellipse.data.network.model.body.bike.BikeDetailBody;
import com.lattis.ellipse.data.network.model.body.maintenance.DamageBikeBody;
import com.lattis.ellipse.data.network.model.response.BasicResponse;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import io.reactivex.Observable;

/**
 * Created by Velo Labs Android on 05-04-2017.
 */

public interface MaintenanceApi {
    @POST("maintenance/create-damage-report/")
    Observable<BasicResponse> damageBikes(@Body DamageBikeBody damageBikeBody);

    @PUT("maintenance/report-bike-theft/")
    Observable<BasicResponse> reportBikeTheft(@Body BikeDetailBody BikeDetailBody);




}
