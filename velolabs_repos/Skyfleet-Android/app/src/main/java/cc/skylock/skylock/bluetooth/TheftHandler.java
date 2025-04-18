package cc.skylock.skylock.bluetooth;

import android.util.Log;

/**
 * Created by admin on 11/02/17.
 */

public class TheftHandler extends Accelerometer {
    String TAG = TheftHandler.class.getName();

    private float sensitivity;

    private double cutOff = 195.73;

    public TheftHandler(float sensitivity ) {
        this.sensitivity = sensitivity;
         super.maxPoints = 20;
    }

    public void set(float sensitivity ) {
        this.sensitivity = sensitivity;
    }

    @Override
    public boolean shouldAlert() {
        if(accelerometerValues.size() < maxPoints) {
            return false;
        }
        float[] stdDevs = allStdDevs();
        float x = stdDevs[0];
        float y = stdDevs[1];
        float z = stdDevs[2];

        if(checkAxis(x) || checkAxis(y) || checkAxis(z)){
            Log.i(TAG,"Throwing theft alert with values: x:"+x+",y:"+y+",z:"+z);
            accelerometerValues.clear();
            return true;
        }
        return false;
    }

    public boolean checkAxis(float value){
        return value + 10.0 * (sensitivity - 0.5) > cutOff;
    }
}
