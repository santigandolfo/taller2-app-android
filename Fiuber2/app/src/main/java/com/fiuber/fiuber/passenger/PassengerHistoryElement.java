package com.fiuber.fiuber.passenger;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PassengerHistoryElement {

    private static final String TAG = "DriverHistoryElement";

    public String cost;
    public String date;
    public String driver;
    public String startLocation;
    public String endLocation;

    public PassengerHistoryElement(JSONObject object) {
        Log.i(TAG, "PassengerHistoryElement");

        try {
            this.cost = object.getString("cost");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm");
            try {
                Date d = sdf.parse(object.getString("createdAt"));
                this.date = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }


            this.driver = String.valueOf(object.getInt("driver_id"));
            this.startLocation = object.getJSONArray("start_location").toString();
            this.endLocation = object.getJSONArray("end_location").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<PassengerHistoryElement> fromJson(JSONArray jsonObjects) {
        Log.i(TAG, "fromJson"+jsonObjects.toString());

        ArrayList<PassengerHistoryElement> elements = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                elements.add(new PassengerHistoryElement(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return elements;
    }

/*    private Date createDate(string dateString){
        String startDateString = "06/27/2007";

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.get
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        "2017-11-30T13:28:57.662Z""
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate;
        try {
            startDate = df.parse(startDateString);
            String newDateString = df.format(startDate);
            System.out.println(newDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }*/
}

