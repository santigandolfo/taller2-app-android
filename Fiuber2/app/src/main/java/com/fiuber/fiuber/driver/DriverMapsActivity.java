package com.fiuber.fiuber.driver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fiuber.fiuber.LoginActivity;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DriverMapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, PlaceSelectionListener, View.OnClickListener, LocationListener {

    private static final String TAG = "DriverMapsActivity";
    private GoogleMap mMap;
    private FirebaseAuth mAuth;

    private FusedLocationProviderClient mFusedLocationClient;

    protected Location lastLocation;
    LatLng lastKnownLocation;
    LatLng destination;
    Marker current_location_marker;
    Marker destination_location_marker;

    private BottomSheetBehavior mBottomSheetBehavior;

    private DrawerLayout mDrawer;
    PolylineOptions mLineOptions;
    Polyline mPolyline;

    String GOOGLE_API_KEY = "AIzaSyAWcT3cBWZ75CxCgC5vq51iJmoZSUKnqyA";

    private ServerHandler mServerHandler;
    SharedPreferences mPreferences;

    String MY_PREFERENCES = "MyPreferences";

    private static final String KEY_LOGIN = "login";

    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    private static final String KEY_RIDE_ID = "ride_id";
    private static final String KEY_INFO = "info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_driver_map);

        findViewById(R.id.search_bar).setVisibility(View.GONE);

        // BottomSheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Buttons
        findViewById(R.id.button_cancel).setOnClickListener(this);
        findViewById(R.id.button_refresh).setOnClickListener(this);
        findViewById(R.id.button_request_ride).setOnClickListener(this);
        findViewById(R.id.button_pay_ride).setOnClickListener(this);
        findViewById(R.id.button_chat).setOnClickListener(this);
        findViewById(R.id.button_view_profile).setOnClickListener(this);

        mServerHandler = new ServerHandler(this.getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        mPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
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
                getLastLocation();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
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
                        // example : way to access view from PlaceAutoCompleteFragment
                        // ((EditText) autocompleteFragment.getView()
                        // .findViewById(R.id.place_autocomplete_search_input)).setText("");
                        autocompleteFragment.setText("");
                        view.setVisibility(View.GONE);
                        updateUI();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));

                    }
                });

    }

    public void sendNotification(View view, String title, String text, Class to) {

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

        mNotificationManager.notify(001, mBuilder.build());
    }



/*    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                }
        }
    }*/

    Response.Listener<JSONObject> updateUserCoordinatesResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.e(TAG, "updateUserCoordinatesResponseListener Successful. Response: " + response.toString());
            mPreferences.edit().putString(KEY_LATITUDE, String.valueOf(lastKnownLocation.latitude)).apply();
            mPreferences.edit().putString(KEY_LONGITUDE, String.valueOf(lastKnownLocation.latitude)).apply();
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
                            if (current_location_marker == null)
                                current_location_marker = mMap.addMarker(new MarkerOptions()
                                        .position(lastKnownLocation)
                                        .title("Current Position"));
                            current_location_marker.setPosition(lastKnownLocation);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));
                            mServerHandler.updateUserCoordinates(mPreferences.getString(KEY_USERNAME, ""), mPreferences.getString(KEY_PASSWORD, ""), String.valueOf(lastKnownLocation.latitude), String.valueOf(lastKnownLocation.longitude), updateUserCoordinatesResponseListener);
                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    }
                });
    }

    @SuppressWarnings("MissingPermission")
    private void moveInitialCamera() {
        Log.d(TAG, "getLastLocation");
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Log.d(TAG, "getLastLocation:all OK!");
                            lastLocation = task.getResult();
                            lastKnownLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            if (current_location_marker == null)
                                current_location_marker = mMap.addMarker(new MarkerOptions()
                                        .position(lastKnownLocation)
                                        .title("Current Position"));
                            current_location_marker.setPosition(lastKnownLocation);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));
                            mServerHandler.updateUserCoordinates(mPreferences.getString(KEY_USERNAME, ""), mPreferences.getString(KEY_PASSWORD, ""), String.valueOf(lastKnownLocation.latitude), String.valueOf(lastKnownLocation.longitude), updateUserCoordinatesResponseListener);
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
        Log.e(TAG, "onStart");

        updateUI();

        NavigationView navigationView = findViewById(R.id.nav_view);
        int size = navigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }

        if ("false".equals(mPreferences.getString(KEY_LOGIN, "false"))) {
            Log.d(TAG, "change activity to LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
        }
    }


    Response.Listener<JSONObject> logoutCancelRideResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.e(TAG, "requestRideResponseListener Successful. Response: " + response.toString());
            mPreferences.edit().clear().apply();
            Log.d(TAG, "Change activity to LoginActivity");
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));

        }
    };

    private void logout() {
        Log.d(TAG, "logout");
        mAuth.signOut();
        mServerHandler.setDriversAvailability(mPreferences.getString(KEY_USERNAME, ""), mPreferences.getString(KEY_PASSWORD, ""),"False", setDriverAsAvailableResponseListener);
        if ("true".equals(mPreferences.getString("on_ride", "false")) || "true".equals(mPreferences.getString("requesting_ride", "false"))) {
            cancelRide(logoutCancelRideResponseListener);
        } else {
            mPreferences.edit().clear().apply();
            Log.d(TAG, "Change activity to LoginActivity");
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
/*        LoginManager.getInstance().logOut();*/
        // mServerHandler.logoutServerUser(mPreferences.getString(KEY_AUTH_TOKEN, ""), logoutServerUserResponseListener, logoutServerUserResponseErrorListener);

    }

    Response.Listener<JSONObject> setDriverAsAvailableResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "setDriverAsAvailableResponseListener Successful. Response: " + response.toString());
            Log.d(TAG, "Change activity to LoginActivity");
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
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

