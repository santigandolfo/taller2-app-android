package com.fiuber.fiuber.passenger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.fiuber.fiuber.Constants;
import com.fiuber.fiuber.R;

public class PassengerProfileActivity extends AppCompatActivity {

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
                startActivity(new Intent(getApplicationContext(), PassengerProfileModificationActivity.class));
            }
        });

        TextView mNameField = findViewById(R.id.text_firstname);
        TextView mSurnameField = findViewById(R.id.text_lastname);
        TextView mUsernameField = findViewById(R.id.text_username);
        TextView mEmailField = findViewById(R.id.text_email);


        mNameField.setText(mPreferences.getString(Constants.KEY_FIRSTNAME, ""));
        mSurnameField.setText(mPreferences.getString(Constants.KEY_LASTNAME, ""));
        mUsernameField.setText(mPreferences.getString(Constants.KEY_USERNAME, ""));
        mEmailField.setText(mPreferences.getString(Constants.KEY_EMAIL, ""));

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
