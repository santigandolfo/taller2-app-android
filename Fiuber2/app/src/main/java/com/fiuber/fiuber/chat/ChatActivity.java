package com.fiuber.fiuber.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.fiuber.fiuber.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class ChatActivity extends AppCompatActivity {
    private ListView listView;
    private String loggedInUserName = "";

    private FirebaseAuth mAuth;

    String FIREBASE_URL = "https://fiuber2-7a583.firebaseio.com/";

    String MY_PREFERENCES = "MyPreferences";

    String FINAL_FIREBASE_URL;

    private static final String KEY_USERNAME = "username";
    private static final String KEY_RIDE_ID = "ride_id";

    SharedPreferences mPreferences;

    String rideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final String TAG = "PassengerMapsActivity";
        mAuth = FirebaseAuth.getInstance();
        mPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        rideId = mPreferences.getString(KEY_RIDE_ID, "");
        FINAL_FIREBASE_URL = FIREBASE_URL + rideId;

        //find views by Ids
        FloatingActionButton fab = findViewById(R.id.fab);
        final EditText input = findViewById(R.id.input);
        listView = findViewById(R.id.list);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInAnonymously:success");
                                showAllOldMessages();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInAnonymously:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            // User is already signed in, show list of messages
            showAllOldMessages();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.getText().toString().trim().equals("")) {
                    Toast.makeText(ChatActivity.this, "Please enter some texts!", Toast.LENGTH_SHORT).show();
                } else {

                    FirebaseDatabase.getInstance()
                            .getReferenceFromUrl(FINAL_FIREBASE_URL)
                            .push()
                            .setValue(new ChatMessage(input.getText().toString(),
                                    mPreferences.getString(KEY_USERNAME, ""),
                                    mAuth.getCurrentUser().getUid())
                            );
                    input.setText("");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sign_out) {
            onBackPressed();
//            startActivity(new Intent(getApplicationContext(), PassengerMapsActivity.class));
        }
        return true;
    }

    private void showAllOldMessages() {
        loggedInUserName = mPreferences.getString(KEY_USERNAME, "");
        Log.d("Chat", "user id: " + loggedInUserName);

        FirebaseListAdapter<ChatMessage> adapter = new MessageAdapter(this, ChatMessage.class, R.layout.item_in_message,
                FirebaseDatabase.getInstance().getReferenceFromUrl(FINAL_FIREBASE_URL));
        listView.setAdapter(adapter);
    }

    public String getLoggedInUserName() {
        return loggedInUserName;
    }
}
