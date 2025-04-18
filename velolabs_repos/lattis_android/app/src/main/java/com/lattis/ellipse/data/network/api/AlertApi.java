package com.lattis.ellipse.data.network.api;

import com.lattis.ellipse.data.network.model.body.alert.ConfirmTheftBody;
import com.lattis.ellipse.data.network.model.body.alert.SendEmergencyAlertBody;
import com.lattis.ellipse.data.network.model.response.BasicResponse;


import retrofit2.http.Body;
import retrofit2.http.POST;
import io.reactivex.Observable;

public interface AlertApi {

    @POST("locks/send-emergency-message/")
    Observable<BasicResponse> sendAlertMessageToEmergencyContacts(@Body SendEmergencyAlertBody sendEmergencyAlertBody);

    @POST("locks/confirm-theft/")
    Observable<BasicResponse> confirmTheft(@Body ConfirmTheftBody confirmTheftBody);


}
