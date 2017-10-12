package com.fiuber.fiuber;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileModificationActivity extends AppCompatActivity {

    private static final String TAG = "ProfileModificationAct";

    private RequestQueue mRequestQueue;

    private ServerHandler mServerHandler;

    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditorPreferences;

    String MY_PREFERENCES = "MyPreferences";

    Toolbar toolbar;

    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";

    private static final String KEY_PASSWORD = "password";

    private EditText mFirstnameField;
    private EditText mLastnameField;
    private EditText mUsernameField;
    private EditText mEmailField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_modification);

        mServerHandler = new ServerHandler(this.getApplicationContext());
        mPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        mEditorPreferences = mPreferences.edit();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        mFirstnameField = findViewById(R.id.text_name);
        mLastnameField = findViewById(R.id.text_surname);
        mUsernameField = findViewById(R.id.text_username);
        mEmailField = findViewById(R.id.text_email);


        mFirstnameField.setText(mPreferences.getString(KEY_FIRSTNAME, ""));
        mLastnameField.setText(mPreferences.getString(KEY_LASTNAME, ""));
        mUsernameField.setText(mPreferences.getString(KEY_USERNAME, ""));
        mEmailField.setText(mPreferences.getString(KEY_EMAIL, ""));

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

    Response.ErrorListener modifyUserProfileResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "modifyUserProfileResponseErrorListener ErrorResponse. Response error: "+error.toString());
        }
    };

    Response.Listener<JSONObject> modifyUserProfileResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.i(TAG, "modifyUserProfileResponseListener Response");
            try {
                String auth_token = response.getString(KEY_AUTH_TOKEN);
                String firstname = mFirstnameField.getText().toString().trim();
                String lastname = mLastnameField.getText().toString().trim();
                String username = mUsernameField.getText().toString().trim();
                String email = mEmailField.getText().toString().trim();
                //mServerHandler.saveModificationsUser(auth_token,firstname, lastname, username, email, saveModificationsUserResponseListener, saveModificationsUserResponseErrorListener);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                onBackPressed();    //Call the back button's method
                return true;
            case R.id.action_save:
                String currentUsername = mPreferences.getString(KEY_USERNAME, "");
                String currentPassword = mPreferences.getString(KEY_PASSWORD, "");
                //loginServerUserJson(currentUsername, currentPassword, modifyUserProfileResponseListener, modifyUserProfileResponseErrorListener)
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}

/*    Response.ErrorListener saveModificationsUserResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "saveModificationsUserResponseErrorListener ErrorResponse. Response error: "+error.toString());
        }
    };

    Response.Listener<JSONObject> saveModificationsUserResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
                    Log.e(TAG, "saveModificationsUserResponseListener Response");
                                mEditorPreferences.putString(KEY_FIRSTNAME, firstname).apply();
                                mEditorPreferences.putString(KEY_LASTNAME, lastname).apply();
                                mEditorPreferences.putString(KEY_USERNAME, username).apply();
                                mEditorPreferences.putString(KEY_EMAIL, email).apply();
            Log.d(TAG, "change activity to ProfileActivity");
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        }
    };
    */
