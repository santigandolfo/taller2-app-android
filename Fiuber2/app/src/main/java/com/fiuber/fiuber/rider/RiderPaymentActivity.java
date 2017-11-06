package com.fiuber.fiuber.rider;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.fiuber.fiuber.R;

public class RiderPaymentActivity extends AppCompatActivity {

    private static final String TAG = "RiderPaymentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_payment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.e(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.home:
                onBackPressed();    //Call the back button's method
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