/*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Log.d(TAG, "optionsItemSelected");
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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
        } else if (id == R.id.settings) {
            Log.d(TAG, "change activity to DriverSettingsActivity");
            startActivity(new Intent(this, DriverSettingsActivity.class));

        } else if (id == R.id.action_logout) {
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

        moveInitialCamera();

    }

    private void clearRoute() {
        if (mPolyline != null)
            mPolyline.remove();
        if (destination_location_marker != null)
            destination_location_marker.remove();


    }

    private void clearMap() {
        clearRoute();
        if (current_location_marker != null)
            current_location_marker.remove();
    }

    /**
     * Callback invoked when a place has been selected from the PlaceAutocompleteFragment.
     */
    @Override
    public void onPlaceSelected(Place place) {
        Log.i(TAG, "Place Selected: " + place.getName());

        if ("true".equals(mPreferences.getString("on_ride", "false")))
            return;

        clearMap();

        current_location_marker = mMap.addMarker(new MarkerOptions()
                .position(lastKnownLocation)
                .title("Current Position"));

        destination_location_marker = mMap.addMarker(new MarkerOptions()
                .position(place.getLatLng())
                .title(place.getName().toString()));

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
                                Route route = direction.getRouteList().get(0);
                                Leg leg = route.getLegList().get(0);
                                ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                mLineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, R.color.colorPrimary);
                                mPolyline = mMap.addPolyline(mLineOptions);
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                builder.include(lastKnownLocation);
                                builder.include(destination);
                                LatLngBounds bounds = builder.build();
                                int padding = 150; // offset from edges of the map in pixels
                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                mMap.animateCamera(cu);
                                showBottomSheet();
                            } else {
                                Toast.makeText(DriverMapsActivity.this, "Couldn't find a route",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                            Log.d(TAG, "Result Direction Failure");
                        }
                    });
        }

        // mPolyline = mMap.addPolyline(new PolylineOptions().add(lastKnownLocation,place.getLatLng()).color(Color.BLUE));
        // Format the returned place's details and display them in the TextView.
