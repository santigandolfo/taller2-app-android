package com.fiuber.fiuber;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private EditText mUsernameField;
    private EditText mPasswordField;

    private ServerHandler mServerHandler;
    SharedPreferences mPreferences;

    String MY_PREFERENCES = "MyPreferences";

    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";


    private static final String KEY_TYPE = "type";
    private static final String KEY_INFO= "info";
    private static final String KEY_LOGIN = "login";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_login);

        mServerHandler = new ServerHandler(this.getApplicationContext());
        mPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        // Views
        mUsernameField = findViewById(R.id.edit_text_username);
        mPasswordField = findViewById(R.id.edit_text_password);

        // Buttons
        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.text_register).setOnClickListener(this);
        findViewById(R.id.text_change_to_driver).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if ("true".equals(mPreferences.getString(KEY_LOGIN, "false"))) {
            startMapActivity(mPreferences.getString(KEY_TYPE, ""));
        }
    }

    Response.ErrorListener loginServerUserResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Login Failed. Response Error: " + error.toString());
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                Log.e(TAG, "Login Failed. Response statusCode: " + response.statusCode);
                Log.e(TAG, "Login Failed. Response data: " + response.data.toString());
            }
            Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
        }
    };

    Response.Listener<JSONObject> loginServerUserResponseListenerJSONObject = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "Login Successful. Response: " + response.toString());
            try {
                mPreferences.edit().putString(KEY_AUTH_TOKEN, response.getString(KEY_AUTH_TOKEN)).apply();
                mPreferences.edit().putString(KEY_PASSWORD, mPasswordField.getText().toString().trim()).apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
            //TODO: Uncoment this
            getUserInfo();

/*            //TODO: Delete this

            Log.d(TAG, "Change activity to PassengerRegisterActivity");
            startActivity(new Intent(LoginActivity.this, PassengerMapsActivity.class));*/
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
                Log.e(TAG, "Getting user information Failed. Response data: " + response.data.toString());
            }
            Toast.makeText(getApplicationContext(), "Couldn't get user information", Toast.LENGTH_SHORT).show();
        }
    };

    Response.Listener<JSONObject> getUserInformationResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "Getting user information Successful. Response: " + response.toString());
            try {
                mPreferences.edit().putString(KEY_FIRSTNAME, response.getJSONObject(KEY_INFO).getString(KEY_FIRSTNAME)).apply();
                mPreferences.edit().putString(KEY_LASTNAME, response.getJSONObject(KEY_INFO).getString(KEY_LASTNAME)).apply();
                mPreferences.edit().putString(KEY_USERNAME, response.getJSONObject(KEY_INFO).getString(KEY_USERNAME)).apply();
                mPreferences.edit().putString(KEY_EMAIL, response.getJSONObject(KEY_INFO).getString(KEY_EMAIL)).apply();
                mPreferences.edit().putString(KEY_TYPE, response.getJSONObject(KEY_INFO).getString(KEY_TYPE)).apply();

            } catch (JSONException e) {
                e.printStackTrace();
            }
            mPreferences.edit().putString(KEY_LOGIN, "true").apply();

            Toast.makeText(getApplicationContext(), "Got user information!", Toast.LENGTH_SHORT).show();

            startMapActivity(mPreferences.getString(KEY_TYPE, ""));
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
        } else if(type.equals("driver")) {
            Log.d(TAG, "Change activity to DriverMapsActivity");
            mServerHandler.setDriversAvailability(mPreferences.getString(KEY_USERNAME, ""), mPreferences.getString(KEY_PASSWORD, ""), "True", setDriverAsAvailableResponseListener);
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