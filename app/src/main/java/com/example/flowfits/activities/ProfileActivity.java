package com.example.flowfits.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.flowfits.MainActivity;
import com.example.flowfits.R;
import com.example.flowfits.models.User;
import com.example.flowfits.viewmodels.ProfileViewModel;

public class ProfileActivity extends AppCompatActivity {

    private ProfileViewModel profileViewModel;
    
    // UI Components
    private ImageView ivProfilePicture;
    private TextView tvDisplayName;
    private TextView tvEmail;
    private TextView tvTotalWorkouts;
    private TextView tvTotalGoals;
    private TextView tvActiveHabits;
    private TextView tvCurrentStreak;
    private Button btnEditProfile;
    private Button btnChangePassword;
    private Button btnLogout;
    private LinearLayout layoutStats;

    // Image picker launcher
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupViewModel();
        setupImagePicker();
        setupClickListeners();
        observeViewModel();
        
        // Load user data
        profileViewModel.loadCurrentUser();
        profileViewModel.loadUserStats();
    }

    private void initViews() {
        // Profile info
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvEmail = findViewById(R.id.tv_email);
        
        // Statistics
        tvTotalWorkouts = findViewById(R.id.tv_total_workouts);
        tvTotalGoals = findViewById(R.id.tv_total_goals);
        tvActiveHabits = findViewById(R.id.tv_active_habits);
        tvCurrentStreak = findViewById(R.id.tv_current_streak);
        layoutStats = findViewById(R.id.layout_stats);
        
        // Action buttons
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void setupViewModel() {
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    profileViewModel.uploadProfilePicture(uri);
                }
            }
        );
    }

    private void setupClickListeners() {
        ivProfilePicture.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void observeViewModel() {
        // Observe current user
        profileViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                updateUserInfo(user);
            }
        });

        // Observe user statistics
        profileViewModel.getUserStats().observe(this, stats -> {
            if (stats != null) {
                updateUserStats(stats);
            }
        });

        // Observe loading state
        profileViewModel.getIsLoading().observe(this, isLoading -> {
            // You can add loading indicators here
        });

        // Observe error messages
        profileViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        // Observe success messages
        profileViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserInfo(User user) {
        tvDisplayName.setText(user.getDisplayName() != null ? user.getDisplayName() : "No name set");
        tvEmail.setText(user.getEmail());

        // Load profile picture
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            Glide.with(this)
                .load(user.getProfilePictureUrl())
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(ivProfilePicture);
        } else {
            ivProfilePicture.setImageResource(R.drawable.ic_person);
        }
    }

    private void updateUserStats(User.UserStats stats) {
        layoutStats.setVisibility(View.VISIBLE);
        tvTotalWorkouts.setText(String.valueOf(stats.getTotalWorkouts()));
        tvTotalGoals.setText(String.valueOf(stats.getTotalGoals()));
        tvActiveHabits.setText(String.valueOf(stats.getActiveHabits()));
        tvCurrentStreak.setText(String.valueOf(stats.getCurrentStreak()));
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        
        EditText etDisplayName = dialogView.findViewById(R.id.et_display_name);
        
        // Pre-fill current display name
        User currentUser = profileViewModel.getCurrentUser().getValue();
        if (currentUser != null && currentUser.getDisplayName() != null) {
            etDisplayName.setText(currentUser.getDisplayName());
        }

        builder.setView(dialogView)
                .setTitle("Edit Profile")
                .setPositiveButton("Save", (dialog, which) -> {
                    String newDisplayName = etDisplayName.getText().toString().trim();
                    if (!TextUtils.isEmpty(newDisplayName)) {
                        profileViewModel.updateDisplayName(newDisplayName);
                    } else {
                        Toast.makeText(this, "Display name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        
        EditText etCurrentPassword = dialogView.findViewById(R.id.et_current_password);
        EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        EditText etConfirmPassword = dialogView.findViewById(R.id.et_confirm_password);

        builder.setView(dialogView)
                .setTitle("Change Password")
                .setPositiveButton("Change", (dialog, which) -> {
                    String currentPassword = etCurrentPassword.getText().toString().trim();
                    String newPassword = etNewPassword.getText().toString().trim();
                    String confirmPassword = etConfirmPassword.getText().toString().trim();

                    if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword)) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPassword.length() < 6) {
                        Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    profileViewModel.changePassword(currentPassword, newPassword);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    profileViewModel.logout();
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    // Navigate to login
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
} 