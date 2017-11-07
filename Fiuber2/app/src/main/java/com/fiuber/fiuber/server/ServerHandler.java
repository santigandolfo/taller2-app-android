package com.fiuber.fiuber.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ServerHandler {

    private static final String TAG = "ServerHandler";
    private RequestQueue mRequestQueue;
    private static final String URL = "https://taller2-fiuber-app-server.herokuapp.com";


    private static final String LOGIN_USER = "/security";
    private static final String CREATE_USER = "/users";
    private static final String GET_VALID_TOKEN = "/security";
    private static final String MODIFY_USER = "/users";
    private static final String GET_USER_INFORMATION = "/users";


    private static final String KEY_TYPE = "type";
    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_AUTH_TOKEN = "auth_token";

    private static final String KEY_CAR_MODEL = "model";
    private static final String KEY_CAR_COLOR = "color";
    private static final String KEY_CAR_ID = "id";
    private static final String KEY_CAR_YEAR = "year";

    private static final String KEY_AUTHORIZATION = "Authorization";


    String MY_PREFERENCES = "MyPreferences";

    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditorPreferences;

    public ServerHandler(Context aplicationContext) {
        HttpStack httpStack;
        httpStack = new CustomHurlStack();
        mRequestQueue = MyRequestQueue.getInstance(aplicationContext/*, httpStack*/).getRequestQueue();
        mPreferences = aplicationContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        mEditorPreferences = mPreferences.edit();
    }

    public <T> void addToRequestQueue(Request<T> req) {
        mRequestQueue.add(req);
    }


    public void createServerUser(String type, String firstname, String lastname, String email, String username, String password, Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "createServerUser:" + email);

        String FINAL_URL = URL + CREATE_USER;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(KEY_TYPE, type);
        params.put(KEY_FIRSTNAME, firstname);
        params.put(KEY_LASTNAME, lastname);
        params.put(KEY_EMAIL, email);
        params.put(KEY_USERNAME, username);
        params.put(KEY_PASSWORD, password);

        Log.e(TAG, "JsonObject: " + params.toString());

        Log.d(TAG, "creating JsonObjectRequest");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, FINAL_URL, new JSONObject(params), responseListener, responseErrorListener) {

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                }

                return volleyError;
            }
        };

        Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
        this.addToRequestQueue(req);

    }

    public void createDriver(String type, String firstname, String lastname, String email, String username, String password,String carModel,String carColor,String carId,String carYear,Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "createServerUser:" + email);

        String FINAL_URL = URL + CREATE_USER;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(KEY_TYPE, type);
        params.put(KEY_FIRSTNAME, firstname);
        params.put(KEY_LASTNAME, lastname);
        params.put(KEY_EMAIL, email);
        params.put(KEY_USERNAME, username);
        params.put(KEY_PASSWORD, password);

        params.put(KEY_CAR_MODEL, carModel);
        params.put(KEY_CAR_COLOR, carColor);
        params.put(KEY_CAR_ID, carId);
        params.put(KEY_CAR_YEAR, carYear);

        Log.e(TAG, "JsonObject: " + params.toString());

        Log.d(TAG, "creating JsonObjectRequest");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, FINAL_URL, new JSONObject(params), responseListener, responseErrorListener) {

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                }

                return volleyError;
            }
        };

        Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
        this.addToRequestQueue(req);

    }

    public void getValidToken(String username, String password, Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "getValidToken:" + username);

        String FINAL_URL = URL + GET_VALID_TOKEN;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(KEY_USERNAME, username);
        params.put(KEY_PASSWORD, password);
        Log.e(TAG, "JsonObject: " + params.toString());

        Log.d(TAG, "creating JsonObjectRequest");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, FINAL_URL, new JSONObject(params), responseListener, responseErrorListener) {

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                }

                return volleyError;
            }
        };

        Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
        this.addToRequestQueue(req);

    }

    public void loginServerUser(String username, String password, Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "loginServerUser:" + username);

        String FINAL_URL = URL + LOGIN_USER;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(KEY_USERNAME, username);
        params.put(KEY_PASSWORD, password);
        Log.e(TAG, "JsonObject: " + params.toString());

        Log.d(TAG, "creating JsonObjectRequest");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, FINAL_URL, new JSONObject(params), responseListener, responseErrorListener) {

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                }

                return volleyError;
            }
        };

        Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
        this.addToRequestQueue(req);

    }

    public void logoutServerUser(final String token, Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "logoutServerUser:");

        String FINAL_URL = URL + LOGIN_USER;
        Log.d(TAG, "creating JsonObjectRequest");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.DELETE, FINAL_URL, null, responseListener, responseErrorListener) {


            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                }

                return volleyError;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put(KEY_AUTHORIZATION, "Bearer " + token);
                return headers;
            }

        };

        Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
        this.addToRequestQueue(req);

    }

    public void getUserInformation(String username, Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "getUserInformation:" + username);

        String FINAL_URL = URL + GET_USER_INFORMATION + "/" + username;

        Log.d(TAG, "creating JsonObjectRequest");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, FINAL_URL, null, responseListener, responseErrorListener) {

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                }

                return volleyError;
            }
        };

        Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
        this.addToRequestQueue(req);

    }

