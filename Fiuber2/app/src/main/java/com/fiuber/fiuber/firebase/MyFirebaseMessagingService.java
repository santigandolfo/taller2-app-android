package com.fiuber.fiuber.firebase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fiuber.fiuber.Constants;
import com.fiuber.fiuber.server.ServerHandler;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    SharedPreferences mPreferences;

    private ServerHandler mServerHandler;


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


            Log.d(TAG, "Sending Intent");
            Intent lbcIntent = new Intent("rideAcceptanceMessage"); //Send to any reciever listening for this
            lbcIntent.putExtra("data", "This is my data!");  //Put whatever it is you want the activity to handle
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(lbcIntent);  //Send the intent

        }
    };

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived");

        mServerHandler = new ServerHandler(this.getApplicationContext());

        Log.d(TAG, "FROM:" + remoteMessage.getFrom());
        Log.d(TAG, "Notification Body:" + remoteMessage.getNotification().getTitle());
        Log.d(TAG, "Notification Body:" + remoteMessage.getNotification().getBody());
        if (remoteMessage.getData().size() > 0)
            Log.d(TAG, "Notification Data:" + remoteMessage.getData().toString());

        mPreferences = getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE);

        //TODO: Separar los direfentes tipos de notificaciones

        //Check if the message contains data
        if (remoteMessage.getData().size() > 0) {
            mPreferences.edit().putString(Constants.KEY_RIDE_ID, remoteMessage.getData().get("id")).apply();

            //TODO: ASK GONZA OR FEDE THE REAL NAME
/*
            mPreferences.edit().putString(Constants.KEY_LATITUDE_INITIAL, remoteMessage.getData().get("passenger_latitude")).apply();
            mPreferences.edit().putString(Constants.KEY_LONGITUDE_INITIAL, remoteMessage.getData().get("passenger_longitude")).apply();
            mPreferences.edit().putString(Constants.KEY_LATITUDE_FINAL, remoteMessage.getData().get("destination_latitude")).apply();
            mPreferences.edit().putString(Constants.KEY_LONGITUDE_FINAL, remoteMessage.getData().get("destination_longitude")).apply();
            mPreferences.edit().putString(Constants.KEY_DRIVER_TO_PASSENGER_DIRECTIONS, remoteMessage.getData().get("passenger_directions")).apply();
            mPreferences.edit().putString(Constants.KEY_PASSENGER_TO_DESTINATION_DIRECTIONS, remoteMessage.getData().get("destination_directions")).apply();
*/


            mServerHandler.getUserInformation(remoteMessage.getData().get("rider"), getUserInformationResponseListener, getUserInformationResponseErrorListener);

        }
    }
}
