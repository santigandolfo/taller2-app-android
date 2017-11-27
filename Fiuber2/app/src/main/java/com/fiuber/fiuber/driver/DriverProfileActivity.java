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

import com.fiuber.fiuber.R;
import com.fiuber.fiuber.passenger.PassengerProfileModificationActivity;

public class DriverProfileActivity extends AppCompatActivity {

    private static final String TAG = "PassengerProfileAct";

    SharedPreferences mPreferences;

    String MY_PREFERENCES = "MyPreferences";

    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";

    //car
    private static final String KEY_CAR_MODEL = "model";
    private static final String KEY_CAR_COLOR = "color";
    private static final String KEY_CAR_PLATE = "plate";
    private static final String KEY_CAR_YEAR = "year";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_profile);

        mPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mPreferences.getString(KEY_FIRSTNAME, "") + " " + mPreferences.getString(KEY_LASTNAME, ""));
        toolbar.setSubtitle("");
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "change activity to PassengerMapsActivity");
                startActivity(new Intent(getApplicationContext(), PassengerProfileModificationActivity.class));
            }
        });

        TextView mNameField = findViewById(R.id.text_firstname);
        TextView mSurnameField = findViewById(R.id.text_lastname);
        TextView mUsernameField = findViewById(R.id.text_username);
        TextView mEmailField = findViewById(R.id.text_email);

        TextView mCarModelField = findViewById(R.id.text_car_model);
        TextView mCarColorField = findViewById(R.id.text_car_color);
        TextView mCarPlateField = findViewById(R.id.text_car_plate);
        TextView mCarYearField = findViewById(R.id.text_car_year);

        mNameField.setText(mPreferences.getString(KEY_FIRSTNAME, ""));
        mSurnameField.setText(mPreferences.getString(KEY_LASTNAME, ""));
        mUsernameField.setText(mPreferences.getString(KEY_USERNAME, ""));
        mEmailField.setText(mPreferences.getString(KEY_EMAIL, ""));

        findViewById(R.id.layout_car_model).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_car_color).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_car_plate).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_car_year).setVisibility(View.VISIBLE);

        mCarModelField.setText(mPreferences.getString(KEY_CAR_MODEL, ""));
        mCarColorField.setText(mPreferences.getString(KEY_CAR_COLOR, ""));
        mCarPlateField.setText(mPreferences.getString(KEY_CAR_PLATE, ""));
        mCarYearField.setText(mPreferences.getString(KEY_CAR_YEAR, ""));

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
