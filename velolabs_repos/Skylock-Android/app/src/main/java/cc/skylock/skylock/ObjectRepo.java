package cc.skylock.skylock;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GcmListenerService;

import java.util.List;

import cc.skylock.skylock.generator.HashGenerator;

/**
 * Created by AlexVijayRaj on 9/23/2015.
 */
public class ObjectRepo extends Activity {

    Context context;
    BluetoothClass objBluetoothClass;
    leftNavDrawerAdapter objLeftNavDrawerAdapter;
    RightNavDrawerAdapter objRightNavDrawerAdapter;
    ListView drawerList, drawerListRight, bluetoothList;
    ImageView ivSignal;
    Button scanBluetooth;
    ImageButton bLock;
    DrawerLayout mDrawerLayout;
    Sharing objSharing;
    ProfileManager objProfileManager;
    AddLock objAddLock;
    Settings objSettings;
    MapActivity objMapActivity;
    JSONClass objJSON;
    RelativeLayout rlPopUp, rlUpArrow, rlGPS;
    BackendClass objBackendClass;
    EmergencyContacts objEmergencyContacts;
    BluetoothManager manager;
    BluetoothGatt mBluetoothGatt;
    BluetoothDevice Device;
    BluetoothAdapter myBluetoothAdapter;
    ArrayAdapter<String> BTArrayAdapter;
    List<BluetoothDevice> tmpBtChecker;
    HashGenerator objHashGenerator;

    BroadcastReceiver bReceiver;

    String fb_id;

    public ObjectRepo(Context context){
        this.context = context;







    }

    public String macRemoveColon(String macId){

        macId = macId.replace(":","");

        return macId;
    }

    public String macAddColon(String macId){
        String x=macId;
        String finals="";
        for(int i=0;i<x.length();i=i+2)
        {
            if((i+2)<x.length())
                finals+=x.substring(i, i+2)+":";
            if((i+2)==x.length())
            {
                finals+=x.substring(i, i+2);

            }

        }
        return finals;
    }

    public void displayToast(String temp){
        Toast.makeText(context, "" +temp,
                Toast.LENGTH_LONG).show();
    }

    public void displayAlertDialog(String Title, String message){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(""+Title);
        alertDialog.setMessage(""+message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }



}
