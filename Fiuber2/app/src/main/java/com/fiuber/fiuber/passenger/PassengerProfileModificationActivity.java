package com.fiuber.fiuber.passenger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

public class PassengerProfileModificationActivity extends AppCompatActivity {

    private static final String TAG = "ProfileModificationAct";

    private ServerHandler mServerHandler;

    SharedPreferences mPreferences;

    Toolbar toolbar;

    private EditText mFirstnameField;
    private EditText mLastnameField;
    private EditText mEmailField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_passenger_profile_modification);

        mServerHandler = new ServerHandler(this.getApplicationContext());
        mPreferences = getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        mFirstnameField = findViewById(R.id.text_firstname);
        mLastnameField = findViewById(R.id.text_lastname);
        mEmailField = findViewById(R.id.text_email);


        mFirstnameField.setText(mPreferences.getString(Constants.KEY_FIRSTNAME, ""));
        mLastnameField.setText(mPreferences.getString(Constants.KEY_LASTNAME, ""));
        mEmailField.setText(mPreferences.getString(Constants.KEY_EMAIL, ""));

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

    Response.ErrorListener saveModificationsUserResponseErrorListener = new Response.ErrorListener() {
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

    Response.Listener<JSONObject> saveModificationsUserResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "saveModificationsUserResponseListener Successful. Response: " + response.toString());
            mPreferences.edit().putString(Constants.KEY_FIRSTNAME, mFirstnameField.getText().toString().trim()).apply();
            mPreferences.edit().putString(Constants.KEY_LASTNAME, mLastnameField.getText().toString().trim()).apply();
            mPreferences.edit().putString(Constants.KEY_EMAIL, mEmailField.getText().toString().trim()).apply();

            Log.d(TAG, "change activity to PassengerProfileActivity");
            startActivity(new Intent(getApplicationContext(), PassengerProfileActivity.class));
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
                    mServerHandler.saveModificationsUser(username, password, firstname, lastname, email, saveModificationsUserResponseListener, saveModificationsUserResponseErrorListener);
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

        return valid;
    }

}





