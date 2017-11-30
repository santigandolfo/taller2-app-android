package com.fiuber.fiuber;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fiuber.fiuber.HistoryElement;
import com.fiuber.fiuber.R;

import java.util.ArrayList;

/**
 * Created by santiago on 30/11/17.
 */

public class HistoryAdapter extends ArrayAdapter<HistoryElement> {
    public HistoryAdapter(Context context, ArrayList<HistoryElement> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        HistoryElement element = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_history_element, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvHome = (TextView) convertView.findViewById(R.id.tvHome);
        // Populate the data into the template view using the data object
        tvName.setText(element.name);
        tvHome.setText(element.hometown);
        // Return the completed view to render on screen
        return convertView;
    }
}
