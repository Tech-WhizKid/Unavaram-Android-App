package com.example.aaharapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import com.example.aaharapp.MyFirebaseInstanceIDService;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    CardView donate, receive, logout, foodmap, about, contact, mypin, history;
    FirebaseAuth fAuth;

    public PopupWindow popupWindow;

    public View popupView;

    private FirebaseMessaging firebaseMessaging;

    private static final String TAG = "MainActivity";

    private FirebaseFirestore firestore;

    private static final String CHANNEL_ID = "my_app_notifications";
    private static final String CHANNEL_NAME = "Notifications";

    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_CODE = 1;

    private static final int PERMISSION_REQUEST_POST_NOTIFICATIONS = 1001;


    private static final String FOOD_DONATION_APP = "<html>" +
            "<head>" +
            "</head>" +
            "<body>" +
            "<h1>Welcome to our Food Donation App</h1>" +
            "<p>Our app allows users to donate excess food to those in need. Simply create an account, enter details about the food you wish to donate, and our app will connect you with local organizations and charities who can distribute the food to those who need it most.</p>" +
            "<h2>Features</h2>" +
            "<ul>" +
            "<li>Easy sign-up process</li>" +
            "<li>User-friendly interface</li>" +
            "<li>Search for local charities and organizations to donate to</li>" +
            "<li>Ability to enter details about food being donated</li>" +
            "<li>Connect with other users to coordinate food donation efforts</li>" +
            "<li>Option to receive updates about food donation efforts in your area</li>" +
            "</ul>" +
            "<h2>How it Works</h2>" +
            "<ol>" +
            "<li>Create an account on our app</li>" +
            "<li>Enter details about the food you wish to donate</li>" +
            "<li>Browse local charities and organizations to donate to, or connect with other users to coordinate efforts</li>" +
            "<li>Once the donation has been made, receive updates about the distribution of the food and the impact it has had on those in need</li>" +
            "</ol>" +
            "<h2>Get Involved</h2>" +
            "<p>If you are interested in helping to distribute food to those in need, or would like to learn more about our app, please contact us at info@fooddonationapp.com.</p>" +
            "</body>" +
            "</html>";

    @Override
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        donate = findViewById(R.id.cardDonate);
        receive = findViewById(R.id.cardReceive);
        foodmap = findViewById(R.id.cardFoodmap);
        mypin = findViewById(R.id.cardMyPin);

        requestNotificationPermission();

        // Initialize Firebase services
        firebaseMessaging = FirebaseMessaging.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        // Get the registration token
        firebaseMessaging.getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d(TAG, "Registration token: " + token);

                        // Store the registration token in Firestore
                        Map<String, Object> data = new HashMap<>();
                        data.put("token", token);
                        firestore.collection("users").document(fAuth.getCurrentUser().getUid()).update(data)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Registration token saved to Firestore"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error saving registration token to Firestore", e));
                    } else {
                        Log.e(TAG, "Error getting registration token", task.getException());
                    }
                });


        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_window_layout, null);
        TextView messageTextView = popupView.findViewById(R.id.message_textview);
        messageTextView.setText(Html.fromHtml(FOOD_DONATION_APP, Html.FROM_HTML_MODE_COMPACT));
        messageTextView.setMovementMethod(LinkMovementMethod.getInstance());

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

// Set up the close button click listener
        Button closeButton = popupView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId())
                {
                    case R.id.menu_profile:
                        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.menu_home:
                        return true;
                    case R.id.menu_notifications:
                        startActivity(new Intent(getApplicationContext(),DataActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.fooddata:
                        startActivity(new Intent(getApplicationContext(),UserdataActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        getSupportActionBar().setTitle("Unavaram");
        getSupportActionBar().show();


        fAuth= FirebaseAuth.getInstance();
        if(fAuth.getCurrentUser() ==null){
            Intent intent = new Intent(this, landingpage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            this.finish();
        }

        donate.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Donate.class);
                startActivity(intent);
            }
        });
        receive.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Receive.class);
                startActivity(intent);
            }
        });
        foodmap.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FoodMap.class);
                startActivity(intent);
            }
        });
        mypin.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyPin.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_one:
                if (popupView != null) { // add null check
                    popupWindow.showAtLocation(popupView.getRootView(), Gravity.CENTER, 0, 0);
                }
                return true;
            case R.id.action_two:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Unavaram");
                String shareMessage = "Let me recommend you this application\n";
                shareMessage += "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
                return true;
            case R.id.action_three:
                Intent in = new Intent(getApplicationContext(), About.class);
                startActivity(in);
                return true;
            case R.id.action_fore:
                Intent i = new Intent(getApplicationContext(),Contact.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_POST_NOTIFICATIONS);
            }
        }
    }
}