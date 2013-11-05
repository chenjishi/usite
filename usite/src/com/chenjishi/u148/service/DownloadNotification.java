package com.chenjishi.u148.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.chenjishi.u148.R;
import com.chenjishi.u148.activity.HomeActivity;

public class DownloadNotification {
	
	private Context mContext;
	private NotificationManager notificationManager;
	private static final int NOTIFY_ID = 1;
	
	public DownloadNotification(Context context) {
		this.mContext=context;
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void updateNotification(int mBytesSoFar, int mTotalBytes) {
		Notification n = new Notification();

		int iconResource = android.R.drawable.stat_sys_download;

		n.icon = iconResource;

		n.flags |= Notification.FLAG_ONGOING_EVENT;

		// Build the RemoteView object
		RemoteViews expandedView = new RemoteViews(mContext.getPackageName(),
				R.layout.status_bar_ongoing_event_progress_bar);
		expandedView.setTextViewText(R.id.title, mContext.getString(R.string.updating_app));
		expandedView.setProgressBar(R.id.progress_bar, mTotalBytes, mBytesSoFar, mTotalBytes == -1);
		expandedView.setTextViewText(R.id.progress_text, getDownloadingText(mTotalBytes, mBytesSoFar));
		n.contentView = expandedView;

		Intent notificationIntent = new Intent(mContext, HomeActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
		n.contentIntent = contentIntent;

		notificationManager.notify(NOTIFY_ID, n);
	}
	
	public void removeNotification() {
		notificationManager.cancel(NOTIFY_ID);
	}

	private String getDownloadingText(long totalBytes, long currentBytes) {
		if (totalBytes <= 0) {
			return "";
		}
		long progress = currentBytes * 100 / totalBytes;
		StringBuilder sb = new StringBuilder();
		sb.append(progress);
		sb.append('%');
		return sb.toString();
	}
}
