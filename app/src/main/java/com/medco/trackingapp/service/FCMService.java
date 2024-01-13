package com.medco.trackingapp.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.medco.trackingapp.R;
import com.medco.trackingapp.activity.LandingActivity;
import com.medco.trackingapp.activity.MainActivity;
import com.medco.trackingapp.utils.Channel;

public class FCMService extends FirebaseMessagingService {
	public static final String TAG = "Firebase Messaging Service";
	//firebase
	public FirebaseUser firebaseUser;
	public FirebaseFirestore firebaseFirestore;
	public DocumentReference mCurrentUserRef;
	public CollectionReference userColl;
	public CollectionReference reportColl;
	private NotificationManagerCompat nmc;

	@Override
	public void onMessageReceived(@NonNull RemoteMessage message) {
		super.onMessageReceived(message);
		nmc = NotificationManagerCompat.from(this);

		//firebase
		FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();

		firebaseFirestore = FirebaseFirestore.getInstance();
		userColl = firebaseFirestore.collection(getString(R.string.collection_user));
		reportColl = firebaseFirestore.collection(getString(R.string.collection_user));

		if (firebaseUser != null) {
			mCurrentUserRef = firebaseFirestore.collection(getString(R.string.collection_user))
				.document(firebaseUser.getUid());
		}

		Log.d(TAG, "From: " + message.getFrom());
		if (message.getNotification() != null) {
			Log.d(TAG, "Message Notification Title: " + message.getNotification().getTitle());
			Log.d(TAG, "Message Notification Body: " + message.getNotification().getBody());
		}
		Log.d(TAG, "Message data payload: " + message.getData());

		// Check if message contains a data payload.
		if (message.getData().size() > 0) {
			sendNotification(message.getData().get("title"),
				message.getData().get("body"),
				message.getData().get("key_1"),
				message.getData().get("key_2"));
			return;
		}

		if (message.getNotification() == null) return;
		sendNotification(message.getNotification().getTitle(), message.getNotification().getBody(),
			null, null);
	}

	@Override
	public void onNewToken(@NonNull String token) {
		super.onNewToken(token);
	}

	@SuppressLint("MissingPermission")
	private void sendNotification(String title, String body, String key_1, String key_2) {
		//key_1 berisi path complaint

		Intent resultIntent;

		if (mCurrentUserRef == null || key_1 == null) {
			resultIntent = new Intent(this, LandingActivity.class);
		} else {
			resultIntent = new Intent(this, MainActivity.class);

			/*DocumentReference ref = firebaseFirestore.document(key_1);

			if (ref.getParent().equals(userColl)) {
				if (mCurrentUserRef.getPath().equals(key_1)) {
					resultIntent = new Intent(this, MainActivity.class);
				} else {
					resultIntent = new Intent(this, DetailUserActivity.class);
				}
			} else if (ref.getParent().equals(reportColl)) {
				resultIntent = new Intent(this, ReportActivity.class);
			} else {
				resultIntent = new Intent(this, ComplaintActivity.class);
			}*/

			resultIntent.putExtra("path", key_1);
		}

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addNextIntentWithParentStack(resultIntent);

		PendingIntent resultPendingIntent;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			resultPendingIntent =
				stackBuilder.getPendingIntent(0, PendingIntent
					.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		} else {
			resultPendingIntent =
				stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		Notification notification = new NotificationCompat.Builder(this,
			Channel.CHANNEL_1_ID)
			.setSmallIcon(R.drawable.logo)
			.setContentTitle(title)
			.setContentText(body)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setCategory(NotificationCompat.CATEGORY_MESSAGE)
			.setContentIntent(resultPendingIntent)
			.setAutoCancel(true)
			.build();

		Log.d(TAG, "sendNotification: Show notification payload");
		nmc.notify(1, notification);
	}
}
