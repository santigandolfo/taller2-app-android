package com.fiuber.fiuber.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fiuber.fiuber.server.ServerHandler;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseInsIDService";

    String MY_PREFERENCES = "MyPreferences";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    SharedPreferences mPreferences;


    @Override
    public void onTokenRefresh() {
        Log.d(TAG, "onTokenRefresh");
        //Get updated token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "New Token: " + refreshedToken);

        ServerHandler mServerHandler = new ServerHandler(this.getApplicationContext());
        mPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

         String username = mPreferences.getString(KEY_USERNAME, "");
        String password = mPreferences.getString(KEY_PASSWORD, "");

        //mServerHandler.sendFirebaseToken(username, password, refreshedToken);
    }
}
