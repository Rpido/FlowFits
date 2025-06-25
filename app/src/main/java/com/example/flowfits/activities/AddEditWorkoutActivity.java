package com.example.flowfits.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowfits.R;
import com.example.flowfits.adapters.ExerciseEditAdapter;
import com.example.flowfits.models.Workout;
import com.example.flowfits.repositories.WorkoutRepository;
import com.example.flowfits.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddEditWorkoutActivity extends AppCompatActivity {
    
    public static final String EXTRA_WORKOUT = "extra_workout";
    public static final String EXTRA_IS_EDIT_MODE = "extra_is_edit_mode";
    
    private Toolbar toolbar;
    private TextInputLayout nameLayout, durationLayout, caloriesLayout, notesLayout, dateLayout;
    private TextInputEditText nameEdit, durationEdit, caloriesEdit, notesEdit, dateEdit;
    private AutoCompleteTextView typeDropdown;
    private MaterialButton saveButton, cancelButton, btnAddExercise;
    
    // Exercise management
    private RecyclerView rvExercises;
    private LinearLayout layoutEmptyExercises;
    private ExerciseEditAdapter exerciseAdapter;
    
    private Workout editingWorkout;
    private boolean isEditMode = false;
    private WorkoutRepository workoutRepository;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private Calendar selectedDate = Calendar.getInstance();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_workout);
        
        // Initialize repository first
        workoutRepository = WorkoutRepository.getInstance();
        
        initializeViews();
        setupToolbar();
        setupDropdowns();
        setupDatePicker();
        setupExerciseManagement();
        handleIntent();
        setupClickListeners();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        
        nameLayout = findViewById(R.id.nameLayout);
        durationLayout = findViewById(R.id.durationLayout);
        caloriesLayout = findViewById(R.id.caloriesLayout);
        notesLayout = findViewById(R.id.notesLayout);
        dateLayout = findViewById(R.id.dateLayout);
        
        nameEdit = findViewById(R.id.nameEdit);
        durationEdit = findViewById(R.id.durationEdit);
        caloriesEdit = findViewById(R.id.caloriesEdit);
        notesEdit = findViewById(R.id.notesEdit);
        dateEdit = findViewById(R.id.dateEdit);
        
        typeDropdown = findViewById(R.id.typeDropdown);
        
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        btnAddExercise = findViewById(R.id.btnAddExercise);
        
        // Exercise management views
        rvExercises = findViewById(R.id.rvExercises);
        layoutEmptyExercises = findViewById(R.id.layoutEmptyExercises);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
    
    private void setupDropdowns() {
        // Workout type dropdown with enhanced types
        String[] workoutTypes = {"CARDIO", "STRENGTH", "FLEXIBILITY", "SPORTS", "HIIT", "YOGA", "PILATES", "CROSSFIT"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, workoutTypes);
        typeDropdown.setAdapter(typeAdapter);
    }
    
    private void setupDatePicker() {
        dateEdit.setOnClickListener(v -> showDatePicker());
        dateEdit.setFocusable(false);
        dateEdit.setClickable(true);
        
        // Set default date to today
        dateEdit.setText(dateFormat.format(selectedDate.getTime()));
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                dateEdit.setText(dateFormat.format(selectedDate.getTime()));
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    private void handleIntent() {
        editingWorkout = (Workout) getIntent().getSerializableExtra(EXTRA_WORKOUT);
        isEditMode = getIntent().getBooleanExtra(EXTRA_IS_EDIT_MODE, false);
        
        // Check if we're editing by workout ID
        String workoutId = getIntent().getStringExtra("WORKOUT_ID");
        
        if (workoutId != null && !workoutId.isEmpty()) {
            // We're editing a workout by ID
            isEditMode = true;
            setTitle("Edit Workout");
            saveButton.setText("Update Workout");
            saveButton.setEnabled(false); // Disable until workout is loaded
            
            // Load workout from database
            workoutRepository.getWorkoutById(workoutId, new WorkoutRepository.WorkoutByIdCallback() {
                @Override
                public void onSuccess(Workout workout) {
                    editingWorkout = workout;
                    
                    // Debug logging
                    android.util.Log.d("AddEditWorkout", "Loaded workout: " + workout.getName());
                    android.util.Log.d("AddEditWorkout", "Exercise count: " + 
                        (workout.getExercises() != null ? workout.getExercises().size() : "null"));
                    
                    // Ensure UI update happens on main thread
                    runOnUiThread(() -> {
                        populateFields();
                        saveButton.setEnabled(true);
                        
                        // Force update the adapter and visibility after a short delay
                        // This ensures all UI components are ready
                        new android.os.Handler().postDelayed(() -> {
                            if (editingWorkout.getExercises() != null && !editingWorkout.getExercises().isEmpty()) {
                                android.util.Log.d("AddEditWorkout", "Force updating exercises after delay");
                                exerciseAdapter.setExercises(editingWorkout.getExercises());
                                updateExerciseVisibility();
                            }
                        }, 100); // 100ms delay
                    });
                }
                
                @Override
                public void onFailure(String error) {
                    android.util.Log.e("AddEditWorkout", "Failed to load workout: " + error);
                    Toast.makeText(AddEditWorkoutActivity.this, "Failed to load workout: " + error, Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        } else if (isEditMode && editingWorkout != null) {
            setTitle("Edit Workout");
            saveButton.setText("Update Workout");
            populateFields();
        } else {
            setTitle("Add New Workout");
            saveButton.setText("Save Workout");
        }
    }
    
    private void populateFields() {
        nameEdit.setText(editingWorkout.getName());
        
        if (editingWorkout.getType() != null) {
            typeDropdown.setText(editingWorkout.getType().name(), false);
        }
        
        if (editingWorkout.getDuration() > 0) {
            durationEdit.setText(String.valueOf(editingWorkout.getDuration()));
        }
        
        if (editingWorkout.getCaloriesBurned() > 0) {
            caloriesEdit.setText(String.valueOf(editingWorkout.getCaloriesBurned()));
        }
        
        if (!TextUtils.isEmpty(editingWorkout.getNotes())) {
            notesEdit.setText(editingWorkout.getNotes());
        }
        
        if (editingWorkout.getDate() != null) {
            selectedDate.setTime(editingWorkout.getDate());
            dateEdit.setText(dateFormat.format(editingWorkout.getDate()));
        }
        
        // Populate exercises with debugging and null checks
        android.util.Log.d("AddEditWorkout", "populateFields - adapter null? " + (exerciseAdapter == null));
        android.util.Log.d("AddEditWorkout", "populateFields - rvExercises null? " + (rvExercises == null));
        
        if (exerciseAdapter == null) {
            android.util.Log.e("AddEditWorkout", "Exercise adapter is null! Reinitializing...");
            setupExerciseManagement();
        }
        
        if (editingWorkout.getExercises() != null && !editingWorkout.getExercises().isEmpty()) {
            android.util.Log.d("AddEditWorkout", "Setting " + editingWorkout.getExercises().size() + " exercises to adapter");
            for (int i = 0; i < editingWorkout.getExercises().size(); i++) {
                Workout.Exercise ex = editingWorkout.getExercises().get(i);
                android.util.Log.d("AddEditWorkout", "Exercise " + i + ": " + ex.getName() + 
                    " - Sets: " + ex.getSets() + ", Reps: " + ex.getReps());
            }
            exerciseAdapter.setExercises(editingWorkout.getExercises());
            updateExerciseVisibility();
        } else {
            android.util.Log.d("AddEditWorkout", "No exercises found in workout");
            exerciseAdapter.setExercises(new java.util.ArrayList<>());
            updateExerciseVisibility();
        }
    }
    
    private void setupExerciseManagement() {
        // Setup exercise adapter
        exerciseAdapter = new ExerciseEditAdapter(this, new ExerciseEditAdapter.OnExerciseChangeListener() {
            @Override
            public void onExerciseChanged(Workout.Exercise exercise, int position) {
                // Exercise updated - no need to do anything special
                updateExerciseVisibility();
            }

            @Override
            public void onExerciseListChanged(List<Workout.Exercise> exercises) {
                updateExerciseVisibility();
            }
        });
        
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        rvExercises.setAdapter(exerciseAdapter);
        rvExercises.setNestedScrollingEnabled(false);
        
        updateExerciseVisibility();
    }
    
    private void updateExerciseVisibility() {
        boolean hasExercises = exerciseAdapter.getItemCount() > 0;
        android.util.Log.d("AddEditWorkout", "updateExerciseVisibility: hasExercises=" + hasExercises + 
            ", itemCount=" + exerciseAdapter.getItemCount());
        rvExercises.setVisibility(hasExercises ? View.VISIBLE : View.GONE);
        layoutEmptyExercises.setVisibility(hasExercises ? View.GONE : View.VISIBLE);
    }
    
    private void showAddExerciseDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_exercise, null);
        
        // Find views in dialog
        AutoCompleteTextView etExerciseName = dialogView.findViewById(R.id.etExerciseName);
        AutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        AutoCompleteTextView spinnerMuscleGroup = dialogView.findViewById(R.id.spinnerMuscleGroup);
        TextInputEditText etDefaultSets = dialogView.findViewById(R.id.etDefaultSets);
        TextInputEditText etDefaultReps = dialogView.findViewById(R.id.etDefaultReps);
        TextInputEditText etDefaultWeight = dialogView.findViewById(R.id.etDefaultWeight);
        
        // Setup dropdowns with common exercises
        String[] commonExercises = {
            "Push-ups", "Pull-ups", "Squats", "Deadlifts", "Bench Press", "Overhead Press",
            "Rows", "Lunges", "Planks", "Burpees", "Mountain Climbers", "Jumping Jacks",
            "Bicep Curls", "Tricep Dips", "Leg Press", "Calf Raises", "Russian Twists"
        };
        ArrayAdapter<String> exerciseNamesAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, commonExercises);
        etExerciseName.setAdapter(exerciseNamesAdapter);
        
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, Constants.EXERCISE_CATEGORIES);
        spinnerCategory.setAdapter(categoryAdapter);
        
        ArrayAdapter<String> muscleAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, Constants.MUSCLE_GROUPS);
        spinnerMuscleGroup.setAdapter(muscleAdapter);
        
        // Create and show dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        // Setup button listeners
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnAdd = dialogView.findViewById(R.id.btnAdd);
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnAdd.setOnClickListener(v -> {
            String exerciseName = etExerciseName.getText().toString().trim();
            if (TextUtils.isEmpty(exerciseName)) {
                Toast.makeText(this, "Please enter exercise name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create new exercise
            Workout.Exercise exercise = new Workout.Exercise(exerciseName);
            exercise.setCategory(spinnerCategory.getText().toString());
            exercise.setMuscleGroup(spinnerMuscleGroup.getText().toString());
            
            // Set default values if provided
            try {
                String setsText = etDefaultSets.getText().toString();
                if (!TextUtils.isEmpty(setsText)) {
                    exercise.setSets(Integer.parseInt(setsText));
                }
                
                String repsText = etDefaultReps.getText().toString();
                if (!TextUtils.isEmpty(repsText)) {
                    exercise.setReps(Integer.parseInt(repsText));
                }
                
                String weightText = etDefaultWeight.getText().toString();
                if (!TextUtils.isEmpty(weightText)) {
                    exercise.setWeight(Double.parseDouble(weightText));
                }
            } catch (NumberFormatException e) {
                // Invalid numbers, ignore defaults
            }
            
            // Add exercise to adapter
            exerciseAdapter.addExercise(exercise);
            dialog.dismiss();
            
            Toast.makeText(this, "Exercise added!", Toast.LENGTH_SHORT).show();
        });
        
        dialog.show();
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveWorkout());
        cancelButton.setOnClickListener(v -> finish());
        btnAddExercise.setOnClickListener(v -> showAddExerciseDialog());
    }
    
    private void saveWorkout() {
        if (!validateInputs()) {
            return;
        }
        
        // Create or update workout
        Workout workout = isEditMode ? editingWorkout : new Workout();
        
        workout.setName(nameEdit.getText().toString().trim());
        
        // Set workout type
        String typeString = typeDropdown.getText().toString();
        try {
            Workout.WorkoutType type = Workout.WorkoutType.valueOf(typeString);
            workout.setType(type);
        } catch (IllegalArgumentException e) {
            workout.setType(Workout.WorkoutType.CARDIO); // Default
        }
        
        workout.setDuration(Integer.parseInt(durationEdit.getText().toString()));
        workout.setCaloriesBurned(Integer.parseInt(caloriesEdit.getText().toString()));
        workout.setNotes(notesEdit.getText().toString().trim());
        workout.setDate(selectedDate.getTime());
        
        // Set exercises
        List<Workout.Exercise> exercises = exerciseAdapter.getExercises();
        workout.setExercises(exercises);
        
        // Calculate totals based on exercises
        workout.calculateTotals();
        
        // Save to repository
        saveButton.setEnabled(false);
        saveButton.setText(isEditMode ? "Updating..." : "Saving...");
        
        WorkoutRepository.WorkoutCallback callback = new WorkoutRepository.WorkoutCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(AddEditWorkoutActivity.this, 
                        isEditMode ? "Workout updated successfully!" : "Workout saved successfully!", 
                        Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }
            
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AddEditWorkoutActivity.this, 
                        "Error: " + error, Toast.LENGTH_LONG).show();
                    saveButton.setEnabled(true);
                    saveButton.setText(isEditMode ? "Update Workout" : "Save Workout");
                });
            }
        };
        
        if (isEditMode) {
            workoutRepository.updateWorkout(workout, callback);
        } else {
            workoutRepository.saveWorkout(workout, callback);
        }
    }
    
    private boolean validateInputs() {
        boolean isValid = true;
        
        // Clear previous errors
        nameLayout.setError(null);
        durationLayout.setError(null);
        caloriesLayout.setError(null);
        
        // Validate name
        if (TextUtils.isEmpty(nameEdit.getText())) {
            nameLayout.setError("Workout name is required");
            isValid = false;
        }
        
        // Validate duration
        String durationStr = durationEdit.getText().toString();
        if (TextUtils.isEmpty(durationStr)) {
            durationLayout.setError("Duration is required");
            isValid = false;
        } else {
            try {
                int duration = Integer.parseInt(durationStr);
                if (duration <= 0) {
                    durationLayout.setError("Duration must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                durationLayout.setError("Please enter a valid number");
                isValid = false;
            }
        }
        
        // Validate calories
        String caloriesStr = caloriesEdit.getText().toString();
        if (TextUtils.isEmpty(caloriesStr)) {
            caloriesLayout.setError("Calories burned is required");
            isValid = false;
        } else {
            try {
                int calories = Integer.parseInt(caloriesStr);
                if (calories < 0) {
                    caloriesLayout.setError("Calories cannot be negative");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                caloriesLayout.setError("Please enter a valid number");
                isValid = false;
            }
        }
        
        // Validate workout type
        if (TextUtils.isEmpty(typeDropdown.getText())) {
            Toast.makeText(this, "Please select a workout type", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        return isValid;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        // TODO: Add unsaved changes dialog if needed
        super.onBackPressed();
    }
} 