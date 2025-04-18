package cc.skylock.skylock.ui.fragment;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import cc.skylock.skylock.Bean.LockList;
import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.AddFriendEllipseActivity;
import cc.skylock.skylock.ui.GetBluetoothScanedDevices;
import cc.skylock.skylock.ui.UiUtils.RippleBackground;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by Velo Labs Android on 23-10-2016.
 */

public class ConnectFriendEllipseFragment extends Fragment implements GetBluetoothScanedDevices {
    private static ConnectFriendEllipseFragment mConnectFriendEllipseFragment;
    HashSet<BluetoothDevice> mBluetoothDeviceList;
    private RippleBackground mRippleBackground;
    TextView textView_header, textView_lockName, textView_label_connectNow;
    Context mContext;
    RelativeLayout content_RelativeLayout, progress_RelativeLayout;
    CardView cardView_connectNow;
    PrefUtil mPrefUtil;
    ArrayList<HashMap<String, String>> menuListData;
    AddFriendEllipseActivity mAddFriendEllipseActivity;
    private String mSharedLockMacId;
    String generateKey = null;
    HashMap<String, String> menulistDataItem;

    public static ConnectFriendEllipseFragment newInstance() {
        if (mConnectFriendEllipseFragment == null) {

            mConnectFriendEllipseFragment = new ConnectFriendEllipseFragment();
        }
        return mConnectFriendEllipseFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_friend_ellipse, container, false);
        mContext = getActivity();
        mPrefUtil = new PrefUtil(mContext);
        menuListData = new ArrayList<>();
        menulistDataItem = new HashMap<>();
        mAddFriendEllipseActivity = (AddFriendEllipseActivity) getActivity();
        cardView_connectNow = (CardView) view.findViewById(R.id.cv_connect_now);
        content_RelativeLayout = (RelativeLayout) view.findViewById(R.id.rl_contentlayout);
        progress_RelativeLayout = (RelativeLayout) view.findViewById(R.id.rl_progress);
        textView_lockName = (TextView) view.findViewById(R.id.tv_lockname);
        textView_header = (TextView) view.findViewById(R.id.tv_titile);
        textView_label_connectNow = (TextView) view.findViewById(R.id.textView_connectnow);
        textView_header.setTypeface(UtilHelper.getTypface(mContext));
        textView_header.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_connectNow.setTypeface(UtilHelper.getTypface(mContext));
        mBluetoothDeviceList = new HashSet<>();
        mRippleBackground = (RippleBackground) view.findViewById(R.id.content);
        mRippleBackground.startRippleAnimation();
        mSharedLockMacId = AddFriendEllipseActivity.sharedMacIdSuccess;
        textView_header.setVisibility(View.GONE);
        showProgress();
        cardView_connectNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAddFriendEllipseActivity != null && mSharedLockMacId != null) {
                    mAddFriendEllipseActivity.bleConnection(mSharedLockMacId, generateKey);
                }
            }
        });
        return view;
    }

    private void sharedLockListData(String connectMacId) {
        final String lockListJson = mPrefUtil.getStringPref(SkylockConstant.PREF_LOCK_LIST, "");
        if (!lockListJson.equals("")) {
            Gson gson = new Gson();
            LockList lockList = gson.fromJson(lockListJson, LockList.class);
            if (lockList.getPayload().getShared_locks().getTo_user() != null &&
                    lockList.getPayload().getShared_locks().getTo_user().size() > 0) {
                for (LockList.PayloadEntity.SharedLocksEntity.ToUserEntity sharedLocksEntity :
                        lockList.getPayload().getShared_locks().getTo_user()) {
                    if (sharedLocksEntity != null) {
                        menulistDataItem.put("LOCK_NAME", sharedLocksEntity.getName());
                        menulistDataItem.put("LOCK_MACID", sharedLocksEntity.getMac_id());
                        menulistDataItem.put("USER_TYPE", "BORROWER");
                        menulistDataItem.put("USERS_ID", sharedLocksEntity.getUsers_id());
                        menulistDataItem.put("LOCK_ID", "" + sharedLocksEntity.getLock_id());
                        menulistDataItem.put("USER_ID", "" + sharedLocksEntity.getUser_id());
                        menulistDataItem.put("SHARE_ID", "" + sharedLocksEntity.getShare_id());
                        mPrefUtil.setStringPref(sharedLocksEntity.getMac_id(), sharedLocksEntity.getName());
                        menuListData.add(menulistDataItem);
                    }
                }
                if (menuListData != null && menuListData.size() > 0) {
                    for (int i = 0; i < menuListData.size(); i++) {
                        if (menuListData.get(i).get("LOCK_MACID").equals(connectMacId)) {
                            if (progress_RelativeLayout != null) {

                                progress_RelativeLayout.setVisibility(View.GONE);
                            }
                            if (content_RelativeLayout != null) {
                                textView_header.setVisibility(View.VISIBLE);
                                content_RelativeLayout.setVisibility(View.VISIBLE);
                                textView_lockName.setText(mPrefUtil.getStringPref(menuListData.get(i).get("LOCK_MACID"), ""));
                                generateKey = UtilHelper.getMD5Hash(menuListData.get(i).get("USER_ID"));
                            }
                            break;
                        }
                    }
                }

            } else {
                hideProgress();
            }
        }

    }

    @Override
    public void scanedDevices(HashSet<BluetoothDevice> bluetoothDevices) {
        if (progress_RelativeLayout != null)
            progress_RelativeLayout.setVisibility(View.GONE);
        try {
            if (bluetoothDevices != null && bluetoothDevices.size() > 0) {
                final ArrayList<String> listOfMacid = new ArrayList<>();
                for (BluetoothDevice device : bluetoothDevices) {
                    listOfMacid.add(UtilHelper.getLockMacIDFromName(device.getName()));
                    mBluetoothDeviceList.add(device);
                }
                if (listOfMacid.contains(mSharedLockMacId)) {
                    sharedLockListData(mSharedLockMacId);
                } else {
                    hideProgress();
                }
            } else {
                hideProgress();
                Toast.makeText(mContext, "No locks found", Toast.LENGTH_SHORT);
                getActivity().finish();
            }
        } catch (Exception e) {

        }

    }

    @Override
    public void deviceConnected() {

    }

    public void showProgress() {
        try {
            progress_RelativeLayout.setVisibility(View.VISIBLE);
            content_RelativeLayout.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void hideProgress() {
        try {
            if (content_RelativeLayout != null && textView_header != null) {
                showNotFoundDialog();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showNotFoundDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.alert_lock_not_reachable);
        dialog.setCancelable(false);
        final TextView textView_label_cancel = (TextView) dialog.findViewById(R.id.tv_title);
        final TextView textView_label_Locate = (TextView) dialog.findViewById(R.id.tv_description);
        final CardView cv_ok = (CardView) dialog.findViewById(R.id.cv_yes_button);
        textView_label_cancel.setTypeface(UtilHelper.getTypface(getContext()));
        textView_label_Locate.setTypeface(UtilHelper.getTypface(getContext()));
        textView_label_cancel.setText(getResources().getString(R.string.warning));
        cv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                getActivity().finish();
            }
        });
        dialog.show();
    }

}
