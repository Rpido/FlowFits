package com.example.flowfits.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.flowfits.R;
import com.example.flowfits.models.Habit;
import com.example.flowfits.viewmodels.HabitsViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddEditHabitActivity extends AppCompatActivity {
    
    // UI Components
    private EditText editTextHabitName;
    private EditText editTextDescription;
    private AutoCompleteTextView spinnerCategory;
    private ChipGroup chipGroupFrequency;
    private ChipGroup chipGroupPriority;
    private Chip chipDaily, chipWeekly, chipCustom;
    private Chip chipLowPriority, chipMediumPriority, chipHighPriority;
    private ChipGroup chipGroupDays;
    private LinearLayout layoutCustomDays;
    private EditText editTextTarget;
    private EditText editTextUnit;
    private MaterialSwitch switchReminder;
    private TextView textReminderTime;
    private LinearLayout layoutReminderTime;
    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabSave;
    
    // Data
    private HabitsViewModel viewModel;
    private Habit editingHabit;
    private boolean isEditMode = false;
    private String selectedReminderTime = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_habit);
        
        // Setup ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HabitsViewModel.class);
        
        // Check if editing existing habit
        editingHabit = (Habit) getIntent().getSerializableExtra("habit");
        isEditMode = editingHabit != null;
        
        // Set title
        setTitle(isEditMode ? getString(R.string.habits_edit) : getString(R.string.habits_add));
        
        initializeViews();
        setupSpinners();
        setupFrequencySelection();
        setupReminderTime();
        setupClickListeners();
        observeViewModel();
        
        if (isEditMode) {
            populateFields();
        }
    }
    
    private void initializeViews() {
        editTextHabitName = findViewById(R.id.editTextHabitName);
        editTextDescription = findViewById(R.id.editTextDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        chipGroupFrequency = findViewById(R.id.chipGroupFrequency);
        chipDaily = findViewById(R.id.chipDaily);
        chipWeekly = findViewById(R.id.chipWeekly);
        chipCustom = findViewById(R.id.chipCustom);
        chipGroupDays = findViewById(R.id.chipGroupDays);
        layoutCustomDays = findViewById(R.id.layoutCustomDays);
        editTextTarget = findViewById(R.id.editTextTarget);
        editTextUnit = findViewById(R.id.editTextUnit);
        chipGroupPriority = findViewById(R.id.chipGroupPriority);
        chipLowPriority = findViewById(R.id.chipLowPriority);
        chipMediumPriority = findViewById(R.id.chipMediumPriority);
        chipHighPriority = findViewById(R.id.chipHighPriority);
        switchReminder = findViewById(R.id.switchReminder);
        textReminderTime = findViewById(R.id.textReminderTime);
        layoutReminderTime = findViewById(R.id.layoutReminderTime);
        fabSave = findViewById(R.id.fabSave);
    }
    
    private void setupSpinners() {
        // Category AutoCompleteTextView
        List<String> categories = new ArrayList<>();
        categories.add(getString(R.string.category_health));
        categories.add(getString(R.string.category_fitness));
        categories.add(getString(R.string.category_nutrition));
        categories.add(getString(R.string.category_mindfulness));
        categories.add(getString(R.string.category_productivity));
        categories.add(getString(R.string.category_learning));
        categories.add(getString(R.string.category_social));
        categories.add(getString(R.string.category_creativity));
        categories.add(getString(R.string.category_lifestyle));
        categories.add(getString(R.string.category_other));
        
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setText(categories.get(0), false); // Set default selection
    }
    
    private void setupFrequencySelection() {
        chipGroupFrequency.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipDaily) {
                layoutCustomDays.setVisibility(View.GONE);
            } else if (checkedId == R.id.chipWeekly) {
                layoutCustomDays.setVisibility(View.GONE);
            } else if (checkedId == R.id.chipCustom) {
                layoutCustomDays.setVisibility(View.VISIBLE);
            }
        });
        
        // Set default to Weekly for user-friendly scheduling
        chipWeekly.setChecked(true);
        layoutCustomDays.setVisibility(View.GONE);
        
        // Set default priority to Medium
        chipMediumPriority.setChecked(true);
    }
    
    private void setupReminderTime() {
        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutReminderTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        
        layoutReminderTime.setOnClickListener(v -> showTimePickerDialog());
        textReminderTime.setOnClickListener(v -> showTimePickerDialog());
        
        // Initially hide reminder time
        layoutReminderTime.setVisibility(View.GONE);
    }
    
    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfDay) -> {
                    selectedReminderTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfDay);
                    textReminderTime.setText(selectedReminderTime);
                }, hour, minute, true);
        
        timePickerDialog.show();
    }
    
    private void setupClickListeners() {
        fabSave.setOnClickListener(v -> saveHabit());
        
        // Add back button functionality
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void observeViewModel() {
        viewModel.getIsCreatingHabit().observe(this, isCreating -> {
            if (isCreating != null) {
                fabSave.setEnabled(!isCreating);
                // Add visual feedback for loading state
                if (isCreating) {
                    fabSave.setIcon(getDrawable(android.R.drawable.ic_popup_sync));
                    fabSave.setText("Saving...");
                } else {
                    fabSave.setIcon(getDrawable(R.drawable.ic_current));
                    fabSave.setText("Save Habit");
                }
                
                // Add timeout mechanism - if creating takes too long, reset the state
                if (isCreating) {
                    fabSave.postDelayed(() -> {
                        if (viewModel.getIsCreatingHabit().getValue() == Boolean.TRUE) {
                            Toast.makeText(this, "Operation is taking longer than expected. Please try again.", Toast.LENGTH_LONG).show();
                            viewModel.clearError();
                            fabSave.setEnabled(true);
                            fabSave.setIcon(getDrawable(R.drawable.ic_current));
                            fabSave.setText("Save Habit");
                        }
                    }, 30000); // 30 second timeout
                }
            }
        });
        
        viewModel.getIsUpdatingHabit().observe(this, isUpdating -> {
            if (isUpdating != null) {
                fabSave.setEnabled(!isUpdating);
                // Add visual feedback for loading state
                if (isUpdating) {
                    fabSave.setIcon(getDrawable(android.R.drawable.ic_popup_sync));
                    fabSave.setText("Updating...");
                } else {
                    fabSave.setIcon(getDrawable(R.drawable.ic_current));
                    fabSave.setText("Save Habit");
                }
                
                // Add timeout mechanism - if updating takes too long, reset the state
                if (isUpdating) {
                    fabSave.postDelayed(() -> {
                        if (viewModel.getIsUpdatingHabit().getValue() == Boolean.TRUE) {
                            Toast.makeText(this, "Operation is taking longer than expected. Please try again.", Toast.LENGTH_LONG).show();
                            viewModel.clearError();
                            fabSave.setEnabled(true);
                            fabSave.setIcon(getDrawable(R.drawable.ic_current));
                            fabSave.setText("Save Habit");
                        }
                    }, 30000); // 30 second timeout
                }
            }
        });
        
        viewModel.getSuccessLiveData().observe(this, success -> {
            if (success != null && !success.isEmpty()) {
                Toast.makeText(this, success, Toast.LENGTH_SHORT).show();
                viewModel.clearSuccess(); // Clear the success message
                setResult(RESULT_OK);
                finish();
            }
        });
        
        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
                
                // Reset button state on error
                fabSave.setEnabled(true);
                fabSave.setIcon(getDrawable(R.drawable.ic_current));
                fabSave.setText("Save Habit");
            }
        });
    }
    
    private void populateFields() {
        if (editingHabit == null) return;
        
        // Basic information
        editTextHabitName.setText(editingHabit.getName());
        editTextDescription.setText(editingHabit.getDescription());
        
        // Category
        if (editingHabit.getCategory() != null) {
            String categoryDisplayName = editingHabit.getCategoryDisplayName();
            spinnerCategory.setText(categoryDisplayName, false);
        }
        
        // Frequency
        if (editingHabit.getFrequency() != null) {
            switch (editingHabit.getFrequency()) {
                case DAILY:
                    chipDaily.setChecked(true);
                    layoutCustomDays.setVisibility(View.GONE);
                    break;
                case WEEKLY:
                    chipWeekly.setChecked(true);
                    layoutCustomDays.setVisibility(View.VISIBLE);
                    populateCustomDays();
                    break;
                case CUSTOM:
                    chipCustom.setChecked(true);
                    layoutCustomDays.setVisibility(View.VISIBLE);
                    populateCustomDays();
                    break;
            }
        }
        
        // Target and unit
        if (editingHabit.getTarget() > 0) {
            editTextTarget.setText(String.valueOf((int) editingHabit.getTarget()));
        }
        if (editingHabit.getUnit() != null) {
            editTextUnit.setText(editingHabit.getUnit());
        }
        
        // Priority
        if (editingHabit.getPriority() != null) {
            switch (editingHabit.getPriority()) {
                case LOW:
                    chipLowPriority.setChecked(true);
                    break;
                case MEDIUM:
                    chipMediumPriority.setChecked(true);
                    break;
                case HIGH:
                    chipHighPriority.setChecked(true);
                    break;
            }
        }
        
        // Reminder
        switchReminder.setChecked(editingHabit.isReminderEnabled());
        if (editingHabit.isReminderEnabled() && editingHabit.getReminderTime() != null) {
            selectedReminderTime = editingHabit.getReminderTime();
            textReminderTime.setText(selectedReminderTime);
            layoutReminderTime.setVisibility(View.VISIBLE);
        }
    }
    
    private void populateCustomDays() {
        if (editingHabit == null || editingHabit.getCustomDays() == null) return;
        
        List<String> selectedDays = editingHabit.getCustomDays();
        
        for (int i = 0; i < chipGroupDays.getChildCount(); i++) {
            View child = chipGroupDays.getChildAt(i);
            if (child instanceof Chip) {
                Chip dayChip = (Chip) child;
                String dayTag = (String) dayChip.getTag();
                if (dayTag != null && selectedDays.contains(dayTag)) {
                    dayChip.setChecked(true);
                }
            }
        }
    }
    
    private void saveHabit() {
        if (!validateInput()) {
            return;
        }
        
        try {
            Habit habit = isEditMode ? editingHabit : new Habit();
            
            // Basic information
            habit.setName(editTextHabitName.getText().toString().trim());
            habit.setDescription(editTextDescription.getText().toString().trim());
            
            // Category
            String selectedCategory = spinnerCategory.getText().toString();
            habit.setCategory(getCategoryKey(selectedCategory));
            
            // Frequency
            Habit.HabitFrequency frequency = getSelectedFrequency();
            habit.setFrequency(frequency);
            
            // Custom days (for weekly/custom frequency)
            if (frequency == Habit.HabitFrequency.WEEKLY || frequency == Habit.HabitFrequency.CUSTOM) {
                habit.setCustomDays(getSelectedDays());
            } else {
                habit.setCustomDays(new ArrayList<>());
            }
            
            // Target and unit
            String targetText = editTextTarget.getText().toString().trim();
            if (!TextUtils.isEmpty(targetText)) {
                try {
                    habit.setTarget(Double.parseDouble(targetText));
                } catch (NumberFormatException e) {
                    habit.setTarget(0);
                }
            } else {
                habit.setTarget(0);
            }
            habit.setUnit(editTextUnit.getText().toString().trim());
            
            // Priority
            Habit.HabitPriority priority = getPriorityFromString(null);
            habit.setPriority(priority);
            
            // Reminder
            habit.setReminderEnabled(switchReminder.isChecked());
            if (switchReminder.isChecked() && selectedReminderTime != null) {
                habit.setReminderTime(selectedReminderTime);
            } else {
                habit.setReminderTime(null);
            }
            
            // Ensure habit is active by default
            if (!isEditMode) {
                habit.setActive(true);
            }
            
            // Save habit
            if (isEditMode) {
                viewModel.updateHabit(habit);
            } else {
                viewModel.createHabit(habit);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving habit: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private boolean validateInput() {
        // Validate habit name
        String habitName = editTextHabitName.getText().toString().trim();
        if (TextUtils.isEmpty(habitName)) {
            editTextHabitName.setError(getString(R.string.validation_habit_name_required));
            editTextHabitName.requestFocus();
            return false;
        }
        
        // Validate frequency and days
        Habit.HabitFrequency frequency = getSelectedFrequency();
        if (frequency == null) {
            Toast.makeText(this, getString(R.string.validation_frequency_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if ((frequency == Habit.HabitFrequency.WEEKLY || frequency == Habit.HabitFrequency.CUSTOM) 
                && getSelectedDays().isEmpty()) {
            Toast.makeText(this, getString(R.string.validation_days_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Validate target (if provided)
        String targetText = editTextTarget.getText().toString().trim();
        if (!TextUtils.isEmpty(targetText)) {
            try {
                double target = Double.parseDouble(targetText);
                if (target <= 0) {
                    editTextTarget.setError(getString(R.string.validation_target_invalid));
                    editTextTarget.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                editTextTarget.setError(getString(R.string.validation_target_invalid));
                editTextTarget.requestFocus();
                return false;
            }
        }
        
        // Validate reminder time (if enabled)
        if (switchReminder.isChecked() && selectedReminderTime == null) {
            Toast.makeText(this, getString(R.string.validation_reminder_time_invalid), Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private Habit.HabitFrequency getSelectedFrequency() {
        List<Integer> checkedIds = chipGroupFrequency.getCheckedChipIds();
        if (checkedIds.isEmpty()) return Habit.HabitFrequency.WEEKLY; // Default
        
        int checkedId = checkedIds.get(0);
        if (checkedId == R.id.chipDaily) {
            return Habit.HabitFrequency.DAILY;
        } else if (checkedId == R.id.chipWeekly) {
            return Habit.HabitFrequency.WEEKLY;
        } else if (checkedId == R.id.chipCustom) {
            return Habit.HabitFrequency.CUSTOM;
        }
        return Habit.HabitFrequency.WEEKLY; // Default
    }
    
    private List<String> getSelectedDays() {
        List<String> selectedDays = new ArrayList<>();
        
        for (int i = 0; i < chipGroupDays.getChildCount(); i++) {
            View child = chipGroupDays.getChildAt(i);
            if (child instanceof Chip) {
                Chip dayChip = (Chip) child;
                if (dayChip.isChecked()) {
                    String dayTag = (String) dayChip.getTag();
                    if (dayTag != null) {
                        selectedDays.add(dayTag);
                    }
                }
            }
        }
        
        return selectedDays;
    }
    
    private String getCategoryKey(String categoryDisplayName) {
        if (categoryDisplayName.equals(getString(R.string.category_health))) return "HEALTH";
        if (categoryDisplayName.equals(getString(R.string.category_fitness))) return "FITNESS";
        if (categoryDisplayName.equals(getString(R.string.category_nutrition))) return "NUTRITION";
        if (categoryDisplayName.equals(getString(R.string.category_mindfulness))) return "MINDFULNESS";
        if (categoryDisplayName.equals(getString(R.string.category_productivity))) return "PRODUCTIVITY";
        if (categoryDisplayName.equals(getString(R.string.category_learning))) return "LEARNING";
        if (categoryDisplayName.equals(getString(R.string.category_social))) return "SOCIAL";
        if (categoryDisplayName.equals(getString(R.string.category_creativity))) return "CREATIVITY";
        if (categoryDisplayName.equals(getString(R.string.category_lifestyle))) return "LIFESTYLE";
        return "OTHER";
    }
    
    private Habit.HabitPriority getPriorityFromString(String priorityDisplayName) {
        List<Integer> checkedIds = chipGroupPriority.getCheckedChipIds();
        if (checkedIds.isEmpty()) return Habit.HabitPriority.MEDIUM; // Default
        
        int checkedId = checkedIds.get(0);
        if (checkedId == R.id.chipLowPriority) {
            return Habit.HabitPriority.LOW;
        } else if (checkedId == R.id.chipMediumPriority) {
            return Habit.HabitPriority.MEDIUM;
        } else if (checkedId == R.id.chipHighPriority) {
            return Habit.HabitPriority.HIGH;
        }
        return Habit.HabitPriority.MEDIUM; // Default
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 