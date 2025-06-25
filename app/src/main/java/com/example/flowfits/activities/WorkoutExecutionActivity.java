package com.example.flowfits.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flowfits.R;
import com.example.flowfits.adapters.LiveExerciseAdapter;
import com.example.flowfits.models.Workout;
import com.example.flowfits.repositories.WorkoutRepository;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WorkoutExecutionActivity extends AppCompatActivity implements LiveExerciseAdapter.OnExerciseUpdateListener {
    
    public static final String EXTRA_WORKOUT = "extra_workout";
    public static final String EXTRA_WORKOUT_ID = "extra_workout_id";
    
    // UI Components
    private TextView workoutTitle;
    private TextView workoutTimer;
    private TextView currentExerciseTitle;
    private TextView exerciseProgress;
    private ProgressBar workoutProgress;
    private RecyclerView exercisesRecyclerView;
    private MaterialCardView currentExerciseCard;
    private TextView currentExerciseName;
    private TextView currentExerciseDetails;
    private TextView restTimer;
    private LinearLayout restTimerLayout;
    private MaterialButton pauseResumeButton;
    private MaterialButton finishButton;
    private MaterialButton skipExerciseButton;
    private MaterialButton skipRestButton;
    
    // Data
    private Workout workout;
    private LiveExerciseAdapter adapter;
    private WorkoutRepository workoutRepository;
    
    // Timer management
    private Handler handler;
    private Runnable workoutTimerRunnable;
    private Runnable restTimerRunnable;
    private long workoutStartTime;
    private long pausedTime = 0;
    private boolean isWorkoutPaused = false;
    private boolean isInRestPeriod = false;
    private int restTimeRemaining = 0;
    private int currentExerciseIndex = 0;
    
    // Session state
    private WorkoutSession currentSession;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_execution);
        
        initViews();
        initTimers();
        initData();
        setupRecyclerView();
        setupClickListeners();
        
        // Initialize workout after everything is set up
        if (workout != null) {
            finalizeWorkoutInitialization();
            startWorkoutSession();
        }
    }
    
    private void initViews() {
        workoutTitle = findViewById(R.id.tv_workout_title);
        workoutTimer = findViewById(R.id.tv_workout_timer);
        currentExerciseTitle = findViewById(R.id.tv_current_exercise_title);
        exerciseProgress = findViewById(R.id.tv_exercise_progress);
        workoutProgress = findViewById(R.id.progress_workout);
        exercisesRecyclerView = findViewById(R.id.rv_exercises);
        currentExerciseCard = findViewById(R.id.card_current_exercise);
        currentExerciseName = findViewById(R.id.tv_current_exercise_name);
        currentExerciseDetails = findViewById(R.id.tv_current_exercise_details);
        restTimer = findViewById(R.id.tv_rest_timer);
        restTimerLayout = findViewById(R.id.layout_rest_timer);
        pauseResumeButton = findViewById(R.id.btn_pause_resume);
        finishButton = findViewById(R.id.btn_finish_workout);
        skipExerciseButton = findViewById(R.id.btn_skip_exercise);
        skipRestButton = findViewById(R.id.btn_skip_rest);
    }
    
    private void initData() {
        workoutRepository = WorkoutRepository.getInstance();
        
        // Get workout from intent
        if (getIntent().hasExtra(EXTRA_WORKOUT)) {
            workout = (Workout) getIntent().getSerializableExtra(EXTRA_WORKOUT);
        } else if (getIntent().hasExtra(EXTRA_WORKOUT_ID)) {
            String workoutId = getIntent().getStringExtra(EXTRA_WORKOUT_ID);
            // Load workout by ID
            workoutRepository.getWorkoutById(workoutId, new WorkoutRepository.WorkoutByIdCallback() {
                @Override
                public void onSuccess(Workout loadedWorkout) {
                    workout = loadedWorkout;
                    // Setup RecyclerView with the loaded workout
                    if (adapter != null) {
                        adapter.updateExercises(workout.getExercises());
                    }
                    finalizeWorkoutInitialization();
                    startWorkoutSession();
                }
                
                @Override
                public void onFailure(String error) {
                    Toast.makeText(WorkoutExecutionActivity.this, "Failed to load workout: " + error, Toast.LENGTH_LONG).show();
                    finish();
                }
            });
            return;
        }
        
        if (workout == null) {
            Toast.makeText(this, "No workout provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
    
    private void finalizeWorkoutInitialization() {
        if (workout == null) {
            Toast.makeText(this, "No workout provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Initialize workout session
        currentSession = new WorkoutSession(workout);
        
        // Set workout title
        workoutTitle.setText(workout.getName());
        
        // Set initial progress
        updateExerciseProgress();
        
        // Initialize current exercise
        if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
            setCurrentExercise(0);
        } else {
            Toast.makeText(this, "No exercises in this workout", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void initTimers() {
        handler = new Handler(Looper.getMainLooper());
        
        workoutTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isWorkoutPaused) {
                    updateWorkoutTimer();
                }
                handler.postDelayed(this, 1000); // Update every second
            }
        };
        
        restTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isInRestPeriod && restTimeRemaining > 0) {
                    restTimeRemaining--;
                    updateRestTimer();
                    handler.postDelayed(this, 1000);
                } else if (isInRestPeriod && restTimeRemaining <= 0) {
                    endRestPeriod();
                }
            }
        };
    }
    
    private void setupRecyclerView() {
        adapter = new LiveExerciseAdapter(this, this);
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exercisesRecyclerView.setAdapter(adapter);
        
        if (workout != null && workout.getExercises() != null) {
            adapter.updateExercises(workout.getExercises());
        }
    }
    
    private void setupClickListeners() {
        pauseResumeButton.setOnClickListener(v -> togglePauseResume());
        finishButton.setOnClickListener(v -> showFinishConfirmation());
        skipExerciseButton.setOnClickListener(v -> skipCurrentExercise());
        skipRestButton.setOnClickListener(v -> skipRestPeriod());
        
        // Back button handling
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());
    }
    
    private void startWorkoutSession() {
        workoutStartTime = System.currentTimeMillis();
        workout.setStartTime(new Date(workoutStartTime));
        currentSession.startSession();
        
        // Start workout timer
        handler.post(workoutTimerRunnable);
        
        // Update UI state
        pauseResumeButton.setText("Pause");
        finishButton.setEnabled(true);
        
        Toast.makeText(this, "Workout started!", Toast.LENGTH_SHORT).show();
    }
    
    private void togglePauseResume() {
        if (isWorkoutPaused) {
            resumeWorkout();
        } else {
            pauseWorkout();
        }
    }
    
    private void pauseWorkout() {
        isWorkoutPaused = true;
        pausedTime = System.currentTimeMillis() - workoutStartTime;
        currentSession.pauseSession();
        
        pauseResumeButton.setText("Resume");
        Toast.makeText(this, "Workout paused", Toast.LENGTH_SHORT).show();
    }
    
    private void resumeWorkout() {
        isWorkoutPaused = false;
        workoutStartTime = System.currentTimeMillis() - pausedTime;
        currentSession.resumeSession();
        
        pauseResumeButton.setText("Pause");
        Toast.makeText(this, "Workout resumed", Toast.LENGTH_SHORT).show();
    }
    
    private void setCurrentExercise(int index) {
        if (index < 0 || workout.getExercises() == null || index >= workout.getExercises().size()) {
            return;
        }
        
        currentExerciseIndex = index;
        Workout.Exercise exercise = workout.getExercises().get(index);
        
        // Update current exercise card
        currentExerciseName.setText(exercise.getName());
        
        String details = "";
        if (exercise.getSets() != null && exercise.getReps() != null) {
            details = exercise.getSets() + " sets × " + exercise.getReps() + " reps";
            if (exercise.getWeight() != null && exercise.getWeight() > 0) {
                details += " @ " + exercise.getWeight() + " " + (exercise.getUnit() != null ? exercise.getUnit() : "kg");
            }
        } else if (exercise.getDistance() != null && exercise.getDistance() > 0) {
            details = exercise.getDistance() + " " + (exercise.getUnit() != null ? exercise.getUnit() : "km");
        }
        currentExerciseDetails.setText(details);
        
        // Show current exercise card
        currentExerciseCard.setVisibility(View.VISIBLE);
        
        // Update progress
        updateExerciseProgress();
        
        // Scroll to current exercise in recycler view
        exercisesRecyclerView.scrollToPosition(index);
        if (adapter != null) {
            adapter.setCurrentExercise(index);
        }
    }
    
    private void skipCurrentExercise() {
        if (currentExerciseIndex < workout.getExercises().size() - 1) {
            // Mark current exercise as skipped (not completed)
            moveToNextExercise();
        } else {
            // Last exercise, finish workout
            finishWorkout();
        }
    }
    
    private void moveToNextExercise() {
        // Start rest period if configured
        Workout.Exercise currentExercise = workout.getExercises().get(currentExerciseIndex);
        if (currentExercise.getRestTimeSeconds() > 0 && currentExerciseIndex < workout.getExercises().size() - 1) {
            startRestPeriod(currentExercise.getRestTimeSeconds());
        } else {
            proceedToNextExercise();
        }
    }
    
    private void proceedToNextExercise() {
        currentExerciseIndex++;
        if (currentExerciseIndex < workout.getExercises().size()) {
            setCurrentExercise(currentExerciseIndex);
        } else {
            finishWorkout();
        }
    }
    
    private void startRestPeriod(int restSeconds) {
        isInRestPeriod = true;
        restTimeRemaining = restSeconds;
        
        restTimerLayout.setVisibility(View.VISIBLE);
        skipRestButton.setVisibility(View.VISIBLE);
        
        // Start rest timer
        handler.post(restTimerRunnable);
        
        Toast.makeText(this, "Rest time started", Toast.LENGTH_SHORT).show();
    }
    
    private void endRestPeriod() {
        isInRestPeriod = false;
        restTimerLayout.setVisibility(View.GONE);
        skipRestButton.setVisibility(View.GONE);
        
        proceedToNextExercise();
        Toast.makeText(this, "Rest time finished", Toast.LENGTH_SHORT).show();
    }
    
    private void skipRestPeriod() {
        if (isInRestPeriod) {
            handler.removeCallbacks(restTimerRunnable);
            endRestPeriod();
        }
    }
    
    private void updateWorkoutTimer() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - workoutStartTime;
        
        int seconds = (int) (elapsedTime / 1000) % 60;
        int minutes = (int) (elapsedTime / (1000 * 60)) % 60;
        int hours = (int) (elapsedTime / (1000 * 60 * 60));
        
        String timeString;
        if (hours > 0) {
            timeString = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
        
        workoutTimer.setText(timeString);
    }
    
    private void updateRestTimer() {
        int minutes = restTimeRemaining / 60;
        int seconds = restTimeRemaining % 60;
        String timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        restTimer.setText(timeString);
    }
    
    private void updateExerciseProgress() {
        if (workout.getExercises() == null || workout.getExercises().isEmpty()) {
            return;
        }
        
        int totalExercises = workout.getExercises().size();
        int completedExercises = 0;
        
        for (Workout.Exercise exercise : workout.getExercises()) {
            if (exercise.isCompleted()) {
                completedExercises++;
            }
        }
        
        // Update progress bar
        int progressPercentage = (int) ((completedExercises * 100.0) / totalExercises);
        workoutProgress.setProgress(progressPercentage);
        
        // Update progress text
        String progressText = completedExercises + " of " + totalExercises + " exercises completed";
        exerciseProgress.setText(progressText);
        
        // Update current exercise title
        String currentExerciseText = "Exercise " + (currentExerciseIndex + 1) + " of " + totalExercises;
        currentExerciseTitle.setText(currentExerciseText);
    }
    
    private void showFinishConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Finish Workout")
                .setMessage("Are you sure you want to finish this workout?")
                .setPositiveButton("Finish", (dialog, which) -> finishWorkout())
                .setNegativeButton("Continue", null)
                .show();
    }
    
    private void finishWorkout() {
        // Stop all timers
        handler.removeCallbacks(workoutTimerRunnable);
        handler.removeCallbacks(restTimerRunnable);
        
        // Calculate final workout data
        long endTime = System.currentTimeMillis();
        workout.setEndTime(new Date(endTime));
        workout.setCompleted(true);
        
        int duration = (int) ((endTime - workoutStartTime) / (1000 * 60)); // Convert to minutes
        workout.setDuration(duration);
        
        // Calculate totals
        workout.calculateTotals();
        
        // End session
        currentSession.endSession();
        
        // Save workout
        workoutRepository.saveWorkout(workout, new WorkoutRepository.WorkoutCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(WorkoutExecutionActivity.this, "Workout completed successfully!", Toast.LENGTH_LONG).show();
                
                // Return to previous activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("completed_workout", workout);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
            
            @Override
            public void onFailure(String error) {
                Toast.makeText(WorkoutExecutionActivity.this, "Failed to save workout: " + error, Toast.LENGTH_LONG).show();
                // Still finish the activity
                finish();
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Exit Workout")
                .setMessage("Are you sure you want to exit? Your progress will be lost.")
                .setPositiveButton("Exit", (dialog, which) -> {
                    // Stop timers and exit
                    handler.removeCallbacks(workoutTimerRunnable);
                    handler.removeCallbacks(restTimerRunnable);
                    super.onBackPressed();
                })
                .setNegativeButton("Continue", null)
                .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up timers
        if (handler != null) {
            handler.removeCallbacks(workoutTimerRunnable);
            handler.removeCallbacks(restTimerRunnable);
        }
    }
    
    // LiveExerciseAdapter.OnExerciseUpdateListener implementation
    @Override
    public void onExerciseCompleted(int position) {
        if (position == currentExerciseIndex) {
            moveToNextExercise();
        }
        updateExerciseProgress();
        handler.post(() -> {
            if (adapter != null) {
                adapter.notifyItemChanged(position);
            }
        });
    }
    
    @Override
    public void onSetCompleted(int exercisePosition, int setNumber) {
        updateExerciseProgress();
        handler.post(() -> {
            if (adapter != null) {
                adapter.notifyItemChanged(exercisePosition);
            }
        });
    }
    
    @Override
    public void onExerciseDataChanged(int position, Workout.Exercise exercise) {
        // Update the exercise data in the workout
        if (position >= 0 && position < workout.getExercises().size()) {
            workout.getExercises().set(position, exercise);
        }
    }
    
    // Inner class for workout session management
    private static class WorkoutSession {
        private Workout workout;
        private Date sessionStartTime;
        private Date sessionPausedTime;
        private boolean isActive;
        private boolean isPaused;
        
        public WorkoutSession(Workout workout) {
            this.workout = workout;
            this.isActive = false;
            this.isPaused = false;
        }
        
        public void startSession() {
            sessionStartTime = new Date();
            isActive = true;
            isPaused = false;
        }
        
        public void pauseSession() {
            if (isActive && !isPaused) {
                sessionPausedTime = new Date();
                isPaused = true;
            }
        }
        
        public void resumeSession() {
            if (isActive && isPaused) {
                isPaused = false;
                sessionPausedTime = null;
            }
        }
        
        public void endSession() {
            isActive = false;
            isPaused = false;
        }
        
        public boolean isActive() {
            return isActive;
        }
        
        public boolean isPaused() {
            return isPaused;
        }
    }
} 