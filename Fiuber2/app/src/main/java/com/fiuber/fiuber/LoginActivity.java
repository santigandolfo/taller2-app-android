package com.fiuber.fiuber;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fiuber.fiuber.driver.DriverMapsActivity;
import com.fiuber.fiuber.driver.DriverRegisterActivity;
import com.fiuber.fiuber.passenger.PassengerMapsActivity;
import com.fiuber.fiuber.passenger.PassengerRegisterActivity;
import com.fiuber.fiuber.server.ServerHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private EditText mUsernameField;
    private EditText mPasswordField;

    private ServerHandler mServerHandler;
    SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_login);

        mServerHandler = new ServerHandler(this.getApplicationContext());
        mPreferences = getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE);

        // Views
        mUsernameField = findViewById(R.id.edit_text_username);
        mPasswordField = findViewById(R.id.edit_text_password);

        // Buttons
        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.text_register).setOnClickListener(this);
        findViewById(R.id.text_change_to_driver).setOnClickListener(this);

        checkLocationPermition();

    }

    private void checkLocationPermition() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 99);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 99:{
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if (mPreferences.getBoolean(Constants.KEY_LOGIN, false)) {
            startMapActivity(mPreferences.getString(Constants.KEY_TYPE, ""));
        }
    }

    Response.ErrorListener loginServerUserResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Login Failed. Response Error: " + error.toString());
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                Log.e(TAG, "Login Failed. Response statusCode: " + response.statusCode);
                Log.e(TAG, "Login Failed. Response data: " + Arrays.toString(response.data));
            }
            Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
        }
    };

    Response.Listener<JSONObject> loginServerUserResponseListenerJSONObject = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "Login Successful. Response: " + response.toString());
            try {
                mPreferences.edit().putString(Constants.KEY_AUTH_TOKEN, response.getString(Constants.KEY_AUTH_TOKEN)).apply();
                mPreferences.edit().putString(Constants.KEY_PASSWORD, mPasswordField.getText().toString().trim()).apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();

            getUserInfo();

        }
    };


    private void login() {
        Log.d(TAG, "login");

        if (!validateLoginForm()) {
            return;
        }

        String username = mUsernameField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        mServerHandler.loginServerUser(username, password, loginServerUserResponseListenerJSONObject, loginServerUserResponseErrorListener);
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
            Toast.makeText(getApplicationContext(), "Couldn't get user information", Toast.LENGTH_SHORT).show();
        }
    };

    Response.Listener<JSONObject> getUserInformationResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "Getting user information Successful. Response: " + response.toString());
            try {
                mPreferences.edit().putString(Constants.KEY_FIRSTNAME, response.getJSONObject(Constants.KEY_INFO).getString(Constants.KEY_FIRSTNAME)).apply();
                mPreferences.edit().putString(Constants.KEY_LASTNAME, response.getJSONObject(Constants.KEY_INFO).getString(Constants.KEY_LASTNAME)).apply();
                mPreferences.edit().putString(Constants.KEY_USERNAME, response.getJSONObject(Constants.KEY_INFO).getString(Constants.KEY_USERNAME)).apply();
                mPreferences.edit().putString(Constants.KEY_EMAIL, response.getJSONObject(Constants.KEY_INFO).getString(Constants.KEY_EMAIL)).apply();
                mPreferences.edit().putString(Constants.KEY_TYPE, response.getJSONObject(Constants.KEY_INFO).getString(Constants.KEY_TYPE)).apply();

                if ("driver".equals(mPreferences.getString(Constants.KEY_TYPE, ""))) {
                    Log.d(TAG, "User is a driver");
                    Log.d(TAG, "cars:" + response.getJSONObject(Constants.KEY_INFO).getJSONArray("cars").toString());
                    mPreferences.edit().putString(Constants.KEY_CAR_MODEL, response.getJSONObject(Constants.KEY_INFO).getJSONArray("cars").getJSONObject(0).getString(Constants.KEY_CAR_MODEL)).apply();
                    mPreferences.edit().putString(Constants.KEY_CAR_BRAND, response.getJSONObject(Constants.KEY_INFO).getJSONArray("cars").getJSONObject(0).getString(Constants.KEY_CAR_BRAND)).apply();
                    mPreferences.edit().putString(Constants.KEY_CAR_COLOR, response.getJSONObject(Constants.KEY_INFO).getJSONArray("cars").getJSONObject(0).getString(Constants.KEY_CAR_COLOR)).apply();
                    mPreferences.edit().putString(Constants.KEY_CAR_YEAR, response.getJSONObject(Constants.KEY_INFO).getJSONArray("cars").getJSONObject(0).getString(Constants.KEY_CAR_YEAR)).apply();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            mPreferences.edit().putBoolean(Constants.KEY_LOGIN, true).apply();

            Toast.makeText(getApplicationContext(), "Got user information!", Toast.LENGTH_SHORT).show();

            startMapActivity(mPreferences.getString(Constants.KEY_TYPE, ""));
        }
    };

    private void getUserInfo() {
        Log.d(TAG, "getUserInfo");
        String username = mUsernameField.getText().toString().trim();

        mServerHandler.getUserInformation(username, getUserInformationResponseListener, getUserInformationResponseErrorListener);
    }

    private void startMapActivity(String type) {
        Log.d(TAG, "startMapActivity");
        if (type.equals("passenger")) {
            Log.d(TAG, "Change activity to PassengerMapsActivity");
            startActivity(new Intent(LoginActivity.this, PassengerMapsActivity.class));
        } else if (type.equals("driver")) {
            Log.d(TAG, "Change activity to DriverMapsActivity");
            mServerHandler.setDriversDuty(mPreferences.getString(Constants.KEY_USERNAME, ""), mPreferences.getString(Constants.KEY_PASSWORD, ""), true, setDriverAsAvailableResponseListener);
        }
    }

    Response.Listener<JSONObject> setDriverAsAvailableResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "setDriverAsAvailableResponseListener Successful. Response: " + response.toString());
            startActivity(new Intent(LoginActivity.this, DriverMapsActivity.class));
        }
    };

    private boolean validateLoginForm() {
        Log.d(TAG, "validateLoginForm");
        boolean valid = true;

        String username = mUsernameField.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            mUsernameField.setError("Required.");
            valid = false;
        } else {
            mUsernameField.setError(null);
        }

        String password = mPasswordField.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_login) {
            Log.d(TAG, "clicked login button");
            login();
        } else if (i == R.id.text_register) {
            Log.d(TAG, "Change activity to PassengerRegisterActivity");
            startActivity(new Intent(this, PassengerRegisterActivity.class));
        } else if (i == R.id.text_change_to_driver) {
            Log.d(TAG, "Change activity to DriverRegisterActivity");
            startActivity(new Intent(this, DriverRegisterActivity.class));
        }
    }
}