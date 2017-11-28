package com.fiuber.fiuber;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class OtherProfileActivity extends AppCompatActivity {

    private static final String TAG = "PassengerProfileAct";

    SharedPreferences mPreferences;

    String MY_PREFERENCES = "MyPreferences";

    private static final String KEY_TYPE = "type";

    private static final String KEY_OTHERS_FIRSTNAME = "others_firstname";
    private static final String KEY_OTHERS_LASTNAME = "others_lastname";
    private static final String KEY_OTHERS_EMAIL = "others_email";
    private static final String KEY_OTHERS_USERNAME = "others_username";

    private static final String KEY_OTHERS_CAR_MODEL = "others_car_mode";
    private static final String KEY_OTHERS_CAR_COLOR = "others_car_color";
    private static final String KEY_OTHERS_CAR_BRAND = "others_car_brand";
    private static final String KEY_OTHERS_CAR_YEAR = "others_car_year";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_profile);

        mPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        TextView mNameField = findViewById(R.id.text_firstname);
        TextView mSurnameField = findViewById(R.id.text_lastname);
        TextView mUsernameField = findViewById(R.id.text_username);
        TextView mEmailField = findViewById(R.id.text_email);

        TextView mCarModelField = findViewById(R.id.text_car_model);
        TextView mCarColorField = findViewById(R.id.text_car_color);
        TextView mCarBrandField = findViewById(R.id.text_car_brand);
        TextView mCarYearField = findViewById(R.id.text_car_year);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(mPreferences.getString(KEY_OTHERS_FIRSTNAME, "") + " " + mPreferences.getString(KEY_OTHERS_LASTNAME, ""));
        toolbar.setSubtitle("");

        mNameField.setText(mPreferences.getString(KEY_OTHERS_FIRSTNAME, ""));
        mSurnameField.setText(mPreferences.getString(KEY_OTHERS_LASTNAME, ""));
        mEmailField.setText(mPreferences.getString(KEY_OTHERS_EMAIL, ""));
        mUsernameField.setText(mPreferences.getString(KEY_OTHERS_USERNAME, ""));

        if ("passenger".equals(mPreferences.getString(KEY_TYPE, ""))) {
            findViewById(R.id.layout_car_model).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_car_color).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_car_brand).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_car_year).setVisibility(View.VISIBLE);

            mCarModelField.setText(mPreferences.getString(KEY_OTHERS_CAR_MODEL, ""));
            mCarColorField.setText(mPreferences.getString(KEY_OTHERS_CAR_COLOR, ""));
            mCarBrandField.setText(mPreferences.getString(KEY_OTHERS_CAR_BRAND, ""));
            mCarYearField.setText(mPreferences.getString(KEY_OTHERS_CAR_YEAR, ""));
        }

        setSupportActionBar(toolbar);

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
