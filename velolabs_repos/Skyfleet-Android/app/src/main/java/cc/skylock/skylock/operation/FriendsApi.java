package cc.skylock.skylock.operation;

import cc.skylock.skylock.Bean.EmergenceContact;
import cc.skylock.skylock.Bean.SuccessResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by prabhu on 3/1/16.
 */
public interface FriendsApi {


    @POST("locks/send-emergency-message/")
    public  Call<SuccessResponse>  EmergencyContactsSendaAlert(@Body EmergenceContact emergenceContact);


}
