package org.varunverma.desijokes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.DateInterval;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.ayansh.CommandExecuter.Command;
import com.ayansh.CommandExecuter.Invoker;
import com.ayansh.CommandExecuter.ProgressInfo;
import com.ayansh.CommandExecuter.ResultObject;
import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.HanuFCMMessagingService;
import com.ayansh.hanudroid.Post;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AppGcmListenerService extends HanuFCMMessagingService {

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {

		// Be Safe. Set Context.
		Application app = Application.getApplicationInstance();
		app.setContext(getApplicationContext());

		String message = remoteMessage.getData().get("message");

		if(message.contentEquals("InfoMessage")){
			// Show message.
			showInfoMessage(remoteMessage.getData());
		}
		else {

			ResultObject result = processMessage(remoteMessage);
			if (result.getData().getBoolean("ShowNotification")) {
				notifyNewContent(result);
			}

			if(message.contentEquals("PerformSync")){
				deleteOldPosts();
			}

		}
	}

	private void showInfoMessage(Map<String,String> data) {
		// Show Info Message

		String subject = data.get("subject");
		String content = data.get("content");
		String mid = data.get("message_id");
		String message = data.get("message");

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

		Notification notification = new NotificationCompat.Builder(this, "INFO_MESSAGE")
				.setContentTitle(subject)
				.setContentText(content)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentIntent(pendingIntent).build();

		notification.icon = R.mipmap.ic_launcher;
		notification.tickerText = subject;
		notification.when = System.currentTimeMillis();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		nm.notify(id, notification);
	}

	private void notifyNewContent(ResultObject result) {
		// Create Notification

		ArrayList<String> postTitleList = result.getData().getStringArrayList("PostTitle");

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		String title;
		String text = postTitleList.get(0);

		int postsDownloaded = result.getData().getInt("PostsDownloaded");
		if (postsDownloaded == 0) {
			title = "New Jokes downloaded.";
		} else {
			title = postsDownloaded + " new joke(s) have been downloaded";
		}

		Intent notificationIntent = new Intent(this, Main.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
		inboxStyle.setBigContentTitle(title + ": " + text);

		// Add joke titles
		Iterator<String> i = postTitleList.listIterator();
		while (i.hasNext()) {
			inboxStyle.addLine(i.next());
		}

		Notification notification = new NotificationCompat.Builder(this, "NEW_CONTENT")
				.setContentTitle(title)
				.setContentText(text)
				.setContentInfo(String.valueOf(postsDownloaded))
				.setSmallIcon(R.mipmap.ic_launcher)
				.setStyle(inboxStyle)
				.setContentIntent(pendingIntent).build();

		notification.icon = R.mipmap.ic_launcher;
		notification.tickerText = title;
		notification.when = System.currentTimeMillis();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		nm.notify(1, notification);

	}

	private void deleteOldPosts(){

		DeleteOldPostsCommand command = new DeleteOldPostsCommand(Command.DUMMY_CALLER);
		command.execute();
	}

}