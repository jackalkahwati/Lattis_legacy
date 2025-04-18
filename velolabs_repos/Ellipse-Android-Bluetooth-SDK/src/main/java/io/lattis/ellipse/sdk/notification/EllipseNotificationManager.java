package io.lattis.ellipse.sdk.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import io.lattis.ellipse.sdk.R;
import io.lattis.ellipse.sdk.model.Alert;
import io.lattis.ellipse.sdk.model.FirmwareUpdateProgress;
import io.lattis.ellipse.sdk.util.IntentUtil;

public class EllipseNotificationManager {

    private static final int NOTIFICATION_ID_FIRMWARE_UPDATE = 1;
    private static final int NOTIFICATION_ID_THEFT_ALERT = 2;
    private static final int NOTIFICATION_ID_CRASH_ALERT = 3;
    private static final int NOTIFICATION_ID_SERVICE_FOREGROUND = 4;

    private final NotificationManager notificationManager;
    private NotificationCompat.Builder firmwareUpdateProgressBuilder;

    public EllipseNotificationManager(Context context) {
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public NotificationCompat.Builder getFirmwareUpdateProgressNotification(Service service,
                                                                                   Class updateActivity){
        PendingIntent pendingIntent = PendingIntent.getActivity(service, 0, new Intent(service, updateActivity), 0);
        return new NotificationCompat.Builder(service).setContentTitle(service.getString(R.string.firmware_update))
                .setContentText(service.getString(R.string.firmware_update_in_progress))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ellipse_icon);
    }

    public void notify(Service service, Class updateActivity, FirmwareUpdateProgress progress){
        if(progress.getStatus() == FirmwareUpdateProgress.Status.IN_PROGRESS){
            if(firmwareUpdateProgressBuilder == null){
                firmwareUpdateProgressBuilder = getFirmwareUpdateProgressNotification(service, service.getClass());
            }
            firmwareUpdateProgressBuilder.setProgress(progress.getTotal(), progress.getProgress(), false);

            if(progress.getProgress() == 0){
                service.startForeground(NOTIFICATION_ID_FIRMWARE_UPDATE, firmwareUpdateProgressBuilder.build());
            } else {
                notificationManager.notify(NOTIFICATION_ID_FIRMWARE_UPDATE, firmwareUpdateProgressBuilder.build());
            }

        } else {
            firmwareUpdateProgressBuilder.setProgress(0,0,false);
            if(progress.getStatus() == FirmwareUpdateProgress.Status.IMAGE_INVALID) {
                firmwareUpdateProgressBuilder.setContentText(service.getString(R.string.firmware_update_fail));
            } else if(progress.getStatus() == FirmwareUpdateProgress.Status.IMAGE_VALID){
                firmwareUpdateProgressBuilder.setContentText(service.getString(R.string.firmware_update_succeed));
            }

            notificationManager.notify(NOTIFICATION_ID_FIRMWARE_UPDATE, firmwareUpdateProgressBuilder.build());
            service.stopForeground(false);
        }
    }

    public void notifyAlert(Service service, Alert alert){
        long[] pattern = {100, 100, 100, 100};
        notificationManager.notify(NOTIFICATION_ID_CRASH_ALERT, new NotificationCompat.Builder(service)
                .setContentTitle(alert.equals(Alert.CRASH) ? service.getString(R.string.crash_alert) : service.getString(R.string.theft_alert))
                .setContentIntent(PendingIntent.getActivity(service, 0,IntentUtil.getAlertIntent(service,alert), 0))
                .setSmallIcon(R.mipmap.ellipse_icon)
                .setVibrate(pattern)
                .build());
    }

    public void notifyAlertMode(Service service, Alert alert){

        PendingIntent pendingIntent = PendingIntent.getActivity(service, 0, new Intent(service,alert.getActivity()), 0);

        NotificationCompat.Action stopAlertAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_close_grey_500_18dp, service.getString(R.string.stop_alert), pendingIntent).build();

        service.startForeground(NOTIFICATION_ID_SERVICE_FOREGROUND, new NotificationCompat.Builder(service)
                .addAction(stopAlertAction)
                .setContentTitle(service.getString(R.string.app_name))
                .setContentText("Content")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ellipse_icon)
                .setOngoing(true)
                .build());
    }
}
