package com.example.flowfits.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.flowfits.R;
import com.example.flowfits.models.Goal;
import com.example.flowfits.repositories.GoalRepository;
import com.example.flowfits.utils.Constants;
import com.example.flowfits.viewmodels.GoalsViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditGoalActivity extends AppCompatActivity {
    
    public static final String EXTRA_GOAL = "extra_goal";
    public static final String EXTRA_IS_EDIT_MODE = "extra_is_edit_mode";
    
    private TextInputLayout titleLayout, descriptionLayout, targetValueLayout, currentValueLayout, deadlineLayout;
    private TextInputEditText titleEdit, descriptionEdit, targetValueEdit, currentValueEdit, deadlineEdit;
    private AutoCompleteTextView categoryDropdown, typeDropdown, unitDropdown, priorityDropdown;
    private MaterialButton saveButton, cancelButton;
    private MaterialButton btn30Days, btn90Days, btn1Year;
    private Toolbar toolbar;
    private CardView templatesCard;
    private ChipGroup templatesChipGroup;
    
    private Goal editingGoal;
    private boolean isEditMode = false;
    private GoalRepository goalRepository;
    private GoalsViewModel goalsViewModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private Calendar selectedDate = Calendar.getInstance();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_goal);
        
        initializeViews();
        setupToolbar();
        setupDropdowns();
        setupDatePicker();
        setupTemplates();
        setupQuickDeadlineButtons();
        handleIntent();
        setupClickListeners();
        
        goalRepository = new GoalRepository();
        goalsViewModel = new ViewModelProvider(this).get(GoalsViewModel.class);
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        
        titleLayout = findViewById(R.id.titleLayout);
        descriptionLayout = findViewById(R.id.descriptionLayout);
        targetValueLayout = findViewById(R.id.targetValueLayout);
        currentValueLayout = findViewById(R.id.currentValueLayout);
        deadlineLayout = findViewById(R.id.deadlineLayout);
        
        titleEdit = findViewById(R.id.titleEdit);
        descriptionEdit = findViewById(R.id.descriptionEdit);
        targetValueEdit = findViewById(R.id.targetValueEdit);
        currentValueEdit = findViewById(R.id.currentValueEdit);
        deadlineEdit = findViewById(R.id.deadlineEdit);
        
        categoryDropdown = findViewById(R.id.categoryDropdown);
        typeDropdown = findViewById(R.id.typeDropdown);
        unitDropdown = findViewById(R.id.unitDropdown);
        priorityDropdown = findViewById(R.id.priorityDropdown);
        
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        
        btn30Days = findViewById(R.id.btn_30_days);
        btn90Days = findViewById(R.id.btn_90_days);
        btn1Year = findViewById(R.id.btn_1_year);
        
        templatesCard = findViewById(R.id.card_templates);
        templatesChipGroup = findViewById(R.id.chip_group_templates);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
    
    private void setupDropdowns() {
        // Category dropdown
        String[] categories = Constants.GOAL_CATEGORIES;
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, categories);
        categoryDropdown.setAdapter(categoryAdapter);
        
        // Type dropdown
        String[] types = {"Increase", "Decrease", "Maintain", "Achieve"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, types);
        typeDropdown.setAdapter(typeAdapter);
        
        // Priority dropdown
        String[] priorities = {"Low", "Medium", "High", "Critical"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, priorities);
        priorityDropdown.setAdapter(priorityAdapter);
        priorityDropdown.setText("Medium", false); // Set default
        
        // Unit dropdown
        String[] units = {"kg", "lbs", "minutes", "hours", "reps", "sets", "miles", "km", "calories", "days", "books", "dollars"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, units);
        unitDropdown.setAdapter(unitAdapter);
    }
    
    private void setupDatePicker() {
        deadlineEdit.setOnClickListener(v -> showDatePicker());
        deadlineEdit.setFocusable(false);
        deadlineEdit.setClickable(true);
    }
    
    private void setupTemplates() {
        if (templatesChipGroup != null && templatesCard != null) {
            // Show templates only for new goals
            if (!isEditMode) {
                templatesCard.setVisibility(View.VISIBLE);
            }
            
            templatesChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty()) {
                    int chipId = checkedIds.get(0);
                    applyTemplate(chipId);
                    if (templatesCard != null) {
                        templatesCard.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
    
    private void setupQuickDeadlineButtons() {
        if (btn30Days != null) {
            btn30Days.setOnClickListener(v -> setDeadlineFromToday(30));
        }
        if (btn90Days != null) {
            btn90Days.setOnClickListener(v -> setDeadlineFromToday(90));
        }
        if (btn1Year != null) {
            btn1Year.setOnClickListener(v -> setDeadlineFromToday(365));
        }
    }
    
    private void setDeadlineFromToday(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        selectedDate = calendar;
        deadlineEdit.setText(dateFormat.format(calendar.getTime()));
        
        // Clear any previous error
        deadlineLayout.setError(null);
    }
    
    private void applyTemplate(int chipId) {
        if (chipId == R.id.chip_weight_loss) {
            titleEdit.setText("Lose 10kg");
            descriptionEdit.setText("Achieve a healthier weight through diet and exercise");
            targetValueEdit.setText("10");
            currentValueEdit.setText("0");
            categoryDropdown.setText("Weight Loss", false);
            typeDropdown.setText("Decrease", false);
            unitDropdown.setText("kg", false);
            priorityDropdown.setText("High", false);
            setDeadlineFromToday(90);
        } else if (chipId == R.id.chip_fitness) {
            titleEdit.setText("Exercise 3 times per week");
            descriptionEdit.setText("Build a consistent workout routine");
            targetValueEdit.setText("156"); // 3 times * 52 weeks
            currentValueEdit.setText("0");
            categoryDropdown.setText("Cardio", false);
            typeDropdown.setText("Achieve", false);
            unitDropdown.setText("reps", false);
            priorityDropdown.setText("High", false);
            setDeadlineFromToday(365);
        } else if (chipId == R.id.chip_reading) {
            titleEdit.setText("Read 12 books");
            descriptionEdit.setText("Expand knowledge and develop reading habit");
            targetValueEdit.setText("12");
            currentValueEdit.setText("0");
            categoryDropdown.setText("Mindfulness", false);
            typeDropdown.setText("Achieve", false);
            unitDropdown.setText("books", false);
            priorityDropdown.setText("Medium", false);
            setDeadlineFromToday(365);
        } else if (chipId == R.id.chip_savings) {
            titleEdit.setText("Save $5000");
            descriptionEdit.setText("Build emergency fund for financial security");
            targetValueEdit.setText("5000");
            currentValueEdit.setText("0");
            categoryDropdown.setText("Sleep", false);
            typeDropdown.setText("Achieve", false);
            unitDropdown.setText("dollars", false);
            priorityDropdown.setText("High", false);
            setDeadlineFromToday(365);
        }
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                deadlineEdit.setText(dateFormat.format(selectedDate.getTime()));
                deadlineLayout.setError(null); // Clear any error
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
    
    private void handleIntent() {
        editingGoal = (Goal) getIntent().getSerializableExtra(EXTRA_GOAL);
        isEditMode = getIntent().getBooleanExtra(EXTRA_IS_EDIT_MODE, false);
        
        if (isEditMode && editingGoal != null) {
            setTitle("Edit Goal");
            toolbar.setTitle("Edit Goal");
            saveButton.setText("Update Goal");
            try {
                saveButton.setIcon(getDrawable(R.drawable.ic_edit));
            } catch (Exception e) {
                // Icon not found, continue without icon
            }
            if (templatesCard != null) {
                templatesCard.setVisibility(View.GONE); // Hide templates for editing
            }
            populateFields();
        } else {
            setTitle("Add New Goal");
            toolbar.setTitle("Create Your Goal");
            saveButton.setText("Create Goal");
            try {
                saveButton.setIcon(getDrawable(R.drawable.ic_add));
            } catch (Exception e) {
                // Icon not found, continue without icon
            }
            // Set default deadline to 30 days from now
            setDeadlineFromToday(30);
            // Show templates for new goals
            if (templatesCard != null) {
                templatesCard.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void populateFields() {
        titleEdit.setText(editingGoal.getTitle());
        descriptionEdit.setText(editingGoal.getDescription());
        targetValueEdit.setText(String.valueOf(editingGoal.getTargetValue()));
        currentValueEdit.setText(String.valueOf(editingGoal.getCurrentValue()));
        categoryDropdown.setText(editingGoal.getCategory(), false);
        typeDropdown.setText(editingGoal.getType(), false);
        unitDropdown.setText(editingGoal.getUnit(), false);
        
        // Set priority
        if (editingGoal.getPriority() != null) {
            priorityDropdown.setText(editingGoal.getPriority().getDisplayName(), false);
        }
        
        try {
            Date deadline = dateFormat.parse(editingGoal.getDeadline());
            if (deadline != null) {
                selectedDate.setTime(deadline);
                deadlineEdit.setText(editingGoal.getDeadline());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    
    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveGoal());
        cancelButton.setOnClickListener(v -> {
            showUnsavedChangesDialog();
        });
    }
    
    private void showUnsavedChangesDialog() {
        // Simple check for unsaved changes
        boolean hasChanges = !TextUtils.isEmpty(titleEdit.getText()) || 
                           !TextUtils.isEmpty(descriptionEdit.getText()) ||
                           !TextUtils.isEmpty(targetValueEdit.getText());
        
        if (hasChanges && !isEditMode) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Discard changes?")
                .setMessage("You have unsaved changes. Are you sure you want to exit?")
                .setPositiveButton("Discard", (dialog, which) -> finish())
                .setNegativeButton("Keep editing", null)
                .show();
        } else {
            finish();
        }
    }
    
    private void saveGoal() {
        if (!validateInputs()) {
            return;
        }
        
        // Create or update goal
        Goal goal = isEditMode ? editingGoal : new Goal();
        
        goal.setTitle(titleEdit.getText().toString().trim());
        goal.setDescription(descriptionEdit.getText().toString().trim());
        goal.setTargetValue(Double.parseDouble(targetValueEdit.getText().toString()));
        goal.setCurrentValue(Double.parseDouble(currentValueEdit.getText().toString()));
        goal.setCategory(categoryDropdown.getText().toString());
        goal.setType(typeDropdown.getText().toString());
        goal.setUnit(unitDropdown.getText().toString());
        goal.setDeadline(deadlineEdit.getText().toString());
        
        // Set priority
        String priorityStr = priorityDropdown.getText().toString().toUpperCase();
        goal.setPriorityString(priorityStr);
        
        if (!isEditMode) {
            goal.setStatusEnum(Goal.GoalStatus.ACTIVE);
            goal.setCreatedAtDate(new Date());
            goal.setArchived(false);
            goal.setStreakDays(0);
            // Initialize empty tags list for new goals
            goal.setTags(new ArrayList<>());
        }
        
        goal.setUpdatedAtDate(new Date());
        
        // Disable save button and show progress
        saveButton.setEnabled(false);
        saveButton.setText(isEditMode ? "Updating..." : "Creating...");
        
        // Add visual feedback for better UX
        saveButton.setIconResource(android.R.drawable.ic_popup_sync);
        cancelButton.setEnabled(false);
        
        // Use ViewModel for better integration and faster UI updates
        if (isEditMode) {
            goalsViewModel.updateGoal(goal);
        } else {
            goalsViewModel.addGoal(goal);
        }
        
        // Observe the result
        goalsViewModel.getSuccessMessage().observe(this, successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                runOnUiThread(() -> {
                    // Reset button state
                    saveButton.setEnabled(true);
                    saveButton.setText(isEditMode ? "Update Goal" : "Create Goal");
                    try {
                        saveButton.setIcon(getDrawable(isEditMode ? R.drawable.ic_edit : R.drawable.ic_add));
                    } catch (Exception e) {
                        saveButton.setIcon(null);
                    }
                    cancelButton.setEnabled(true);
                    
                    Toast.makeText(AddEditGoalActivity.this, successMessage + " 🎯", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
                goalsViewModel.clearMessages();
            }
        });
        
        goalsViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                runOnUiThread(() -> {
                    Toast.makeText(AddEditGoalActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    // Reset button state on error
                    saveButton.setEnabled(true);
                    saveButton.setText(isEditMode ? "Update Goal" : "Create Goal");
                    try {
                        saveButton.setIcon(getDrawable(isEditMode ? R.drawable.ic_edit : R.drawable.ic_add));
                    } catch (Exception e) {
                        saveButton.setIcon(null);
                    }
                    cancelButton.setEnabled(true);
                });
                goalsViewModel.clearMessages();
            }
        });
    }
    
    private boolean validateInputs() {
        boolean isValid = true;
        
        // Clear previous errors
        titleLayout.setError(null);
        targetValueLayout.setError(null);
        currentValueLayout.setError(null);
        deadlineLayout.setError(null);
        
        // Validate title
        if (TextUtils.isEmpty(titleEdit.getText())) {
            titleLayout.setError("Please enter a goal title");
            titleLayout.requestFocus();
            isValid = false;
        } else if (titleEdit.getText().toString().trim().length() < 3) {
            titleLayout.setError("Title must be at least 3 characters");
            titleLayout.requestFocus();
            isValid = false;
        }
        
        // Description is now optional, no validation needed
        
        // Validate target value
        String targetValueStr = targetValueEdit.getText().toString();
        if (TextUtils.isEmpty(targetValueStr)) {
            targetValueLayout.setError("Target value is required");
            if (isValid) targetValueLayout.requestFocus();
            isValid = false;
        } else {
            try {
                double targetValue = Double.parseDouble(targetValueStr);
                if (targetValue <= 0) {
                    targetValueLayout.setError("Target must be greater than 0");
                    if (isValid) targetValueLayout.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                targetValueLayout.setError("Please enter a valid number");
                if (isValid) targetValueLayout.requestFocus();
                isValid = false;
            }
        }
        
        // Validate current value
        String currentValueStr = currentValueEdit.getText().toString();
        if (TextUtils.isEmpty(currentValueStr)) {
            currentValueLayout.setError("Starting value is required");
            if (isValid) currentValueLayout.requestFocus();
            isValid = false;
        } else {
            try {
                double currentValue = Double.parseDouble(currentValueStr);
                if (currentValue < 0) {
                    currentValueLayout.setError("Value cannot be negative");
                    if (isValid) currentValueLayout.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                currentValueLayout.setError("Please enter a valid number");
                if (isValid) currentValueLayout.requestFocus();
                isValid = false;
            }
        }
        
        // Validate dropdowns with better messages
        if (TextUtils.isEmpty(categoryDropdown.getText())) {
            Toast.makeText(this, "Please select a category for your goal", Toast.LENGTH_SHORT).show();
            categoryDropdown.requestFocus();
            isValid = false;
        }
        
        if (TextUtils.isEmpty(typeDropdown.getText())) {
            Toast.makeText(this, "Please select how you want to track progress", Toast.LENGTH_SHORT).show();
            if (isValid) typeDropdown.requestFocus();
            isValid = false;
        }
        
        if (TextUtils.isEmpty(unitDropdown.getText())) {
            Toast.makeText(this, "Please select a unit of measurement", Toast.LENGTH_SHORT).show();
            if (isValid) unitDropdown.requestFocus();
            isValid = false;
        }
        
        // Validate deadline
        if (TextUtils.isEmpty(deadlineEdit.getText())) {
            deadlineLayout.setError("Please set a deadline");
            if (isValid) deadlineLayout.requestFocus();
            isValid = false;
        }
        
        return isValid;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            showUnsavedChangesDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        showUnsavedChangesDialog();
    }
} 