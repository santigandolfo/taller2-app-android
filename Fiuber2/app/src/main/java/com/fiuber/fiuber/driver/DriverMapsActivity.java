package com.fiuber.fiuber.driver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.util.DirectionConverter;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fiuber.fiuber.LoginActivity;
import com.fiuber.fiuber.OtherProfileActivity;
import com.fiuber.fiuber.R;
import com.fiuber.fiuber.chat.ChatActivity;
import com.fiuber.fiuber.geofence.MyGeofence;
import com.fiuber.fiuber.Constants;
import com.fiuber.fiuber.server.ServerHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class DriverMapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, View.OnClickListener, LocationListener {

    private static final String TAG = "DriverMapsActivity";
    private GoogleMap mMap;
    private FirebaseAuth mAuth;

    private FusedLocationProviderClient mFusedLocationClient;

    protected Location lastLocation;
    LatLng lastKnownLocation;
    LatLng destination;
    LatLng passengerLocation;
    Marker currentLocationMarker;
    Marker destinationLocationMarker;
    Marker passengerLocationMarker;

    LocationManager locationManager;
    String locationProvider;

    private BottomSheetBehavior mBottomSheetBehavior;

    private DrawerLayout mDrawer;
    PolylineOptions mPasengerLineOptions;
    PolylineOptions mDestinationLineOptions;
    Polyline mPasengerPolyline;
    Polyline mDestinationPolyline;

    String GOOGLE_API_KEY = "AIzaSyAWcT3cBWZ75CxCgC5vq51iJmoZSUKnqyA";

    private ServerHandler mServerHandler;
    SharedPreferences mPreferences;

    private MyGeofence myGeofence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_driver_map);

        myGeofence = new MyGeofence(this);

        // BottomSheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Buttons
        findViewById(R.id.button_cancel).setOnClickListener(this);
        findViewById(R.id.button_chat).setOnClickListener(this);
        findViewById(R.id.button_view_profile).setOnClickListener(this);
        findViewById(R.id.button_start_trip).setOnClickListener(this);
        findViewById(R.id.button_finish_trip).setOnClickListener(this);

        mServerHandler = new ServerHandler(this.getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        mPreferences = getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mDrawer = findViewById(R.id.drawer_layout);

        updateUI();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLocationPermition();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    return;
                getLastLocation();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        LocalBroadcastManager.getInstance(this).registerReceiver(destinationReachedReceiver, new IntentFilter("activate_geofence"));
        LocalBroadcastManager.getInstance(this).registerReceiver(cancelRideReceiver, new IntentFilter("cancel_ride"));
        LocalBroadcastManager.getInstance(this).registerReceiver(acceptRideReceiver, new IntentFilter("accept_ride"));

        checkLocationPermition();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            finish();

        this.initializeLocationManager();

    }

    Response.ErrorListener startTripResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "startTrip Failed. Response Error: " + error.toString());
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                Log.e(TAG, "Getting user information Failed. Response statusCode: " + response.statusCode);
                Log.e(TAG, "Getting user information Failed. Response data: " + Arrays.toString(response.data));
            }
            Toast.makeText(getApplicationContext(), "Couldn't start trip", Toast.LENGTH_SHORT).show();
        }
    };

    Response.Listener<JSONObject> startTripResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "startTripResponseListener Successful. Response: " + response.toString());
            try {
                if (!"fail".equals(response.getString("status"))) {
                    myGeofence.startGeofencing(destination);
                    mPreferences.edit().putString(Constants.KEY_STATE, "on_ride").apply();
                    updateUI();
                } else {
                    Toast.makeText(getApplicationContext(), "Couldn't start trip", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void startTrip() {

        mServerHandler.startTrip(mPreferences.getString(Constants.KEY_USERNAME, ""),
                mPreferences.getString(Constants.KEY_PASSWORD, ""),
                mPreferences.getString(Constants.KEY_RIDE_ID, ""),
                startTripResponseListener, startTripResponseErrorListener);
    }

    Response.ErrorListener finishTripResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "finishTrip Failed. Response Error: " + error.toString());
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                Log.e(TAG, "Getting user information Failed. Response statusCode: " + response.statusCode);
                Log.e(TAG, "Getting user information Failed. Response data: " + Arrays.toString(response.data));
            }
            Toast.makeText(getApplicationContext(), "Couldn't finish trip", Toast.LENGTH_SHORT).show();
        }
    };

    Response.Listener<JSONObject> finishTripResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "finishTripResponseListener Successful. Response: " + response.toString());
            try {
                if (!"fail".equals(response.getString("status"))) {
                    mPreferences.edit().putString(Constants.KEY_STATE, "free").apply();
                    updateUI();
                    clearPassengerConstants();
                } else {
                    Toast.makeText(getApplicationContext(), "Couldn't finish trip", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    private void finishTrip() {

        mServerHandler.finishTrip(mPreferences.getString(Constants.KEY_USERNAME, ""),
                mPreferences.getString(Constants.KEY_PASSWORD, ""),
                mPreferences.getString(Constants.KEY_RIDE_ID, ""),
                finishTripResponseListener, finishTripResponseErrorListener);
    }

    private BroadcastReceiver destinationReachedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "destinationReached");

            if ("picking_up_passenger".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {
                mPreferences.edit().putString(Constants.KEY_STATE, "request_start_trip").apply();
                if (passengerLocationMarker != null)
                    passengerLocationMarker.remove();
                if (mPasengerPolyline != null)
                    mPasengerPolyline.remove();

            } else if ("on_ride".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {
                myGeofence.stopGeoFencing();
                mPreferences.edit().putString(Constants.KEY_STATE, "request_finish_trip").apply();
            }
            updateUI();
        }
    };

    private BroadcastReceiver cancelRideReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "cancelRide");
            cancelRideUpdate();
        }
    };

    private BroadcastReceiver acceptRideReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "acceptRide");

