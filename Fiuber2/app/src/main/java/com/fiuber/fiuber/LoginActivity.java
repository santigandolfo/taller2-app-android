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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private EditText mFirstnameField;
    private EditText mLastnameField;
    private EditText mEmailField;
    private EditText mUsernameField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;

    private CallbackManager mCallbackManager;

    private ServerHandler mServerHandler;
    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditorPreferences;

    String MY_PREFERENCES = "MyPreferences";

    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mServerHandler = new ServerHandler(this.getApplicationContext());
        mPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        mEditorPreferences = mPreferences.edit();

        // Views
        mFirstnameField = findViewById(R.id.edit_text_firstname);
        mLastnameField = findViewById(R.id.edit_text_lastname);
        mEmailField = findViewById(R.id.edit_text_email);
        mUsernameField = findViewById(R.id.edit_text_username);
        mPasswordField = findViewById(R.id.edit_text_password);

        // Buttons
        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.text_register).setOnClickListener(this);
        findViewById(R.id.text_change_to_driver).setOnClickListener(this);

        //Firebase Authenticator
        mAuth = FirebaseAuth.getInstance();

        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });
        // [END initialize_fblogin]
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if("true".equals(mPreferences.getString("login", "false"))){
            Log.d(TAG, "change activity to MapsActivity");
            startActivity(new Intent(this, MapsActivity.class));
        }
/*        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "change activity to MapsActivity");
            startActivity(new Intent(this, MapsActivity.class));

        }*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
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
/*        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            //Toast.makeText(LoginActivity.this, "Log In Successful", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "loginServerUser");
                                mServerHandler.loginServerUser(username, password, loginServerUserResponseListenerJSONObject, loginServerUserResponseErrorListener);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Login Failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        // [END sign_in_with_email]*/
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "change activity to MapsActivity");
                                startActivity(new Intent(LoginActivity.this, MapsActivity.class));

                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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



/*
    //TODO: MODIFY WHEN POSSIBLE!!
    public void createUserTEST2(String email, String password) throws JSONException {
        Log.d(TAG, "createUser:" + email);

        String FINAL_URL = URL + CREATE_USER;

        JSONObject jsonBody = new JSONObject();
        jsonBody.put(KEY_EMAIL, email);
        jsonBody.put(KEY_PASSWORD, password);

            //TODO: DELETE THIS:
            jsonBody.put("type", "passenger");
            jsonBody.put("username", "c");
            jsonBody.put("password", "c");
            jsonBody.put("fb","c");
            jsonBody.put("firstName", "C");
            jsonBody.put("lastName", "C");
            jsonBody.put("country", "Argentina");
            jsonBody.put("email", "c@c.com");
            jsonBody.put("birthdate", "01/01/2001");


    final String mRequestBody = jsonBody.toString();

    Response.Listener<String> responseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            Log.i(TAG, response);
            Toast.makeText(LoginActivity.this, "Registration with server successfull!", Toast.LENGTH_SHORT).show();

        }
    };
    Response.ErrorListener responseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.toString());
            Toast.makeText(LoginActivity.this, "Error while trying to Register with server.", Toast.LENGTH_SHORT).show();

        }
    };

    StringRequest stringRequest = new StringRequest(Request.Method.POST, FINAL_URL, responseListener, responseErrorListener) {
        @Override
        public String getBodyContentType() {
            return "application/json";
        }

        @Override
        public byte[] getBody() throws AuthFailureError {
            try {
                return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
            } catch (UnsupportedEncodingException uee) {
                VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                return null;
            }
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            String responseString = "";

            responseString = String.valueOf(response.statusCode);

            return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
        }
    };
        Log.d(TAG, "Adding stringRequest to requestQueue");
                mRequestQueue.add(stringRequest);

                }
*/