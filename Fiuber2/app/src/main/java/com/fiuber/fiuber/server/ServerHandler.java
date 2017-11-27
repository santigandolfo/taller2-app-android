package com.fiuber.fiuber.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import com.fiuber.fiuber.Constants;
import com.fiuber.fiuber.R;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static android.provider.Settings.Global.getString;

public class ServerHandler {

    private static final String TAG = "ServerHandler";
    private RequestQueue mRequestQueue;

    private SharedPreferences mPreferences;

    private String token;

    public ServerHandler(Context aplicationContext) {
        mRequestQueue = MyRequestQueue.getInstance(aplicationContext).getRequestQueue();
        mPreferences = aplicationContext.getSharedPreferences(Constants.MY_PREFERENCES, Context.MODE_PRIVATE);
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

    public void createServerUser(String type,
                                 String firstname,
                                 String lastname,
                                 String email,
                                 String username,
                                 String password,
                                 Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "createServerUser:" + username);

        String FINAL_URL = Constants.URL + Constants.CREATE_USER;

        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, type);
        params.put(Constants.KEY_FIRSTNAME, firstname);
        params.put(Constants.KEY_LASTNAME, lastname);
        params.put(Constants.KEY_EMAIL, email);
        params.put(Constants.KEY_USERNAME, username);
        params.put(Constants.KEY_PASSWORD, password);

        Log.d(TAG, "JsonObject: " + params.toString());

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

    private void getValidToken(String username,
                               String password,
                               Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "getValidToken:" + username);

