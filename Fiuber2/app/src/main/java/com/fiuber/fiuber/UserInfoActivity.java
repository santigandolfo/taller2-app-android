package com.fiuber.fiuber;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class UserInfoActivity extends AppCompatActivity  implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";

    private EditText mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        // Buttons
        findViewById(R.id.buttonSignOut).setOnClickListener(this);

        //Firebase Authenticator
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Log.d(TAG, "change activity to LogInActivity");
            startActivity(new Intent(this, LogInActivity.class));

        }

    }

    private void signOut() {
        Log.d(TAG, "signOut");
        mAuth.signOut();
        LoginManager.getInstance().logOut();
        Log.d(TAG, "change activity to LogInActivity");
        startActivity(new Intent(this, LogInActivity.class));
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.buttonSignOut) {
            signOut();
        }
    }
}