package com.example.homeglucose;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    EditText editTextEmail, editTextPassword, editTextConfirmPassword, editTextDateOfBirth, editTextZIP, editTextName;
    Button buttonSubmit, buttonGoToLogin;
    ProgressBar progressBar;
    TextView textViewLoadingMessage, textViewInstructions;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    String birthDate;
    CheckBox checkBoxTermsAgreement;
    CheckBox checkBoxTruthful;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initializeVarsAndViews();

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        buttonGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


    }

    private void initializeVarsAndViews() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        editTextEmail = findViewById(R.id.editText_email);
        editTextPassword = findViewById(R.id.editText_password);
        editTextConfirmPassword = findViewById(R.id.editText_confirmPassword);
        editTextName = findViewById(R.id.editText_name);
        editTextDateOfBirth = findViewById(R.id.editText_dateOfBirth);
        editTextZIP = findViewById(R.id.editText_ZIP);
        checkBoxTermsAgreement = findViewById(R.id.checkBox_agreeToTerms);
        checkBoxTruthful = findViewById(R.id.checkBox_answeredTruthfully);
        scrollView = findViewById(R.id.scrollView3);

        buttonSubmit = findViewById(R.id.button_submit);
        buttonGoToLogin = findViewById(R.id.button_backToLogin);

        progressBar = findViewById(R.id.progressBar_loading);

        textViewLoadingMessage = findViewById(R.id.textView_loadingMessage);
        textViewInstructions = findViewById(R.id.textView_instructions);

        mAuth = FirebaseAuth.getInstance();

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void registerUser() {

        final String name = editTextName.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        final String zip = editTextZIP.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String passwordConfirm = editTextConfirmPassword.getText().toString().trim();
        final boolean agreeToTerms = checkBoxTermsAgreement.isChecked();
        final boolean truthful = checkBoxTruthful.isChecked();

        if (!agreeToTerms) {
            checkBoxTermsAgreement.setError("You must agree to the Terms.");
            checkBoxTermsAgreement.requestFocus();
            return;
        }

        if (!truthful) {
            checkBoxTruthful.setError("Why would you lie to me?");
            checkBoxTruthful.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            editTextName.setError("Name Required");
            editTextName.requestFocus();
            return;
        }
        if(!name.matches("^[\\p{L} .'-]+$")) {
            editTextName.setError("Name is only letters");
            editTextName.requestFocus();
        }
        //check date of birth logic
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate dateOfBirth;

        try {
            dateOfBirth = LocalDate.parse(editTextDateOfBirth.getText().toString().trim(),dtf);
            birthDate = dtf.format(dateOfBirth);
        } catch (DateTimeParseException e) {
            editTextDateOfBirth.setError("Date Of Birth must be a valid date in the format MM/DD/YYYY");
            editTextDateOfBirth.requestFocus();
            return;
        }

        //born in the future?

        if (dateOfBirth.isAfter(LocalDate.now())) {
            editTextDateOfBirth.setError("Are you a time traveler? Mortals cannot be born in the future.");
            editTextDateOfBirth.requestFocus();
            return;
        }

        //check the age
        if (Period.between(dateOfBirth, LocalDate.now()).getYears() < 18) {
            editTextDateOfBirth.setError("Must be 18 to use.");
            editTextDateOfBirth.requestFocus();
            return;
        }

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
        if (zip.isEmpty()) {
            editTextZIP.setError("ZIP Code Required");
            editTextZIP.requestFocus();
            return;
        }
        if (zip.length() < 5 || zip.length() >6) {
            editTextZIP.setError("ZIP must be 5 digits");
            editTextZIP.requestFocus();
            return;
        }

        if (!zip.matches("[0-9]+")) {
            editTextZIP.setError("ZIP must be a number.");
            editTextZIP.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password Required");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 8) {
            editTextPassword.setError("Password must be at least 8 characters long");
            editTextPassword.requestFocus();
            return;
        }

        if (!password.matches(passwordConfirm)) {
            editTextPassword.setError("Passwords must match");
            editTextPassword.setText("");
            editTextConfirmPassword.setText("");
            editTextPassword.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        textViewLoadingMessage.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        checkBoxTruthful.setVisibility(View.GONE);
        checkBoxTermsAgreement.setVisibility(View.GONE);
        buttonSubmit.setVisibility(View.GONE);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                textViewLoadingMessage.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    String userId = mAuth.getUid();
                    db = FirebaseFirestore.getInstance();
                    Map<String, Object> user = new HashMap<>();
                    user.put("emailAddress", email);
                    user.put("userId", userId);
                    user.put("createDateTime", LocalDate.now().toString());
                    user.put("dateOfBirth", birthDate);
                    user.put("name", name);
                    user.put("ZIP", zip);
                    db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                public void onSuccess(Void avoid) {
                                    progressBar.setVisibility(View.GONE);
                                    textViewLoadingMessage.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "User Registered Successfully", Toast.LENGTH_SHORT).show();
                                    FirebaseUser user = mAuth.getInstance().getCurrentUser();
                                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(SignUp.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    Intent intent = new Intent(SignUp.this, UserLandingPage.class);
                                    intent.putExtra("ZIP", zip);
                                    finish();
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    //Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(SignUp.this, "ERROR", Toast.LENGTH_SHORT).show();
                                    //Log.w(TAG, "Error adding document", e);
                                }
                            });


                } else {
                    //Toast.makeText(getApplicationContext(), "Something Bad Happened", Toast.LENGTH_SHORT).show();
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "Email already in use", Toast.LENGTH_SHORT).show();
                        editTextEmail.setText("");
                        editTextEmail.setError("Email already taken");
                        editTextPassword.setText("");
                        editTextConfirmPassword.setText("");
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });



    }

}
