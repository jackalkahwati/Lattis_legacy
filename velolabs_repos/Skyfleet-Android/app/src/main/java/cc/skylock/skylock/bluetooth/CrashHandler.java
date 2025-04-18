package cc.skylock.skylock.bluetooth;

import android.util.Log;

/**
 * Created by admin on 11/02/17.
 */

public class CrashHandler extends Accelerometer {
    String TAG = CrashHandler.class.getName();

    private float sensitivity;

    private double stdDevCutOff =323.0;
    private double valueCuttOff =782.3;

    CrashHandler(){
        super.maxPoints = 3;
    }

    @Override
    public boolean shouldAlert() {
        if(accelerometerValues.size() < maxPoints) {
            return false;
        }
        float[] stdDevs = allStdDevs();
        float xDev = stdDevs[0];
        float yDev = stdDevs[1];
        float zDev = stdDevs[2];

        float[] aves = average();
        float xAve = aves[0];
        float yAve = aves[1];
        float zAve = aves[2];

        if((check(xAve) || check(yAve) || check(zAve)) && (checkStdDev(xDev) || checkStdDev(yDev) || checkStdDev(zDev))){
            Log.i(TAG,"Throwing Crash alert with values Ave: x:"+xAve+",y:"+yAve+",z:"+zAve);
            Log.i(TAG,"Throwing Crash alert with values: Dev x:"+xDev+",y:"+yDev+",z:"+zDev);
            accelerometerValues.clear();
            return true;
        }
        return false;
    }

    public boolean checkStdDev(float value){
        return value  > stdDevCutOff;
    }

    public boolean check(float value){
        return value  > valueCuttOff;
    }
}
