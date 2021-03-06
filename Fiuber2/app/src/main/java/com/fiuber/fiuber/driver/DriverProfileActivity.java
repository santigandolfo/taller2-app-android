package com.fiuber.fiuber.driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.fiuber.fiuber.Constants;
import com.fiuber.fiuber.R;

public class DriverProfileActivity extends AppCompatActivity {

    private static final String TAG = "PassengerProfileAct";

    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_profile);

        mPreferences = getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(mPreferences.getString(Constants.KEY_FIRSTNAME, "") + " " + mPreferences.getString(Constants.KEY_LASTNAME, ""));
        toolbar.setSubtitle("");
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "change activity to PassengerMapsActivity");
                startActivity(new Intent(getApplicationContext(), DriverProfileModificationActivity.class));
            }
        });

        TextView mNameField = findViewById(R.id.text_firstname);
        TextView mSurnameField = findViewById(R.id.text_lastname);
        TextView mUsernameField = findViewById(R.id.text_username);
        TextView mEmailField = findViewById(R.id.text_email);

        TextView mCarModelField = findViewById(R.id.text_car_model);
        TextView mCarColorField = findViewById(R.id.text_car_color);
        TextView mCarBrandField = findViewById(R.id.text_car_brand);
        TextView mCarYearField = findViewById(R.id.text_car_year);

        mNameField.setText(mPreferences.getString(Constants.KEY_FIRSTNAME, ""));
        mSurnameField.setText(mPreferences.getString(Constants.KEY_LASTNAME, ""));
        mUsernameField.setText(mPreferences.getString(Constants.KEY_USERNAME, ""));
        mEmailField.setText(mPreferences.getString(Constants.KEY_EMAIL, ""));

        findViewById(R.id.text_car).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_car_model).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_car_color).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_car_brand).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_car_year).setVisibility(View.VISIBLE);

        mCarModelField.setText(mPreferences.getString(Constants.KEY_CAR_MODEL, ""));
        mCarColorField.setText(mPreferences.getString(Constants.KEY_CAR_COLOR, ""));
        mCarBrandField.setText(mPreferences.getString(Constants.KEY_CAR_BRAND, ""));
        mCarYearField.setText(mPreferences.getString(Constants.KEY_CAR_YEAR, ""));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
