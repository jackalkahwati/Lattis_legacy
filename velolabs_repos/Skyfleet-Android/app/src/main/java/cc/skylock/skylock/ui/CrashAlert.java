package cc.skylock.skylock.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

import cc.skylock.skylock.Bean.EmergenceContact;
import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.R;
import cc.skylock.skylock.operation.FriendsApi;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.service.LocationService;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin on 10/09/16.
 */
public class CrashAlert extends Activity {

    CardView sendEmergencyCall;
    TextView cancelEmergencyCall;
    TextView alertMyContacts, label1, label2;
    PrefUtil mPrefUtil;
    Context mContext;
    int delay = 1000; //milliseconds
    private static Handler passwordHandler = new Handler();
    private static Runnable runnable;
    TextView callTimer, seconds;
    boolean stopEmergencyCall = false;
    int carshAlertTimer = 30;
    ProgressBar mProgressBar;
    int colorapptheme = 0;
    final int carshID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_crash);
        mContext = this;
        mPrefUtil = new PrefUtil(mContext);
        colorapptheme = ResourcesCompat.getColor(getResources(), R.color.app_background, null);
        alertMyContacts = (TextView) findViewById(R.id.textView_label_locatemybike);
        callTimer = (TextView) findViewById(R.id.tv_crash_timing);
        sendEmergencyCall = (CardView) findViewById(R.id.cv_locatemybike);
        cancelEmergencyCall = (TextView) findViewById(R.id.tv_crash_got_it);
        label1 = (TextView) findViewById(R.id.tv_crash_description_2);
        label2 = (TextView) findViewById(R.id.tv_crash_description_1);
        mProgressBar = (ProgressBar) findViewById(R.id.crash_progressBar);
        seconds = (TextView) findViewById(R.id.tv_status);
        mProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.crash_progress_background, null));
        changeStatusBarColor(colorapptheme);
        callTimer.setTypeface(UtilHelper.getTypface(mContext));
        cancelEmergencyCall.setTypeface(UtilHelper.getTypface(mContext));
        label1.setTypeface(UtilHelper.getTypface(mContext));
        label2.setTypeface(UtilHelper.getTypface(mContext));
        alertMyContacts.setTypeface(UtilHelper.getTypface(mContext));
        seconds.setTypeface(UtilHelper.getTypface(mContext));
        sendEmergencyCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emergencyCall(mContext);
                //   confirmCrashCall();
                cancelNotification(CrashAlert.this, SkylockConstant.NOTIFICATION_ID + carshID);
                stopEmergencyCall = true;
                finish();
            }

        });
        cancelEmergencyCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelNotification(CrashAlert.this, SkylockConstant.NOTIFICATION_ID + carshID);
                stopEmergencyCall = true;
                finish();
            }
        });
        passwordHandler.postDelayed(runnable = new Runnable() {
            public void run() {
                if (!stopEmergencyCall) {
                    if (carshAlertTimer >= 0) {
                        callTimer.setText("" + carshAlertTimer);
                        carshAlertTimer -= 1;
                        passwordHandler.postDelayed(this, delay);
                    } else {
                        emergencyCall(mContext);
                    }
                }
            }
        }, delay);


    }

/*
    private void confirmCrashCall() {
        final LockWebServiceApi mLockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
    }
*/

    private static void cancelNotification(Context ctx, int notifyId) {
        try {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
            nMgr.cancel(notifyId);
        } catch (Exception e) {

        }
    }

    private void changeStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    @Override
    protected void onRestart() {

        super.onRestart();
    }

    public void emergencyCall(Context context) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) mContext.getSystemService(ns);
        nMgr.cancel(SkylockConstant.NOTIFICATION_ID + 0);
        Rect bounds = mProgressBar.getIndeterminateDrawable().getBounds(); // get current bounds before change drawable
        mProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.crash_progress_done, null));
        mProgressBar.getIndeterminateDrawable().setBounds(bounds); // set bounds back

        LocationService.getLocationManager(context);
        FriendsApi emergencyCall = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(FriendsApi.class);
        EmergenceContact emergenceContact = new EmergenceContact();
        emergenceContact.setMac_id(HomePageActivity.connectedMacAddress);
        emergenceContact.setCrash_id(SkylockConstant.LOCK_CRASH_ID);
        EmergenceContact.LocationEntity positionBean = new EmergenceContact.LocationEntity();
        positionBean.setLatitude(LocationService.getLocationManager(mContext).updateCoordinates().latitude);
        positionBean.setLongitude(LocationService.getLocationManager(mContext).updateCoordinates().longitude);
        emergenceContact.setLocation(positionBean);
        PrefUtil prefUtil = new PrefUtil(context);
        try {
            final String ecList = prefUtil.getStringPref(SkylockConstant.PREF_EMERGENCY_CONTACT_LIST, "");
            ArrayList<EmergenceContact.ContactsEntity> contactsDetails = new ArrayList<>();

            if (!Objects.equals(ecList, "") && ecList != null) {
                JSONArray ecListJson = new JSONArray(ecList);
                for (int noOfContacts = 0; noOfContacts < ecListJson.length(); noOfContacts++) {
                    EmergenceContact.ContactsEntity contactsDetail = new EmergenceContact.ContactsEntity();
                    contactsDetail.setFirst_name(ecListJson.getJSONObject(noOfContacts).getString("name"));
                    contactsDetail.setLast_name("");
                    contactsDetail.setCountry_code(GetCountryZipCode());
                    contactsDetail.setPhone_number(ecListJson.getJSONObject(noOfContacts).getString("number"));
                    contactsDetails.add(contactsDetail);
                }
                emergenceContact.setContacts(contactsDetails);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Call<SuccessResponse> emergencyFriendsCall = emergencyCall.EmergencyContactsSendaAlert(emergenceContact);

        emergencyFriendsCall.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                if (response.code() == 200) {
                }
            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {
            }
        });
    }

    public String GetCountryZipCode() {
        String CountryID = "";
        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getNetworkCountryIso().toUpperCase();
        return CountryID;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

