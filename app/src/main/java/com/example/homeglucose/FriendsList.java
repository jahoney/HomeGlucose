package com.example.homeglucose;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FriendsList extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private AdView mAdView;
    private String userID;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        initializeVarsAndViews();

    }

    private void initializeVarsAndViews() {
        //mAdView = findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getInstance().getCurrentUser();
        userID = mAuth.getUid();


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
                startActivity(new Intent(FriendsList.this, UserLandingPage.class));
                return true;

            case R.id.itemMyFriends:
                startActivity(new Intent(FriendsList.this, FriendsList.class));
            case R.id.itemSignOut:
                mAuth.signOut();
                startActivity(new Intent(FriendsList.this, MainActivity.class));
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








}
