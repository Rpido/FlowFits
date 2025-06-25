package com.example.flowfits;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class FlowFitsApplication extends Application {
    private static final String TAG = "FlowFitsApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.d(TAG, "Initializing FlowFits Application");
        
        // Initialize Firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }
        
        // Enable offline persistence BEFORE any other Firebase Database usage
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Log.d(TAG, "Firebase Database persistence enabled");
        } catch (Exception e) {
            Log.w(TAG, "Firebase Database persistence already enabled or failed: " + e.getMessage());
        }
    }
} 