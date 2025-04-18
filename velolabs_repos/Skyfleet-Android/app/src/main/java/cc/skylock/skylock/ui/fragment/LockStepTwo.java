package cc.skylock.skylock.ui.fragment;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashSet;

import cc.skylock.skylock.R;
import cc.skylock.skylock.adapter.ScannedDevicelistAdapter;
import cc.skylock.skylock.ui.AddLockActivity;
import cc.skylock.skylock.ui.GetBluetoothScanedDevices;
import cc.skylock.skylock.ui.HomePageActivity;
import cc.skylock.skylock.ui.UiUtils.RippleBackground;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by prabhu on 1/18/16.
 */
public class LockStepTwo extends Fragment implements GetBluetoothScanedDevices {
    int bluetoothProgressCount = 0;
    ListView recyclerView;
    LinearLayout linearLayout_buttons;
    HashSet<BluetoothDevice> mBluetoothDeviceList;
    private static LockStepTwo f;
    TextView textView_description_three, textView_descriptionOne, textView_descriptionTwo, textView_header;
    View view;
    RelativeLayout content_RelativeLayout, progress_RelativeLayout;
    RippleBackground mRippleBackground;
    AddLockActivity mAddLockActivity;


    public static LockStepTwo newInstance() {
        if (f == null) {

            f = new LockStepTwo();
        }
        return f;
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAddLockActivity = (AddLockActivity) getActivity();
        View v = inflater.inflate(R.layout.add_lock_2, container, false);
        content_RelativeLayout = (RelativeLayout) v.findViewById(R.id.rl_contentlayout);
        progress_RelativeLayout = (RelativeLayout) v.findViewById(R.id.rl_progress);
        recyclerView = (ListView) v.findViewById(R.id.scannedLockList);
        mRippleBackground = (RippleBackground) v.findViewById(R.id.content);
        mRippleBackground.startRippleAnimation();
        textView_descriptionOne = (TextView) v.findViewById(R.id.tv_description);
        textView_descriptionTwo = (TextView) v.findViewById(R.id.tv_description_1);
        textView_description_three = (TextView) v.findViewById(R.id.tv_description_2);
        textView_header = (TextView) v.findViewById(R.id.tv_titile);
        textView_descriptionOne.setTypeface(UtilHelper.getTypface(mAddLockActivity));
        textView_descriptionTwo.setTypeface(UtilHelper.getTypface(mAddLockActivity));
        textView_description_three.setTypeface(UtilHelper.getTypface(mAddLockActivity));
        textView_header.setTypeface(UtilHelper.getTypface(mAddLockActivity));
        content_RelativeLayout.setVisibility(View.GONE);
        mBluetoothDeviceList = new HashSet<>();
        showProgress();
        textView_descriptionTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().startActivity(new Intent(getActivity(), HomePageActivity.class)
                        .putExtra("typeOfNotification", 2));
            }
        });
        return v;

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
            progress_RelativeLayout.setVisibility(View.GONE);
            content_RelativeLayout.setVisibility(View.VISIBLE);
            textView_header.setText(R.string.ellipse_not_found);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void bleConnection(BluetoothDevice mBluetoothDevice, boolean autoConnect) {
        if (mAddLockActivity != null && mBluetoothDevice != null) {
            if (!mAddLockActivity.bleConnection(mBluetoothDevice, autoConnect)) {
                if (progress_RelativeLayout != null) {
                    progress_RelativeLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void scanedDevices(HashSet<BluetoothDevice> bluetoothDevices) {
        try {

            if (progress_RelativeLayout != null) {
                progress_RelativeLayout.setVisibility(View.GONE);
            }
            if (content_RelativeLayout != null) {
                content_RelativeLayout.setVisibility(View.VISIBLE);
            }
            if (bluetoothDevices != null) {
                for (BluetoothDevice device : bluetoothDevices) {
                    mBluetoothDeviceList.add(device);
                    if (mBluetoothDeviceList != null && mBluetoothDeviceList.size() > 0) {
                        textView_header.setText("We've found the following Ellipses");
                        ScannedDevicelistAdapter scannedDevicelistAdapter = new ScannedDevicelistAdapter(getContext(), mBluetoothDeviceList, this);
                        this.recyclerView.setAdapter(scannedDevicelistAdapter);
                    } else {
                        if (getActivity() != null) {
                            textView_header.setText(R.string.ellipse_not_found);
                        }
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.setVisibility(View.VISIBLE);
                                textView_header.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deviceConnected() {

    }


}
