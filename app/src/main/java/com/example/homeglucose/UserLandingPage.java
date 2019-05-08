package com.example.homeglucose;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserLandingPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Bitmap bm;
    private FirebaseFirestore db;
    private StorageReference imageStorageRef;
    private StorageTask uploadTask;
    private static final int PIC_IMAGE_REQUEST = 1;
    private Uri profilePicUri;
    private String picURI;
    Group groupLoading;
    TextView textViewVerified;
    ImageView imageViewProfilePic;
    TextView textViewZIP;
    TextView textViewName;
    TextView textViewLatLong;
    private ProgressBar progressBarUploading;
    DocumentReference userRef;
    private boolean emailVerified;
    private boolean verificationEmailSent;
    String userID;
    Bundle bundle;
    private String zip;
    private String name;
    private static final String TAG = "UserLandingPage";
    private static final String apiKey = "AIzaSyD1olqhCudGtDdlY8T1UotkrmsDCbiLG5c";


    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_landing_page);
        //MobileAds.initialize(this, "ca-app-pub-3054158969967843~3934071774");
        //getSupportActionBar().hide();
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        init();

        if (!emailVerified && !verificationEmailSent) {
            textViewVerified.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //Toast.makeText(UserLandingPage.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                            verificationEmailSent = true;
                            textViewVerified.setOnClickListener(null);
                            textViewVerified.setBackgroundColor(Color.BLUE);
                            textViewVerified.setText("Verification email sent. Check your email.");
                        }
                    });
                }
            });
        }
        imageViewProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadTask != null && uploadTask.isInProgress()){
                    Toast.makeText(UserLandingPage.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else openFileChooser();
                //Toast.makeText(UserLandingPage.this, "Profile Pic Clicked", Toast.LENGTH_SHORT).show();
                //fix this later
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemMyProfile:
                startActivity(new Intent(UserLandingPage.this, UserLandingPage.class));
                return true;

            case R.id.itemMyFriends:
                startActivity(new Intent(UserLandingPage.this, FriendsList.class));
            case R.id.itemSignOut:
                mAuth.signOut();
                startActivity(new Intent(UserLandingPage.this, MainActivity.class));
                return true;
            case R.id.item3:
                Toast.makeText(this, "Item 3 Clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.subitem1:
                Toast.makeText(this, "SubItem 1 Clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.subitem2:
                Toast.makeText(this, "SubItem 2 Clicked", Toast.LENGTH_SHORT).show();
                return true;

            default:

        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUserInformation() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            //get information about user


            if (!user.isEmailVerified()) {
                textViewVerified.setVisibility(View.VISIBLE);
                emailVerified = false;
                verificationEmailSent = false;
            } else {
                emailVerified = true;
                verificationEmailSent = true;
            }

        }

        userRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        groupLoading.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                if (document.getString("ZIP") != null)zip = document.getString("ZIP");
                                if (document.getString("name") != null)name = document.getString("name");
                                if (document.getString("profilePicID") != null) {
                                    //time to get the profile pic
                                    String profilePicId = document.getString("profilePicID").trim();
                                    try {
                                        imageStorageRef.child(profilePicId).getDownloadUrl()
                                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        Picasso.with(UserLandingPage.this).load(uri).into(imageViewProfilePic);

                                                    }
                                                });
                                    } catch (Exception e) {
                                        Toast.makeText(UserLandingPage.this, "OH GOD!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                textViewZIP.setText(zip);
                                textViewName.setText(name);

                                Toast.makeText(UserLandingPage.this, "Sending off for Geocode", Toast.LENGTH_SHORT).show();
                                new GeocodeCity().execute(textViewZIP.getText().toString().replace(" ","+"));
                            } else {
                                Toast.makeText(UserLandingPage.this, "User does not have a zip code", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(UserLandingPage.this, task.getException().toString().trim(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private class GeocodeCity extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textViewLatLong.setText("Loading...");
        }

        @Override
        protected String doInBackground(String... strings) {
            String response;
            try {
                String address = strings[0];
                HttpDataHandler http = new HttpDataHandler();
                String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",address,apiKey);
                response = http.getHttpData(url);
                return response;
            } catch (Exception e){

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                String lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry").getJSONObject("location").get("lat").toString().trim();
                String lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry").getJSONObject("location").get("lng").toString().trim();
                String city = ((JSONArray) jsonObject.get("results")).getJSONObject(0).getJSONArray("address_components").getJSONObject(1).get("long_name").toString().trim();
                city += ", " + ((JSONArray) jsonObject.get("results")).getJSONObject(0).getJSONArray("address_components").getJSONObject(2).get("long_name").toString().trim();



                textViewLatLong.setText(String.format("Coordinates : %s / %s ", lat, lng));
                textViewZIP.setText(city);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void init() {

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
        String adsServer = getResources().getString(R.string.test_banner_ad_id);
        //init ads
        MobileAds.initialize(getApplicationContext(),adsServer);
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        groupLoading = findViewById(R.id.group_loading);
        groupLoading.setVisibility(View.VISIBLE);
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        userID = mAuth.getUid();
        final String filePath = "uploads/" + userID;
        imageStorageRef = FirebaseStorage.getInstance().getReference(filePath);

        imageViewProfilePic = findViewById(R.id.imageView_profilePic);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.profile_blank_picture);
        imageViewProfilePic.setImageBitmap(bm);

        textViewVerified = findViewById(R.id.textView_notVerified);
        textViewName = findViewById(R.id.textView_name);
        textViewZIP = findViewById(R.id.textView_zip);
        textViewLatLong = findViewById(R.id.textView_latLong);
        progressBarUploading=findViewById(R.id.progressBar_uploading);
        //myToolbar = (Toolbar) findViewById(R.id.toolbar);
        //setActionBar(myToolbar);

        bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("USERID")) {
            userID = bundle.get("USERID").toString().trim();
        }
        userRef = db.collection("users").document(userID);
        loadUserInformation();
        groupLoading.setVisibility(View.GONE);
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
            final String picId = System.currentTimeMillis() + "." + getFileExtension(profilePicUri);
            final StorageReference fileRef = imageStorageRef.child(picId);
            uploadTask = fileRef.putFile(profilePicUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Upload upload = new Upload(fileRef.toString().trim(), taskSnapshot.getUploadSessionUri().toString().trim());
                            Map<String, Object> user = new HashMap<>();
                            user.put("profilePicID", picId);
                            userRef.update(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBarUploading.setVisibility(View.GONE);
                                            Toast.makeText(UserLandingPage.this, "Profile Picture Updated", Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(UserLandingPage.this, "Failed to change profile picture", Toast.LENGTH_SHORT).show();
                                        }
                                    });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserLandingPage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }



}