/*
            Toast.makeText(getApplicationContext(), "RECEIVED FIREBASE NOTIFICATION",
                    Toast.LENGTH_SHORT).show();
*/

            passengerLocation = new LatLng(Double.parseDouble(mPreferences.getString(Constants.KEY_LATITUDE_INITIAL, "0")),
                    Double.parseDouble(mPreferences.getString(Constants.KEY_LONGITUDE_INITIAL, "0")));
            destination = new LatLng(Double.parseDouble(mPreferences.getString(Constants.KEY_LATITUDE_FINAL, "0")),
                    Double.parseDouble(mPreferences.getString(Constants.KEY_LONGITUDE_FINAL, "0")));
            drawDirections();

            TextView mNameField = findViewById(R.id.text_passenger_name);
            String fullName = mPreferences.getString(Constants.KEY_OTHERS_FIRSTNAME, "") + " " + mPreferences.getString(Constants.KEY_OTHERS_LASTNAME, "");
            mNameField.setText(fullName);

            mPreferences.edit().putString(Constants.KEY_STATE, "picking_up_passenger").apply();
            //TODO: Uncomment this
            myGeofence.startGeofencing(passengerLocation);
            updateUI();
        }
    };


    Response.Listener<JSONObject> updateUserCoordinatesResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "updateUserCoordinatesResponseListener Successful. Response: " + response.toString());
            mPreferences.edit().putString(Constants.KEY_LATITUDE, String.valueOf(lastKnownLocation.latitude)).apply();
            mPreferences.edit().putString(Constants.KEY_LONGITUDE, String.valueOf(lastKnownLocation.latitude)).apply();
        }
    };

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        Log.d(TAG, "getLastLocation");

        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Log.d(TAG, "getLastLocation:all OK!");
                            lastLocation = task.getResult();
                            lastKnownLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            if (currentLocationMarker == null) {
                                currentLocationMarker = mMap.addMarker(new MarkerOptions()
                                        .position(lastKnownLocation)
                                        .title("Current Position"));
                            }
                            currentLocationMarker.setPosition(lastKnownLocation);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));

                            mServerHandler.updateUserCoordinates(mPreferences.getString(Constants.KEY_USERNAME, ""),
                                    mPreferences.getString(Constants.KEY_PASSWORD, ""),
                                    String.valueOf(lastKnownLocation.latitude),
                                    String.valueOf(lastKnownLocation.longitude),
                                    updateUserCoordinatesResponseListener);
                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        if (!mPreferences.getBoolean(Constants.KEY_LOGIN, false)) {
            Log.d(TAG, "change activity to LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
        }


        NavigationView navigationView = findViewById(R.id.nav_view);
        int size = navigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }

        String username = mPreferences.getString(Constants.KEY_USERNAME, "");
        String password = mPreferences.getString(Constants.KEY_PASSWORD, "");
        mServerHandler.sendFirebaseToken(username, password, FirebaseInstanceId.getInstance().getToken());

        updateUI();
    }


    private void initializeLocationManager() {

        //get the location manager
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //define the location manager criteria
        Criteria criteria = new Criteria();

        this.locationProvider = locationManager.getBestProvider(criteria, false);

        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(locationProvider);

        //initialize the location
        if (location != null) {

            onLocationChanged(location);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();

        this.locationManager.requestLocationUpdates(this.locationProvider, 400, 1, this);

        String username = mPreferences.getString(Constants.KEY_USERNAME, "");
        String password = mPreferences.getString(Constants.KEY_PASSWORD, "");
        mServerHandler.sendFirebaseToken(username, password, FirebaseInstanceId.getInstance().getToken());

        checkLocationPermition();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        myGeofence.reconnect();
        updateUI();
    }

    private Response.ErrorListener logoutCancelRideResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "logoutCancelRide Failed. Response Error: " + error.toString());
            mPreferences.edit().clear().apply();
            Log.d(TAG, "Change activity to LoginActivity");
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
    };

    Response.Listener<JSONObject> logoutCancelRideResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "logoutCancelRideResponseListener Successful. Response: " + response.toString());
            mPreferences.edit().clear().apply();
            Log.d(TAG, "Change activity to LoginActivity");
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));

        }
    };

    private void logout() {
        Log.d(TAG, "logout");
        mAuth.signOut();
        mServerHandler.setDriversDuty(mPreferences.getString(Constants.KEY_USERNAME, ""), mPreferences.getString(Constants.KEY_PASSWORD, ""), false, setDriverAsNotOnDutyResponseListener);
        if (!"free".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {
            cancelRide(logoutCancelRideResponseListener, logoutCancelRideResponseErrorListener);
            myGeofence.stopGeoFencing();
        } else {
            mPreferences.edit().clear().apply();
            Log.d(TAG, "Change activity to LoginActivity");
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
    }

    Response.Listener<JSONObject> setDriverAsNotOnDutyResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "setDriverAsNotOnDutyResponseListener Successful. Response: " + response.toString());
        }
    };

    @Override
    public void onBackPressed() {
        mDrawer = findViewById(R.id.drawer_layout);
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        Log.d(TAG, "navigationItemSelected");
        int id = item.getItemId();

        if (id == R.id.profile) {
            Log.d(TAG, "change activity to DriverProfileActivity");
            startActivity(new Intent(this, DriverProfileActivity.class));
        } else if (id == R.id.payment) {
            Log.d(TAG, "change activity to DriverPaymentActivity");
            startActivity(new Intent(this, DriverPaymentActivity.class));
        } else if (id == R.id.history) {
            Log.d(TAG, "change activity to DriverHistoryActivity");
            startActivity(new Intent(this, DriverHistoryActivity.class));
        } /*else if (id == R.id.settings) {
            Log.d(TAG, "change activity to DriverSettingsActivity");
            startActivity(new Intent(this, DriverSettingsActivity.class));

        } */else if (id == R.id.action_logout) {
            logout();
        }

        mDrawer = findViewById(R.id.drawer_layout);
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
        checkLocationPermition();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        getLastLocation();

    }

    private void clearRoute() {
        if (mPasengerPolyline != null)
            mPasengerPolyline.remove();
        if (mDestinationPolyline != null)
            mDestinationPolyline.remove();
        if (destinationLocationMarker != null)
            destinationLocationMarker.remove();
        if (passengerLocationMarker != null)
            passengerLocationMarker.remove();
    }

    private void clearMap() {
        clearRoute();
        if (currentLocationMarker != null)
            currentLocationMarker.remove();
    }

    private void updateUI() {
        Log.i(TAG, "updateUI");

        mBottomSheetBehavior.setPeekHeight(50);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        if ("free".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {
            findViewById(R.id.text_waiting_for_passenger).setVisibility(View.VISIBLE);
            findViewById(R.id.text_passenger_name).setVisibility(View.GONE);
            findViewById(R.id.button_view_profile).setVisibility(View.GONE);
            findViewById(R.id.button_chat).setVisibility(View.GONE);
            findViewById(R.id.button_cancel).setVisibility(View.GONE);
            findViewById(R.id.button_start_trip).setVisibility(View.GONE);
            findViewById(R.id.button_finish_trip).setVisibility(View.GONE);
            clearRoute();
        } else if ("request_start_trip".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {
            findViewById(R.id.text_waiting_for_passenger).setVisibility(View.GONE);
            findViewById(R.id.text_passenger_name).setVisibility(View.GONE);
            findViewById(R.id.button_view_profile).setVisibility(View.GONE);
            findViewById(R.id.button_chat).setVisibility(View.GONE);
            findViewById(R.id.button_cancel).setVisibility(View.VISIBLE);
            findViewById(R.id.button_start_trip).setVisibility(View.VISIBLE);
            findViewById(R.id.button_finish_trip).setVisibility(View.GONE);
        } else if ("picking_up_passenger".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {
            findViewById(R.id.text_waiting_for_passenger).setVisibility(View.GONE);
            findViewById(R.id.text_passenger_name).setVisibility(View.VISIBLE);
            findViewById(R.id.button_view_profile).setVisibility(View.VISIBLE);
            findViewById(R.id.button_chat).setVisibility(View.VISIBLE);
            findViewById(R.id.button_cancel).setVisibility(View.VISIBLE);
            findViewById(R.id.button_start_trip).setVisibility(View.GONE);
            findViewById(R.id.button_finish_trip).setVisibility(View.GONE);
        } else if ("on_ride".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {
            findViewById(R.id.text_waiting_for_passenger).setVisibility(View.GONE);
            findViewById(R.id.text_passenger_name).setVisibility(View.GONE);
            findViewById(R.id.button_view_profile).setVisibility(View.VISIBLE);
            findViewById(R.id.button_chat).setVisibility(View.VISIBLE);
            findViewById(R.id.button_cancel).setVisibility(View.VISIBLE);
            findViewById(R.id.button_start_trip).setVisibility(View.GONE);
            findViewById(R.id.button_finish_trip).setVisibility(View.GONE);
        } else if ("request_finish_trip".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {
            findViewById(R.id.text_waiting_for_passenger).setVisibility(View.GONE);
            findViewById(R.id.text_passenger_name).setVisibility(View.GONE);
            findViewById(R.id.button_view_profile).setVisibility(View.GONE);
            findViewById(R.id.button_chat).setVisibility(View.GONE);
            findViewById(R.id.button_cancel).setVisibility(View.VISIBLE);
            findViewById(R.id.button_start_trip).setVisibility(View.GONE);
            findViewById(R.id.button_finish_trip).setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_chat) {
            Log.d(TAG, "clicked chat button");
            chat();
        } else if (i == R.id.button_view_profile) {
            Log.d(TAG, "clicked view_profile button");
            viewPassengersProfile();
        } else if (i == R.id.button_cancel) {
            Log.d(TAG, "clicked cancel button");
            cancelRide(cancelRideResponseListener, cancResponseErrorListener);
        } else if (i == R.id.button_start_trip) {
            Log.d(TAG, "clicked start trip");
            startTrip();
        } else if (i == R.id.button_finish_trip) {
            Log.d(TAG, "clicked finish trip");
            finishTrip();
        }
    }

    private void drawDirections() {
        Log.i(TAG, "drawDirections");
        clearMap();

        ArrayList<LatLng> passengerLocationWaypoints = (ArrayList<LatLng>) PolyUtil.decode(mPreferences.getString(Constants.KEY_DRIVER_TO_PASSENGER_DIRECTIONS, ""));
        ArrayList<LatLng> destinationWaypoints = (ArrayList<LatLng>) PolyUtil.decode(mPreferences.getString(Constants.KEY_PASSENGER_TO_DESTINATION_DIRECTIONS, ""));

        //Add origin marker
        currentLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(lastKnownLocation));

        //Add destination marker
        passengerLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(passengerLocation));

        //Add destination marker
        destinationLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(destination));

        //Start drawing route
        mPasengerLineOptions = DirectionConverter.createPolyline(getApplicationContext(), passengerLocationWaypoints, 5, R.color.colorPrimary);
        mPasengerPolyline = mMap.addPolyline(mPasengerLineOptions);
        //Finish drawing route

        //Start drawing route
        mDestinationLineOptions = DirectionConverter.createPolyline(getApplicationContext(), destinationWaypoints, 5, R.color.colorPrimary);
        mDestinationPolyline = mMap.addPolyline(mDestinationLineOptions);
        //Finish drawing route

        //fit directions to screen
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(lastKnownLocation);
        builder.include(passengerLocation);
        builder.include(destination);
        LatLngBounds bounds = builder.build();
        int padding = 150; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);

        Log.i(TAG, "drawDirections finished");
    }

    private void cancelRideUpdate() {
        mPreferences.edit().putString(Constants.KEY_STATE, "free").apply();
        mPreferences.edit().remove(Constants.KEY_RIDE_ID).apply();
        updateUI();
        myGeofence.stopGeoFencing();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));
    }

    private Response.ErrorListener cancResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "cancelRide Failed. Response Error: " + error.toString());
            cancelRideUpdate();
        }
    };

    Response.Listener<JSONObject> cancelRideResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "cancelRideResponseListener Successful. Response: " + response.toString());
            cancelRideUpdate();
        }
    };


    private void cancelRide(Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.i(TAG, "cancelRide");

        if (!"free".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {
            mServerHandler.cancelRide(mPreferences.getString(Constants.KEY_USERNAME, ""),
                    mPreferences.getString(Constants.KEY_USERNAME, ""),
                    mPreferences.getString(Constants.KEY_RIDE_ID, ""),
                    responseListener, responseErrorListener);
        } else {
            cancelRideUpdate();
        }
    }

    private void chat() {
        Log.i(TAG, "chat");
        startActivity(new Intent(getApplicationContext(), ChatActivity.class));
    }

    private void viewPassengersProfile() {
        Log.i(TAG, "viewDriverProfile");
        startActivity(new Intent(getApplicationContext(), OtherProfileActivity.class));
    }

    private void checkLocationPermition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");

        checkLocationPermition();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        getLastLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void clearPassengerConstants() {
        mPreferences.edit().putString(Constants.KEY_OTHERS_FIRSTNAME, "").apply();
        mPreferences.edit().putString(Constants.KEY_OTHERS_LASTNAME, "").apply();
        mPreferences.edit().putString(Constants.KEY_OTHERS_EMAIL, "").apply();
        mPreferences.edit().putString(Constants.KEY_OTHERS_USERNAME, "").apply();

        mPreferences.edit().putString(Constants.KEY_OTHERS_CAR_MODEL, "").apply();
        mPreferences.edit().putString(Constants.KEY_OTHERS_CAR_COLOR, "").apply();
        mPreferences.edit().putString(Constants.KEY_OTHERS_CAR_BRAND, "").apply();
        mPreferences.edit().putString(Constants.KEY_OTHERS_CAR_YEAR, "").apply();

        mPreferences.edit().putString(Constants.KEY_RIDE_ID, "").apply();

        mPreferences.edit().putString(Constants.KEY_DRIVER_TO_PASSENGER_DIRECTIONS, "").apply();
        mPreferences.edit().putString(Constants.KEY_PASSENGER_TO_DESTINATION_DIRECTIONS, "").apply();

        mPreferences.edit().putString(Constants.KEY_LATITUDE_INITIAL, "0").apply();
        mPreferences.edit().putString(Constants.KEY_LONGITUDE_INITIAL, "0").apply();
        mPreferences.edit().putString(Constants.KEY_LATITUDE_FINAL, "0").apply();
        mPreferences.edit().putString(Constants.KEY_LONGITUDE_FINAL, "0").apply();
    }

}