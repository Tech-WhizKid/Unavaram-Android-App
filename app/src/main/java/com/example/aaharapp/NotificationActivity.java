package com.example.aaharapp;

import androidx.appcompat.app.AppCompatActivity;
//import android.content.Intent;
import android.app.Notification;
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


    }
    @Override
    protected void onResume() {
        super.onResume();

        // Check if the activity was launched from the notification
        if (getIntent().getExtras() != null) {

        }
    }
}
