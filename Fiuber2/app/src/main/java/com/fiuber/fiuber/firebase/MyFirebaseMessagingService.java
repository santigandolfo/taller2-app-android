package com.fiuber.fiuber.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fiuber.fiuber.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    String MY_PREFERENCES = "MyPreferences";

    private static final String KEY_TYPE = "type";

    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";

    private static final String KEY_CAR_MODEL = "model";
    private static final String KEY_CAR_COLOR = "color";
    private static final String KEY_CAR_PLATE = "plate";
    private static final String KEY_CAR_YEAR = "year";


    private static final String KEY_OTHERS_FIRSTNAME = "others_firstname";
    private static final String KEY_OTHERS_LASTNAME = "others_lastname";
    private static final String KEY_OTHERS_EMAIL = "others_email";
    private static final String KEY_OTHERS_USERNAME = "others_username";

    private static final String KEY_OTHERS_CAR_MODEL = "others_car_mode";
    private static final String KEY_OTHERS_CAR_COLOR = "others_car_color";
    private static final String KEY_OTHERS_CAR_PLATE = "others_car_plate";
    private static final String KEY_OTHERS_CAR_YEAR = "others_car_year";

    SharedPreferences mPreferences;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived");


        Log.d(TAG, "FROM:" + remoteMessage.getFrom());

        mPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        //Check if the message contains data
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
            mPreferences.edit().putString(KEY_FIRSTNAME, remoteMessage.getData().get(KEY_OTHERS_FIRSTNAME)).apply();
            mPreferences.edit().putString(KEY_LASTNAME, remoteMessage.getData().get(KEY_OTHERS_LASTNAME)).apply();
            mPreferences.edit().putString(KEY_USERNAME, remoteMessage.getData().get(KEY_OTHERS_USERNAME)).apply();
            mPreferences.edit().putString(KEY_EMAIL, remoteMessage.getData().get(KEY_OTHERS_EMAIL)).apply();

            if (remoteMessage.getData().containsKey(KEY_CAR_MODEL)) {
                mPreferences.edit().putString(KEY_CAR_MODEL, remoteMessage.getData().get(KEY_OTHERS_CAR_MODEL)).apply();
                mPreferences.edit().putString(KEY_CAR_COLOR, remoteMessage.getData().get(KEY_OTHERS_CAR_COLOR)).apply();
                mPreferences.edit().putString(KEY_CAR_PLATE, remoteMessage.getData().get(KEY_OTHERS_CAR_PLATE)).apply();
                mPreferences.edit().putString(KEY_CAR_YEAR, remoteMessage.getData().get(KEY_OTHERS_CAR_YEAR)).apply();
            }
        }

        Intent lbcIntent = new Intent("rideAcceptanceMessage"); //Send to any reciever listening for this
        lbcIntent.putExtra("data", "This is my data!");  //Put whatever it is you want the activity to handle
        LocalBroadcastManager.getInstance(this).sendBroadcast(lbcIntent);  //Send the intent
/*
        //Check if the message contains notification

        if(remoteMessage.getNotification() != null) {
            Log.d(TAG, "Mesage body:" + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody());
        }*/
    }

    /**
     * Dispay the notification
     * @param body
     */
/*    private void sendNotification(String body) {

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0*//*Request code*//*, intent, PendingIntent.FLAG_ONE_SHOT);
        //Set sound of notification
        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notifiBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Firebase Cloud Messaging")
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 *//*ID of notification*//*, notifiBuilder.build());

        Intent lbcIntent = new Intent("rideAcceptedMessage"); //Send to any reciever listening for this
        lbcIntent.putExtra("data", "This is my data!");  //Put whatever it is you want the activity to handle
        LocalBroadcastManager.getInstance(this).sendBroadcast(lbcIntent);  //Send the intent

    }*/
}
