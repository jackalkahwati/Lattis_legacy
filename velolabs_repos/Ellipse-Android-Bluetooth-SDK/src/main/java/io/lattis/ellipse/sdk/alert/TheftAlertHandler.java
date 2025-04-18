package io.lattis.ellipse.sdk.alert;

import android.util.Log;

import hugo.weaving.DebugLog;
import io.lattis.ellipse.sdk.util.SensorUtil;

public class TheftAlertHandler extends AlertHandler {

    private static final int SAMPLE_SIZE = 20;

    private float sensitivity;

    public void setSensitivity(float sensitivity ) {
        this.sensitivity = sensitivity;
    }

    @Override
    public int getSampleCapacity() {
        return SAMPLE_SIZE;
    }

    @Override
    public boolean isInAlert() {
        boolean alert =  SensorUtil.isTheftAlert(getSampleData(),SAMPLE_SIZE,sensitivity);
        if(alert){
            Log.w(TheftAlertHandler.class.getSimpleName(),"is In Theft Alert !!!");
            clearSampleData();
        }
        return alert;
    }
}
