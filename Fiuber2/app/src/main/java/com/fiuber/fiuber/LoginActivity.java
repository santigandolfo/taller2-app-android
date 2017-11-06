package com.fiuber.fiuber;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private EditText mUsernameField;
    private EditText mPasswordField;

    private ServerHandler mServerHandler;
    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditorPreferences;

    String MY_PREFERENCES = "MyPreferences";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mServerHandler = new ServerHandler(this.getApplicationContext());
        mPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        mEditorPreferences = mPreferences.edit();

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
        if("true".equals(mPreferences.getString("login", "false"))){
            Log.d(TAG, "change activity to MapsActivity");
            startActivity(new Intent(this, MapsActivity.class));
        }
    }

    Response.ErrorListener loginServerUserResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Response error: " + error.toString());
            NetworkResponse response = error.networkResponse;
            if(response != null && response.data != null){
                Log.e(TAG, "Response statusCode: "+response.statusCode);
                Log.e(TAG, "Response data: "+response.data.toString());
            }
            Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
        }
    };

    Response.Listener<JSONObject> loginServerUserResponseListenerJSONObject = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            //TODO: Difetentiate between driver and rider here
            Log.d(TAG, "Login Successfull. Response: " + response.toString());
            Toast.makeText(getApplicationContext(), "Login Successfull", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Change activity to MapsActivity");
            try {
                mEditorPreferences.putString("auth_token", response.getString("auth_token")).apply();
                Log.d(TAG, "mPreferences Token: " + mPreferences.getString("auth_token", ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mEditorPreferences.putString("login", "true").apply();
            startActivity(new Intent(LoginActivity.this, DriverMapsActivity.class));
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
            Log.d(TAG, "change activity to RegisterUserActivity");
            startActivity(new Intent(this, RegisterUserActivity.class));
        } else if (i == R.id.text_change_to_driver) {
            Log.d(TAG, "change activity to RegisterDriverActivity");
            startActivity(new Intent(this, RegisterDriverActivity.class));
        }
    }
}