/*    private Response.ErrorListener getValidTokenErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Response error: " + error.toString());
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                Log.e(TAG, "Response statusCode: " + response.statusCode);
                Log.e(TAG, "Response data: " + Arrays.toString(response.data));
            }
        }
    };

    private Response.Listener<JSONObject> getValidTokenListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {

            Log.d(TAG, "Validation Successfull. Response: " + response.toString());

            String FINAL_URL = URL + MODIFY_USER + "/" + mPreferences.getString(KEY_NEW_FIRSTNAME, "");

            mEditorPreferences.putString(KEY_AUTH_TOKEN, response.getString(KEY_AUTH_TOKEN)).apply();

            HashMap<String, String> params = new HashMap<String, String>();
            params.put(KEY_FIRSTNAME, mPreferences.getString(KEY_NEW_FIRSTNAME, ""));
            params.put(KEY_LASTNAME, mPreferences.getString(KEY_NEW_LASTNAME, ""));
            params.put(KEY_USERNAME, mPreferences.getString(KEY_NEW_USERNAME, ""));
            params.put(KEY_EMAIL, mPreferences.getString(KEY_NEW_EMAIL, ""));

            Log.d(TAG, "creating JsonObjectRequest");
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, FINAL_URL, new JSONObject(params), responseListener, responseErrorListener) {

                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                        volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                    }

                    return volleyError;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put(KEY_AUTHORIZATION, "Bearer " + response.getString(KEY_AUTH_TOKEN));
                    return headers;
                }
            };

            Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
            addToRequestQueue(req);
        }
    };*/

    public void saveModificationsUser(final String currentUsername, final String currentPassword, final String firstname, final String lastname, final String email, final String username, final Response.Listener<JSONObject> responseListener, final Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "saveModificationsUser:" + email);

        getValidToken(currentUsername, currentPassword, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "saveModificationsUser Successfull. Response: " + response.toString());

                String FINAL_URL = URL + MODIFY_USER + "/" + username;

                try {
                    Log.d(TAG, "Validation Successfull. TOKEN: " + response.getString(KEY_AUTH_TOKEN));
                    mEditorPreferences.putString(KEY_AUTH_TOKEN, response.getString(KEY_AUTH_TOKEN)).apply();
                    Log.d(TAG, "Validation Successfull. REPEAT TOKEN: " + mPreferences.getString(KEY_AUTH_TOKEN, ""));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HashMap<String, String> params = new HashMap<String, String>();
                params.put(KEY_FIRSTNAME, firstname);
                params.put(KEY_LASTNAME, lastname);
                params.put(KEY_EMAIL, email);
                params.put(KEY_USERNAME, username);
                Log.d(TAG, "BODY: " + params.toString());

                Log.d(TAG, "URL: " + FINAL_URL);

                Log.d(TAG, "creating JsonObjectRequest");
                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.PUT,
                        FINAL_URL,
                        new JSONObject(params), responseListener, responseErrorListener) {

                    @Override
                    protected VolleyError parseNetworkError(VolleyError volleyError) {
                        if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                            volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                        }

                        return volleyError;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put(KEY_AUTHORIZATION, "Bearer " + mPreferences.getString(KEY_AUTH_TOKEN, ""));
                        Log.d(TAG, "HEADER: " + headers.toString());
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "saveModificationsUser Failed. Response Error: " + error.toString());
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    Log.e(TAG, "Response statusCode: " + response.statusCode);
                    Log.e(TAG, "Response data: " + Arrays.toString(response.data));
                }
            }
        });


    }

}