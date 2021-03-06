package com.fiuber.fiuber.passenger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v4.app.NotificationCompat;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fiuber.fiuber.AddPaymentActivity;
import com.fiuber.fiuber.Constants;
import com.fiuber.fiuber.LoginActivity;
import com.fiuber.fiuber.OtherProfileActivity;
import com.fiuber.fiuber.R;
import com.fiuber.fiuber.chat.ChatActivity;
import com.fiuber.fiuber.server.ServerHandler;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class PassengerMapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, PlaceSelectionListener, View.OnClickListener, LocationListener {

    private static final String TAG = "PassengerMapsActivity";
    private GoogleMap mMap;
    private FirebaseAuth mAuth;

    private FusedLocationProviderClient mFusedLocationClient;

    protected Location lastLocation;
    LatLng lastKnownLocation;
    LatLng destination;
    Marker currentLocationMarker;
    Marker destinationLocationMarker;

    LocationManager locationManager;
    String locationProvider;

    private BottomSheetBehavior mBottomSheetBehavior;

    private DrawerLayout mDrawer;
    PolylineOptions mLineOptions;
    Polyline mPolyline;

    String GOOGLE_API_KEY = "AIzaSyAWcT3cBWZ75CxCgC5vq51iJmoZSUKnqyA";

    private ServerHandler mServerHandler;
    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_passenger_map);

        checkLocationPermition();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            finish();

        // BottomSheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Buttons
        findViewById(R.id.button_cancel).setOnClickListener(this);
        findViewById(R.id.button_request_ride).setOnClickListener(this);
        findViewById(R.id.button_pay_ride).setOnClickListener(this);
        findViewById(R.id.button_chat).setOnClickListener(this);
        findViewById(R.id.button_view_profile).setOnClickListener(this);

        mServerHandler = new ServerHandler(this.getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        mPreferences = getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mDrawer = findViewById(R.id.drawer_layout);

        updateUI();

        findViewById(R.id.iv_menu_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawer.openDrawer(GravityCompat.START);
            }
        });

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


        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);


        autocompleteFragment.setOnPlaceSelectedListener(this);
        autocompleteFragment.setHint("Where do you want to go?");

        autocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        autocompleteFragment.setText("");
                        view.setVisibility(View.GONE);
                        mPreferences.edit().putString(Constants.KEY_STATE, "free").apply();
                        updateUI();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));
                    }
                });

        LocalBroadcastManager.getInstance(this).registerReceiver(startTripReceiver, new IntentFilter("start_trip"));
        LocalBroadcastManager.getInstance(this).registerReceiver(finishTripReceiver, new IntentFilter("finish_trip"));
        LocalBroadcastManager.getInstance(this).registerReceiver(cancelRideReceiver, new IntentFilter("cancel_ride"));

        this.initializeLocationManager();

    }

    private void setTextForDisplay(){
        if (!"".equals(mPreferences.getString(Constants.KEY_OTHERS_FIRSTNAME, ""))) {
            TextView mNameField = findViewById(R.id.text_driver_name);
            String fullName = mPreferences.getString(Constants.KEY_OTHERS_FIRSTNAME, "") + " " + mPreferences.getString(Constants.KEY_OTHERS_LASTNAME, "");
            mNameField.setText(fullName);
        }

        if (0 != mPreferences.getFloat(Constants.KEY_ESTIMATED_COST, 0)) {
            TextView textEstimatedCost = findViewById(R.id.text_estimated_cost);
            DecimalFormat df = new DecimalFormat("#.##");
            textEstimatedCost.setText("~$" + df.format(mPreferences.getFloat(Constants.KEY_ESTIMATED_COST, 0)));
        }
        if (0 != mPreferences.getFloat(Constants.KEY_COST, 0)) {
            Button buttonPayRide = findViewById(R.id.button_pay_ride);
            DecimalFormat df = new DecimalFormat("#.##");
            buttonPayRide.setText("Pay: $" + df.format(mPreferences.getFloat(Constants.KEY_COST, 0)));
        }
    }

    private BroadcastReceiver cancelRideReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "destinationReached");
            mPreferences.edit().putString(Constants.KEY_STATE, "free").apply();
            clearOtherUserConstants();
            updateUI();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));

        }
    };

    private BroadcastReceiver startTripReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "tripStarted");

            mPreferences.edit().putString(Constants.KEY_STATE, "on_ride").apply();
            updateUI();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));

        }
    };



    private BroadcastReceiver finishTripReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "destinationReached");

            mPreferences.edit().putString(Constants.KEY_STATE, "paying").apply();

            updateUI();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));
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

    private void drawCurrentLocationMarker(LatLng location){
        if (currentLocationMarker == null) {
            currentLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(location));
        }
        currentLocationMarker.setPosition(location);
    }

    private void drawDestinationLocationMarker(LatLng location){
        if (destinationLocationMarker == null) {
            destinationLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(location));
        }
        destinationLocationMarker.setPosition(location);
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        Log.d(TAG, "getLastLocation");

        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {

                            lastLocation = task.getResult();
                            lastKnownLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            drawCurrentLocationMarker(lastKnownLocation);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));
                            Log.d(TAG, "getLastLocation:All OK!");
                            if (!"free".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {
                                mServerHandler.updateUserCoordinates(mPreferences.getString(Constants.KEY_USERNAME, ""),
                                        mPreferences.getString(Constants.KEY_PASSWORD, ""),
                                        String.valueOf(lastKnownLocation.latitude),
                                        String.valueOf(lastKnownLocation.longitude),
                                        updateUserCoordinatesResponseListener);
                            }
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

        checkLocationPermition();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

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

        updateUI();
    }

    private void logout() {
        Log.d(TAG, "logout");
        mAuth.signOut();
        cancelRide();
        mPreferences.edit().clear().apply();
        Log.d(TAG, "Change activity to LoginActivity");
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

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
            Log.d(TAG, "change activity to PassengerProfileActivity");
            startActivity(new Intent(this, PassengerProfileActivity.class));
        } else if (id == R.id.payment) {
            Log.d(TAG, "change activity to PassengerPaymentActivity");
            startActivity(new Intent(this, AddPaymentActivity.class));
        } else if (id == R.id.history) {
            Log.d(TAG, "change activity to PassengerHistoryActivity");
            startActivity(new Intent(this, PassengerHistoryActivity.class));
        }/* else if (id == R.id.settings) {
            Log.d(TAG, "change activity to PassengerSettingsActivity");
            startActivity(new Intent(this, PassengerSettingsActivity.class));

        }*/ else if (id == R.id.action_logout) {
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
        if (mPolyline != null) {
            mPolyline.remove();
            mPolyline = null;
        }
        if (destinationLocationMarker != null) {
            destinationLocationMarker.remove();
            destinationLocationMarker = null;
        }
    }

    private void zoomOutToDestination() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(lastKnownLocation);
        builder.include(destination);
        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }


    @Override
    public void onPlaceSelected(final Place place) {
        Log.i(TAG, "Place Selected: " + place.getName());

        destination = place.getLatLng();

        if (lastKnownLocation != null && destination != null) {
            GoogleDirection.withServerKey(GOOGLE_API_KEY)
                    .from(lastKnownLocation)
                    .to(destination)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            String status = direction.getStatus();
                            if (status.equals(RequestResult.OK)) {
                                mServerHandler.updateUserCoordinates(mPreferences.getString(Constants.KEY_USERNAME, ""),
                                        mPreferences.getString(Constants.KEY_PASSWORD, ""),
                                        String.valueOf(lastKnownLocation.latitude),
                                        String.valueOf(lastKnownLocation.longitude),
                                        updateUserCoordinatesResponseListener);
                                mPreferences.edit().putString(Constants.KEY_STATE, "place_selected").apply();
                                updateUI();
                                zoomOutToDestination();
                            } else {
                                Toast.makeText(getApplicationContext(), "Couldn't find a route",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                            Log.d(TAG, "Result Direction Failure");
                        }
                    });
        }
    }

    private void updateUI() {
        Log.i(TAG, "updateUI");
        setTextForDisplay();
        if ("free".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {

            findViewById(R.id.search_bar).setVisibility(View.VISIBLE);

            mBottomSheetBehavior.setPeekHeight(0);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            findViewById(R.id.rl_text_waiting_for_driver).setVisibility(View.GONE);
            findViewById(R.id.rl_text_driver_name).setVisibility(View.GONE);
            findViewById(R.id.text_estimated_cost).setVisibility(View.GONE);
            findViewById(R.id.rl_button_cancel).setVisibility(View.GONE);
            findViewById(R.id.button_request_ride).setVisibility(View.GONE);
            findViewById(R.id.button_pay_ride).setVisibility(View.GONE);
            findViewById(R.id.button_chat).setVisibility(View.GONE);
            findViewById(R.id.button_view_profile).setVisibility(View.GONE);
            clearRoute();
        } else {
            mBottomSheetBehavior.setPeekHeight(38);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            if ("place_selected".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {

                findViewById(R.id.search_bar).setVisibility(View.VISIBLE);

                findViewById(R.id.rl_text_waiting_for_driver).setVisibility(View.GONE);
                findViewById(R.id.rl_text_driver_name).setVisibility(View.GONE);
                findViewById(R.id.text_estimated_cost).setVisibility(View.GONE);
                findViewById(R.id.rl_button_cancel).setVisibility(View.VISIBLE);
                findViewById(R.id.button_request_ride).setVisibility(View.VISIBLE);
                findViewById(R.id.button_pay_ride).setVisibility(View.GONE);
                findViewById(R.id.button_chat).setVisibility(View.GONE);
                findViewById(R.id.button_view_profile).setVisibility(View.GONE);
            } else {

                findViewById(R.id.search_bar).setVisibility(View.GONE);

                if ("waiting_for_driver".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {
                    findViewById(R.id.rl_text_waiting_for_driver).setVisibility(View.GONE);
                    findViewById(R.id.rl_text_driver_name).setVisibility(View.VISIBLE);
                    findViewById(R.id.text_estimated_cost).setVisibility(View.VISIBLE);
                    findViewById(R.id.rl_button_cancel).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_request_ride).setVisibility(View.GONE);
                    findViewById(R.id.button_pay_ride).setVisibility(View.GONE);
                    findViewById(R.id.button_chat).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_view_profile).setVisibility(View.VISIBLE);
                } else if ("on_ride".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {
                    findViewById(R.id.rl_text_waiting_for_driver).setVisibility(View.GONE);
                    findViewById(R.id.rl_text_driver_name).setVisibility(View.VISIBLE);
                    findViewById(R.id.text_estimated_cost).setVisibility(View.VISIBLE);
                    findViewById(R.id.rl_button_cancel).setVisibility(View.GONE);
                    findViewById(R.id.button_request_ride).setVisibility(View.GONE);
                    findViewById(R.id.button_pay_ride).setVisibility(View.GONE);
                    findViewById(R.id.button_chat).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_view_profile).setVisibility(View.VISIBLE);
                } else if ("paying".equals(mPreferences.getString(Constants.KEY_STATE, "free"))) {
                    findViewById(R.id.rl_text_waiting_for_driver).setVisibility(View.GONE);
                    findViewById(R.id.rl_text_driver_name).setVisibility(View.VISIBLE);
                    findViewById(R.id.text_estimated_cost).setVisibility(View.GONE);
                    findViewById(R.id.rl_button_cancel).setVisibility(View.GONE);
                    findViewById(R.id.button_request_ride).setVisibility(View.GONE);
                    findViewById(R.id.button_pay_ride).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_chat).setVisibility(View.GONE);
                    findViewById(R.id.button_view_profile).setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {
        Log.e(TAG, "onError: Status = " + status.toString());

        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_cancel) {
            Log.d(TAG, "clicked cancel button");
            cancelRide();
        } else if (i == R.id.button_request_ride) {
            Log.d(TAG, "clicked request_ride button");
            requestRide();
        } else if (i == R.id.button_pay_ride) {
            Log.d(TAG, "clicked pay_ride button");
            payRide();
        } else if (i == R.id.button_chat) {
            Log.d(TAG, "clicked chat button");
            chat();
        } else if (i == R.id.button_view_profile) {
            Log.d(TAG, "clicked view_profile button");
            viewDriversProfile();
        }
    }

    private void drawDirections(String encodedDirections) {
        Log.i(TAG, "drawDirections");
        clearRoute();

        ArrayList<LatLng> latLngs = (ArrayList<LatLng>) PolyUtil.decode(encodedDirections);

        drawCurrentLocationMarker(lastKnownLocation);
        drawDestinationLocationMarker(destination);

        //Start drawing route
        mLineOptions = DirectionConverter.createPolyline(getApplicationContext(), latLngs, 5, R.color.colorPrimary);
        mPolyline = mMap.addPolyline(mLineOptions);
        zoomOutToDestination();
        //Finish drawing route
        Log.i(TAG, "drawDirections finished");
    }

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
                mPreferences.edit().putString(Constants.KEY_OTHERS_CAR_MODEL, response.getJSONObject(Constants.KEY_INFO).getJSONArray("cars").getJSONObject(0).getString(Constants.KEY_CAR_MODEL)).apply();
                mPreferences.edit().putString(Constants.KEY_OTHERS_CAR_BRAND, response.getJSONObject(Constants.KEY_INFO).getJSONArray("cars").getJSONObject(0).getString(Constants.KEY_CAR_BRAND)).apply();
                mPreferences.edit().putString(Constants.KEY_OTHERS_CAR_COLOR, response.getJSONObject(Constants.KEY_INFO).getJSONArray("cars").getJSONObject(0).getString(Constants.KEY_CAR_COLOR)).apply();
                mPreferences.edit().putString(Constants.KEY_OTHERS_CAR_YEAR, response.getJSONObject(Constants.KEY_INFO).getJSONArray("cars").getJSONObject(0).getString(Constants.KEY_CAR_YEAR)).apply();
                updateUI();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.ErrorListener requestRideResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "requestRideResponseErrorListener Failed. Response Error: " + error.toString());
            mPreferences.edit().putString(Constants.KEY_STATE, "place_selected").apply();


            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                Log.e(TAG, "Response statusCode: " + response.statusCode);
                Log.e(TAG, "Response data: " + Arrays.toString(response.data));
            }
            Toast.makeText(getApplicationContext(), "Couldn't make request",
                    Toast.LENGTH_SHORT).show();
            updateUI();
        }
    };

    Response.Listener<JSONObject> requestRideResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "requestRideResponseListener Successful. Response: " + response.toString());
            try {
                if (!"fail".equals(response.getString("status"))) {
                    mPreferences.edit().putString(Constants.KEY_RIDE_ID, response.getString("id")).apply();
                    drawDirections(response.getString("directions"));

                    mServerHandler.getUserInformation(response.getString("driver"), getUserInformationResponseListener, getUserInformationResponseErrorListener);

                    mPreferences.edit().putString(Constants.KEY_STATE, "waiting_for_driver").apply();
                    mPreferences.edit().putFloat(Constants.KEY_ESTIMATED_COST, Float.parseFloat(String.valueOf(response.getDouble("estimated_cost")))).apply();
                    updateUI();
                } else {
                    mPreferences.edit().putString(Constants.KEY_STATE, "place_selected").apply();
                    Toast.makeText(getApplicationContext(), "Couldn't make request",
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            updateUI();

        }
    };

    private void requestRide() {
        Log.i(TAG, "requestRide");

        String username = mPreferences.getString(Constants.KEY_USERNAME, "");
        String password = mPreferences.getString(Constants.KEY_PASSWORD, "");
        String latitudeInitial = String.valueOf(lastKnownLocation.latitude);
        String longitudeInitial = String.valueOf(lastKnownLocation.longitude);
        String latitudeFinal = String.valueOf(destination.latitude);
        String longitudeFinal = String.valueOf(destination.longitude);

        mServerHandler.requestRide(username, password, latitudeInitial, longitudeInitial, latitudeFinal, longitudeFinal, requestRideResponseListener, requestRideResponseErrorListener);
        updateUI();
    }


    private void cancelRide() {
        Log.i(TAG, "cancelRide");
        mPreferences.edit().putString(Constants.KEY_STATE, "free").apply();

        mServerHandler.cancelRide(mPreferences.getString(Constants.KEY_USERNAME, ""),
                mPreferences.getString(Constants.KEY_PASSWORD, ""),
                mPreferences.getString(Constants.KEY_RIDE_ID, ""));
        clearOtherUserConstants();
        updateUI();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));
    }

    Response.Listener<JSONObject> payRideResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "payRideResponseListener Successful. Response: " + response.toString());
            Toast.makeText(getApplicationContext(), "Payment Successful!",
                    Toast.LENGTH_SHORT).show();
            mPreferences.edit().putString(Constants.KEY_STATE, "free").apply();
            clearOtherUserConstants();
            updateUI();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));
        }
    };

    private Response.ErrorListener payRideResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "payRideResponseErrorListener Failed. Response Error: " + error.toString());
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                Log.e(TAG, "Response statusCode: " + response.statusCode);
                Log.e(TAG, "Response data: " + Arrays.toString(response.data));
            }
            Toast.makeText(getApplicationContext(), "Couldn't make payment",
                    Toast.LENGTH_SHORT).show();
        }
    };

    private void payRide() {
        Log.i(TAG, "payRide");
        if (mPreferences.getString(Constants.KEY_NUMBER, "").isEmpty()) {
            Log.i(TAG, "No card");
            Toast.makeText(getApplicationContext(), "No Credit Card",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), AddPaymentActivity.class));
        } else {
            String rideId = mPreferences.getString(Constants.KEY_RIDE_ID, "");
            Float value = mPreferences.getFloat(Constants.KEY_COST, 0);
            String month = mPreferences.getString(Constants.KEY_EXPIRATION_MONTH, "");
            String year = mPreferences.getString(Constants.KEY_EXPIRATION_YEAR, "");
            String method = mPreferences.getString(Constants.KEY_METHOD, "");
            String number = mPreferences.getString(Constants.KEY_NUMBER, "");
            String cvv = mPreferences.getString(Constants.KEY_CCVV, "");
            String type = mPreferences.getString(Constants.KEY_PAYMENT_TYPE, "");

            mServerHandler.generatePayment(rideId, value, month, year, method, number, cvv, type,
                    payRideResponseListener, payRideResponseErrorListener);
        }
    }

    private void chat() {
        Log.i(TAG, "chat");
        startActivity(new Intent(getApplicationContext(), ChatActivity.class));
    }

    private void viewDriversProfile() {
        Log.i(TAG, "viewDriverProfile");
        startActivity(new Intent(getApplicationContext(), OtherProfileActivity.class));
    }

    private void checkLocationPermition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
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

    private void clearOtherUserConstants() {
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