package com.fiuber.fiuber.passenger;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.fiuber.fiuber.HistoryAdapter;
import com.fiuber.fiuber.R;
import com.fiuber.fiuber.HistoryElement;

import java.util.ArrayList;

public class PassengerHistoryActivity extends AppCompatActivity {

    private static final String TAG = "PassengerHistoryAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_history);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

/*        // Construct the data source
        ArrayList<HistoryElement> arrayOfUsers = new ArrayList<HistoryElement>();
     // Create the adapter to convert the array to views
        HistoryAdapter adapter = new HistoryAdapter(this, arrayOfUsers);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.lvItems);
        listView.setAdapter(adapter);*/

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();    //Call the back button's method
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
