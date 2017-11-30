package com.fiuber.fiuber.driver;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.android.volley.Response;
import com.fiuber.fiuber.Constants;
import com.fiuber.fiuber.R;
import com.fiuber.fiuber.server.ServerHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DriverHistoryActivity extends AppCompatActivity {

    private static final String TAG = "DriverHistoryActivity";
    private ServerHandler mServerHandler;
    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_history);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mServerHandler = new ServerHandler(this.getApplicationContext());
        mPreferences = getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE);

        mServerHandler.getHistory(mPreferences.getString(Constants.KEY_USERNAME, ""),
                mPreferences.getString(Constants.KEY_PASSWORD, ""),
                getHistoryResponseListener);


    }

    Response.Listener<JSONObject> getHistoryResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, "getHistory Successful. Response: " + response.toString());
            // Construct the data source
            ArrayList<DriverHistoryElement> arrayOfUsers = new ArrayList<DriverHistoryElement>();
            // Create the adapter to convert the array to views
            DriverHistoryAdapter adapter = new DriverHistoryAdapter(getApplicationContext(), arrayOfUsers);
            // Attach the adapter to a ListView
            ListView listView = (ListView) findViewById(R.id.lvItems);
            listView.setAdapter(adapter);

            JSONArray jsonArray = null;
            try {
                jsonArray = response.getJSONArray("trips");
                ArrayList<DriverHistoryElement> newUsers = DriverHistoryElement.fromJson(jsonArray);
                adapter.addAll(newUsers);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();    //Call the back button's method
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
