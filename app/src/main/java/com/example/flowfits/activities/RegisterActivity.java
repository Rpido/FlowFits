package com.example.flowfits.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowfits.MainActivity;
import com.example.flowfits.R;
import com.example.flowfits.models.User;
import com.example.flowfits.repositories.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout nameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private TextInputEditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private MaterialButton registerButton, loginButton;
    private CircularProgressIndicator progressIndicator;
    
    private FirebaseAuth firebaseAuth;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        initializeFirebase();
        setupClickListeners();
    }

    private void initializeViews() {
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);
        progressIndicator = findViewById(R.id.progressIndicator);
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
    }

    private void setupClickListeners() {
        registerButton.setOnClickListener(v -> registerUser());
        loginButton.setOnClickListener(v -> goToLogin());
    }

    private void registerUser() {
        if (!validateInputs()) {
            return;
        }

        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        setLoadingState(true);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration successful
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Update user profile with display name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            // Create user profile directly without email verification
                                            createUserProfile(firebaseUser, name);
                                        } else {
                                            setLoadingState(false);
                                            Toast.makeText(RegisterActivity.this, 
                                                    "Failed to update profile", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        // Registration failed
                        setLoadingState(false);
                        String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Registration failed";
                        Toast.makeText(RegisterActivity.this, 
                                "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createUserProfile(FirebaseUser firebaseUser, String name) {
        User user = new User();
        user.setUserId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setDisplayName(name);
        user.setCreatedAt(new Date());
        user.setLastLoginAt(new Date());

        userRepository.createUser(user, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                setLoadingState(false);
                
                Toast.makeText(RegisterActivity.this, 
                        "Registration successful! Welcome to FlowFits!", 
                        Toast.LENGTH_SHORT).show();
                
                // Navigate directly to main activity since user is now logged in
                navigateToMainActivity();
            }
            
            @Override
            public void onFailure(String error) {
                setLoadingState(false);
                Toast.makeText(RegisterActivity.this, 
                        "Failed to create user profile: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Reset errors
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        boolean isValid = true;

        // Validate name
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Name is required");
            isValid = false;
        } else if (name.length() < 2) {
            nameLayout.setError("Name must be at least 2 characters");
            isValid = false;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email address");
            isValid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        } else if (!isPasswordStrong(password)) {
            passwordLayout.setError("Password must contain letters and numbers");
            isValid = false;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError("Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    private boolean isPasswordStrong(String password) {
        // Check if password contains at least one letter and one number
        return password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }

    private void setLoadingState(boolean isLoading) {
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!isLoading);
        loginButton.setEnabled(!isLoading);
        nameEditText.setEnabled(!isLoading);
        emailEditText.setEnabled(!isLoading);
        passwordEditText.setEnabled(!isLoading);
        confirmPasswordEditText.setEnabled(!isLoading);
        
        if (isLoading) {
            registerButton.setText("Creating Account...");
        } else {
            registerButton.setText("Create Account");
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        goToLogin();
        super.onBackPressed();
    }
} 