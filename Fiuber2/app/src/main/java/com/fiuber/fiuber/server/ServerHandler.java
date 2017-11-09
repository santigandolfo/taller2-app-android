package com.fiuber.fiuber.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;

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
    private static final String MODIFY_DRIVER = "/drivers";
    private static final String GET_USER_INFORMATION = "/users";

    private static final String REQUEST_RIDE = "/riders";


    private static final String KEY_TYPE = "type";
    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_AUTH_TOKEN = "auth_token";

    private static final String KEY_AVAILABILITY = "availability";

    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    private static final String KEY_LATITUDE_INITIAL = "latitude_initial";
    private static final String KEY_LONGITUDE_INITIAL = "longitude_initial";
    private static final String KEY_LATITUDE_FINAL = "latitude_final";
    private static final String KEY_LONGITUDE_FINAL = "longitude_final";

    private static final String KEY_CAR_MODEL = "model";
    private static final String KEY_CAR_COLOR = "color";
    private static final String KEY_CAR_PLATE = "id";
    private static final String KEY_CAR_YEAR = "year";

    private static final String KEY_AUTHORIZATION = "Authorization";


    private SharedPreferences mPreferences;

    private String token;

    public ServerHandler(Context aplicationContext) {
        mRequestQueue = MyRequestQueue.getInstance(aplicationContext).getRequestQueue();
        String MY_PREFERENCES = "MyPreferences";
        mPreferences = aplicationContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
    }

    private <T> void addToRequestQueue(Request<T> req) {
        mRequestQueue.add(req);
    }

    private Response.ErrorListener defaultResponseErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Request Failed. Response Error: " + error.toString());
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                Log.e(TAG, "Response statusCode: " + response.statusCode);
                Log.e(TAG, "Response data: " + Arrays.toString(response.data));
            }
        }
    };

    Response.Listener<JSONObject> defaultResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "Request Successful. Response: " + response.toString());
        }
    };

    public void createServerUser(String type, String firstname, String lastname, String email, String username, String password, Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "createServerUser:" + username);

        String FINAL_URL = URL + CREATE_USER;

        HashMap<String, String> params = new HashMap<>();
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

    private void getValidToken(String username, String password, Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "getValidToken:" + username);

        String FINAL_URL = URL + GET_VALID_TOKEN;

        HashMap<String, String> params = new HashMap<>();
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

        HashMap<String, String> params = new HashMap<>();
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

    public void setDriversAvailability(final String username, final String password, final String availability, final Response.Listener<JSONObject> responseListener) {
        Log.d(TAG, "setDriversAvailability:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = URL + MODIFY_DRIVER + "/" + username;

                try {
                    token = response.getString(KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                HashMap<String, String> params = new HashMap<>();
                params.put(KEY_AVAILABILITY, availability);

                Log.d(TAG, "creating JsonObjectRequest");
                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.PATCH,
                        FINAL_URL,
                        new JSONObject(params), responseListener, defaultResponseErrorListener) {

                    @Override
                    protected VolleyError parseNetworkError(VolleyError volleyError) {
                        if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                            volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                        }

                        return volleyError;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put(KEY_AUTHORIZATION, "Bearer " + mPreferences.getString(KEY_AUTH_TOKEN, ""));
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void saveModificationsUser(final String username, final String password, final String firstname, final String lastname, final String email, final Response.Listener<JSONObject> responseListener, final Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "saveModificationsUser:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = URL + MODIFY_USER + "/" + username;

                try {
                    token = response.getString(KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HashMap<String, String> params = new HashMap<>();
                params.put(KEY_FIRSTNAME, firstname);
                params.put(KEY_LASTNAME, lastname);
                params.put(KEY_EMAIL, email);
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
                        Map<String, String> headers = new HashMap<>();
                        headers.put(KEY_AUTHORIZATION, "Bearer " + token);
                        Log.d(TAG, "HEADER: " + headers.toString());
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void saveModificationsCar(final String username, final String password, final String carModel, final String carColor, final String carPlate, final String carYear, final Response.Listener<JSONObject> responseListener, final Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "saveModificationsCar:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = URL + MODIFY_DRIVER + "/" + username + "/" + "car";

                try {
                    token = response.getString(KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HashMap<String, String> params = new HashMap<>();
                params.put(KEY_CAR_MODEL, carModel);
                params.put(KEY_CAR_COLOR, carColor);
                params.put(KEY_CAR_PLATE, carPlate);
                params.put(KEY_CAR_YEAR, carYear);

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
                        Map<String, String> headers = new HashMap<>();
                        headers.put(KEY_AUTHORIZATION, "Bearer " + token);
                        Log.d(TAG, "HEADER: " + headers.toString());
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void updateUserCoordinates(final String username, final String password, final String latitude, final String longitude, final Response.Listener<JSONObject> responseListener) {
        Log.d(TAG, "updateUserCoordinates:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = URL + MODIFY_USER + "/" + username + "/" + "coordinates";

                try {
                    token = response.getString(KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HashMap<String, String> params = new HashMap<>();
                params.put(KEY_LATITUDE, latitude);
                params.put(KEY_LONGITUDE, longitude);

                Log.d(TAG, "BODY: " + params.toString());

                Log.d(TAG, "URL: " + FINAL_URL);

                Log.d(TAG, "creating JsonObjectRequest");
                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.PUT,
                        FINAL_URL,
                        new JSONObject(params), responseListener, defaultResponseErrorListener) {

                    @Override
                    protected VolleyError parseNetworkError(VolleyError volleyError) {
                        if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                            volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                        }

                        return volleyError;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put(KEY_AUTHORIZATION, "Bearer " + token);
                        Log.d(TAG, "HEADER: " + headers.toString());
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void requestRide(final String username, final String password, final String latitudeInitial, final String longitudeInitial, final String latitudeFinal, final String longitudeFinal, final Response.Listener<JSONObject> responseListener) {
        Log.d(TAG, "requestRide:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = URL + REQUEST_RIDE + "/" + username + "/" + "request";

                try {
                    token = response.getString(KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HashMap<String, String> params = new HashMap<>();
                params.put(KEY_LATITUDE_INITIAL, latitudeInitial);
                params.put(KEY_LONGITUDE_INITIAL, longitudeInitial);
                params.put(KEY_LATITUDE_FINAL, latitudeFinal);
                params.put(KEY_LONGITUDE_FINAL, longitudeFinal);
                Log.d(TAG, "BODY: " + params.toString());
                Log.d(TAG, "URL: " + FINAL_URL);


                Log.d(TAG, "creating JsonObjectRequest");
                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.POST,
                        FINAL_URL,
                        new JSONObject(params), responseListener, defaultResponseErrorListener) {

                    @Override
                    protected VolleyError parseNetworkError(VolleyError volleyError) {
                        if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                            volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                        }

                        return volleyError;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put(KEY_AUTHORIZATION, "Bearer " + token);
                        Log.d(TAG, "HEADER: " + headers.toString());
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void cancelRide(final String username, final String password, final Response.Listener<JSONObject> responseListener) {
        Log.d(TAG, "cancelRide:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = URL + REQUEST_RIDE + "/" + username + "/" + "request";

                try {
                    token = response.getString(KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "creating JsonObjectRequest");
                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.DELETE,
                        FINAL_URL,
                        null, responseListener, defaultResponseErrorListener) {

                    @Override
                    protected VolleyError parseNetworkError(VolleyError volleyError) {
                        if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                            volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                        }

                        return volleyError;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put(KEY_AUTHORIZATION, "Bearer " + token);
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }
}
/*    public void sendNotification(String rideId, String chatToken) {
        Log.d(TAG, "sendNotification:" + rideId);

        String FINAL_URL = "https://fcm.googleapis.com/v1/projects/fiuber2-7a583/messages:send";

        try {
            JSONObject notification = new JSONObject();
            notification.put("body", "New Message");
            notification.put("title", "New Message");
            JSONObject message = new JSONObject();
            message.put("to", chatToken);
            message.put("notification", notification);
            Log.d(TAG, "BODY: " + message.toString());

            Log.d(TAG, "creating JsonObjectRequest");
            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    FINAL_URL,
                    message, defaultResponseListener, defaultResponseErrorListener) {

                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                        volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                    }

                    return volleyError;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put(KEY_AUTHORIZATION, "Bearer " + "AIzaSyCyxTI4oa79jDtxGC8zO4Ny_zk6JmJlT-0");
                    headers.put("Content-Type", "application/json");
                    Log.d(TAG, "HEADER: " + headers.toString());
                    return headers;
                }
            };

            Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
            addToRequestQueue(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}*/


/*    //TODO: Ask Gonza or Fede the real logout URL
    public void logoutServerUser(String username, final String token, Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "logoutServerUser:");

        String FINAL_URL = URL + LOGIN_USER + username;

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

    }*/