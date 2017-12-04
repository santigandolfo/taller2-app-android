package com.fiuber.fiuber.driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fiuber.fiuber.Constants;
import com.fiuber.fiuber.R;
import com.fiuber.fiuber.LoginActivity;
import com.fiuber.fiuber.server.ServerHandler;

import org.json.JSONObject;

import java.util.Arrays;

public class DriverRegisterActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "DriverRegisterActivity";

    private EditText mFirstnameField;
    private EditText mLastnameField;
    private EditText mEmailField;
    private EditText mUsernameField;
    private EditText mPasswordField;

    private EditText mCarModelField;
    private EditText mCarColorField;
    private EditText mCarBrandField;
    private EditText mCarYearField;

    private ServerHandler mServerHandler;
    SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);

        mServerHandler = new ServerHandler(this.getApplicationContext());
        mPreferences = getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE);

        // Views
        mFirstnameField = findViewById(R.id.edit_text_firstname);
        mLastnameField = findViewById(R.id.edit_text_lastname);
        mEmailField = findViewById(R.id.edit_text_email);
        mUsernameField = findViewById(R.id.edit_text_username);
        mPasswordField = findViewById(R.id.edit_text_password);

        //Car Views
        mCarModelField = findViewById(R.id.edit_text_car_model);
        mCarColorField = findViewById(R.id.edit_text_car_color);
        mCarBrandField = findViewById(R.id.edit_text_car_brand);
        mCarYearField = findViewById(R.id.edit_text_car_year);

        mCarYearField.setInputType(InputType.TYPE_CLASS_NUMBER);

        // Buttons
        findViewById(R.id.button_register).setOnClickListener(this);
        findViewById(R.id.text_change_to_passenger).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPreferences.getBoolean("login", false)) {
            Log.d(TAG, "change activity to DriverMapsActivity");
            startActivity(new Intent(this, DriverMapsActivity.class));
        }
    }

    Response.ErrorListener createDriverResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "createDriverResponseErrorListener Failed. Response Error: " + error.toString());
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                Log.e(TAG, "Response statusCode: " + response.statusCode);
                Log.e(TAG, "Response data: " + Arrays.toString(response.data));
            }
            Toast.makeText(getApplicationContext(), "Creating Driver Failed", Toast.LENGTH_SHORT).show();
        }
    };

    Response.Listener<JSONObject> setDriverAsAvailableResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "setDriverAsAvailableResponseListener Successful. Response: " + response.toString());
            Log.d(TAG, "Change activity to DriverMapsActivity");
            startActivity(new Intent(getApplicationContext(), DriverMapsActivity.class));
        }
    };

    Response.Listener<JSONObject> saveCarInformationResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "saveCarInformationResponseListener Successful. Response: " + response.toString());

            mPreferences.edit().putString(Constants.KEY_CAR_MODEL, mCarModelField.getText().toString().trim()).apply();
            mPreferences.edit().putString(Constants.KEY_CAR_COLOR, mCarColorField.getText().toString().trim()).apply();
            mPreferences.edit().putString(Constants.KEY_CAR_BRAND, mCarBrandField.getText().toString().trim()).apply();
            mPreferences.edit().putString(Constants.KEY_CAR_YEAR, mCarYearField.getText().toString().trim()).apply();
            mPreferences.edit().putBoolean(Constants.KEY_LOGIN, true).apply();

            mServerHandler.setDriversDuty(mPreferences.getString(Constants.KEY_USERNAME, ""), mPreferences.getString(Constants.KEY_PASSWORD, ""), true, setDriverAsAvailableResponseListener);
        }
    };

    Response.Listener<JSONObject> createDriverResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "createDriverResponseListener Successfull. Response: " + response.toString());

            mPreferences.edit().putString(Constants.KEY_TYPE, "driver").apply();
            mPreferences.edit().putString(Constants.KEY_FIRSTNAME, mFirstnameField.getText().toString().trim()).apply();
            mPreferences.edit().putString(Constants.KEY_LASTNAME, mLastnameField.getText().toString().trim()).apply();
            mPreferences.edit().putString(Constants.KEY_EMAIL, mEmailField.getText().toString().trim()).apply();
            mPreferences.edit().putString(Constants.KEY_USERNAME, mUsernameField.getText().toString().trim()).apply();
            mPreferences.edit().putString(Constants.KEY_PASSWORD, mPasswordField.getText().toString().trim()).apply();

            String username = mUsernameField.getText().toString().trim();
            String password = mPasswordField.getText().toString().trim();

            String carModel = mCarModelField.getText().toString().trim();
            String carColor = mCarColorField.getText().toString().trim();
            String carBrand = mCarBrandField.getText().toString().trim();
            String carYear = mCarYearField.getText().toString().trim();

            mServerHandler.saveModificationsCar(username, password, carModel, carColor, carBrand, carYear, saveCarInformationResponseListener, createDriverResponseErrorListener);
        }
    };


    private void createAccount() {
        Log.d(TAG, "createAccount");

        if (!validateCreateAccountForm()) {
            return;
        }

        String username = mUsernameField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        String type = "driver";
        String firstname = mFirstnameField.getText().toString().trim();
        String lastname = mLastnameField.getText().toString().trim();
        String email = mEmailField.getText().toString().trim();

        mServerHandler.createServerUser(type, firstname, lastname, email, username, password, createDriverResponseListener, createDriverResponseErrorListener);

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
        } else if (i == R.id.text_change_to_passenger) {
            Log.d(TAG, "change activity to LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}