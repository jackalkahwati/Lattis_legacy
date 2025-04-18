package io.lattis.ellipse.sdk.util;

import android.content.Context;
import android.content.Intent;

import io.lattis.ellipse.sdk.manager.EllipseManager;
import io.lattis.ellipse.sdk.model.Alert;

public class IntentUtil {

    public static Intent getAlertIntent(Context context, Alert alert){
        Intent intent = new Intent(context, alert.getActivity());
        intent.setAction(EllipseManager.ACTION_ALERT);
        intent.putExtra(EllipseManager.EXTRA_LOCK_ID, alert.getLockId());
        if(alert.getAccelerometerData()!=null){
            intent.putExtra(EllipseManager.EXTRA_ACCELEROMETER_DATA_MAV_X, alert.getAccelerometerData().getMav().getX());
            intent.putExtra(EllipseManager.EXTRA_ACCELEROMETER_DATA_MAV_Y, alert.getAccelerometerData().getMav().getY());
            intent.putExtra(EllipseManager.EXTRA_ACCELEROMETER_DATA_MAV_Z, alert.getAccelerometerData().getMav().getZ());
            intent.putExtra(EllipseManager.EXTRA_ACCELEROMETER_DATA_DEVIATION_X, alert.getAccelerometerData().getMav().getX());
            intent.putExtra(EllipseManager.EXTRA_ACCELEROMETER_DATA_DEVIATION_Y, alert.getAccelerometerData().getMav().getY());
            intent.putExtra(EllipseManager.EXTRA_ACCELEROMETER_DATA_DEVIATION_Z, alert.getAccelerometerData().getMav().getZ());
        }
        return intent;
    }
}
