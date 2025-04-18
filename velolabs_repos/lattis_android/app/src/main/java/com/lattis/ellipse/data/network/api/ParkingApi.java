package com.lattis.ellipse.data.network.api;

import com.lattis.ellipse.data.network.model.body.parking.FindParkingBody;
import com.lattis.ellipse.data.network.model.body.parking.GetParkingFeeForFleetBody;
import com.lattis.ellipse.data.network.model.body.parking.GetParkingZoneBody;
import com.lattis.ellipse.data.network.model.response.parking.FindParkingResponse;
import com.lattis.ellipse.data.network.model.response.parking.GetParkingFeeForFleetResponse;
import com.lattis.ellipse.data.network.model.response.parking.GetParkingZoneRepsonse;

import retrofit2.http.Body;
import retrofit2.http.POST;
import io.reactivex.Observable;

public interface ParkingApi {

    @POST("parking/get-parking-spots/")
    Observable<FindParkingResponse> findParkings(@Body FindParkingBody findParkingBody);

    @POST("parking/get-parking-zones-for-fleet/")
    Observable<GetParkingZoneRepsonse> getParkingZone(@Body GetParkingZoneBody getParkingZoneBody);

    @POST("parking/get-parking-spots-for-fleet/")
    Observable<FindParkingResponse> getParkingSpotForFleet(@Body GetParkingZoneBody getParkingZoneBody);

    @POST("fleet/check-parking-fee/")
    Observable<GetParkingFeeForFleetResponse> getParkingFeeForFleet(@Body GetParkingFeeForFleetBody getParkingFeeForFleetBody);

}
