package cc.skylock.skylock.ui.fragment;

import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import cc.skylock.skylock.Bean.LockList;
import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.Bean.UnShareLockRequest;
import cc.skylock.skylock.R;
import cc.skylock.skylock.adapter.LockListAdapter;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.ui.DeleteAccountActivity;
import cc.skylock.skylock.ui.HomePageActivity;
import cc.skylock.skylock.ui.LockSettingsActivity;
import cc.skylock.skylock.utils.LockDetailsHelper;
import cc.skylock.skylock.utils.Network.NetworkUtil;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Velo Labs Android on 07-06-2016.
 */
public class MyEllipsesFragment extends Fragment implements GetLockInfo {
    private View view;
    private PrefUtil mPrefUtil;
    private Context mContext;
    private RecyclerView skylockList;
    private ImageView overFlow_imageview, imageView_small_lock;
    private ImageView deletelock_ImageView;
    private ArrayList<HashMap<String, String>> myLockAndShareLockListData;
    private RelativeLayout relativeLayout_lockIcon, currentConnectLockRl, settings_RelativeLayout, tools_RelativeLayout;
    private HomePageActivity myHomeActivity;
    public static MyEllipsesFragment myEllipsesFragment;
    private TextView tv_lockName, tv_lockname_bottom, tv_lockStatus, textView_Lockdescription,
            tv_label_currently_connect, tv_label_previous_connect, tv_label_delete, tv_label_settings;
    private String macID = null;
    private RelativeLayout lockUnlock_RelativeLayout, rl_progressbar;
    private LockListAdapter lockListAdapter;
    private SwipeLayout swipeLayout;
    private String shareID = null, shared_to_user_id = null, lockMacId = null, lockId = null;
    boolean isSharedLock = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_myellipses, null);
        ((HomePageActivity) mContext).getLockInfo = this;
        mContext = getActivity();
        myLockAndShareLockListData = new ArrayList<>();
        overFlow_imageview = (ImageView) view.findViewById(R.id.iv_overflow);
        tv_label_delete = (TextView) view.findViewById(R.id.tv_delete);
        tv_label_settings = (TextView) view.findViewById(R.id.tv_settings);
        settings_RelativeLayout = (RelativeLayout) view.findViewById(R.id.rl_settingslock);
        relativeLayout_lockIcon = (RelativeLayout) view.findViewById(R.id.rl_lockLayout);
        tools_RelativeLayout = (RelativeLayout) view.findViewById(R.id.rl_tools);
        deletelock_ImageView = (ImageView) view.findViewById(R.id.iv_deletelock);
        textView_Lockdescription = (TextView) view.findViewById(R.id.tv_lockConnet);
        currentConnectLockRl = (RelativeLayout) view.findViewById(R.id.rl_currently_connect);
        tv_label_currently_connect = (TextView) view.findViewById(R.id.tv_label_currently_connect);
        tv_label_previous_connect = (TextView) view.findViewById(R.id.tv_label_previous_connect);
        swipeLayout = (SwipeLayout) view.findViewById(R.id.swipeSelectedLock);
        tv_lockName = (TextView) view.findViewById(R.id.tv_lockName);
        tv_lockname_bottom = (TextView) view.findViewById(R.id.tv_lockName_bottom);
        tv_lockStatus = (TextView) view.findViewById(R.id.tv_lockstatus);
        imageView_small_lock = (ImageView) view.findViewById(R.id.iv_lock);
        lockUnlock_RelativeLayout = (RelativeLayout) view.findViewById(R.id.rl_lockunlocklayout);
        rl_progressbar = (RelativeLayout) view.findViewById(R.id.rl_progressbar);
        rl_progressbar.setVisibility(View.GONE);
        tv_lockName.setTypeface(UtilHelper.getTypface(mContext));
        tv_lockname_bottom.setTypeface(UtilHelper.getTypface(mContext));
        tv_lockStatus.setTypeface(UtilHelper.getTypface(mContext));
        tv_label_delete.setTypeface(UtilHelper.getTypface(mContext));
        tv_label_settings.setTypeface(UtilHelper.getTypface(mContext));
        tv_label_currently_connect.setTypeface(UtilHelper.getTypface(mContext));
        tv_label_previous_connect.setTypeface(UtilHelper.getTypface(mContext));
        textView_Lockdescription.setTypeface(UtilHelper.getTypface(mContext));
        textView_Lockdescription.setVisibility(View.INVISIBLE);
        currentConnectLockRl.setVisibility(View.INVISIBLE);
        tv_lockStatus.setVisibility(View.INVISIBLE);
        relativeLayout_lockIcon.setEnabled(false);
        tools_RelativeLayout.setEnabled(false);
        myHomeActivity = ((HomePageActivity) getActivity());
        mPrefUtil = new PrefUtil(mContext);
        skylockList = (RecyclerView) view.findViewById(R.id.myEllipsesLockList);
        if (myHomeActivity != null && HomePageActivity.mCurrentlyconnectedGatt != null)
            myHomeActivity.getHWInfo();
        rl_progressbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        deletelock_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isSharedLock) {
                    if (shareID != null && shared_to_user_id != null)
                        stopSharing(shareID, shared_to_user_id, lockMacId, lockId);
                } else {
                    myHomeActivity.stopTimer();
                    showDeleteScreen(macID, true);
                }
            }
        });


        lockUnlock_RelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomePageActivity) getActivity()).clearBackStack();
                // ((HomePageActivity) getActivity()).onBackPressed();
            }
        });

        settings_RelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getActivity(), LockSettingsActivity.class);
                intent.putExtra("MAC_ID", macID);
                startActivity(intent);
            }
        });

        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLayout.setLeftSwipeEnabled(false);
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, tools_RelativeLayout);
        return view;


    }


    public void showDeleteScreen(String macID, boolean iscurrentlyConnected) {
        if (NetworkUtil.isNetworkAvailable(mContext) && macID != null) {
            mContext.startActivity(new Intent(mContext, DeleteAccountActivity.class)
                    .putExtra("DELETION_TYPE", 1)
                    .putExtra("LOCK_MACID", macID)
                    .putExtra("CURRENTLY_CONNECTED", iscurrentlyConnected)
            );
        } else
            Toast.makeText(mContext, "No network connection", Toast.LENGTH_SHORT).show();
    }

    private void setTextToolBarHeader(String header, boolean isShowIcon) {
        ((HomePageActivity) getActivity()).changeHeaderUI(header, ResourcesCompat.getColor(getResources(),
                R.color.colorPrimarylightdark, null), Color.WHITE);
        ((HomePageActivity) getActivity()).showAddNewIcon(isShowIcon);
    }

    @Override
    public void onResume() {
        setTextToolBarHeader("ELLIPSES", true);
        dispalySkylock();
        if (NetworkUtil.isNetworkAvailable(mContext))
            getLockList();
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        ((HomePageActivity) getActivity()).showAddNewIcon(false);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void getLockList() {

        SkylockConstant.userToken = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_TOKEN, SkylockConstant.userToken);
        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<LockList> getLockList = lockWebServiceApi.GetLockData();
        getLockList.enqueue(new Callback<LockList>() {
            @Override
            public void onResponse(Call<LockList> call, Response<LockList> response) {
                if (response.code() == 200) {
                    LockList payloadEntity = response.body();
                    Gson gson = new Gson();
                    String lockJson = gson.toJson(payloadEntity);
                    mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LIST, lockJson);
                    dispalySkylock();
                }
                else{
                    dispalySkylock();
                }

            }

            @Override
            public void onFailure(Call<LockList> call, Throwable t) {
                Log.e("There are some problem", t.toString());
            }
        });
    }

    private void dispalySkylock() {


        String lockListJson = mPrefUtil.getStringPref(SkylockConstant.PREF_LOCK_LIST, "");
        if (!lockListJson.equals("")) {
            myLockAndShareLockListData = LockDetailsHelper.convertJsonToGson(mContext, lockListJson);
        }
        skylockList.setLayoutManager(new LinearLayoutManager(getActivity()));
        ArrayList<HashMap<String, String>> lockList = new ArrayList<>();
        if (myLockAndShareLockListData != null && myLockAndShareLockListData.size() > 0) {
            for (HashMap<String, String> lockItem : myLockAndShareLockListData) {
                if (!isLockConnectedCurrently(lockItem.get("LOCK_MACID"), lockItem.get("USER_TYPE"))) {
                    lockList.add(lockItem);
                } else {
                    if (lockItem.get("USER_TYPE").equals("BORROWER")) {
                        shared_to_user_id = lockItem.get("SHARE_TO_USER_ID");
                        shareID = lockItem.get("SHARE_ID");
                        lockId = lockItem.get("LOCK_ID");
                        lockMacId = lockItem.get("LOCK_MACID");
                    }
                }
            }
        }
        currentLockHideAndShow();
        if (getActivity() != null) {
            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            skylockList.setLayoutManager(llm);
            lockListAdapter = new LockListAdapter(getActivity(), lockList, this);
            skylockList.setAdapter(lockListAdapter);
        }
    }

    public static MyEllipsesFragment newInstance() {
        if (myEllipsesFragment == null) {
            myEllipsesFragment = new MyEllipsesFragment();
        }
        return myEllipsesFragment;
    }


    @Override
    public void onGetHardwareInfo(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    HomePageActivity.mCurrentlyconnectedGatt = gatt;
                    dispalySkylock();
                }
            });
        }
        if (gatt != null) {
            macID = UtilHelper.getLockMacIDFromName(gatt.getDevice().getName());
            final String lockname = mPrefUtil.getStringPref(macID, "");
            final int position = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 4);
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_lockName.setText(lockname);
                        tv_lockname_bottom.setText(lockname);
                        if (position == 1 || position == 2) {
                            tv_lockStatus.setText(R.string.tap_to_unlock);
                            imageView_small_lock.setImageResource(R.drawable.icon_small_lock);
                        } else if (position == 0) {
                            tv_lockStatus.setText(R.string.tap_to_lock);
                            imageView_small_lock.setImageResource(R.drawable.icon_small_unlock);
                        }
                    }
                });
            }
        }

    }

    @Override
    public void onGetBLESignal(BluetoothGatt gatt, final int value) {
        Log.i("Signal Strength", "" + value);
    }

    @Override
    public void onBleDisconnect() {
        disconnect();
    }

    private void disconnect() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dispalySkylock();
                    isSharedLock = false;
                    currentConnectLockRl.setVisibility(View.INVISIBLE);
                    tv_lockname_bottom.setText(R.string.no_ellipse_connected);
                    imageView_small_lock.setImageResource(R.drawable.icon_small_lock);
                    tv_lockStatus.setVisibility(View.INVISIBLE);

                }
            });
        }
    }

    @Override
    public void onBoardFailed() {
        HomePageActivity.mCurrentlyconnectedGatt = null;
        hideProgressbar();
        disconnect();
    }

    private void hideProgressbar() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rl_progressbar.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onConnectionTimeOut() {
        HomePageActivity.mCurrentlyconnectedGatt = null;
        hideProgressbar();
        disconnect();
    }

    private void currentLockHideAndShow() {

        if (myLockAndShareLockListData != null && myLockAndShareLockListData.size() > 0) {
            for (HashMap<String, String> lockDetails : myLockAndShareLockListData) {
                if (isLockConnectedCurrently(lockDetails.get("LOCK_MACID"), lockDetails.get("USER_TYPE"))) {
                    textView_Lockdescription.setText(R.string.connected);
                    tv_lockName.setText(lockDetails.get("LOCK_NAME"));
                    textView_Lockdescription.setVisibility(View.VISIBLE);
                    currentConnectLockRl.setVisibility(View.VISIBLE);
                    tv_lockStatus.setVisibility(View.VISIBLE);
                    rl_progressbar.setVisibility(View.GONE);
                    break;
                } else {
                    textView_Lockdescription.setVisibility(View.INVISIBLE);
                    currentConnectLockRl.setVisibility(View.INVISIBLE);
                    tv_lockStatus.setVisibility(View.INVISIBLE);

                }
            }

        } else {
            currentConnectLockRl.setVisibility(View.INVISIBLE);
        }

    }

    private boolean isLockConnectedCurrently(String mac, String type) {

        if (HomePageActivity.mCurrentlyconnectedGatt != null && HomePageActivity.mCurrentlyconnectedGatt.getDevice() != null && mac != null) {
            if (mac.equals(UtilHelper.getLockMacIDFromName(HomePageActivity.mCurrentlyconnectedGatt.getDevice().getName()))) {
                if (type.equals("BORROWER")) {
                    isSharedLock = true;
                    tv_label_delete.setText(mContext.getResources().getString(R.string.unshare));
                    settings_RelativeLayout.setEnabled(false);
                    //   swipeLayout.setRightSwipeEnabled(false);
                } else {
                    settings_RelativeLayout.setEnabled(true);
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    public void bleConnection(final String macId, final String userID) {
        if (myHomeActivity != null && macId != null) {
            if (myHomeActivity.bleConnection(macId, userID)) {
                rl_progressbar.setVisibility(View.VISIBLE);
            } else {
                ((HomePageActivity) getActivity()).intializeBluetoothLE();
                rl_progressbar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UtilHelper.analyticTrackUserAction("Ellipses screen open", "Custom", "", null, "ANDROID");
    }


    public void stopSharing(final String shareID, final String shared_to_user_id, final String lockMacId, final String lockId) {
        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.alert_add_ec);
        dialog.setCancelable(false);
        final TextView textView_label_cancel = (TextView) dialog.findViewById(R.id.tv_title);
        final TextView textView_label_Locate = (TextView) dialog.findViewById(R.id.tv_description);
        final CardView cv_ok = (CardView) dialog.findViewById(R.id.cv_yes_button);
        final CardView cv_cancel = (CardView) dialog.findViewById(R.id.cv_cancel_button);
        textView_label_cancel.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_Locate.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_cancel.setText(getResources().getString(R.string.warning));
        textView_label_Locate.setText(getResources().getString(R.string.borrower_lock_remove_warning));
        cv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        cv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSharedLock) {
                    ((HomePageActivity) getActivity()).closeConnection();

                }
                conFirmStopSharing(shareID, shared_to_user_id, lockMacId, lockId);
                dialog.cancel();
            }
        });
        dialog.show();
    }


    public void conFirmStopSharing(final String shareID, String shared_to_user_id, final String lockMacId, final String lockId) {
        if (shareID != null) {
            rl_progressbar.setVisibility(View.VISIBLE);
            LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
            UnShareLockRequest mUnShareLockRequest = new UnShareLockRequest();
            mUnShareLockRequest.setShare_id(shareID);
            mUnShareLockRequest.setShared_to_user_id(shared_to_user_id);
            Call<SuccessResponse> shareLock = lockWebServiceApi.RevokeshareLock(mUnShareLockRequest);
            shareLock.enqueue(new Callback<SuccessResponse>() {
                @Override
                public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                    rl_progressbar.setVisibility(View.GONE);
                    if (response.code() == 200) {
                        mPrefUtil.setBooleanPref(SkylockConstant.PREF_KEY_SHARED_LOCK + lockMacId, false);
                        mPrefUtil.setStringPref(SkylockConstant.PREF_KEY_LOCK_SHARED_TO + lockId, "");
                        skylockList.setAdapter(lockListAdapter);
                        getLockList();
                        UtilHelper.analyticTrackUserAction("Unshare lock (by owner)", "Share", "Sharing", "" + response.code(), "ANDROID");
                    } else {
                        Toast.makeText(getActivity(), "Try again later", Toast.LENGTH_SHORT).show();

                    }
                }

                @Override
                public void onFailure(Call<SuccessResponse> call, Throwable t) {
                    rl_progressbar.setVisibility(View.GONE);
                }
            });
        }

    }

}


