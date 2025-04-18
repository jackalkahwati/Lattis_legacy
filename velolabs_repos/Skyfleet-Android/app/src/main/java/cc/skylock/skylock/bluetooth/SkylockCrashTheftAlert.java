package cc.skylock.skylock.bluetooth;

import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Timer;

import cc.skylock.skylock.utils.SkylockConstant;

/**
 * Created by AlexVijayRaj on 7/24/2015.
 */
public class SkylockCrashTheftAlert {

    Context context;
    Dialog dialogCrash, dialogCrashAfter, dialogTheft;
    ImageButton ibIgnore, ibHelp;
    TextView tvTimer, tvTimeTheft, tvTimeCrash, tvTimeCrashAfter;
    Timer timer;
    int count = 30;
    private int flagCrashTheftValue = 0; //Crash theft flag; 0 = off; 1 = crash ON; 2 = theft ON;
//    private int flagCrashValue = 0; //Crash theft flag; 0 = off; 1 = crash ON; 2 = theft ON;
    private int theftLevel = 2; // 1 = low; 2 = medium; 3 = high;
    /*    private int thresholdCrashMAV = 250;
        private int thresholdCrashSD = 16900;
            private int thresholdTheftMAV = 70;
    private int thresholdTheftSD = 8100;*/
    private int thresholdCrashMAV = 1400;
    private int thresholdCrashSD = 800;
    private int thresholdTheftMAV = 1200;
    private int thresholdTheftSD = 700;
    TheftHandler theftHandler = new TheftHandler(5);
    CrashHandler crashHandler = new CrashHandler();
    public SkylockCrashTheftAlert(Context context1) {
        context = context1;
    }


    public boolean isCrash() {
        if (flagCrashTheftValue == 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isTheft() {
        if (flagCrashTheftValue == 2) {
            return true;
        } else {
            return false;
        }
    }

    public void flagCrash( ) {
        flagCrashTheftValue = 1;
    }

    public void flagTheft(int theftLevel1) {
        if(theftLevel1 > 0){
            theftLevel = theftLevel1;
        }else{
            theftLevel = 2;
        }
        flagCrashTheftValue = 2;
    }
    public void disableCrashTheft() {
        flagCrashTheftValue = 0;
    }

    public void putCharacterstic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        final Integer mavX = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
        final Integer mavY = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2);
        final Integer mavZ = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 4);
        final Integer sdX = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 6);
        final Integer sdY = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 8);
        final Integer sdZ = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 10);

        Log.i("mavX : ", "" + mavX);
        Log.i("mavY : ", "" + mavY);
        Log.i("mavZ : ", "" + mavZ);
        Log.i("sdX : ", "" + sdX);
        Log.i("sdY : ", "" + sdY);
        Log.i("sdY : ", "" + sdZ);
        Log.i("flagTheft", "" + theftLevel);

        AccelerometerValue accelerometerValue = new AccelerometerValue(mavX,mavY,mavZ,sdX,sdY,sdZ);

        if (isTheft()) {
            theftHandler.set(theftLevel);
            theftHandler.add(accelerometerValue);
            if(theftHandler.shouldAlert()){
                alertTheft(gatt, characteristic);
            }
        }
        if (isCrash()) {
            crashHandler.add(accelerometerValue);
            if(crashHandler.shouldAlert()){
                alertCrash(gatt, characteristic);
            }
        }
    }


    private void alertCrash(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        SkylockBluetoothManage.getInstance().mUiCall.onCrashed(gatt, characteristic);
        Log.i("Skylock : ", "Crash Alert");
    }

    private void alertTheft(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.i("Skylock : ", "Theft Alert");
        SkylockBluetoothManage.getInstance().mUiCall.onTheft(gatt, characteristic);
    }


}
