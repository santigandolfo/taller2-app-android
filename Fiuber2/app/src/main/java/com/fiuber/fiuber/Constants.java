package com.fiuber.fiuber;

public class Constants {


    //misc
    static final String KEY_INFO = "info";

    //urls
    //payment urls
    public static final String GENERATE_PAYMENT_TOKEN_URL = "http://shielded-escarpment-27661.herokuapp.com/api/v1/user/oauth/authorize";
    public static final String PAYMENTS_URL = "http://shielded-escarpment-27661.herokuapp.com/api/v1/payments";
    //app server urls
    public static final String URL = "https://taller2-fiuber-app-server.herokuapp.com";
    public static final String SECURITY = "/security";
    public static final String USERS = "/users";
    public static final String DRIVERS = "/drivers";
    public static final String RIDERS = "/riders";

    //tokens
    public static final String KEY_AUTH_TOKEN = "auth_token";
    public static final String KEY_FIREBASE_TOKEN = "push_token";
    public static final String KEY_PAYMENT_TOKEN = "access_token";

    //position info
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LATITUDE_INITIAL = "latitude_initial";
    public static final String KEY_LONGITUDE_INITIAL = "longitude_initial";
    public static final String KEY_LATITUDE_FINAL = "latitude_final";
    public static final String KEY_LONGITUDE_FINAL = "longitude_final";

    //payment info
    public static final String KEY_EXPIRATION_MONTH = "expiration_month";
    public static final String KEY_EXPIRATION_YEAR = "expiration_year";
    public static final String KEY_METHOD = "method";
    public static final String KEY_NUMBER = "number";
    public static final String KEY_CCVV = "ccvv";
    public static final String KEY_PAYMENT_TYPE = "type";
    public static final String KEY_VALUE = "value";

    public static final String KEY_AUTHORIZATION = "Authorization";

    //geofence data
    public static final float GEOFENCE_RADIUS_IN_METERS = 200;
    public static final String GEOFENCE_ID = "ID";

    //other user info
    public static final String KEY_OTHERS_FIRSTNAME = "others_firstname";
    public static final String KEY_OTHERS_LASTNAME = "others_lastname";
    public static final String KEY_OTHERS_EMAIL = "others_email";
    public static final String KEY_OTHERS_USERNAME = "others_username";

    //other user car
    public static final String KEY_OTHERS_CAR_MODEL = "others_car_mode";
    public static final String KEY_OTHERS_CAR_COLOR = "others_car_color";
    public static final String KEY_OTHERS_CAR_BRAND = "others_car_brand";
    public static final String KEY_OTHERS_CAR_YEAR = "others_car_year";

    //user info
    public static final String KEY_TYPE = "type";
    public static final String KEY_FIRSTNAME = "firstname";
    public static final String KEY_LASTNAME = "lastname";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    //driver info
    public static final String KEY_AVAILABILITY = "availability";
    //state in app
    public static final String KEY_STATE = "state";

    //user car
    public static final String KEY_CAR_MODEL = "model";
    public static final String KEY_CAR_COLOR = "color";
    public static final String KEY_CAR_BRAND = "brand";
    public static final String KEY_CAR_YEAR = "year";

    //state
    public static final String KEY_LOGIN = "login";

    //shared preferences name
    public static final String KEY_MY_PREFERENCES = "MyPreferences";

    //ride info
    public static final String KEY_RIDE_ID = "ride_id";
}