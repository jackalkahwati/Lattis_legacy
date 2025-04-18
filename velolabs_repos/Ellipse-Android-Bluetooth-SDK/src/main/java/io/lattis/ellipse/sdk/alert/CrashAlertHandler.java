package io.lattis.ellipse.sdk.alert;

import io.lattis.ellipse.sdk.util.SensorUtil;

public class CrashAlertHandler extends AlertHandler {

    private static final int SAMPLE_SIZE = 3;

    @Override
    public int getSampleCapacity() {
        return SAMPLE_SIZE;
    }

    @Override
    public boolean isInAlert() {
        boolean alert = SensorUtil.isCrashAlert(getSampleData(), SAMPLE_SIZE);
        if(alert){
            clearSampleData();
        }
        return alert;
    }
}
