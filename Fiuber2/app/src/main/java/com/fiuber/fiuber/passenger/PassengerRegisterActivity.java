package com.fiuber.fiuber.passenger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fiuber.fiuber.LoginActivity;
import com.fiuber.fiuber.R;
import com.fiuber.fiuber.driver.DriverRegisterActivity;
import com.fiuber.fiuber.server.ServerHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class PassengerRegisterActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "PassengerRegisterAct";

    private EditText mFirstnameField;
    private EditText mLastnameField;
    private EditText mEmailField;
    private EditText mUsernameField;
    private EditText mPasswordField;

    private ServerHandler mServerHandler;
    SharedPreferences mPreferences;

    String MY_PREFERENCES = "MyPreferences";

    private static final String KEY_TYPE = "type";
    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private static final String KEY_LOGIN = "login";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_register_user);

        mServerHandler = new ServerHandler(this.getApplicationContext());
        mPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        // Views
        mFirstnameField = findViewById(R.id.edit_text_firstname);
        mLastnameField = findViewById(R.id.edit_text_lastname);
        mEmailField = findViewById(R.id.edit_text_email);
        mUsernameField = findViewById(R.id.edit_text_username);
        mPasswordField = findViewById(R.id.edit_text_password);

        // Buttons
        findViewById(R.id.text_login).setOnClickListener(this);
        findViewById(R.id.button_register).setOnClickListener(this);
        findViewById(R.id.text_change_to_driver).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
        // Check if user is signed in (non-null) and update UI accordingly.
        if("true".equals(mPreferences.getString(KEY_LOGIN, "false"))){
            Log.d(TAG, "Change activity to PassengerMapsActivity");
            startActivity(new Intent(this, PassengerMapsActivity.class));
        }
    }

    Response.ErrorListener createPassengerResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "createPassengerResponseErrorListener Failed. Response Error: " + error.toString());
            NetworkResponse response = error.networkResponse;
            if(response != null && response.data != null){
                Log.e(TAG, "Response statusCode: "+response.statusCode);
                Log.e(TAG, "Response data: "+response.data.toString());
            }
            Toast.makeText(getApplicationContext(), "Creating Passenger Failed", Toast.LENGTH_SHORT).show();
        }
    };


    Response.Listener<JSONObject> createPassengerResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "Creating Passenger Successfull. Response: " + response.toString());

            mPreferences.edit().putString(KEY_TYPE, "passenger").apply();
            mPreferences.edit().putString(KEY_FIRSTNAME, mFirstnameField.getText().toString().trim()).apply();
            mPreferences.edit().putString(KEY_LASTNAME, mLastnameField.getText().toString().trim()).apply();
            mPreferences.edit().putString(KEY_EMAIL, mEmailField.getText().toString().trim()).apply();
            mPreferences.edit().putString(KEY_USERNAME, mUsernameField.getText().toString().trim()).apply();
            mPreferences.edit().putString(KEY_PASSWORD, mPasswordField.getText().toString().trim()).apply();
            mPreferences.edit().putString(KEY_LOGIN, "true").apply();

            Toast.makeText(getApplicationContext(), "Creating Passenger Successfull!", Toast.LENGTH_SHORT).show();

            Log.d(TAG, "Change activity to PassengerMapsActivity");
            startActivity(new Intent(getApplicationContext(), PassengerMapsActivity.class));
        }
    };


    private void createAccount() {
        Log.d(TAG, "createAccount" );
        if (!validateCreateAccountForm()) {
            return;
        }

        String type = "passenger";
        String firstname = mFirstnameField.getText().toString().trim();
        String lastname = mLastnameField.getText().toString().trim();
        String email = mEmailField.getText().toString().trim();
        String username = mUsernameField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        mServerHandler.createServerUser(type, firstname, lastname, email, username, password, createPassengerResponseListener, createPassengerResponseErrorListener);
    }

    private boolean validateCreateAccountForm() {
        Log.d(TAG, "validateCreateAccountForm");
        boolean valid = true;

        String name = mFirstnameField.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            mFirstnameField.setError("Required.");
            valid = false;
        } else {
            mFirstnameField.setError(null);
        }

        String lastname = mLastnameField.getText().toString().trim();
        if (TextUtils.isEmpty(lastname)) {
            mLastnameField.setError("Required.");
            valid = false;
        } else {
            mLastnameField.setError(null);
        }

        String email = mEmailField.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

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
        if (i == R.id.button_register) {
            Log.d(TAG, "clicked register button");
            createAccount();
        } else if (i == R.id.text_login) {
            Log.d(TAG, "Change activity to LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
        } else if (i == R.id.text_change_to_driver) {
            Log.d(TAG, "Change activity to DriverRegisterActivity");
            startActivity(new Intent(this, DriverRegisterActivity.class));
        }
    }
}