package cc.skylock.skylock.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.fragment.LockStepTwo;
import cc.skylock.skylock.utils.Network.NetworkUtil;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by Velo Labs Android on 24-05-2016.
 */
public class ScannedDevicelistAdapter extends BaseAdapter {


    private Context mContext;

    private ArrayList<BluetoothDevice> mBluetoothDevices;
    ViewHolderItem viewHolder;
    LayoutInflater inflater;
    LockStepTwo mLockStepTwo;

    public ScannedDevicelistAdapter(Context mContext, HashSet<BluetoothDevice> mBluetoothDevicesList, LockStepTwo fragment) {
        this.mContext = mContext;
        this.mLockStepTwo = fragment;
        Set<BluetoothDevice> setTemp = mBluetoothDevicesList;
        mBluetoothDevices = new ArrayList<>();
        for (BluetoothDevice subset : mBluetoothDevicesList) {
            mBluetoothDevices.add(subset);
        }
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rootView = convertView;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final BluetoothDevice bluetoothDevice = mBluetoothDevices.get(position);
        if (rootView == null) {
            viewHolder = new ViewHolderItem();
            rootView = inflater.inflate(R.layout.bluetooth_scaned_list, null, false);
            viewHolder.title_TextView = (TextView) rootView.findViewById(R.id.tv_deviceName);
            viewHolder.description_TextView = (TextView) rootView.findViewById(R.id.tv_devicid);
            viewHolder.connect_cardView = (CardView) rootView.findViewById(R.id.cv_touch_button);
            viewHolder.ledBlink_TextView = (TextView) rootView.findViewById(R.id.tv_blink);
            viewHolder.connect_TextView = (TextView) rootView.findViewById(R.id.tv_touch_button);
            viewHolder.title_TextView.setTypeface(UtilHelper.getTypface(mContext));
            viewHolder.description_TextView.setTypeface(UtilHelper.getTypface(mContext));
            viewHolder.ledBlink_TextView.setTypeface(UtilHelper.getTypface(mContext));
            viewHolder.connect_TextView.setTypeface(UtilHelper.getTypface(mContext));
            //     viewHolder.icon_ImageView = (ImageView) rootView.findViewById(R.id.iv_icon);
            rootView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) rootView.getTag();
        }
        viewHolder.title_TextView.setText(bluetoothDevice.getName());
        viewHolder.description_TextView.setText(bluetoothDevice.getAddress());
        viewHolder.connect_cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.isNetworkAvailable(mContext))
                    mLockStepTwo.bleConnection(mBluetoothDevices.get(position), true);
                else
                    Toast.makeText(mContext, "No network connection", Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.ledBlink_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLockStepTwo.bleConnection(mBluetoothDevices.get(position), false);
            }
        });
        return rootView;

    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return mBluetoothDevices.size();
    }

    class ViewHolderItem {

        TextView title_TextView, ledBlink_TextView, connect_TextView;
        TextView description_TextView;
        CardView connect_cardView;
        ImageView icon_ImageView;

    }

}