/*        mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(), place.getId(),
                place.getAddress(), place.getPhoneNumber(), place.getWebsiteUri()));

        CharSequence attributions = place.getAttributions();
        if (!TextUtils.isEmpty(attributions)) {
            mPlaceAttribution.setText(Html.fromHtml(attributions.toString()));
        } else {
            mPlaceAttribution.setText("");
        }*/
    }

    private void showBottomSheet() {
        Log.i(TAG, "showBottomSheet");
        mBottomSheetBehavior.setPeekHeight(50);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        findViewById(R.id.button_cancel).setVisibility(View.VISIBLE);
        findViewById(R.id.button_request_ride).setVisibility(View.VISIBLE);
        findViewById(R.id.button_pay_ride).setVisibility(View.GONE);
        findViewById(R.id.button_chat).setVisibility(View.GONE);
        findViewById(R.id.button_view_profile).setVisibility(View.GONE);
    }

    private void updateUI() {
        Log.i(TAG, "updateUI");
        if ("false".equals(mPreferences.getString("on_ride", "false"))) {
            mBottomSheetBehavior.setPeekHeight(0);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            clearRoute();
        } else if ("true".equals(mPreferences.getString("on_ride", "false"))) {
            mBottomSheetBehavior.setPeekHeight(50);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            verifyRideState();
        }


    }

    private void verifyRideState() {
        Log.i(TAG, "verifyRideState");
        if ("false".equals(mPreferences.getString("reached_destination", "false"))) {
            findViewById(R.id.button_cancel).setVisibility(View.VISIBLE);
            findViewById(R.id.button_request_ride).setVisibility(View.GONE);
            findViewById(R.id.button_pay_ride).setVisibility(View.GONE);
            findViewById(R.id.button_chat).setVisibility(View.VISIBLE);
            findViewById(R.id.button_view_profile).setVisibility(View.VISIBLE);
        }
        if ("true".equals(mPreferences.getString("reached_destination", "false"))) {
            findViewById(R.id.button_cancel).setVisibility(View.GONE);
            findViewById(R.id.button_request_ride).setVisibility(View.GONE);
            findViewById(R.id.button_pay_ride).setVisibility(View.VISIBLE);
            findViewById(R.id.button_chat).setVisibility(View.GONE);
            findViewById(R.id.button_view_profile).setVisibility(View.GONE);
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
            cancelRide(cancelRideResponseListener);
        } else if (i == R.id.button_request_ride) {
            Log.d(TAG, "clicked cancel button");
            requestRide();
        } else if (i == R.id.button_pay_ride) {
            Log.d(TAG, "clicked cancel button");
            payRide();
        } else if (i == R.id.button_chat) {
            Log.d(TAG, "clicked cancel button");
            chat();
        } else if (i == R.id.button_view_profile) {
            Log.d(TAG, "clicked cancel button");
            viewDriversProfile();
        }
    }

    Response.Listener<JSONObject> requestRideResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.e(TAG, "requestRideResponseListener Successful. Response: " + response.toString());

            //TODO: Change this:

            try {
                mPreferences.edit().putString(KEY_RIDE_ID, response.getString("id")).apply();
                Log.e(TAG, "RIDE ID: " + mPreferences.getString(KEY_RIDE_ID, ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendNotification(getCurrentFocus(), "Ride",  "Your ride request has been made",DriverMapsActivity.class);
            mPreferences.edit().putString("requesting_ride", "false").apply();
            mPreferences.edit().putString("on_ride", "true").apply();
            findViewById(R.id.text_waiting_for_driver).setVisibility(View.GONE);
            findViewById(R.id.button_cancel).setVisibility(View.VISIBLE);
            findViewById(R.id.button_refresh).setVisibility(View.GONE);
            findViewById(R.id.button_request_ride).setVisibility(View.GONE);
            findViewById(R.id.button_pay_ride).setVisibility(View.GONE);
            findViewById(R.id.button_chat).setVisibility(View.VISIBLE);
            findViewById(R.id.button_view_profile).setVisibility(View.VISIBLE);
            updateUI();

        }
    };

    //TODO: Change finished to true when location is equal to destination
    private void requestRide() {
        Log.i(TAG, "requestRide");
        mPreferences.edit().putString("requesting_ride", "true").apply();
        findViewById(R.id.text_waiting_for_driver).setVisibility(View.VISIBLE);
        findViewById(R.id.button_cancel).setVisibility(View.VISIBLE);
        findViewById(R.id.button_refresh).setVisibility(View.GONE);
        findViewById(R.id.button_request_ride).setVisibility(View.GONE);
        findViewById(R.id.button_pay_ride).setVisibility(View.GONE);
        findViewById(R.id.button_chat).setVisibility(View.GONE);
        findViewById(R.id.button_view_profile).setVisibility(View.GONE);

        String username = mPreferences.getString(KEY_USERNAME, "");
        String password = mPreferences.getString(KEY_PASSWORD, "");
        String latitudeInitial = String.valueOf(lastKnownLocation.latitude);
        String longitudeInitial = String.valueOf(lastKnownLocation.longitude);
        String latitudeFinal = String.valueOf(destination.latitude);
        String longitudeFinal = String.valueOf(destination.longitude);

        mServerHandler.requestRide(username, password, latitudeInitial, longitudeInitial, latitudeFinal, longitudeFinal,requestRideResponseListener);
    }


    //TODO: Change finished to true when location is equal to destination
    private void receiveRide() {
        Log.i(TAG, "receiveRide");
        mPreferences.edit().putString("on_ride", "true").apply();
        findViewById(R.id.button_cancel).setVisibility(View.VISIBLE);
        findViewById(R.id.button_request_ride).setVisibility(View.GONE);
        findViewById(R.id.button_pay_ride).setVisibility(View.GONE);
        findViewById(R.id.button_chat).setVisibility(View.VISIBLE);
        findViewById(R.id.button_view_profile).setVisibility(View.VISIBLE);
        updateUI();
    }

    Response.Listener<JSONObject> cancelRideResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.e(TAG, "requestRideResponseListener Successful. Response: " + response.toString());
        }
    };

    private void cancelRide(    Response.Listener<JSONObject> responseListener) {
        Log.i(TAG, "cancelRide");
        if ("true".equals(mPreferences.getString("on_ride", "false")) || "true".equals(mPreferences.getString("requesting_ride", "false")))
            mServerHandler.cancelRide(mPreferences.getString(KEY_USERNAME, ""), mPreferences.getString(KEY_USERNAME, ""), responseListener);
        mPreferences.edit().putString("on_ride", "false").apply();
        mPreferences.edit().putString("requesting_ride", "false").apply();
        mPreferences.edit().remove(KEY_RIDE_ID).apply();
        findViewById(R.id.button_cancel).setVisibility(View.GONE);
        findViewById(R.id.button_refresh).setVisibility(View.GONE);
        findViewById(R.id.button_request_ride).setVisibility(View.GONE);
        findViewById(R.id.button_pay_ride).setVisibility(View.GONE);
        findViewById(R.id.button_chat).setVisibility(View.GONE);
        findViewById(R.id.button_view_profile).setVisibility(View.GONE);
        updateUI();
        if (mPolyline != null)
            mPolyline.remove();

        if (destination_location_marker != null)
            destination_location_marker.remove();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));
    }

    private void payRide() {
        Log.i(TAG, "payRide");
        mPreferences.edit().putString("on_ride", "false").apply();
        mPreferences.edit().putString("reached_destination", "false").apply();
        findViewById(R.id.button_cancel).setVisibility(View.GONE);
        findViewById(R.id.button_request_ride).setVisibility(View.GONE);
        findViewById(R.id.button_pay_ride).setVisibility(View.GONE);
        findViewById(R.id.button_chat).setVisibility(View.GONE);
        findViewById(R.id.button_view_profile).setVisibility(View.GONE);
        updateUI();
    }

    private void chat() {
        Log.i(TAG, "chat");
        startActivity(new Intent(getApplicationContext(), ChatActivity.class));
    }

    private void viewDriversProfile() {
    }

    private void checkLocationPermition() {
        if (ActivityCompat.checkSelfPermission(DriverMapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 10);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "getLastLocation");
        checkLocationPermition();
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Log.d(TAG, "getLastLocation:all OK!");
                            lastLocation = task.getResult();
                            lastKnownLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            if (current_location_marker == null)
                                current_location_marker = mMap.addMarker(new MarkerOptions()
                                        .position(lastKnownLocation)
                                        .title("Current Position"));
                            current_location_marker.setPosition(lastKnownLocation);
                            mServerHandler.updateUserCoordinates(mPreferences.getString(KEY_USERNAME, ""), mPreferences.getString(KEY_PASSWORD, ""), String.valueOf(lastKnownLocation.latitude), String.valueOf(lastKnownLocation.longitude), updateUserCoordinatesResponseListener);
                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    }
                });
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
}