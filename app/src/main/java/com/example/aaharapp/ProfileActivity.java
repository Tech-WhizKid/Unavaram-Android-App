package com.example.aaharapp;

import static android.content.ContentValues.TAG;

import static com.google.common.io.Files.getFileExtension;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;

public class ProfileActivity extends AppCompatActivity  {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookref = db.collection("users");
    FirebaseFirestore fStore;
    TextView mName,mPhone,mEmail,mDob,Log;
    FirebaseAuth fAuth;

    ShapedImageView mProfile;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri filePath;
    private StorageReference storageReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fAuth = FirebaseAuth.getInstance();
        mName = findViewById(R.id.TV_display_name);
        mEmail = findViewById(R.id.TV_email);
        mPhone = findViewById(R.id.TV_number);
        mDob = findViewById(R.id.TV_country);
        Log = findViewById(R.id.TV_Log);
        mProfile = findViewById(R.id.Profile_image);

        getSupportActionBar().setTitle("");
        getSupportActionBar().show();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24);
        // Initialize Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch file chooser
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
            }
        });


        Log.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProfileActivity.this, landingpage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        loadData();
        loadImage();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_p);
        bottomNavigationView.setSelectedItemId(R.id.menu_profile);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId())
                {
                    case R.id.menu_profile:
                        return true;
                    case R.id.menu_home:
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);
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
    }

    private ProgressDialog progressDialog;
    // Handle result of file chooser
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get selected image
            filePath = data.getData();

            // Start UCrop activity
            UCrop.Options options = new UCrop.Options();
            options.setCompressionFormat(Bitmap.CompressFormat.PNG);
            options.setCompressionQuality(90);
            UCrop.of(filePath, Uri.fromFile(new File(getCacheDir(), "cropped_image")))
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(1024, 1024)
                    .withOptions(options)
                    .start(ProfileActivity.this);
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            // Get cropped image
            Uri croppedUri = UCrop.getOutput(data);

            // Show progress bar dialog
            progressDialog = new ProgressDialog(ProfileActivity.this);
            progressDialog.setTitle("Uploading Image");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.show();

            // Upload image to Firebase Storage
            StorageReference ref = storageReference.child("images/" + fAuth.getCurrentUser().getUid());
            ref.putFile(croppedUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();

                            // Retrieve image from Firebase Storage and display in ImageView
                            StorageReference imageRef = storageReference.child("images/" + taskSnapshot.getMetadata().getName());
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.get().load(uri).into(mProfile);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setProgress((int) progress);
                        }
                    });
        }
    }

    public void loadData(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(fAuth.getCurrentUser().getUid());

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Retrieve user data from Firestore document
                    String name = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");
                    String phoneNumber = documentSnapshot.getString("phone");
                    String dateOfBirth = documentSnapshot.getString("DOB");

                    mName.setText(name);
                    mEmail.setText(email);
                    mPhone.setText(phoneNumber);
                    mDob.setText(dateOfBirth);
                }
            }
        });

    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case UCrop.REQUEST_CROP:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, re-start UCrop activity
                    UCrop.of(filePath, Uri.fromFile(new File(getCacheDir(), "cropped_image")))
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(1024, 1024)
                            .start(ProfileActivity.this);
                } else {
                    Toast.makeText(ProfileActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void loadImage(){
        StorageReference imageRef = storageReference.child("images/" + fAuth.getCurrentUser().getUid());
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(mProfile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this,"Unable to load image",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}