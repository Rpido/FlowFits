package com.example.flowfits;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.flowfits.activities.LoginActivity;
import com.example.flowfits.activities.ProfileActivity;
import com.example.flowfits.fragments.AnalyticsFragment;
import com.example.flowfits.fragments.DashboardFragment;
import com.example.flowfits.fragments.GoalsFragment;
import com.example.flowfits.fragments.HabitsFragment;
import com.example.flowfits.fragments.WorkoutsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        setupViews();
        setupToolbar();
        setupBottomNavigation();
        
        // Set default fragment to Dashboard
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }
    }

    private void setupViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("FlowFits");
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                
                if (itemId == R.id.nav_dashboard) {
                    selectedFragment = new DashboardFragment();
                } else if (itemId == R.id.nav_workouts) {
                    selectedFragment = new WorkoutsFragment();
                } else if (itemId == R.id.nav_habits) {
                    selectedFragment = new HabitsFragment();
                } else if (itemId == R.id.nav_goals) {
                    selectedFragment = new GoalsFragment();
                } else if (itemId == R.id.nav_analytics) {
                    selectedFragment = new AnalyticsFragment();
                }
                
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                
                return false;
            }
        });
    }

    public void navigateToTab(int tabIndex) {
        // Map tab indices to bottom navigation items
        int menuItemId;
        switch (tabIndex) {
            case 0:
                menuItemId = R.id.nav_dashboard;
                break;
            case 1:
                menuItemId = R.id.nav_goals;
                break;
            case 2:
                menuItemId = R.id.nav_workouts;
                break;
            case 3:
                menuItemId = R.id.nav_habits;
                break;
            case 4:
                menuItemId = R.id.nav_analytics;
                break;
            default:
                return;
        }
        
        bottomNavigationView.setSelectedItemId(menuItemId);
    }

    // Navigation methods for dashboard quick actions
    public void navigateToWorkouts() {
        bottomNavigationView.setSelectedItemId(R.id.nav_workouts);
    }

    public void navigateToHabits() {
        bottomNavigationView.setSelectedItemId(R.id.nav_habits);
    }

    public void navigateToGoals() {
        bottomNavigationView.setSelectedItemId(R.id.nav_goals);
    }

    public void navigateToAnalytics() {
        bottomNavigationView.setSelectedItemId(R.id.nav_analytics);
    }

    public void navigateToDashboard() {
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_profile) {
            // Launch ProfileActivity
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
