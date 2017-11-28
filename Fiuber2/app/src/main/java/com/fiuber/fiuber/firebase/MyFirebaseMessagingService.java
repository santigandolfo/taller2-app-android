package com.fiuber.fiuber.firebase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.fiuber.fiuber.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";


    SharedPreferences mPreferences;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived");


        Log.d(TAG, "FROM:" + remoteMessage.getFrom());
        Log.d(TAG, "Full Notification:" + remoteMessage.toString());

        mPreferences = getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE);

        //Check if the message contains data
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
            mPreferences.edit().putString(Constants.KEY_OTHERS_FIRSTNAME, remoteMessage.getData().get(Constants.KEY_FIRSTNAME)).apply();
            mPreferences.edit().putString(Constants.KEY_OTHERS_LASTNAME, remoteMessage.getData().get(Constants.KEY_LASTNAME)).apply();
            mPreferences.edit().putString(Constants.KEY_OTHERS_USERNAME, remoteMessage.getData().get(Constants.KEY_USERNAME)).apply();
            mPreferences.edit().putString(Constants.KEY_OTHERS_EMAIL, remoteMessage.getData().get(Constants.KEY_EMAIL)).apply();

            if (remoteMessage.getData().containsKey(Constants.KEY_CAR_MODEL)) {
                mPreferences.edit().putString(Constants.KEY_OTHERS_CAR_MODEL, remoteMessage.getData().get(Constants.KEY_CAR_MODEL)).apply();
                mPreferences.edit().putString(Constants.KEY_OTHERS_CAR_COLOR, remoteMessage.getData().get(Constants.KEY_CAR_COLOR)).apply();
                mPreferences.edit().putString(Constants.KEY_OTHERS_CAR_BRAND, remoteMessage.getData().get(Constants.KEY_CAR_BRAND)).apply();
                mPreferences.edit().putString(Constants.KEY_OTHERS_CAR_YEAR, remoteMessage.getData().get(Constants.KEY_CAR_YEAR)).apply();
            }
        }

        Log.d(TAG, "Sending Intent");
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

    /*
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
