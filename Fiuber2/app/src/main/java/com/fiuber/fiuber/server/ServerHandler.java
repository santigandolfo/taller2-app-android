package com.fiuber.fiuber.server;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fiuber.fiuber.Constants;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ServerHandler {

    private static final String TAG = "ServerHandler";
    private RequestQueue mRequestQueue;

    private String auth_token;
    private String payments_token;

    public ServerHandler(Context aplicationContext) {
        mRequestQueue = MyRequestQueue.getInstance(aplicationContext).getRequestQueue();
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

    private Response.Listener<JSONObject> defaultResponseListener = new Response.Listener<JSONObject>() {
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

        String FINAL_URL = Constants.URL + Constants.USERS;

        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, type);
        params.put(Constants.KEY_FIRSTNAME, firstname);
        params.put(Constants.KEY_LASTNAME, lastname);
        params.put(Constants.KEY_EMAIL, email);
        params.put(Constants.KEY_USERNAME, username);
        params.put(Constants.KEY_PASSWORD, password);

        Log.d(TAG, "JsonObject: " + params.toString());

        Log.d(TAG, "creating JsonObjectRequest");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                FINAL_URL,
                new JSONObject(params),
                responseListener, responseErrorListener) {

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

        String FINAL_URL = Constants.URL + Constants.SECURITY;

        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.KEY_USERNAME, username);
        params.put(Constants.KEY_PASSWORD, password);
        Log.d(TAG, "JsonObject: " + params.toString());

        Log.d(TAG, "creating JsonObjectRequest");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                FINAL_URL,
                new JSONObject(params),
                responseListener, responseErrorListener) {

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
        String client_id = Constants.CLIENT_ID;
        String client_secret = Constants.CLIENT_SECRET;
        JSONObject params = new JSONObject();

        try {
            params.put("client_id", client_id);
            params.put("client_secret", client_secret);
            Log.d(TAG, "JsonObject: " + params.toString());

            Log.d(TAG, "creating JsonObjectRequest");
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                    FINAL_URL,
                    params,
                    responseListener, responseErrorListener) {

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
                    headers.put("Content-Type", "application/json");
                    return headers;
                }

            };

            Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
            this.addToRequestQueue(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void generatePayment(final String rideId,
                                final Float value,
                                final String month,
                                final String year,
                                final String method,
                                final String number,
                                final String cvv,
                                final String type,
                                final Response.Listener<JSONObject> responseListener, final Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "generatePayment:" + rideId);

        getValidPaymentToken(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.PAYMENTS_URL;

                String transaction_id = "fiuber-app-" + rideId;

                try {
                    payments_token = response.getString(Constants.KEY_PAYMENT_TOKEN);

                    JSONObject paymentMethod = new JSONObject();
                    paymentMethod.put(Constants.KEY_EXPIRATION_MONTH, month);
                    paymentMethod.put(Constants.KEY_EXPIRATION_YEAR, year);
                    paymentMethod.put(Constants.KEY_METHOD, method);
                    paymentMethod.put(Constants.KEY_NUMBER, number);
                    paymentMethod.put(Constants.KEY_CCVV, cvv);
                    paymentMethod.put(Constants.KEY_PAYMENT_TYPE, type);

                    JSONObject params = new JSONObject();
                    params.put(Constants.KEY_TRANSACTION_ID, transaction_id);
                    params.put(Constants.KEY_CURRENCY, "ARS");
                    params.put(Constants.KEY_VALUE, value);
                    params.put(Constants.KEY_PAYMENT_METHOD, paymentMethod);

                    Log.d(TAG, "creating JsonObjectRequest");
                    JsonObjectRequest req = new JsonObjectRequest(
                            Request.Method.POST,
                            FINAL_URL,
                            params, responseListener, responseErrorListener) {

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
                            headers.put("Content-Type", "application/json");
                            headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + payments_token);
                            return headers;
                        }
                    };

                    Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                    addToRequestQueue(req);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, defaultResponseErrorListener);
    }

    public void loginServerUser(String username,
                                String password,
                                Response.Listener<JSONObject> responseListener, Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "loginServerUser:" + username);

        String FINAL_URL = Constants.URL + Constants.SECURITY;

        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.KEY_USERNAME, username);
        params.put(Constants.KEY_PASSWORD, password);
        Log.d(TAG, "JsonObject: " + params.toString());

        Log.d(TAG, "creating JsonObjectRequest");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                FINAL_URL,
                new JSONObject(params),
                responseListener, responseErrorListener) {

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

        String FINAL_URL = Constants.URL + Constants.USERS + "/" + username;

        Log.d(TAG, "creating JsonObjectRequest");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                FINAL_URL,
                null,
                responseListener, responseErrorListener) {

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

    public void setDriversDuty(final String username,
                               final String password,
                               final Boolean duty,
                               final Response.Listener<JSONObject> responseListener) {
        Log.d(TAG, "setDriversDuty:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.URL + Constants.DRIVERS + "/" + username;

                try {
                    auth_token = response.getString(Constants.KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject newJSONObject = new JSONObject();

                try {
                    newJSONObject.put(Constants.KEY_DUTY, duty);

                    Log.d(TAG, "creating JsonObjectRequest");
                    JsonObjectRequest req = new JsonObjectRequest(
                            Request.Method.PATCH,
                            FINAL_URL,
                            newJSONObject, responseListener, defaultResponseErrorListener) {

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
                            headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + auth_token);
                            return headers;
                        }
                    };

                    Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                    addToRequestQueue(req);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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

                String FINAL_URL = Constants.URL + Constants.USERS + "/" + username;

                try {
                    auth_token = response.getString(Constants.KEY_AUTH_TOKEN);
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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + auth_token);
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
                                     final String carBrand,
                                     final String carYear,
                                     final Response.Listener<JSONObject> responseListener, final Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "saveModificationsCar:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.URL + Constants.DRIVERS + "/" + username + "/" + "cars";

                try {
                    auth_token = response.getString(Constants.KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject newJSONObject = new JSONObject();

                try {
                    newJSONObject.put(Constants.KEY_CAR_MODEL, carModel);
                    newJSONObject.put(Constants.KEY_CAR_COLOR, carColor);
                    newJSONObject.put(Constants.KEY_CAR_BRAND, carBrand);
                    newJSONObject.put(Constants.KEY_CAR_YEAR, Integer.parseInt(carYear));

                    Log.d(TAG, "BODY: " + newJSONObject.toString());

                    Log.d(TAG, "creating JsonObjectRequest");
                    JsonObjectRequest req = new JsonObjectRequest(
                            Request.Method.POST,
                            FINAL_URL,
                            newJSONObject, responseListener, responseErrorListener) {

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
                            headers.put("Content-Type", "application/json");
                            headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + auth_token);
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

                String FINAL_URL = Constants.URL + Constants.USERS + "/" + username + "/" + "coordinates";

                try {
                    auth_token = response.getString(Constants.KEY_AUTH_TOKEN);
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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + auth_token);
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
                            final Response.Listener<JSONObject> responseListener, final Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "requestRide:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.URL + Constants.RIDERS + "/" + username + "/" + "request";

                try {
                    auth_token = response.getString(Constants.KEY_AUTH_TOKEN);
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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + auth_token);
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
                           final String requestId) {
        Log.d(TAG, "cancelRide:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.URL + Constants.REQUESTS + "/" + requestId;

                try {
                    auth_token = response.getString(Constants.KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "creating JsonObjectRequest");
                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.DELETE,
                        FINAL_URL,
                        null, defaultResponseListener, defaultResponseErrorListener) {

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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + auth_token);
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

                String FINAL_URL = Constants.URL + Constants.USERS + "/" + username + "/" + "push-token";

                try {
                    auth_token = response.getString(Constants.KEY_AUTH_TOKEN);
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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + auth_token);
                        Log.d(TAG, "HEADER: " + headers.toString());
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void startTrip(final String username,
                          final String password,
                          final String requestId,
                          final Response.Listener<JSONObject> responseListener, final Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "startTrip:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.URL + Constants.DRIVERS + "/" + username + "/" + "trip";

                try {
                    auth_token = response.getString(Constants.KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HashMap<String, String> params = new HashMap<>();
                params.put("request_id", requestId);
                Log.d(TAG, "BODY: " + params.toString());
                Log.d(TAG, "URL: " + FINAL_URL);


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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + auth_token);
                        Log.d(TAG, "HEADER: " + headers.toString());
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void finishTrip(final String username,
                           final String password,
                           final Response.Listener<JSONObject> responseListener, final Response.ErrorListener responseErrorListener) {
        Log.d(TAG, "finishTrip:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.URL + Constants.DRIVERS + "/" + username + "/" + "trip";
                try {
                    auth_token = response.getString(Constants.KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "creating JsonObjectRequest");
                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.DELETE,
                        FINAL_URL,
                        null, responseListener, responseErrorListener) {

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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + auth_token);
                        return headers;
                    }
                };

                Log.d(TAG, "Adding req to mRequestQueue: " + req.toString());
                addToRequestQueue(req);

            }
        }, defaultResponseErrorListener);
    }

    public void getHistory(final String username,
                           final String password,
                           final Response.Listener<JSONObject> responseListener) {
        Log.d(TAG, "startTrip:" + username);

        getValidToken(username, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, "Validation Successfull. Response: " + response.toString());

                String FINAL_URL = Constants.URL + Constants.USERS + "/" + username + "/" + "trip";

                try {
                    auth_token = response.getString(Constants.KEY_AUTH_TOKEN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "creating JsonObjectRequest");
                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.GET,
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
                        headers.put(Constants.KEY_AUTHORIZATION, "Bearer " + auth_token);
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