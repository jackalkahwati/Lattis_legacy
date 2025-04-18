package io.lattis.ellipse.sdk.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import io.lattis.ellipse.sdk.service.BluetoothService;

public class NotificationUtil {

    public static PendingIntent getStopAlertPendingIntent(Context context, String action){
        Intent notificationIntent = new Intent(context, BluetoothService.class);
        notificationIntent.setAction(action);
        return PendingIntent.getService(context, 0, new Intent(context, BluetoothService.class), 0);
    }
}
