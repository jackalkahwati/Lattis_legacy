package io.lattis.ellipse.sdk.util;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;

import hugo.weaving.DebugLog;
import io.lattis.ellipse.sdk.model.AccelerometerData;

public class SensorUtil {

    private static final int NO_VALUE = 0;

    private static final double THEFT_CUT_OFF = 195.73;

    private static final double CRASH_CUT_OFF = 782.3;
    private static final double CRASH_STANDARD_DEVIATION_CUT_OFF = 323.0;


    private static final float[] EMPTY = new float[]  {NO_VALUE,NO_VALUE,NO_VALUE,NO_VALUE,NO_VALUE,NO_VALUE};

    private static float standardDeviation(ArrayBlockingQueue<AccelerometerData> accelerometerData, float summedValue){
        int accelerometerValueSize = accelerometerData.size();
        if (accelerometerValueSize == 0 || accelerometerValueSize == 1) {
            return  0;
        }
        return (float) Math.sqrt(summedValue/  (accelerometerValueSize - 1));
    }

    private static float[] getAverage(ArrayBlockingQueue<AccelerometerData> accelerometerData){

        int accelerometerValueSize = accelerometerData.size();
        if (accelerometerValueSize == 0 ) {
            return EMPTY;
        }
        float xAve = 0;
        float yAve = 0;
        float zAve = 0;
        float xStdAve = 0;
        float yStdAve = 0;
        float zStdAve = 0;

        for (AccelerometerData accelerometerValue: accelerometerData) {
            xAve += accelerometerValue.getMav().getX();
            yAve += accelerometerValue.getMav().getY();
            zAve += accelerometerValue.getMav().getZ();
            xStdAve += accelerometerValue.getDeviation().getX();
            yStdAve += accelerometerValue.getDeviation().getY();
            zStdAve += accelerometerValue.getDeviation().getZ();
        }
        return new float[]  {
                xAve/accelerometerValueSize,
                yAve/accelerometerValueSize,
                zAve/accelerometerValueSize,
                xStdAve/accelerometerValueSize,
                yStdAve/accelerometerValueSize,
                zStdAve/accelerometerValueSize};
    }

    private static float[] getAccelerometerStandardDeviation(ArrayBlockingQueue<AccelerometerData> accelerometerData){
        int accelerometerValueSize = accelerometerData.size();
        if (accelerometerValueSize == 0 || accelerometerValueSize == 1) {
            return EMPTY;
        }
        float[] averages = getAverage(accelerometerData);
        float xAve,yAve,zAve,xStdAve,yStdAve,zStdAve;
        try{
            xAve = averages[0];
            yAve = averages[1];
            zAve = averages[2];
            xStdAve = averages[3];
            yStdAve = averages[4];
            zStdAve = averages[5];
        }catch (Exception e){
            return EMPTY;
        }
        float xDev = 0;
        float yDev = 0;
        float zDev = 0;
        float xStdDev = 0;
        float yStdDev = 0;
        float zStdDev = 0;
        for (AccelerometerData accelerometerValue: accelerometerData) {
            xDev +=  Math.pow(accelerometerValue.getMav().getX()-xAve,2.0);
            yDev +=  Math.pow(accelerometerValue.getMav().getY()-yAve,2.0);
            zDev +=  Math.pow(accelerometerValue.getMav().getZ()-zAve,2.0);
            xStdDev +=  Math.pow(accelerometerValue.getDeviation().getX()-xStdAve,2.0);
            yStdDev +=  Math.pow(accelerometerValue.getDeviation().getY()-yStdAve,2.0);
            zStdDev +=  Math.pow(accelerometerValue.getDeviation().getZ()-zStdAve,2.0);
        }
        float txValue =  standardDeviation(accelerometerData,xDev);
        float tyValue = standardDeviation(accelerometerData, yDev);
        float tzValue = standardDeviation(accelerometerData, zDev);
        float txStdDev = standardDeviation(accelerometerData, xStdDev);
        float tyStdDev = standardDeviation(accelerometerData, yStdDev);
        float tzStdDev = standardDeviation(accelerometerData, zStdDev);
        Log.i("TAG","txValue"+txValue+"tyValue"+tyValue+"tzValue"+tzValue+"txStdDev"+txStdDev+"tyStdDev"+tyStdDev+"tzStdDev"+tzStdDev);
        return new float[]{txValue,tyValue,tzValue,txStdDev,tyStdDev,tzStdDev};
    }

    @DebugLog
    public static boolean isTheftAlert(ArrayBlockingQueue<AccelerometerData> accelerometerData,
                                       int sampleSize,
                                       float sensitivity){
        if(accelerometerData.size() < sampleSize) {
            return false;
        }
        float[] standardDeviation = SensorUtil.getAccelerometerStandardDeviation(accelerometerData);
        float x = standardDeviation[0];
        float y = standardDeviation[1];
        float z = standardDeviation[2];

        return checkLimitTheft(x, sensitivity) || checkLimitTheft(y, sensitivity) || checkLimitTheft(z, sensitivity);
    }

    @DebugLog
    public static boolean isCrashAlert(ArrayBlockingQueue<AccelerometerData> accelerometerData, int sampleSize){
        if(accelerometerData.size() < sampleSize) {
            return false;
        }
        float[] stdDevs = getAccelerometerStandardDeviation(accelerometerData);
        float xDev = stdDevs[0];
        float yDev = stdDevs[1];
        float zDev = stdDevs[2];

        float[] aves = getAverage(accelerometerData);
        float xAve = aves[0];
        float yAve = aves[1];
        float zAve = aves[2];

        return (checkLimitCrash(xAve) || checkLimitCrash(yAve) || checkLimitCrash(zAve)) &&
               (checkLimitCrashStandardDeviation(xDev) || checkLimitCrashStandardDeviation(yDev) || checkLimitCrashStandardDeviation(zDev));
    }

    private static boolean checkLimitCrashStandardDeviation(float value){
        return value  > CRASH_STANDARD_DEVIATION_CUT_OFF;
    }

    private static boolean checkLimitCrash(float value){
        return value  > CRASH_CUT_OFF;
    }

    private static boolean checkLimitTheft(float value, float sensitivity){
        return value + 10.0 * (sensitivity - 0.5) > THEFT_CUT_OFF;
    }
}
