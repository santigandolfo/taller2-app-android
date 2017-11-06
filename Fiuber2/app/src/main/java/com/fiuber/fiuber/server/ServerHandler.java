package com.fiuber.fiuber.server;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ServerHandler {

    private static final String TAG = "ServerHandler";
    private RequestQueue mRequestQueue;
    private static final String URL = "https://taller2-fiuber-app-server.herokuapp.com";

    private static final String CREATE_USER = "/users";
    private static final String LOGIN_USER = "/security";

    private static final String MODIFY_USER = "/users";


    private static final String KEY_TYPE = "type";
    private static final String KEY_FIRSTNAME = "name";
    private static final String KEY_LASTNAME = "surname";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_AUTH_TOKEN = "auth_token";

    private static final String KEY_AUTHORIZATION = "Authorization";

    public ServerHandler(Context aplicationContext) {
        HttpStack httpStack;
        httpStack = new CustomHurlStack();
        mRequestQueue = MyRequestQueue.getInstance(aplicationContext/*, httpStack*/).getRequestQueue();
    }

    public <T> void addToRequestQueue(Request<T> req) {
        mRequestQueue.add(req);
    }


    public void createServerUser(String type, String firstname, String lastname, String email, String username,  String password, Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "createServerUser:" + email);

        String FINAL_URL = URL + CREATE_USER;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(KEY_TYPE, type);
        params.put(KEY_FIRSTNAME, firstname);
        params.put(KEY_LASTNAME, lastname);
        params.put(KEY_EMAIL, email);
        params.put(KEY_USERNAME, username);
        params.put(KEY_PASSWORD, password);

        Log.e(TAG, "JsonObject: "+params.toString());

        Log.d(TAG, "creating JsonObjectRequest");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, FINAL_URL, new JSONObject(params), responseListener, responseErrorListener){

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
        Log.d(TAG, "createServerUser:" + username);

        String FINAL_URL = URL + LOGIN_USER;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(KEY_USERNAME, username);
        params.put(KEY_PASSWORD, password);
        Log.e(TAG, "JsonObject: "+params.toString());

        Log.d(TAG, "creating JsonObjectRequest");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, FINAL_URL, new JSONObject(params), responseListener, responseErrorListener){

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

    public void saveModificationsUser(String auth_token, String firstname, String lastname, String email, String username, String password, Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "createServerUser:" + email);

        String FINAL_URL = URL + MODIFY_USER;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(KEY_AUTH_TOKEN, auth_token);
        params.put(KEY_FIRSTNAME, firstname);
        params.put(KEY_LASTNAME, lastname);
        params.put(KEY_EMAIL, email);
        params.put(KEY_USERNAME, username);
        params.put(KEY_PASSWORD, password);

        Log.d(TAG, "creating JsonObjectRequest");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, FINAL_URL, new JSONObject(params), responseListener, responseErrorListener){

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

}