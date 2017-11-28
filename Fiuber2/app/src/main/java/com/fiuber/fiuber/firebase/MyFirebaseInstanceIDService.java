package com.fiuber.fiuber.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.fiuber.fiuber.Constants;
import com.fiuber.fiuber.server.ServerHandler;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseInsIDService";

    SharedPreferences mPreferences;


    @Override
    public void onTokenRefresh() {
        Log.d(TAG, "onTokenRefresh");
        //Get updated token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "New Token: " + refreshedToken);

        ServerHandler mServerHandler = new ServerHandler(this.getApplicationContext());
        mPreferences = getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE);

         String username = mPreferences.getString(Constants.KEY_USERNAME, "");
        String password = mPreferences.getString(Constants.KEY_PASSWORD, "");

        if (!username.equals(""))
            mServerHandler.sendFirebaseToken(username, password, refreshedToken);
    }
}
