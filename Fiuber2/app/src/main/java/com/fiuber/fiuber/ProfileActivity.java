package com.fiuber.fiuber;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    SharedPreferences mPreferences;

    String MY_PREFERENCES = "MyPreferences";

    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("");
        setSupportActionBar(toolbar);

        mPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "change activity to MapsActivity");
                startActivity(new Intent(getApplicationContext(), ProfileModificationActivity.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView mNameField = findViewById(R.id.text_name);
        TextView mSurnameField = findViewById(R.id.text_surname);
        TextView mUsernameField = findViewById(R.id.text_username);
        TextView mEmailField = findViewById(R.id.text_email);


        mNameField.setText(mPreferences.getString(KEY_FIRSTNAME, ""));
        mSurnameField.setText(mPreferences.getString(KEY_LASTNAME, ""));
        mUsernameField.setText(mPreferences.getString(KEY_USERNAME, ""));
        mEmailField.setText(mPreferences.getString(KEY_EMAIL, ""));



    }
}