        String FINAL_URL = Constants.URL + Constants.GET_VALID_TOKEN;

        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.KEY_USERNAME, username);
        params.put(Constants.KEY_PASSWORD, password);
        Log.d(TAG, "JsonObject: " + params.toString());

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

    private void getValidPaymentToken(Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "getValidPaymentToken");

        String FINAL_URL = Constants.GENERATE_PAYMENT_TOKEN_URL;
        String client_id = Resources.getSystem().getString(R.string.client_id);
        String client_secret = Resources.getSystem().getString(R.string.client_secret);
        HashMap<String, String> params = new HashMap<>();
        params.put("client_id", client_id);
        params.put("client_secret", client_secret);
        Log.d(TAG, "JsonObject: " + params.toString());

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

    public void generatePayment(final String rideId,
                                final String value,
                                final String month,
                                final String year,
                                final String method,
                                final String number,
                                final String cvv,
                                final String type,
                                final Response.Listener<JSONObject> responseListener,final Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "setDriversAvailability:" + rideId);

        getValidPaymentToken(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                try {
                    token = response.getString(Constants.KEY_PAYMENT_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String FINAL_URL = Constants.PAYMENTS_URL;

                String transaction_id = "fiuber-app-"+ rideId;

                HashMap<String, String> paymentMethodHash = new HashMap<>();
                paymentMethodHash.put(Constants.KEY_EXPIRATION_MONTH, month);
                paymentMethodHash.put(Constants.KEY_EXPIRATION_YEAR, year);
                paymentMethodHash.put(Constants.KEY_METHOD, method);
                paymentMethodHash.put(Constants.KEY_NUMBER, number);
                paymentMethodHash.put(Constants.KEY_CVV, cvv);
                paymentMethodHash.put(Constants.KEY_PAYMENT_TYPE, type);

                JSONObject paymentMethod = new JSONObject(paymentMethodHash);

                HashMap<String, String> params = new HashMap<>();
                params.put("transaction_id", transaction_id);
                params.put("currency", "ARS");
                params.put("value", value);

                Log.d(TAG, "creating JsonObjectRequest");
                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.POST,
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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + mPreferences.getString(Constants.KEY_AUTH_TOKEN, ""));
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void loginServerUser(String username,
                                String password,
                                Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "loginServerUser:" + username);

        String FINAL_URL = Constants.URL + Constants.LOGIN_USER;

        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.KEY_USERNAME, username);
        params.put(Constants.KEY_PASSWORD, password);
        Log.d(TAG, "JsonObject: " + params.toString());

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

    public void getUserInformation(String username,
                                   Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "getUserInformation:" + username);

        String FINAL_URL = Constants.URL + Constants.GET_USER_INFORMATION + "/" + username;

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

    public void setDriversAvailability(final String username,
                                       final String password,
                                       final String availability,
                                       final Response.Listener<JSONObject> responseListener) {
        Log.d(TAG, "setDriversAvailability:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.URL + Constants.MODIFY_DRIVER + "/" + username;

                try {
                    token = response.getString(Constants.KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                HashMap<String, String> params = new HashMap<>();
                params.put(Constants.KEY_AVAILABILITY, availability);

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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + mPreferences.getString(Constants.KEY_AUTH_TOKEN, ""));
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void saveModificationsUser(final String username,
                                      final String password,
                                      final String firstname,
                                      final String lastname,
                                      final String email,
                                      final Response.Listener<JSONObject> responseListener, final Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "saveModificationsUser:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.URL + Constants.MODIFY_USER + "/" + username;

                try {
                    token = response.getString(Constants.KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HashMap<String, String> params = new HashMap<>();
                params.put(Constants.KEY_FIRSTNAME, firstname);
                params.put(Constants.KEY_LASTNAME, lastname);
                params.put(Constants.KEY_EMAIL, email);
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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + token);
                        Log.d(TAG, "HEADER: " + headers.toString());
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void saveModificationsCar(final String username,
                                     final String password,
                                     final String carModel,
                                     final String carColor,
                                     final String carPlate,
                                     final String carYear,
                                     final Response.Listener<JSONObject> responseListener, final Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "saveModificationsCar:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.URL + Constants.MODIFY_USER + "/" + username + "/" + "cars";

                try {
                    token = response.getString(Constants.KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HashMap<String, String> params = new HashMap<>();
                params.put(Constants.KEY_CAR_MODEL, carModel);
                params.put(Constants.KEY_CAR_COLOR, carColor);
                params.put(Constants.KEY_CAR_PLATE, carPlate);
                params.put(Constants.KEY_CAR_YEAR, carYear);

                Log.d(TAG, "creating JsonObjectRequest");
                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.POST,
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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + token);
                        Log.d(TAG, "HEADER: " + headers.toString());
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void updateUserCoordinates(final String username,
                                      final String password,
                                      final String latitude,
                                      final String longitude,
                                      final Response.Listener<JSONObject> responseListener) {
        Log.d(TAG, "updateUserCoordinates:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.URL + Constants.MODIFY_USER + "/" + username + "/" + "coordinates";

                try {
                    token = response.getString(Constants.KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HashMap<String, String> params = new HashMap<>();
                params.put(Constants.KEY_LATITUDE, latitude);
                params.put(Constants.KEY_LONGITUDE, longitude);

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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + token);
                        Log.d(TAG, "HEADER: " + headers.toString());
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void requestRide(final String username,
                            final String password,
                            final String latitudeInitial,
                            final String longitudeInitial,
                            final String latitudeFinal,
                            final String longitudeFinal,
                            final Response.Listener<JSONObject> responseListener) {
        Log.d(TAG, "requestRide:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.URL + Constants.REQUEST_RIDE + "/" + username + "/" + "request";

                try {
                    token = response.getString(Constants.KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HashMap<String, String> params = new HashMap<>();
                params.put(Constants.KEY_LATITUDE_INITIAL, latitudeInitial);
                params.put(Constants.KEY_LONGITUDE_INITIAL, longitudeInitial);
                params.put(Constants.KEY_LATITUDE_FINAL, latitudeFinal);
                params.put(Constants.KEY_LONGITUDE_FINAL, longitudeFinal);
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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + token);
                        Log.d(TAG, "HEADER: " + headers.toString());
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void cancelRide(final String username,
                           final String password,
                           final Response.Listener<JSONObject> responseListener) {
        Log.d(TAG, "cancelRide:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.URL + Constants.REQUEST_RIDE + "/" + username + "/" + "request";

                try {
                    token = response.getString(Constants.KEY_AUTH_TOKEN);
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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + token);
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void sendFirebaseToken(final String username,
                                  String password,
                                  final String refreshedToken) {
        Log.d(TAG, "sendFirebaseToken:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.URL + Constants.MODIFY_USER + "/" + username + "/" + "push-token";

                try {
                    token = response.getString(Constants.KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HashMap<String, String> params = new HashMap<>();
                params.put(Constants.KEY_FIREBASE_TOKEN, refreshedToken);

                Log.d(TAG, "BODY: " + params.toString());

                Log.d(TAG, "URL: " + FINAL_URL);

                Log.d(TAG, "creating JsonObjectRequest");
                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.PUT,
                        FINAL_URL,
                        new JSONObject(params), defaultResponseListener, defaultResponseErrorListener) {

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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + token);
                        Log.d(TAG, "HEADER: " + headers.toString());
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