package com.example.flowfits.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowfits.R;
import com.example.flowfits.activities.AddEditWorkoutActivity;
import com.example.flowfits.adapters.WorkoutAdapter;
import com.example.flowfits.firebase.FirebaseManager;
import com.example.flowfits.models.Workout;
import com.example.flowfits.viewmodels.WorkoutsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutsFragment extends Fragment {
    private static final String TAG = "WorkoutsFragment";
    
    // UI Components
    private RecyclerView rvWorkouts;
    private MaterialCardView cardWorkoutStats;
    private View layoutEmptyState;
    private TextView tvThisWeekCount, tvTotalWorkouts, tvWorkoutStreak;
    private MaterialButton btnQuickWorkout, btnCreateWorkout, btnCreateFirstWorkout, btnViewAll;
    
    // Data & Adapters
    private WorkoutsViewModel viewModel;
    private WorkoutAdapter adapter;
    private List<Workout> workoutList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workouts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupViewModel();
        setupClickListeners();
        
        // Load data
        loadWorkouts();
    }

    private void initializeViews(View view) {
        // RecyclerView
        rvWorkouts = view.findViewById(R.id.rv_workouts);
        
        // Stats components
        cardWorkoutStats = view.findViewById(R.id.card_workout_stats);
        tvThisWeekCount = view.findViewById(R.id.tv_this_week_count);
        tvTotalWorkouts = view.findViewById(R.id.tv_total_workouts);
        tvWorkoutStreak = view.findViewById(R.id.tv_workout_streak);
        
        // Empty state
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        
        // Buttons
        btnQuickWorkout = view.findViewById(R.id.btn_quick_workout);
        btnCreateWorkout = view.findViewById(R.id.btn_create_workout);
        btnCreateFirstWorkout = view.findViewById(R.id.btn_create_first_workout);
        btnViewAll = view.findViewById(R.id.btn_view_all);
    }

    private void setupRecyclerView() {
        adapter = new WorkoutAdapter(workoutList, this::onWorkoutClick, this::onWorkoutEdit, this::onWorkoutDelete);
        rvWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWorkouts.setAdapter(adapter);
        rvWorkouts.setNestedScrollingEnabled(false);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WorkoutsViewModel.class);
        
        // Observe workouts
        viewModel.getWorkouts().observe(getViewLifecycleOwner(), workouts -> {
            if (workouts != null) {
                workoutList.clear();
                workoutList.addAll(workouts);
                adapter.notifyDataSetChanged();
                updateUI();
                updateStats(workouts);
                Log.d(TAG, "Workouts updated: " + workouts.size() + " workouts");
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // You can add loading indicators here if needed
        });

        // Observe errors
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "WorkoutViewModel error: " + error);
            }
        });
    }

    private void setupClickListeners() {
        // Quick workout button
        btnQuickWorkout.setOnClickListener(v -> startQuickWorkout());
        
        // Create workout buttons (both main and first workout)
        View.OnClickListener createWorkoutListener = v -> openCreateWorkout();
        btnCreateWorkout.setOnClickListener(createWorkoutListener);
        btnCreateFirstWorkout.setOnClickListener(createWorkoutListener);
        
        // View all button
        btnViewAll.setOnClickListener(v -> openAllWorkouts());
    }

    private void updateUI() {
        boolean hasWorkouts = !workoutList.isEmpty();
        
        // Show/hide empty state
        layoutEmptyState.setVisibility(hasWorkouts ? View.GONE : View.VISIBLE);
        rvWorkouts.setVisibility(hasWorkouts ? View.VISIBLE : View.GONE);
        
        // Show/hide stats card
        cardWorkoutStats.setVisibility(hasWorkouts ? View.VISIBLE : View.GONE);
        
        // Show/hide view all button
        btnViewAll.setVisibility(hasWorkouts && workoutList.size() > 3 ? View.VISIBLE : View.GONE);
        
        Log.d(TAG, "UI updated - Has workouts: " + hasWorkouts + ", Count: " + workoutList.size());
    }

    private void updateStats(List<Workout> workouts) {
        if (workouts == null || workouts.isEmpty()) return;
        
        try {
            // Calculate this week's workouts
            int thisWeekCount = calculateThisWeekWorkouts(workouts);
            tvThisWeekCount.setText(String.valueOf(thisWeekCount));
            
            // Total workouts
            tvTotalWorkouts.setText(String.valueOf(workouts.size()));
            
            // Calculate workout streak
            int streak = calculateWorkoutStreak(workouts);
            tvWorkoutStreak.setText(String.valueOf(streak));
            
            Log.d(TAG, "Stats updated - This week: " + thisWeekCount + ", Total: " + workouts.size() + ", Streak: " + streak);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating stats", e);
            // Set default values
            tvThisWeekCount.setText("0");
            tvTotalWorkouts.setText(String.valueOf(workouts.size()));
            tvWorkoutStreak.setText("0");
        }
    }

    private int calculateThisWeekWorkouts(List<Workout> workouts) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        long weekStart = calendar.getTimeInMillis();
        long now = System.currentTimeMillis();
        
        int count = 0;
        
        for (Workout workout : workouts) {
            try {
                if (workout.getDate() != null) {
                    long workoutTime = workout.getDate().getTime();
                    if (workoutTime >= weekStart && workoutTime <= now) {
                        count++;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing workout date", e);
            }
        }
        
        return count;
    }

    private int calculateWorkoutStreak(List<Workout> workouts) {
        if (workouts.isEmpty()) return 0;
        
        try {
            // Sort workouts by date (most recent first)
            List<Workout> sortedWorkouts = new ArrayList<>(workouts);
            
            sortedWorkouts.sort((w1, w2) -> {
                try {
                    Date date1 = w1.getDate();
                    Date date2 = w2.getDate();
                    if (date1 != null && date2 != null) {
                        return date2.compareTo(date1); // Most recent first
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error comparing dates", e);
                }
                return 0;
            });
            
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            int streak = 0;
            Calendar checkDate = (Calendar) today.clone();
            
            // Check if there's a workout today or yesterday to start the streak
            boolean foundRecent = false;
            for (int i = 0; i < 2 && !foundRecent; i++) {
                String checkDateStr = sdf.format(checkDate.getTime());
                for (Workout workout : sortedWorkouts) {
                    if (workout.getDate() != null && checkDateStr.equals(sdf.format(workout.getDate()))) {
                        foundRecent = true;
                        break;
                    }
                }
                if (!foundRecent) {
                    checkDate.add(Calendar.DAY_OF_MONTH, -1);
                }
            }
            
            if (!foundRecent) return 0;
            
            // Count consecutive days with workouts
            checkDate = (Calendar) today.clone();
            for (int i = 0; i < 30; i++) { // Check last 30 days max
                String checkDateStr = sdf.format(checkDate.getTime());
                boolean hasWorkout = false;
                
                for (Workout workout : sortedWorkouts) {
                    if (workout.getDate() != null && checkDateStr.equals(sdf.format(workout.getDate()))) {
                        hasWorkout = true;
                        break;
                    }
                }
                
                if (hasWorkout) {
                    streak++;
                } else {
                    break;
                }
                
                checkDate.add(Calendar.DAY_OF_MONTH, -1);
            }
            
            return streak;
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating workout streak", e);
            return 0;
        }
    }

    private void startQuickWorkout() {
        try {
            // Check if user is authenticated
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(getContext(), "Please log in to create workouts", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create a quick 20-minute full-body workout
            Workout quickWorkout = new Workout();
            quickWorkout.setName("Quick Full-Body Workout");
            quickWorkout.setType(Workout.WorkoutType.STRENGTH); // Required by database rules
            quickWorkout.setNotes("A quick 20-minute workout to get your blood pumping!");
            quickWorkout.setDuration(20);
            quickWorkout.setCaloriesBurned(150); // Estimated calories for a 20-min workout
            quickWorkout.setDate(new Date());
            
            // Add some basic exercises
            List<Workout.Exercise> exercises = new ArrayList<>();
            
            Workout.Exercise pushUps = new Workout.Exercise("Push-ups");
            pushUps.setSets(3);
            pushUps.setReps(15);
            pushUps.setCategory("Push");
            pushUps.setMuscleGroup("Chest");
            exercises.add(pushUps);
            
            Workout.Exercise squats = new Workout.Exercise("Squats");
            squats.setSets(3);
            squats.setReps(20);
            squats.setCategory("Legs");
            squats.setMuscleGroup("Legs");
            exercises.add(squats);
            
            Workout.Exercise plank = new Workout.Exercise("Plank Hold");
            plank.setSets(3);
            plank.setReps(1);
            plank.setNotes("Hold for 30 seconds");
            plank.setCategory("Core");
            plank.setMuscleGroup("Core");
            exercises.add(plank);
            
            Workout.Exercise jumpingJacks = new Workout.Exercise("Jumping Jacks");
            jumpingJacks.setSets(3);
            jumpingJacks.setReps(30);
            jumpingJacks.setCategory("Cardio");
            jumpingJacks.setMuscleGroup("Full Body");
            exercises.add(jumpingJacks);
            
            quickWorkout.setExercises(exercises);
            
            // Calculate totals before saving
            quickWorkout.calculateTotals();
            
            // Save the workout
            viewModel.saveWorkout(quickWorkout);
            
            Toast.makeText(getContext(), "🎉 Quick workout started! You've got this! 💪", Toast.LENGTH_LONG).show();
            
            Log.d(TAG, "Quick workout created and started");
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting quick workout", e);
            Toast.makeText(getContext(), "Error starting quick workout. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCreateWorkout() {
        Intent intent = new Intent(getActivity(), AddEditWorkoutActivity.class);
        startActivity(intent);
    }

    private void openAllWorkouts() {
        // TODO: Implement view all workouts functionality
        Toast.makeText(getContext(), "View all workouts - Coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void loadWorkouts() {
        if (viewModel != null) {
            viewModel.loadWorkouts();
            Log.d(TAG, "Loading workouts...");
        }
    }

    // RecyclerView item click handlers
    private void onWorkoutClick(Workout workout) {
        try {
            // Check if user is authenticated
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(getContext(), "Please log in to start workouts", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check if workout has exercises
        if (workout.getExercises() == null || workout.getExercises().isEmpty()) {
                Toast.makeText(getContext(), "This workout has no exercises to perform", Toast.LENGTH_SHORT).show();
            return;
        }
        
            // Launch WorkoutExecutionActivity
        Intent intent = new Intent(getActivity(), com.example.flowfits.activities.WorkoutExecutionActivity.class);
        intent.putExtra(com.example.flowfits.activities.WorkoutExecutionActivity.EXTRA_WORKOUT, workout);
            startActivity(intent);
            
            Log.d(TAG, "Starting workout execution for: " + workout.getName());
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting workout execution", e);
            Toast.makeText(getContext(), "Error starting workout. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void onWorkoutEdit(Workout workout) {
        Intent intent = new Intent(getActivity(), AddEditWorkoutActivity.class);
        intent.putExtra("WORKOUT_ID", workout.getWorkoutId());
        startActivity(intent);
    }

    private void onWorkoutDelete(Workout workout) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Workout")
                .setMessage("Are you sure you want to delete \"" + workout.getName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (viewModel != null) {
                    viewModel.deleteWorkout(workout.getWorkoutId());
                        Toast.makeText(getContext(), "Workout deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWorkouts(); // Refresh data when returning to fragment
    }
} 