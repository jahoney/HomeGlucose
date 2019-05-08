package com.example.homeglucose;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class CreatePersonalProfile extends AppCompatActivity {

    ImageView imageViewProfilePic;
    TextView textViewCity;
    Bundle bundle;
    private static final int PIC_IMAGE_REQUEST = 1;
    private Uri profilePicUri;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userID;
    private DocumentReference userRef;
    private StorageReference imageStorageRef;
    private StorageTask uploadTask;
    Bitmap bm;
    private String location;
    private String zip;
    ProgressBar progressBar;
    private static final String TAG = "MainActivity";

    private AdView mAdView;
    private final String apiKey = "AIzaSyA_niZBRycoxHpADqSpV-zVbPYQbDwmRX0";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_personal_profile);
        initializeVarsAndViews();
        imageViewProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progressBar.setVisibility(View.VISIBLE);
                if (uploadTask != null && uploadTask.isInProgress()){
                    Toast.makeText(CreatePersonalProfile.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else openFileChooser();
                //progressBar.setVisibility(View.GONE);
            }
        });

    }

    private void initializeVarsAndViews() {
        imageViewProfilePic = findViewById(R.id.imageView_profilePicture);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.profile_blank_picture);
        imageViewProfilePic.setImageBitmap(bm);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        userRef = db.collection("users").document(userID);
        imageStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        progressBar = findViewById(R.id.progressBar_loading);
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        //textViewCity = findViewById(R.id.textView_city);
        bundle = getIntent().getExtras();
        zip = bundle.getString("ZIP").toLowerCase();
        textViewCity.setText(zip);


        // Initialize Places.
        Places.initialize(getApplicationContext(), apiKey);

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);
        //retrieve user info
        /*
        userRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                zip = document.getString("ZIP");
                                textViewCity.setText(zip);
                            } else {
                                Toast.makeText(CreatePersonalProfile.this, "User does not have a zip code", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(CreatePersonalProfile.this, task.getException().toString().trim(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

*/

    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PIC_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PIC_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profilePicUri = data.getData();
            Picasso.with(this).load(profilePicUri).into(imageViewProfilePic);
            uploadFile();
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (profilePicUri != null) {
            final StorageReference fileRef = imageStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(profilePicUri));
            uploadTask = fileRef.putFile(profilePicUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(0);
                                }
                            },550);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreatePersonalProfile.this, "Upload Complete", Toast.LENGTH_SHORT).show();
                            //Upload upload = new Upload(fileRef.toString().trim(), taskSnapshot.getUploadSessionUri().toString().trim());
                            Map<String, Object> user = new HashMap<>();
                            user.put("profilePicUri", fileRef.toString());
                            userRef.update(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(CreatePersonalProfile.this, "Image URI Set", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CreatePersonalProfile.this, "Failed to save URI", Toast.LENGTH_SHORT).show();
                                        }
                                    });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreatePersonalProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

}
