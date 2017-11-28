package com.fiuber.fiuber.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.fiuber.fiuber.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class MyGeofence implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MyGeofence";

    private GoogleApiClient googleApiClient;
    private PendingIntent pendingIntent;
    private Context mAplicationContext;

    public MyGeofence(Context aplicationContext) {

        mAplicationContext = aplicationContext;

        googleApiClient = new GoogleApiClient.Builder(aplicationContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
    }

    public void reconnect() {
        Log.e(TAG, "reconnect");
        googleApiClient.reconnect();
    }

    public void disconnect() {
        Log.e(TAG, "disconnect");
        googleApiClient.disconnect();
    }

    public void startGeofencing(LatLng latLng) {
        Log.d(TAG, "startGeofencing");
        pendingIntent = getGeofencePendingIntent();
        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
                .addGeofence(getGeofence(latLng))
                .build();

        if (!googleApiClient.isConnected()) {
            Log.d(TAG, "Google API client not connected");
        } else {
            try {
                LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.d(TAG, "Successfully Geofencing Connected");
                        } else {
                            Log.d(TAG, "Failed to add Geofencing " + status.getStatus());
                        }
                    }
                });
            } catch (SecurityException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    @NonNull
    private Geofence getGeofence(LatLng latLng) {
        Log.d(TAG, "getGeofence");
        return new Geofence.Builder()
                .setRequestId(Constants.GEOFENCE_ID)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(latLng.latitude, latLng.longitude, Constants.GEOFENCE_RADIUS_IN_METERS)
                .setNotificationResponsiveness(1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Log.d(TAG, "getGeofencePendingIntent");
        if (pendingIntent != null) {
            return pendingIntent;
        }
        Intent intent = new Intent(mAplicationContext, GeofenceRegistrationService.class);
        return PendingIntent.getService(mAplicationContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    public void stopGeoFencing() {
        Log.d(TAG, "stopGeoFencing");
        pendingIntent = getGeofencePendingIntent();
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, pendingIntent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess())
                            Log.d(TAG, "Stop geofencing");
                        else
                            Log.d(TAG, "Not stop geofencing");
                    }
                });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google Api Client Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google Connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection Failed:" + connectionResult.getErrorMessage());
    }
}