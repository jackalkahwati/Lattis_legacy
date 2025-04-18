package cc.skylock.skylock.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import cc.skylock.skylock.R;
import cc.skylock.skylock.utils.SkylockConstant;

public class NotificationView {

	public static void showNotification(Context context, String title, String messageBody,int typeOfNotification, Class<?> cls){
		NotificationManager notificationManager = (NotificationManager)
				context.getSystemService(context.NOTIFICATION_SERVICE);
		int icon = R.drawable.ic_notification;
		long when = System.currentTimeMillis();

		//Define sound URI
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); 

		//ticker text
		CharSequence tickerText = title + " : " + messageBody ;
		PendingIntent pendingIntent = null;
		if(cls != null){
			Intent notificationlaunchActivity = new Intent(context, cls);
			//	notificationlaunchActivity.putExtra("typeOfNotification", typeOfNotification);
			notificationlaunchActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			  pendingIntent =
					PendingIntent.getActivity(context, 0, notificationlaunchActivity, PendingIntent.FLAG_UPDATE_CURRENT);

		}
		Notification notification = new NotificationCompat.Builder(context)

		.setContentTitle(title)
		.setContentText(messageBody)
		.setContentIntent(pendingIntent)
		.setTicker(tickerText)
		.setSmallIcon(icon)
		.setWhen(when)
		.setSound(soundUri)
		.setStyle(new NotificationCompat.BigTextStyle()
		.bigText(messageBody))
		.build();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(SkylockConstant.NOTIFICATION_ID+typeOfNotification, notification);
	}
}
