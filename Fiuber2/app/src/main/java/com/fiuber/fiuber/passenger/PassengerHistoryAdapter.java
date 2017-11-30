package com.fiuber.fiuber.passenger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.fiuber.fiuber.R;

import java.util.ArrayList;

public class PassengerHistoryAdapter extends ArrayAdapter<PassengerHistoryElement> {

    private static final String TAG = "PassengerHistoryAdapter";

    public PassengerHistoryAdapter(Context context, ArrayList<PassengerHistoryElement> users) {
        super(context, 0, users);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "getView");
        // Get the data item for this position
        PassengerHistoryElement element = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_passenger_history_element, parent, false);
        }
        // Lookup view for data population
        TextView tv_cost = (TextView) convertView.findViewById(R.id.tv_cost);
        TextView tv_date = (TextView) convertView.findViewById(R.id.tv_date);
        TextView tv_driver = (TextView) convertView.findViewById(R.id.tv_driver);
        TextView tv_start_location = (TextView) convertView.findViewById(R.id.tv_start_location);
        TextView tv_end_location = (TextView) convertView.findViewById(R.id.tv_end_location);
        tv_cost.setText("$"+element.cost);
        tv_date.setText(element.date);
        tv_driver.setText(element.driver);
        tv_start_location.setText(element.startLocation);
        tv_end_location.setText(element.endLocation);
        return convertView;
    }
}