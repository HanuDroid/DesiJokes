package org.varunverma.desijokes;

import org.varunverma.CommandExecuter.ResultObject;
import org.varunverma.hanu.Application.HanuGCMIntentService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class GCMIntentService extends HanuGCMIntentService {

	@Override
	protected void onMessage(Context context, Intent intent) {

		String message = intent.getExtras().getString("message");
		if (message.contentEquals("InfoMessage")) {
			// Show Info Message to the User
			showInfoMessage(intent);
		} else {

			ResultObject result = processMessage(context, intent);

			if (result.isCommandExecutionSuccess() && result.getResultCode() == 200) {
				createNotification(intent);
			}
		}
	}

	private void showInfoMessage(Intent intent) {
		// Show Info Message
		String subject = intent.getExtras().getString("subject");
		String content = intent.getExtras().getString("content");

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Create Intent and Set Extras
		Intent notificationIntent = new Intent(this, DisplayFile.class);

		notificationIntent.putExtra("Title", "Info:");
		notificationIntent.putExtra("Subject", subject);
		notificationIntent.putExtra("Content", content);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		Notification notification = new NotificationCompat.Builder(this)
										.setContentTitle(subject)
										.setContentText(content)
										.setSmallIcon(R.drawable.ic_launcher)
										.setContentIntent(pendingIntent).build();

		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = subject;
		notification.when = System.currentTimeMillis();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		nm.notify(2, notification);

	}

	private void createNotification(Intent intent) {
		// Create Notification

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		String message = intent.getExtras().getString("notif_message");
		if(message == null || message.contentEquals("")){
			message = "New jokes are available";
		}
		
		Intent notificationIntent = new Intent(this, Main.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		
		Notification notification = new NotificationCompat.Builder(this)
										.setContentTitle(message)
										.setContentText(message)
										.setSmallIcon(R.drawable.ic_launcher)
										.setContentIntent(pendingIntent)
										.build();
		
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = message;
		notification.when = System.currentTimeMillis();
		
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		nm.notify(1, notification);

	}

}