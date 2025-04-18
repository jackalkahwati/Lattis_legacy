package cc.skylock.skylock.operation;

import cc.skylock.skylock.Bean.AcceptSharing;
import cc.skylock.skylock.Bean.ConfirmTheftParameter;
import cc.skylock.skylock.Bean.CrashAndTheftParameter;
import cc.skylock.skylock.Bean.CrashResponse;
import cc.skylock.skylock.Bean.FirmwareUpdates;
import cc.skylock.skylock.Bean.LockKeyGen;
import cc.skylock.skylock.Bean.LockList;
import cc.skylock.skylock.Bean.LockMessagesResponse;
import cc.skylock.skylock.Bean.PasswordHintParameter;
import cc.skylock.skylock.Bean.SetFWVersion;
import cc.skylock.skylock.Bean.TheftResponse;
import cc.skylock.skylock.Bean.UpdateLockNameResponse;
import cc.skylock.skylock.Bean.UpdateLockNameParameter;
import cc.skylock.skylock.Bean.SendMacIdAsParameter;
import cc.skylock.skylock.Bean.ShareLockRequest;
import cc.skylock.skylock.Bean.ShareLockResponse;
import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.Bean.TouchPadSequence;
import cc.skylock.skylock.Bean.UnShareLockRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;


/**
 * Created by prabhu on 1/30/16.
 */
public interface LockWebServiceApi {

    @GET(" locks/users-locks/")
    public Call<LockList> GetLockData();

    @POST("locks/registration/")
    public Call<LockKeyGen> AddLock(@Body SendMacIdAsParameter macKey);

    @POST("locks/signed-message-and-public-key/")
    public Call<LockMessagesResponse> GetLocksignedAndPublicMessage(@Body SendMacIdAsParameter macKey);

    @POST("locks/save-pin-code/")
    public Call<SuccessResponse> SaveTouchPinCode(@Body TouchPadSequence macKey);

    @POST("locks/delete-lock/")
    public Call<SuccessResponse> DeleteLock(@Body SendMacIdAsParameter sendMacIdAsParameter);

    @GET(" locks/firmware-versions/")
    public Call<FirmwareUpdates> GetLatestFirmwareVersion();

    @POST(" locks/firmware-log/")
    public Call<SuccessResponse> GetFirmwareUpdationInfo();

    @POST("locks/firmware/")
    public Call<FirmwareUpdates> GetFirmwareUpdates();

    @POST("locks/update-lock/")
    public Call<UpdateLockNameResponse> AddLockName(@Body UpdateLockNameParameter updateLockNameParameter);

    @POST("locks/confirm-theft/")
    public Call<SuccessResponse> ConfirmationTheft(@Body ConfirmTheftParameter confirmTheftParameter);

    @POST("locks/theft-detected/")
    public Call<TheftResponse> TheftDetection(@Body CrashAndTheftParameter confirmTheftParameter);


    @POST("locks/crash-detected/")
    public Call<CrashResponse> CrashDetection(@Body CrashAndTheftParameter confirmTheftParameter);


    @POST("locks/share")
    public Call<ShareLockResponse> ShareLock(@Body ShareLockRequest shareLockRequest);

    @POST("locks/revoke-sharing/")
    public Call<SuccessResponse> RevokeshareLock(@Body UnShareLockRequest unShareLockRequest);

    @POST("locks/share-confirmation/")
    public Call<AcceptSharing> AcceptSharing(@Body PasswordHintParameter passwordHintParameter);


    @POST("")
    public Call<String> getRouteOfYouLock();


}
