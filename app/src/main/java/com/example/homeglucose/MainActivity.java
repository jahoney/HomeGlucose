package com.example.homeglucose;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.homeglucose.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    FirebaseAuth mAuth;

    EditText editTextEmail, editTextPassword;

    Button buttonLogin, buttonRegister;
    TextView textViewLoadingMessage, textViewDisclaimer;
    Group groupLoginInfo;

    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        initializeViewAndControls();
        isServicesOK();

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });




    }

    private void initializeViewAndControls() {
        editTextEmail = (EditText) findViewById(R.id.editText_email);
        editTextPassword = (EditText) findViewById(R.id.editText_password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_loading);
        buttonLogin = findViewById(R.id.button_login);
        buttonRegister = findViewById(R.id.button_register);
        textViewLoadingMessage = findViewById(R.id.textView_loadingMessage);
        textViewDisclaimer = findViewById(R.id.textView_disclaimer);
        mAuth = FirebaseAuth.getInstance();
        groupLoginInfo = findViewById(R.id.group_loginInfo);


    }

    private void userLogin() {
        editTextEmail.setError(null);
        editTextPassword.setError(null);

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmail.setError("Email Required");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Email not Valid");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Password Required");
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        textViewLoadingMessage.setVisibility(View.VISIBLE);
        groupLoginInfo.setVisibility(View.GONE);


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            finish();
                            progressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(MainActivity.this, UserLandingPage.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            //user did not login properly
                            progressBar.setVisibility(View.GONE);
                            textViewLoadingMessage.setVisibility(View.GONE);
                            groupLoginInfo.setVisibility(View.VISIBLE);

                            editTextEmail.setError("Username and/or password are invalid. Try again.");
                            editTextPassword.setError("");
                            editTextEmail.requestFocus();

                        }
                    }
                });
    }




    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(this, UserLandingPage.class));
        }
    }

    public boolean isServicesOK() {
        Log.d(TAG,"checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            //all good
            Log.d(TAG, "Google Play Services are working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //error occurred but can fix
            Log.d(TAG, "fixable error occurred");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


}
