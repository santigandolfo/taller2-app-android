package com.fiuber.fiuber.driver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.fiuber.fiuber.R;
import com.fiuber.fiuber.passenger.PassengerMapsActivity;

public class DriverHistoryActivity extends AppCompatActivity {

    private static final String TAG = "DriverHistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_history);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                //onBackPressed();
                Intent intent = new Intent(this, PassengerMapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);//Call the back button's method
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
