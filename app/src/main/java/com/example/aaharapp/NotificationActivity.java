package com.example.aaharapp;

import androidx.appcompat.app.AppCompatActivity;
//import android.content.Intent;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.messaging.RemoteMessage;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Intent intent = getIntent();

        TextView titleTextView = findViewById(R.id.Title);
        TextView messageTextView = findViewById(R.id.msg);

        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        Log.d(TAG, "Received notification: " + title + " - " + message);

        // Update the UI with the notification data
        titleTextView.setText(title);
        messageTextView.setText(message);


    }

}
