package com.fiuber.fiuber.geofence;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofenceRegistrationService extends IntentService {

    private static final String TAG = "GeoIntentService";

    public static final String GEOFENCE_ID = "ID";

    public GeofenceRegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.d(TAG, "GeofencingEvent error " + geofencingEvent.getErrorCode());
        } else {
            int transaction = geofencingEvent.getGeofenceTransition();
            List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();
            Geofence geofence = geofences.get(0);
            if (transaction == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.d(TAG, "You are inside Stanford University");

                Intent lbcIntent = new Intent("googlegeofence"); //Send to any reciever listening for this
                lbcIntent.putExtra("data", "This is my data!");  //Put whatever it is you want the activity to handle
                LocalBroadcastManager.getInstance(this).sendBroadcast(lbcIntent);  //Send the intent

            } else {
                Log.d(TAG, "You are outside Stanford University");
            }
        }
    }
}