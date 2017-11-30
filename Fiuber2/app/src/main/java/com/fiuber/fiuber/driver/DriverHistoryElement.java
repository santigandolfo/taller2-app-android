package com.fiuber.fiuber.driver;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DriverHistoryElement {

    private static final String TAG = "DriverHistoryElement";

    public String cost;
    public String date;
    public String passenger;
    public String startLocation;
    public String endLocation;

    public DriverHistoryElement(JSONObject object) {
        Log.i(TAG, "DriverHistoryElement");
        try {
            this.cost = object.getString("cost");
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm");
                Date d = sdf.parse(object.getString("createdAt"));
                this.date = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            this.passenger = object.getString("passenger_id");
            this.startLocation = object.getJSONArray("start_location").toString();
            this.endLocation = object.getJSONArray("end_location").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<DriverHistoryElement> fromJson(JSONArray jsonObjects) {
        Log.i(TAG, "fromJson: "+jsonObjects.toString());
        ArrayList<DriverHistoryElement> elements = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                elements.add(new DriverHistoryElement(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return elements;
    }
}

