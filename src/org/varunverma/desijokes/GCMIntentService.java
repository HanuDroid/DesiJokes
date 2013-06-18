package org.varunverma.desijokes;

import java.util.ArrayList;
import java.util.Iterator;

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

			if (result.getData().getBoolean("ShowNotification")) {
				createNotification(result);
			}
		}		
	}

	private void showInfoMessage(Intent intent) {
		// Show Info Message
		String subject = intent.getExtras().getString("subject");
		String content = intent.getExtras().getString("content");
		String mid = intent.getExtras().getString("message_id");
		if(mid == null || mid.contentEquals("")){
			mid = "0";
		}
		int id = Integer.valueOf(mid);

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Create Intent and Set Extras
		Intent notificationIntent = new Intent(this, DisplayFile.class);

		notificationIntent.putExtra("Title", "Info:");
		notificationIntent.putExtra("Subject", subject);
		notificationIntent.putExtra("Content", content);
		notificationIntent.addCategory(subject);

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

		nm.notify(id, notification);

	}

	private void createNotification(ResultObject result) {
		// Create Notification

		ArrayList<String> postTitleList = result.getData().getStringArrayList("PostTitle");

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		String title;

		int postsDownloaded = result.getData().getInt("PostsDownloaded");
		if (postsDownloaded == 0) {
			title = "New jokes downloaded.";
		} else {
			title = postsDownloaded + " new joke(s) have been downloaded";
		}

		Intent notificationIntent = new Intent(this, Main.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
														.setContentTitle(title)
														.setContentInfo(String.valueOf(postsDownloaded))
														.setSmallIcon(R.drawable.ic_launcher)
														.setContentIntent(pendingIntent);

		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

		inboxStyle.setBigContentTitle(title);
		inboxStyle.setSummaryText("New jokes downloaded");

		// Add joke titles
		Iterator<String> i = postTitleList.listIterator();
		while (i.hasNext()) {
			inboxStyle.addLine(i.next());
		}

		// Moves the big view style object into the notification object.
		notifBuilder.setStyle(inboxStyle);

		Notification notification = notifBuilder.build();
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = title;
		notification.when = System.currentTimeMillis();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		nm.notify(1, notification);

	}

}