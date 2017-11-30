package com.fiuber.fiuber.driver;

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

public class DriverHistoryAdapter extends ArrayAdapter<DriverHistoryElement> {

    private static final String TAG = "DriverHistoryAdapter";

    public DriverHistoryAdapter(Context context, ArrayList<DriverHistoryElement> users) {
        super(context, 0, users);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "getView");
        // Get the data item for this position
        DriverHistoryElement element = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_driver_history_element, parent, false);
        }
        // Lookup view for data population
        TextView tv_cost = (TextView) convertView.findViewById(R.id.tv_cost);
        TextView tv_date = (TextView) convertView.findViewById(R.id.tv_date);
        TextView tv_passenger = (TextView) convertView.findViewById(R.id.tv_passenger);
        TextView tv_start_location = (TextView) convertView.findViewById(R.id.tv_start_location);
        TextView tv_end_location = (TextView) convertView.findViewById(R.id.tv_end_location);

        // Populate the data into the template view using the data object
        tv_cost.setText("$"+element.cost);
        tv_date.setText(element.date);
        tv_passenger.setText(element.passenger);
        tv_start_location.setText(element.startLocation);
        tv_end_location.setText(element.endLocation);

        // Return the completed view to render on screen
        return convertView;
    }
}