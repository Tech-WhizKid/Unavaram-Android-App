package com.example.aaharapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DataActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<model> datalist;
    FirebaseFirestore db;
    Adapter adapter;
    FirebaseAuth fAuth= FirebaseAuth.getInstance();
    public String userID = fAuth.getCurrentUser().getUid();

    private int prevScrollY = 0;
    private BottomNavigationView bottomNavigationView;
    private static final int SCROLL_THRESHOLD = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        recyclerView=(RecyclerView)findViewById(R.id.urec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        datalist=new ArrayList<>();
        adapter=new Adapter(datalist);
        recyclerView.setAdapter(adapter);

        getSupportActionBar().setTitle("History");
        getSupportActionBar().show();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24);

        bottomNavigationView = findViewById(R.id.bottom_navigation_du);
        bottomNavigationView.setSelectedItemId(R.id.menu_notifications);

        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY + SCROLL_THRESHOLD) {
                    // Scrolling down
                    hideBottomNavigationView();
                } else if (scrollY < oldScrollY - SCROLL_THRESHOLD) {
                    // Scrolling up
                    showBottomNavigationView();
                }
                prevScrollY = scrollY;
            }
        });

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
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);
                    case R.id.menu_notifications:
                        return true;
                    case R.id.fooddata:
                        startActivity(new Intent(getApplicationContext(),UserdataActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });


        db= FirebaseFirestore.getInstance();
        db.collection("Backup").orderBy("timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list=queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot d:list)
                        {

                            model obj=d.toObject(model.class);
                            assert obj != null;
                            obj.setId(d.getId());
                            //datalist.add(obj);
                            String Userid = (String) d.get("userid");
                            if(Userid.equals(userID)) {
                                datalist.add(obj);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void hideBottomNavigationView() {
        bottomNavigationView.animate().translationY(bottomNavigationView.getHeight()).setDuration(300);
    }

    private void showBottomNavigationView() {
        bottomNavigationView.animate().translationY(0).setDuration(300);
    }

}