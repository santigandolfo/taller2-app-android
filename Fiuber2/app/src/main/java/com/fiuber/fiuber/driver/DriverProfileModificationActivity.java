package com.fiuber.fiuber.driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fiuber.fiuber.Constants;
import com.fiuber.fiuber.R;
import com.fiuber.fiuber.server.ServerHandler;

import org.json.JSONObject;

import java.util.Arrays;

public class DriverProfileModificationActivity extends AppCompatActivity {

    private static final String TAG = "DriverProfileModAct";

    private ServerHandler mServerHandler;

    SharedPreferences mPreferences;


    Toolbar toolbar;


    private EditText mFirstnameField;
    private EditText mLastnameField;
    private EditText mEmailField;

    private EditText mCarModelField;
    private EditText mCarColorField;
    private EditText mCarBrandField;
    private EditText mCarYearField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_profile_modification);

        mServerHandler = new ServerHandler(this.getApplicationContext());
        mPreferences = getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        mFirstnameField = findViewById(R.id.text_firstname);
        mLastnameField = findViewById(R.id.text_lastname);
        mEmailField = findViewById(R.id.text_email);

        mCarModelField = findViewById(R.id.text_car_model);
        mCarColorField = findViewById(R.id.text_car_color);
        mCarBrandField = findViewById(R.id.text_car_brand);
        mCarYearField = findViewById(R.id.text_car_year);

        mCarModelField.setVisibility(View.VISIBLE);
        mCarColorField.setVisibility(View.VISIBLE);
        mCarBrandField.setVisibility(View.VISIBLE);
        mCarYearField.setVisibility(View.VISIBLE);

        mCarYearField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        //Defaults
        mFirstnameField.setText(mPreferences.getString(Constants.KEY_FIRSTNAME, ""));
        mLastnameField.setText(mPreferences.getString(Constants.KEY_LASTNAME, ""));
        mEmailField.setText(mPreferences.getString(Constants.KEY_EMAIL, ""));

        mCarModelField.setText(mPreferences.getString(Constants.KEY_CAR_MODEL, ""));
        mCarColorField.setText(mPreferences.getString(Constants.KEY_CAR_MODEL, ""));
        mCarBrandField.setText(mPreferences.getString(Constants.KEY_CAR_BRAND, ""));
        mCarYearField.setText(mPreferences.getString(Constants.KEY_CAR_YEAR, ""));

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    Response.ErrorListener saveCarInformationResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "saveModificationsUserResponseErrorListener Failed. Response Error: " + error.toString());
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                Log.e(TAG, "Response statusCode: " + response.statusCode);
                Log.e(TAG, "Response data: " + Arrays.toString(response.data));
            }
            Toast.makeText(getApplicationContext(), "Modification of Profile Failed", Toast.LENGTH_SHORT).show();
        }
    };

    Response.Listener<JSONObject> saveCarInformationResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.e(TAG, "saveCarInformationResponseListener Successful. Response: " + response.toString());

            mPreferences.edit().putString(Constants.KEY_CAR_MODEL, mCarModelField.getText().toString().trim()).apply();
            mPreferences.edit().putString(Constants.KEY_CAR_COLOR, mCarColorField.getText().toString().trim()).apply();
            mPreferences.edit().putString(Constants.KEY_CAR_BRAND, mCarBrandField.getText().toString().trim()).apply();
            mPreferences.edit().putString(Constants.KEY_CAR_YEAR, mCarYearField.getText().toString().trim()).apply();

            Log.d(TAG, "change activity to DriverProfileActivity");
            startActivity(new Intent(getApplicationContext(), DriverProfileActivity.class));
        }
    };

    Response.Listener<JSONObject> saveUserInformationResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.e(TAG, "saveUserInformationResponseListener Successful. Response: " + response.toString());

            mPreferences.edit().putString(Constants.KEY_FIRSTNAME, mFirstnameField.getText().toString().trim()).apply();
            mPreferences.edit().putString(Constants.KEY_LASTNAME, mLastnameField.getText().toString().trim()).apply();
            mPreferences.edit().putString(Constants.KEY_EMAIL, mEmailField.getText().toString().trim()).apply();

            String username = mPreferences.getString(Constants.KEY_USERNAME, "");
            String password = mPreferences.getString(Constants.KEY_PASSWORD, "");

            String carModel = mCarModelField.getText().toString().trim();
            String carColor = mCarColorField.getText().toString().trim();
            String carBrand = mCarBrandField.getText().toString().trim();
            String carYear = mCarYearField.getText().toString().trim();

            //TODO: Change this
            Log.d(TAG, "change activity to DriverProfileActivity");
            startActivity(new Intent(getApplicationContext(), DriverProfileActivity.class));

            //mServerHandler.saveModificationsCar(username, password, carModel, carColor, carBrand, carYear, saveCarInformationResponseListener, saveCarInformationResponseErrorListener);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                onBackPressed();    //Call the back button's method
                return true;
            case R.id.action_save:
                if (validateFullForm()) {

                    String username = mPreferences.getString(Constants.KEY_USERNAME, "");
                    String password = mPreferences.getString(Constants.KEY_PASSWORD, "");

                    String firstname = mFirstnameField.getText().toString().trim();
                    String lastname = mLastnameField.getText().toString().trim();
                    String email = mEmailField.getText().toString().trim();

                    mServerHandler.saveModificationsUser(username, password, firstname, lastname, email, saveUserInformationResponseListener, saveCarInformationResponseErrorListener);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private boolean validateFullForm() {
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

        String carModel = mCarModelField.getText().toString().trim();
        if (TextUtils.isEmpty(carModel)) {
            mCarModelField.setError("Required.");
            valid = false;
        } else {
            mCarModelField.setError(null);
        }

        String carColor = mCarColorField.getText().toString().trim();
        if (TextUtils.isEmpty(carColor)) {
            mCarColorField.setError("Required.");
            valid = false;
        } else {
            mCarColorField.setError(null);
        }

        String carBrand = mCarBrandField.getText().toString().trim();
        if (TextUtils.isEmpty(carBrand)) {
            mCarBrandField.setError("Required.");
            valid = false;
        } else {
            mCarBrandField.setError(null);
        }

        String carYear = mCarYearField.getText().toString().trim();
        if (TextUtils.isEmpty(carYear)) {
            mCarYearField.setError("Required.");
            valid = false;
        } else {
            mCarYearField.setError(null);
        }

        return valid;
    }

}









