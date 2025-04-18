package cc.skylock.skylock.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cc.skylock.skylock.Bean.LockList;
import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.Bean.UnShareLockRequest;
import cc.skylock.skylock.R;
import cc.skylock.skylock.adapter.ShareLockListAdapter;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.ui.HomePageActivity;
import cc.skylock.skylock.ui.alert.CentralizedAlertDialog;
import cc.skylock.skylock.utils.Network.NetworkUtil;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Velo Labs Android on 30-07-2016.
 */
public class SharingChildFragment_LockList extends Fragment {
    public static SharingChildFragment_LockList sharingChildFragment_lockList;
    View view;
    RecyclerView skylockList;
    PrefUtil mPrefUtil;
    public static String currentSharingLockID;
    private ArrayList<HashMap<String, String>> myLockListData;
    private ArrayList<HashMap<String, String>> mySharedLockListData;
    private ArrayList<HashMap<String, String>> mTotalLockListData;
    Context mContext;
    ShareLockListAdapter lockListAdapter;
    RelativeLayout loading_RelativeLayout, failure_RelativeLayout;
    TextView failure_TextView;
    Call<LockList> getLockListWS;

    public static SharingChildFragment_LockList newInstance() {
        if (sharingChildFragment_lockList == null) {
            sharingChildFragment_lockList = new SharingChildFragment_LockList();
        }
        return sharingChildFragment_lockList;
    }

    ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.childfragment_locklist, null);
        mContext = getActivity();
        mPrefUtil = new PrefUtil(mContext);
        failure_TextView = (TextView) view.findViewById(R.id.tv_nolocks);
        skylockList = (RecyclerView) view.findViewById(R.id.rv_share_lock_list);
        loading_RelativeLayout = (RelativeLayout) view.findViewById(R.id.rl_progressbar);
        failure_RelativeLayout = (RelativeLayout) view.findViewById(R.id.rl_content_failure);
        mProgressBar = (ProgressBar) view.findViewById(R.id.mProgressBar);
        loading_RelativeLayout.setVisibility(View.VISIBLE);
        failure_RelativeLayout.setVisibility(View.GONE);
        failure_TextView.setTypeface(UtilHelper.getTypface(mContext));

        return view;
    }


    private void removeFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getFragments() != null) {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            }
        }

    }

    public void handleContactsPermission(int position) {
        Log.i("position ", "" + position);
        if (weHavePermissionToReadContacts()) {
            ShowContactsList();
        }

    }

    private boolean weHavePermissionToReadContacts() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                this.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 123);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    public void ShowContactsList() {
        Fragment contactList = Contacts.newInstance();
        Contacts.newInstance().setTypeContact("SharingLock");
        ((HomePageActivity) getActivity()).changeHeaderUI("SHARING", ResourcesCompat.getColor(getResources(),
                R.color.colorPrimarylightdark, null), Color.WHITE);
        ((HomePageActivity) getActivity()).setFragment(contactList, true, "ContactList");
    }

    private void displaySkylock() {

        myLockListData = new ArrayList<>();
        mySharedLockListData = new ArrayList<>();
        mTotalLockListData = new ArrayList<>();
        HashMap<String, String> menulistDataItem = new HashMap<>();

        String lockListJson = mPrefUtil.getStringPref(SkylockConstant.PREF_LOCK_LIST, "");
        if (!lockListJson.equals("")) {
            final Gson gson = new Gson();
            final LockList lockList = gson.fromJson(lockListJson, LockList.class);
            if (lockList.getPayload().getUser_locks() != null && lockList.getPayload().getUser_locks().size() > 0) {
                for (LockList.PayloadEntity.UserLocksEntity myLocksEntity : lockList.getPayload().getUser_locks()) {
                    menulistDataItem = new HashMap<>();
                    menulistDataItem.put("LOCK_NAME", myLocksEntity.getName());
                    menulistDataItem.put("LOCK_MACID", myLocksEntity.getMac_id());
                    menulistDataItem.put("LOCK_ID", "" + myLocksEntity.getLock_id());
                    menulistDataItem.put("SHARE_ID", "" + myLocksEntity.getShare_id());
                    mPrefUtil.setBooleanPref(SkylockConstant.PREF_KEY_SHARED_LOCK + myLocksEntity.getMac_id(), false);
                    myLockListData.add(menulistDataItem);
                }
            }
            if (lockList.getPayload().getShared_locks() != null && lockList.getPayload().getShared_locks().getBy_user().getActive().size() > 0) {
                for (LockList.PayloadEntity.SharedLocksEntity.ByUserEntity.ActiveEntity mActiveEntity :
                        lockList.getPayload().getShared_locks().getBy_user().getActive()) {
                    menulistDataItem = new HashMap<>();
                    if (mActiveEntity != null) {
                        final String sharedMacId = mActiveEntity.getMac_id();
                        menulistDataItem.put(sharedMacId, mActiveEntity.getMac_id());
                        menulistDataItem.put("LOCK_NAME", mActiveEntity.getName());
                        menulistDataItem.put("LOCK_MACID", mActiveEntity.getMac_id());
                        menulistDataItem.put("LOCK_ID", "" + mActiveEntity.getLock_id());
                        menulistDataItem.put("USER_ID", "" + mActiveEntity.getUser_id());
                        menulistDataItem.put("SHARED_TO_USER_ID", "" + mActiveEntity.getShared_to_user_id());
                        mPrefUtil.setBooleanPref(SkylockConstant.PREF_KEY_SHARED_LOCK + mActiveEntity.getMac_id(), true);
                        menulistDataItem.put("SHARE_ID", "" + mActiveEntity.getShare_id());
                        mySharedLockListData.add(menulistDataItem);
                    }
                }
            }

        }
        if (myLockListData != null && myLockListData.size() > 0) {
            if (mySharedLockListData != null && mySharedLockListData.size() > 0) {
                for (int i = 0; i < myLockListData.size(); i++) {
                    HashMap<String, String> mDataItem = new HashMap<>();
                    if (i < mySharedLockListData.size()) {
                        for (int j = 0; j < mySharedLockListData.size(); j++) {

                            if (myLockListData.get(i).get("LOCK_MACID").equals(mySharedLockListData.get(j).get("LOCK_MACID"))) {
                                mDataItem.put("LOCK_MACID", mySharedLockListData.get(j).get("LOCK_MACID"));
                                mDataItem.put("LOCK_NAME", mySharedLockListData.get(j).get("LOCK_NAME"));
                                mDataItem.put("LOCK_ID", mySharedLockListData.get(j).get("LOCK_ID"));
                                mDataItem.put("SHARED_TO_USER_ID", mySharedLockListData.get(j).get("SHARED_TO_USER_ID"));
                                mDataItem.put("SHARE_ID", mySharedLockListData.get(j).get("SHARE_ID"));
                                mDataItem.put("USER_ID", mySharedLockListData.get(j).get("USER_ID"));
                                mTotalLockListData.add(mDataItem);
                            } else {
                                mDataItem.put("LOCK_MACID", myLockListData.get(i).get("LOCK_MACID"));
                                mDataItem.put("LOCK_NAME", myLockListData.get(i).get("LOCK_NAME"));
                                mDataItem.put("LOCK_ID", myLockListData.get(i).get("LOCK_ID"));
                                mDataItem.put("SHARED_TO_USER_ID", null);
                                mDataItem.put("SHARE_ID", myLockListData.get(i).get("SHARE_ID"));
                                mDataItem.put("USER_ID", myLockListData.get(i).get("USER_ID"));
                                mTotalLockListData.add(mDataItem);
                            }

                        }

                    } else {
                        mDataItem.put("LOCK_MACID", myLockListData.get(i).get("LOCK_MACID"));
                        mDataItem.put("LOCK_NAME", myLockListData.get(i).get("LOCK_NAME"));
                        mDataItem.put("LOCK_ID", myLockListData.get(i).get("LOCK_ID"));
                        mDataItem.put("SHARED_TO_USER_ID", null);
                        mDataItem.put("SHARE_ID", myLockListData.get(i).get("SHARE_ID"));
                        mDataItem.put("USER_ID", myLockListData.get(i).get("USER_ID"));
                        mTotalLockListData.add(mDataItem);
                    }

                }
            } else {
                mTotalLockListData.addAll(myLockListData);
            }

        }

        skylockList.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (sharingChildFragment_lockList != null) {
            lockListAdapter = new ShareLockListAdapter(mTotalLockListData, sharingChildFragment_lockList);
            skylockList.setAdapter(lockListAdapter);
        }


    }

    public void shareLockCall(String friendNumber, String friendName) {
    }

    @Override
    public void onResume() {
        ((HomePageActivity) getActivity()).changeHeaderUI("SHARING",
                ResourcesCompat.getColor(getResources(),
                        R.color.colorPrimarylightdark, null),Color.WHITE);
        if (NetworkUtil.isNetworkAvailable(mContext))
            getLockList();
        else {
            failure_RelativeLayout.setVisibility(View.VISIBLE);
            loading_RelativeLayout.setVisibility(View.GONE);
            failure_TextView.setText(getResources().getString(R.string.network_error));
            CentralizedAlertDialog.showDialog(mContext, getResources().getString(R.string.network_error), getResources().getString(R.string.no_internet_alert), 0);
        }
        super.onResume();
    }

    public void stopSharing(final String shareID, String shared_to_user_id, final String lockMacId, final String lockId) {
        if (shareID != null) {
            loading_RelativeLayout.setVisibility(View.VISIBLE);
            LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
            UnShareLockRequest mUnShareLockRequest = new UnShareLockRequest();
            mUnShareLockRequest.setShare_id(shareID);
            mUnShareLockRequest.setShared_to_user_id(shared_to_user_id);
            Call<SuccessResponse> shareLock = lockWebServiceApi.RevokeshareLock(mUnShareLockRequest);
            shareLock.enqueue(new Callback<SuccessResponse>() {
                @Override
                public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                    if (response.code() == 200) {
                        mPrefUtil.setBooleanPref(SkylockConstant.PREF_KEY_SHARED_LOCK + lockMacId, false);
                        mPrefUtil.setStringPref(SkylockConstant.PREF_KEY_LOCK_SHARED_TO + lockId, "");
                        skylockList.setAdapter(lockListAdapter);
                        getLockList();
                        UtilHelper.analyticTrackUserAction("Unshare lock (by owner)", "Share", "Sharing", "" + response.code(), "ANDROID");
                    } else {
                        loading_RelativeLayout.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "Try again later", Toast.LENGTH_SHORT).show();

                    }
                }

                @Override
                public void onFailure(Call<SuccessResponse> call, Throwable t) {
                    loading_RelativeLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ShowContactsList();
        } else {
            getActivity().finish();
            return;
        }
    }

    private void getLockList() {

        SkylockConstant.userToken = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_TOKEN, SkylockConstant.userToken);
        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        getLockListWS = lockWebServiceApi.GetLockData();

        getLockListWS.enqueue(new Callback<LockList>() {
            @Override
            public void onResponse(Call<LockList> call, Response<LockList> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus() == 200) {
                        loading_RelativeLayout.setVisibility(View.GONE);
                        final LockList payloadEntity = response.body();
                        final Gson gson = new Gson();
                        final String lockJson = gson.toJson(payloadEntity);
                        mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LIST, lockJson);
                        final List<LockList.PayloadEntity.UserLocksEntity> userLocksEntity = response.body().getPayload().getUser_locks();
                        if (userLocksEntity != null && userLocksEntity.size() > 0)
                            displaySkylock();
                        else
                            failure_RelativeLayout.setVisibility(View.VISIBLE);

                    } else {
                        mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LIST, "");

                    }
                }
            }

            @Override
            public void onFailure(Call<LockList> call, Throwable t) {
                loading_RelativeLayout.setVisibility(View.GONE);
            }
        });


    }

    @Override
    public void onStop() {
        if (getLockListWS != null) {
            getLockListWS.cancel();
        }
        super.onStop();
    }
}
