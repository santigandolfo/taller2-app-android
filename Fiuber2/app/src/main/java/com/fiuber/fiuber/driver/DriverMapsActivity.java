package com.fiuber.fiuber.driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.facebook.login.LoginManager;
import com.fiuber.fiuber.R;
import com.fiuber.fiuber.rider.RiderHistoryActivity;
import com.fiuber.fiuber.rider.LoginActivity;
import com.fiuber.fiuber.rider.RiderPaymentActivity;
import com.fiuber.fiuber.rider.RiderProfileActivity;
import com.fiuber.fiuber.rider.SettingsActivity;
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

import org.json.JSONObject;

import java.util.ArrayList;

public class DriverMapsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, PlaceSelectionListener {

        private static final String TAG = "DriverMapsActivity";
        private GoogleMap mMap;

        private FusedLocationProviderClient mFusedLocationClient;

        protected Location lastLocation;
        LatLng lastKnownLocation;
        Marker current_location_marker;
        Marker destination_location_marker;

        private DrawerLayout mDrawer;
        PolylineOptions mLineOptions;
        Polyline mPolyline;

        String GOOGLE_API_KEY = "AIzaSyAWcT3cBWZ75CxCgC5vq51iJmoZSUKnqyA";

        private ServerHandler mServerHandler;
        SharedPreferences mPreferences;
        SharedPreferences.Editor mEditorPreferences;

        String MY_PREFERENCES = "MyPreferences";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_driver_maps);

            mServerHandler = new ServerHandler(this.getApplicationContext());
            mPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
            mEditorPreferences = mPreferences.edit();

            findViewById(R.id.iv_menu_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawer.openDrawer(GravityCompat.START);
                }
            });

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            mDrawer = findViewById(R.id.drawer_layout);

            final FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{
                                    android.Manifest.permission.ACCESS_FINE_LOCATION
                            }, 10);
                        }
                        return;
                    }
                    getLastLocation();
                }
            });

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            mDrawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);


            PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.autocomplete_fragment);


            autocompleteFragment.setOnPlaceSelectedListener(this);
            autocompleteFragment.setHint("Where do you want to go?");
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode) {
                case 10:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    }
            }
        }

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
            // Check if user is signed in (non-null) and update UI accordingly.
/*        FirebaseUser currentUser = mAuth.getCurrentUser();*/

            NavigationView navigationView = findViewById(R.id.nav_view);
            int size = navigationView.getMenu().size();
            for (int i = 0; i < size; i++) {
                navigationView.getMenu().getItem(i).setChecked(false);
            }

            if("false".equals(mPreferences.getString("login", "false"))){
                Log.d(TAG, "change activity to LoginActivity");
                startActivity(new Intent(this, LoginActivity.class));
            }

/*        if (currentUser == null) {
            Log.d(TAG, "change activity to LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));

        }*/

        }


        Response.ErrorListener logoutServerUserResponseErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Response error: " + error.toString());
/*
            NetworkResponse response = error.networkResponse;
            if(response != null && response.data != null){
                switch(response.statusCode){
                    case 401:
                        Log.e(TAG, "Logout Successfull: "+response.data.toString());
                        Toast.makeText(getApplicationContext(), "Logout Successfull: (expired_token)", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Change activity to LoginActivity");
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        break;
                }
            }

*/
                Log.d(TAG, "Change activity to LoginActivity");
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));

            }
        };

        Response.Listener<JSONObject> logoutServerUserResponseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "Logout Successfull. Response: " + response.toString());
                Toast.makeText(getApplicationContext(), "Logout Successfull", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Change activity to LoginActivity");
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        };

        private void logout() {
            Log.d(TAG, "logout");
/*        mAuth.signOut();*/
            mEditorPreferences.clear().apply();
            mEditorPreferences.putString("login", "false").apply();
            LoginManager.getInstance().logOut();
            mServerHandler.logoutServerUser(mPreferences.getString("auth_token", ""), logoutServerUserResponseListener, logoutServerUserResponseErrorListener);

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

        @Override
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
        }

        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.
            Log.d(TAG, "navigationItemSelected");
            int id = item.getItemId();

            if (id == R.id.profile) {
                Log.d(TAG, "change activity to RiderProfileActivity");
                startActivity(new Intent(this, RiderProfileActivity.class));
            } else if (id == R.id.payment) {
                Log.d(TAG, "change activity to RiderPaymentActivity");
                startActivity(new Intent(this, RiderPaymentActivity.class));
            } else if (id == R.id.history) {
                Log.d(TAG, "change activity to RiderHistoryActivity");
                startActivity(new Intent(this, RiderHistoryActivity.class));
            } else if (id == R.id.settings) {
                Log.d(TAG, "change activity to SettingsActivity");
                startActivity(new Intent(this, SettingsActivity.class));

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

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, 10);
                }
                return;
            }

            moveInitialCamera();

        }

        @Override
        public void onPlaceSelected(Place place) {
            Log.i(TAG, "Place Selected: " + place.getName());

            if (mPolyline != null)
                mPolyline.remove();

            if (current_location_marker == null)
                current_location_marker = mMap.addMarker(new MarkerOptions()
                        .position(lastKnownLocation)
                        .title("Current Position"));
            current_location_marker.setPosition(lastKnownLocation);

            if (destination_location_marker == null)
                destination_location_marker = mMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .title(place.getName().toString()));
            destination_location_marker.setPosition(place.getLatLng());
            destination_location_marker.setTitle(place.getName().toString());

            Log.d(TAG, "Generating Polyline");

            final LatLng destination = place.getLatLng();
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
                                    if (mPolyline != null)
                                        mPolyline.remove();
                                    mPolyline = mMap.addPolyline(mLineOptions);
                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                    builder.include(lastKnownLocation);
                                    builder.include(destination);
                                    LatLngBounds bounds = builder.build();
                                    int padding = 150; // offset from edges of the map in pixels
                                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                    mMap.animateCamera(cu);
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
            Log.d(TAG, "Finished generating Polyline");

        }

        @Override
        public void onError(Status status) {
            Log.e(TAG, "onError: Status = " + status.toString());

            Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

