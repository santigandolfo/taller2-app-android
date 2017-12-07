package com.fiuber.fiuber.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fiuber.fiuber.Constants;
import com.fiuber.fiuber.R;
import com.fiuber.fiuber.driver.DriverMapsActivity;
import com.fiuber.fiuber.passenger.PassengerMapsActivity;
import com.fiuber.fiuber.server.ServerHandler;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    SharedPreferences mPreferences;


    Response.ErrorListener getUserInformationResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Getting user information Failed. Response Error: " + error.toString());
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                Log.e(TAG, "Getting user information Failed. Response statusCode: " + response.statusCode);
                Log.e(TAG, "Getting user information Failed. Response data: " + Arrays.toString(response.data));
            }
        }
    };

    Response.Listener<JSONObject> getUserInformationResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "Getting user information Successful. Response: " + response.toString());
            try {
                mPreferences.edit().putString(Constants.KEY_OTHERS_FIRSTNAME, response.getJSONObject(Constants.KEY_INFO).getString(Constants.KEY_FIRSTNAME)).apply();
                mPreferences.edit().putString(Constants.KEY_OTHERS_LASTNAME, response.getJSONObject(Constants.KEY_INFO).getString(Constants.KEY_LASTNAME)).apply();
                mPreferences.edit().putString(Constants.KEY_OTHERS_USERNAME, response.getJSONObject(Constants.KEY_INFO).getString(Constants.KEY_USERNAME)).apply();
                mPreferences.edit().putString(Constants.KEY_OTHERS_EMAIL, response.getJSONObject(Constants.KEY_INFO).getString(Constants.KEY_EMAIL)).apply();
                mPreferences.edit().putString(Constants.KEY_OTHERS_TYPE, response.getJSONObject(Constants.KEY_INFO).getString(Constants.KEY_TYPE)).apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mPreferences.edit().putBoolean(Constants.KEY_LOGIN, true).apply();


            Log.d(TAG, "Sending acceptRide Intent");
            Intent lbcIntent = new Intent("accept_ride"); //Send to any reciever listening for this
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(lbcIntent);  //Send the intent

        }
    };

    public void sendNotification(String title, String text, Class to) {
        Log.d(TAG, "sendNotification");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this);

//Create the intent that’ll fire when the user taps the notification//

        Intent intent = new Intent(this, to);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        mBuilder.setContentIntent(pendingIntent);

        mBuilder.setSmallIcon(R.drawable.notification_icon);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);

        NotificationManager mNotificationManager =

                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager != null) {
            mNotificationManager.notify(001, mBuilder.build());
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived");

        ServerHandler mServerHandler = new ServerHandler(this.getApplicationContext());

        Log.d(TAG, "FROM:" + remoteMessage.getFrom());
        Log.d(TAG, "Notification Body:" + remoteMessage.getNotification().getTitle());
        Log.d(TAG, "Notification Body:" + remoteMessage.getNotification().getBody());
        if (remoteMessage.getData().size() > 0)
            Log.d(TAG, "Notification Data:" + remoteMessage.getData().toString());

        mPreferences = getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE);

        //Check if the message contains data
        if ("trip_assigned".equals(remoteMessage.getNotification().getBody())) {
            Log.d(TAG, "Saving trip data");
            mPreferences.edit().putString(Constants.KEY_RIDE_ID, remoteMessage.getData().get("id")).apply();
            sendNotification("Trip", "You have been asigned a ride!", DriverMapsActivity.class);
            try {
                JSONObject data = new JSONObject(remoteMessage.getData().get("trip_coordinates"));
                mPreferences.edit().putString(Constants.KEY_LATITUDE_INITIAL, data.getString(Constants.KEY_LATITUDE_INITIAL)).apply();
                mPreferences.edit().putString(Constants.KEY_LONGITUDE_INITIAL, data.getString(Constants.KEY_LONGITUDE_INITIAL)).apply();
                mPreferences.edit().putString(Constants.KEY_LATITUDE_FINAL, data.getString(Constants.KEY_LATITUDE_FINAL)).apply();
                mPreferences.edit().putString(Constants.KEY_LONGITUDE_FINAL, data.getString(Constants.KEY_LONGITUDE_FINAL)).apply();
                mPreferences.edit().putString(Constants.KEY_DRIVER_TO_PASSENGER_DIRECTIONS, remoteMessage.getData().get("directions_to_passenger")).apply();
                mPreferences.edit().putString(Constants.KEY_PASSENGER_TO_DESTINATION_DIRECTIONS, remoteMessage.getData().get("directions_trip")).apply();

                mServerHandler.getUserInformation(remoteMessage.getData().get("rider"), getUserInformationResponseListener, getUserInformationResponseErrorListener);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mServerHandler.getUserInformation(remoteMessage.getData().get("rider"), getUserInformationResponseListener, getUserInformationResponseErrorListener);
        } else if ("trip_cancelled".equals(remoteMessage.getNotification().getBody())) {
            Log.d(TAG, "Sending cancelRide Intent");
            Intent lbcCancelIntent = new Intent("cancel_ride"); //Send to any reciever listening for this
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(lbcCancelIntent);  //Send the intent
        } else if ("trip_finished".equals(remoteMessage.getNotification().getBody())) {
            Log.d(TAG, "Sending finishTrip Intent");
            mPreferences.edit().putFloat(Constants.KEY_COST, Float.parseFloat(remoteMessage.getData().get(Constants.KEY_COST))).apply();
            Intent lbcFinishIntent = new Intent("finish_trip"); //Send to any reciever listening for this
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(lbcFinishIntent);  //Send the intent
        } else if ("trip_started".equals(remoteMessage.getNotification().getBody())) {
            Log.d(TAG, "Sending startTrip Intent");
            sendNotification("Trip", "Your driver is here!", PassengerMapsActivity.class);
            Intent lbcFinishIntent = new Intent("start_trip"); //Send to any reciever listening for this
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(lbcFinishIntent);  //Send the intent
        }

    }
}
