package cc.skylock.skylock.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import cc.skylock.skylock.Bean.LockList;
import cc.skylock.skylock.Bean.SendMacIdAsParameter;
import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.ui.DeleteAccountActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Velo Labs Android on 28-01-2017.
 */

public class LockDetailsHelper {
    private static PrefUtil mPrefUtil;
    private static LockList lockList;
    private static ArrayList<HashMap<String, String>> myLockAndShareLockListData;
    final static Gson gson = new Gson();

    public static ArrayList<HashMap<String, String>> convertJsonToGson(Context mContext, String lockListJson) {
        try {
            mPrefUtil = new PrefUtil(mContext);
            myLockAndShareLockListData = new ArrayList<>();
            lockList = gson.fromJson(lockListJson, LockList.class);
            if (lockList.getPayload().getUser_locks() != null && lockList.getPayload().getUser_locks().size() > 0) {
                for (LockList.PayloadEntity.UserLocksEntity userLocksEntity : lockList.getPayload().getUser_locks()) {
                    if (userLocksEntity != null && userLocksEntity.getName() != null
                            && !userLocksEntity.getName().isEmpty()) {
                        final HashMap<String, String> menulistDataItem = new HashMap<>();
                        menulistDataItem.put("LOCK_NAME", userLocksEntity.getName());
                        menulistDataItem.put("LOCK_MACID", userLocksEntity.getMac_id());
                        menulistDataItem.put("USER_TYPE", "OWNER");
                        menulistDataItem.put("USER_ID", "" + userLocksEntity.getUser_id());
                        menulistDataItem.put("LOCK_ID", "" + userLocksEntity.getLock_id());
                        menulistDataItem.put("SHARE_ID", "" + userLocksEntity.getShare_id());
                        menulistDataItem.put("USERS_ID", mPrefUtil.getStringPref(SkylockConstant.PREF_USERS_ID, ""));
                        mPrefUtil.setStringPref(userLocksEntity.getMac_id(), userLocksEntity.getName());
                        mPrefUtil.setIntPref(SkylockConstant.PREF_LOCK_ID + userLocksEntity.getMac_id(), userLocksEntity.getLock_id());
                        myLockAndShareLockListData.add(menulistDataItem);
                    } else {
                        deleteLock(userLocksEntity.getMac_id());
                    }
                }
            }
            if (lockList.getPayload().getShared_locks().getTo_user() != null && lockList.getPayload().getShared_locks().getTo_user().size() > 0) {
                final HashMap<String, String> menulistDataItem = new HashMap<>();
                for (LockList.PayloadEntity.SharedLocksEntity.ToUserEntity mySharedLocksBean : lockList.getPayload().getShared_locks().getTo_user()) {
                    if (mySharedLocksBean != null) {
                        menulistDataItem.put("LOCK_NAME", mySharedLocksBean.getName());
                        menulistDataItem.put("LOCK_MACID", mySharedLocksBean.getMac_id());
                        menulistDataItem.put("USER_TYPE", "BORROWER");
                        menulistDataItem.put("USERS_ID", mySharedLocksBean.getUsers_id());
                        menulistDataItem.put("LOCK_ID", "" + mySharedLocksBean.getLock_id());
                        menulistDataItem.put("USER_ID", "" + mySharedLocksBean.getUser_id());
                        menulistDataItem.put("SHARE_ID", "" + mySharedLocksBean.getShare_id());
                        menulistDataItem.put("SHARE_TO_USER_ID", "" + mySharedLocksBean.getShared_to_user_id());
                        mPrefUtil.setStringPref(mySharedLocksBean.getMac_id(), mySharedLocksBean.getName());
                        myLockAndShareLockListData.add(menulistDataItem);
                    }

                }

            }
            return myLockAndShareLockListData;
        } catch (Exception e) {
            return null;
        }
    }

    private static void deleteLock(final String macAddress) {
        final SendMacIdAsParameter sendMacIdAsParameter = new SendMacIdAsParameter();
        if (macAddress != null) {
            sendMacIdAsParameter.setMac_id(macAddress);
            LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);

            Call<SuccessResponse> delete = lockWebServiceApi.DeleteLock(sendMacIdAsParameter);

            delete.enqueue(new Callback<SuccessResponse>() {
                @Override
                public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                    if (response.code() == 200) {
                        try {
                            mPrefUtil.setStringPref(macAddress, "");
                            mPrefUtil.setIntPref(macAddress + SkylockConstant.PREF_LOCK_ID, 0);
                            mPrefUtil.setStringPref(macAddress + SkylockConstant.SKYLOCK_PUBLIC_KEYS, "");
                            mPrefUtil.setStringPref(macAddress + SkylockConstant.SKYLOCK_SIGNED_MESSAGES, "");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<SuccessResponse> call, Throwable t) {

                }
            });
        }
    }
}
