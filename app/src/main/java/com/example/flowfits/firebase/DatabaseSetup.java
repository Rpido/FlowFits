package com.example.flowfits.firebase;

import android.util.Log;
import com.example.flowfits.models.User;
import com.example.flowfits.utils.Constants;
import com.google.firebase.database.DatabaseReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DatabaseSetup {
    private static final String TAG = "DatabaseSetup";
    private FirebaseManager firebaseManager;
    private DatabaseReference databaseRef;

    public DatabaseSetup() {
        firebaseManager = FirebaseManager.getInstance();
        databaseRef = firebaseManager.getDatabaseReference();
    }

    /**
     * Initialize the database with user profile
     * Creates a clean user profile without any sample data
     */
    public void initializeDatabase(String userId, DatabaseInitCallback callback) {
        if (userId == null) {
            Log.e(TAG, "User ID is null, cannot initialize database");
            if (callback != null) {
                callback.onFailure("User ID is required");
            }
            return;
        }

        Log.d(TAG, "Initializing database for user: " + userId);
        
        // Create user profile
        createUserProfile(userId, callback);
    }

    private void createUserProfile(String userId, DatabaseInitCallback callback) {
        // Create user profile
        User user = new User();
        user.setUserId(userId);
        user.setDisplayName("FlowFits User");
        user.setEmail(firebaseManager.getCurrentUser() != null ? 
                      firebaseManager.getCurrentUser().getEmail() : "user@flowfits.com");
        user.setDateJoined(new Date());
        
        // Create user stats
        User.UserStats stats = new User.UserStats();
        stats.setTotalWorkouts(0);
        stats.setGoalsCompleted(0);
        stats.setTotalCaloriesBurned(0);
        stats.setLongestStreak(0);
        user.setStats(stats);
        
        // Create user preferences
        User.UserPreferences preferences = new User.UserPreferences();
        preferences.setNotifications(true);
        preferences.setReminderTime("18:00");
        preferences.setUnits("metric");
        user.setPreferences(preferences);

        // Save user to database
        databaseRef.child(Constants.USERS_COLLECTION)
                .child(userId)
                .setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User created successfully");
                    // Complete initialization without sample data
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create user: " + e.getMessage());
                    if (callback != null) {
                        callback.onFailure("Failed to create user: " + e.getMessage());
                    }
                });
    }

    /**
     * Create the basic database structure
     */
    public void createDatabaseStructure() {
        Map<String, Object> structure = new HashMap<>();
        structure.put(Constants.USERS_COLLECTION + "/placeholder", true);
        structure.put(Constants.GOALS_COLLECTION + "/placeholder", true);
        structure.put(Constants.WORKOUTS_COLLECTION + "/placeholder", true);
        structure.put(Constants.HABITS_COLLECTION + "/placeholder", true);
        structure.put(Constants.HABIT_LOGS_COLLECTION + "/placeholder", true);
        structure.put(Constants.ACHIEVEMENTS_COLLECTION + "/placeholder", true);
        structure.put(Constants.ANALYTICS_COLLECTION + "/placeholder", true);

        databaseRef.updateChildren(structure)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Database structure created"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to create database structure: " + e.getMessage()));
    }

    public interface DatabaseInitCallback {
        void onSuccess();
        void onFailure(String error);
    }
} 