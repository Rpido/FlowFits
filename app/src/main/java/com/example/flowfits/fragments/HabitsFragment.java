package com.example.flowfits.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.flowfits.R;
import com.example.flowfits.activities.AddEditHabitActivity;
import com.example.flowfits.adapters.HabitsAdapter;
import com.example.flowfits.models.Habit;
import com.example.flowfits.models.HabitLog;
import com.example.flowfits.viewmodels.HabitsViewModel;
import com.google.android.material.button.MaterialButton;


import java.util.ArrayList;
import java.util.List;

public class HabitsFragment extends Fragment implements HabitsAdapter.OnHabitClickListener {
    
    private static final int REQUEST_ADD_HABIT = 1001;
    private static final int REQUEST_EDIT_HABIT = 1002;
    
    // UI Components
    private RecyclerView recyclerView;
    private HabitsAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;

    private AutoCompleteTextView categorySpinner;
    private TextView totalHabitsText, completedTodayText, completionRateText;
    private LinearLayout btnCreateHabit;
    private MaterialButton btnGetStarted;
    
    // ViewModel
    private HabitsViewModel viewModel;
    
    // Data
    private List<Habit> currentHabits = new ArrayList<>();
    private List<String> completedHabitIds = new ArrayList<>();
    private List<HabitLog> todaysLogs = new ArrayList<>();
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HabitsViewModel.class);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habits, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupCategorySpinner();
        setupClickListeners();
        observeViewModel();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Load initial data
        viewModel.refreshData();
    }
    
    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewHabits);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        totalHabitsText = view.findViewById(R.id.totalHabitsText);
        completedTodayText = view.findViewById(R.id.completedTodayText);
        completionRateText = view.findViewById(R.id.completionRateText);
        btnCreateHabit = view.findViewById(R.id.btnCreateHabit);
        btnGetStarted = view.findViewById(R.id.btnGetStarted);
    }
    
    private void setupRecyclerView() {
        adapter = new HabitsAdapter(getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        // Add swipe-to-delete functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position < currentHabits.size()) {
                    Habit habit = currentHabits.get(position);
                    showDeleteConfirmation(habit);
                    // Reset the item position while showing dialog
                    adapter.notifyItemChanged(position);
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshData();
        });
        
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                R.color.success,
                R.color.warning
        );
    }


    private void setupCategorySpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        categories.add("Health");
        categories.add("Fitness");
        categories.add("Nutrition");
        categories.add("Mindfulness");
        categories.add("Productivity");
        categories.add("Learning");
        categories.add("Creativity");
        categories.add("Social");
        categories.add("Lifestyle");
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        categorySpinner.setAdapter(adapter);
        categorySpinner.setText(categories.get(0), false);
        
        categorySpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = categories.get(position);
            if (selectedCategory.equals("All Categories")) {
                viewModel.setCategory("ALL");
            } else {
                viewModel.setCategory(selectedCategory.toUpperCase());
            }
        });
    }
    
    private void setupClickListeners() {
        btnCreateHabit.setOnClickListener(v -> openAddEditHabitActivity(null));
        btnGetStarted.setOnClickListener(v -> openAddEditHabitActivity(null));

    }
    

    
    private void showQuickCheckInDialog() {
        if (currentHabits.isEmpty()) return;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Quick Check-in");
        builder.setMessage("Mark completed habits for today:");
        
        List<String> habitNames = new ArrayList<>();
        boolean[] checkedItems = new boolean[currentHabits.size()];
        
        for (int i = 0; i < currentHabits.size(); i++) {
            Habit habit = currentHabits.get(i);
            habitNames.add(habit.getName());
            checkedItems[i] = completedHabitIds.contains(habit.getHabitId());
        }
        
        builder.setMultiChoiceItems(habitNames.toArray(new String[0]), checkedItems,
                (dialog, which, isChecked) -> {
                    // Handle individual item changes if needed
                });
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            for (int i = 0; i < checkedItems.length; i++) {
                Habit habit = currentHabits.get(i);
                boolean wasCompleted = completedHabitIds.contains(habit.getHabitId());
                boolean isNowCompleted = checkedItems[i];
                
                if (wasCompleted != isNowCompleted) {
                    onHabitToggleCompletion(habit, isNowCompleted);
                }
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void observeViewModel() {
        viewModel.getAllHabitsLiveData().observe(getViewLifecycleOwner(), habits -> {
            if (habits != null) {
                currentHabits = habits;
                adapter.setHabits(habits);
                updateEmptyState();
                updateAnalytics();
            }
        });
        
        viewModel.getHabitLogsLiveData().observe(getViewLifecycleOwner(), logs -> {
            if (logs != null) {
                updateCompletedHabits(logs);
                updateAnalytics();
                
                // Filter today's logs and pass to adapter and store for dialog use
                todaysLogs.clear();
                for (HabitLog log : logs) {
                    if (log.isFromToday()) {
                        todaysLogs.add(log);
                    }
                }
                adapter.setTodaysLogs(todaysLogs);
            }
        });
        
        viewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                if (isLoading) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showToast("Error: " + error);
                swipeRefreshLayout.setRefreshing(false);
                viewModel.clearError();
            }
        });
    }

    private void updateCompletedHabits(List<HabitLog> logs) {
        completedHabitIds.clear();
        for (HabitLog log : logs) {
            // Only add today's completed habits
            if (log.isCompleted() && log.isFromToday()) {
                completedHabitIds.add(log.getHabitId());
            }
        }
        if (adapter != null) {
            adapter.setCompletedHabits(completedHabitIds);
        }
    }
    
    private void updateEmptyState() {
        boolean isEmpty = currentHabits == null || currentHabits.isEmpty();
        emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
    
    private void updateAnalytics() {
        int totalHabits = currentHabits.size();
        int completedToday = completedHabitIds.size();
        int completionRate = totalHabits > 0 ? (completedToday * 100) / totalHabits : 0;
        
        totalHabitsText.setText(String.valueOf(totalHabits));
        completedTodayText.setText(String.valueOf(completedToday));
        completionRateText.setText(completionRate + "%");
    }
    
    @Override
    public void onHabitClick(Habit habit) {
                showHabitDetails(habit);
    }
    
    @Override
    public void onHabitLongClick(Habit habit) {
        showHabitActionDialog(habit);
    }
    
    @Override
    public void onHabitToggleCompletion(Habit habit, boolean isCompleted) {
        if (isCompleted) {
            // Show progress input dialog for habits with targets
            if (habit.getTarget() > 0) {
                showProgressInputDialog(habit, true);
        } else {
                // Simple completion toggle
                viewModel.logHabitCompletion(habit.getHabitId(), isCompleted);
                showToast("Great job! Habit completed for today 🎉");
            }
        } else {
            // Uncomplete the habit
            viewModel.logHabitCompletion(habit.getHabitId(), false);
            showToast("Habit unmarked for today");
        }
    }
    
    @Override
    public void onHabitEdit(Habit habit) {
        openAddEditHabitActivity(habit);
    }
    
    @Override
    public void onHabitDelete(Habit habit) {
        showDeleteConfirmation(habit);
    }

    @Override
    public void onQuickCompleteHabit(Habit habit) {
        boolean isCurrentlyCompleted = completedHabitIds.contains(habit.getHabitId());
        onHabitToggleCompletion(habit, !isCurrentlyCompleted);
    }

    @Override
    public void onUpdateProgress(Habit habit) {
        // Show the quick update dialog here - for now just show simple completion
        showProgressInputDialog(habit, true);
    }

    @Override
    public void onMoreOptions(Habit habit) {
        showHabitActionDialog(habit);
    }
    
    private void openAddEditHabitActivity(Habit habit) {
        Intent intent = new Intent(getContext(), AddEditHabitActivity.class);
        if (habit != null) {
            intent.putExtra("habit", habit);
            startActivityForResult(intent, REQUEST_EDIT_HABIT);
        } else {
            startActivityForResult(intent, REQUEST_ADD_HABIT);
        }
    }
    
    private void showHabitDetails(Habit habit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(habit.getName());
            
            StringBuilder details = new StringBuilder();
        details.append("Category: ").append(habit.getCategory()).append("\n");
        details.append("Frequency: ").append(habit.getDisplayFrequency()).append("\n");
        details.append("Priority: ").append(habit.getPriority().getDisplayName()).append("\n");
        details.append("Status: ").append(habit.isActive() ? "Active" : "Paused").append("\n");
        
        if (habit.getDescription() != null && !habit.getDescription().isEmpty()) {
            details.append("Description: ").append(habit.getDescription()).append("\n");
        }
        
            if (habit.getTarget() > 0) {
            details.append("Target: ").append(habit.getTarget()).append(" ").append(habit.getUnit()).append("\n");
            }
            
            if (habit.getStreakData() != null) {
            details.append("Current Streak: ").append(habit.getStreakData().getCurrentStreak()).append(" days\n");
            details.append("Longest Streak: ").append(habit.getStreakData().getLongestStreak()).append(" days\n");
                }
        
        if (habit.getReminderTime() != null && !habit.getReminderTime().isEmpty()) {
            details.append("Reminder: ").append(habit.getReminderTime());
        }
            
            builder.setMessage(details.toString());
        builder.setPositiveButton("Edit", (dialog, which) -> openAddEditHabitActivity(habit));
            builder.setNegativeButton("Close", null);
            builder.show();
    }
    
    private void showHabitActionDialog(Habit habit) {
        String[] actions = {"Edit", "Toggle Status", "View Details", "Delete"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(habit.getName());
        builder.setItems(actions, (dialog, which) -> {
            switch (which) {
                case 0: // Edit
                    openAddEditHabitActivity(habit);
                    break;
                case 1: // Toggle Status
                    onHabitStatusToggle(habit);
                    break;
                case 2: // View Details
                    showHabitDetails(habit);
                    break;
                case 3: // Delete
                    showDeleteConfirmation(habit);
                    break;
            }
        });
        builder.show();
    }

    private void showDeleteConfirmation(Habit habit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Habit");
        builder.setMessage("Are you sure you want to delete \"" + habit.getName() + "\"?\n\nThis action cannot be undone and will remove all associated progress data.");
        builder.setIcon(R.drawable.ic_delete);
        
            builder.setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteHabit(habit.getHabitId());
            });
        
            builder.setNegativeButton("Cancel", null);
            builder.show();
    }
    
    private void showProgressInputDialog(Habit habit, boolean isCompleted) {
        // For habits without targets, use simple completion toggle
        if (habit.getTarget() <= 0) {
            viewModel.logHabitCompletion(habit.getHabitId(), isCompleted);
            showToast(isCompleted ? "Great job! Habit completed for today 🎉" : "Habit unmarked for today");
            return;
        }
        
        // Create custom dialog for target-based habits
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_quick_habit_update, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Get current progress for today
        double currentProgress = 0.0;
        for (HabitLog log : todaysLogs) {
            if (habit.getHabitId().equals(log.getHabitId())) {
                currentProgress = log.getProgressValue();
                break;
            }
        }

        // Initialize dialog views
        TextView habitName = dialogView.findViewById(R.id.habitName);
        TextView habitTarget = dialogView.findViewById(R.id.habitTarget);
        ImageView habitIcon = dialogView.findViewById(R.id.habitIcon);
        TextView currentProgressText = dialogView.findViewById(R.id.currentProgressText);
        TextView progressPercentage = dialogView.findViewById(R.id.progressPercentage);
        ProgressBar progressIndicator = dialogView.findViewById(R.id.progressIndicator);
        
        EditText progressInput = dialogView.findViewById(R.id.progressInput);
        TextView unitLabel = dialogView.findViewById(R.id.unitLabel);
        MaterialButton decreaseButton = dialogView.findViewById(R.id.decreaseButton);
        MaterialButton increaseButton = dialogView.findViewById(R.id.increaseButton);
        
        MaterialButton quickAdd1 = dialogView.findViewById(R.id.quickAdd1);
        MaterialButton quickAdd2 = dialogView.findViewById(R.id.quickAdd2);
        MaterialButton quickAdd5 = dialogView.findViewById(R.id.quickAdd5);
        
        MaterialButton markCompleteButton = dialogView.findViewById(R.id.markCompleteButton);
        MaterialButton markIncompleteButton = dialogView.findViewById(R.id.markIncompleteButton);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        MaterialButton saveButton = dialogView.findViewById(R.id.saveButton);

        LinearLayout progressInputSection = dialogView.findViewById(R.id.progressInputSection);
        LinearLayout quickCompleteSection = dialogView.findViewById(R.id.quickCompleteSection);

        // Set up basic info
        habitName.setText(habit.getName());
        habitTarget.setText(String.format("Target: %.0f %s today", habit.getTarget(), habit.getUnit()));
        unitLabel.setText(habit.getUnit());
        progressInput.setText(String.valueOf((int) currentProgress));

        // Set category icon
        Habit.HabitCategory categoryEnum = habit.getCategoryEnum();
        if (categoryEnum != null) {
            switch (categoryEnum) {
                case HEALTH:
                    habitIcon.setImageResource(R.drawable.ic_health);
                    break;
                case FITNESS:
                    habitIcon.setImageResource(R.drawable.ic_fitness);
                    break;
                case NUTRITION:
                    habitIcon.setImageResource(R.drawable.ic_nutrition);
                    break;
                case MINDFULNESS:
                    habitIcon.setImageResource(R.drawable.ic_mindfulness);
                    break;
                case PRODUCTIVITY:
                    habitIcon.setImageResource(R.drawable.ic_productivity);
                    break;
                case LEARNING:
                    habitIcon.setImageResource(R.drawable.ic_learning);
                    break;
                case SOCIAL:
                    habitIcon.setImageResource(R.drawable.ic_social);
                    break;
                case CREATIVITY:
                    habitIcon.setImageResource(R.drawable.ic_creativity);
                    break;
                case LIFESTYLE:
                    habitIcon.setImageResource(R.drawable.ic_lifestyle);
                    break;
                default:
                    habitIcon.setImageResource(R.drawable.ic_category);
                    break;
            }
        } else {
            habitIcon.setImageResource(R.drawable.ic_category);
        }

        // Show appropriate sections
        progressInputSection.setVisibility(View.VISIBLE);
        quickCompleteSection.setVisibility(View.VISIBLE);

        // Update progress display
        Runnable updateProgress = () -> {
            try {
                double progress = Double.parseDouble(progressInput.getText().toString());
                double percentage = Math.min(100.0, (progress / habit.getTarget()) * 100.0);
                
                currentProgressText.setText(String.format("%.0f / %.0f %s", progress, habit.getTarget(), habit.getUnit()));
                progressPercentage.setText(String.format("%.0f%%", percentage));
                progressIndicator.setProgress((int) percentage);
            } catch (NumberFormatException e) {
                currentProgressText.setText(String.format("0 / %.0f %s", habit.getTarget(), habit.getUnit()));
                progressPercentage.setText("0%");
                progressIndicator.setProgress(0);
            }
        };

        updateProgress.run();

        // Set up input controls
        decreaseButton.setOnClickListener(v -> {
            try {
                double current = Double.parseDouble(progressInput.getText().toString());
                if (current > 0) {
                    progressInput.setText(String.valueOf((int) Math.max(0, current - 1)));
                    updateProgress.run();
                }
            } catch (NumberFormatException e) {
                progressInput.setText("0");
            }
        });

        increaseButton.setOnClickListener(v -> {
            try {
                double current = Double.parseDouble(progressInput.getText().toString());
                double target = habit.getTarget();
                if (current < target) {
                    progressInput.setText(String.valueOf((int) Math.min(target, current + 1)));
                    updateProgress.run();
                }
            } catch (NumberFormatException e) {
                progressInput.setText("1");
            }
        });

        // Quick add buttons
        quickAdd1.setOnClickListener(v -> {
            try {
                double current = Double.parseDouble(progressInput.getText().toString());
                double target = habit.getTarget();
                double newValue = Math.min(target, current + 1);
                progressInput.setText(String.valueOf((int) newValue));
                updateProgress.run();
            } catch (NumberFormatException e) {
                progressInput.setText("1");
            }
        });

        quickAdd2.setOnClickListener(v -> {
            try {
                double current = Double.parseDouble(progressInput.getText().toString());
                double target = habit.getTarget();
                double newValue = Math.min(target, current + 2);
                progressInput.setText(String.valueOf((int) newValue));
                updateProgress.run();
            } catch (NumberFormatException e) {
                progressInput.setText("2");
            }
        });

        quickAdd5.setOnClickListener(v -> {
            try {
                double current = Double.parseDouble(progressInput.getText().toString());
                double target = habit.getTarget();
                double newValue = Math.min(target, current + 5);
                progressInput.setText(String.valueOf((int) newValue));
                updateProgress.run();
            } catch (NumberFormatException e) {
                progressInput.setText("5");
            }
        });

        // Progress input change listener
        progressInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateProgress.run();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                try {
                    double value = Double.parseDouble(s.toString());
                    double target = habit.getTarget();
                    
                    // Validate the input to not exceed target
                    if (value > target) {
                        // Remove the listener temporarily to avoid infinite loop
                        progressInput.removeTextChangedListener(this);
                        progressInput.setText(String.valueOf((int) target));
                        progressInput.setSelection(progressInput.getText().length());
                        // Re-add the listener
                        progressInput.addTextChangedListener(this);
                        showToast("Cannot exceed target of " + (int) target + " " + habit.getUnit());
                    } else if (value < 0) {
                        // Don't allow negative values
                        progressInput.removeTextChangedListener(this);
                        progressInput.setText("0");
                        progressInput.setSelection(progressInput.getText().length());
                        progressInput.addTextChangedListener(this);
                        showToast("Value cannot be negative");
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }
        });

        // Quick complete buttons
        markCompleteButton.setOnClickListener(v -> {
            progressInput.setText(String.valueOf((int) habit.getTarget()));
            updateProgress.run();
            
            // Auto-save when marking complete
                viewModel.logHabitCompletion(habit.getHabitId(), true, habit.getTarget(), habit.getTarget(), habit.getUnit());
            showToast("Excellent! Goal achieved! 🎯");
            dialog.dismiss();
        });

        markIncompleteButton.setOnClickListener(v -> {
            viewModel.logHabitCompletion(habit.getHabitId(), false);
            showToast("Habit marked as skipped for today");
            dialog.dismiss();
        });

        // Action buttons
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            try {
                double progress = Double.parseDouble(progressInput.getText().toString());
                double target = habit.getTarget();
                boolean isComplete = progress >= target;
                
                viewModel.logHabitCompletion(habit.getHabitId(), isComplete, progress, target, habit.getUnit());
                
                if (isComplete) {
                    showToast("Excellent! Goal achieved! 🎯");
                } else if (progress > 0) {
                    showToast(String.format("Progress saved: %.0f %s", progress, habit.getUnit()));
                } else {
                    showToast("Progress reset to 0");
                }
                
                dialog.dismiss();
            } catch (NumberFormatException e) {
                showToast("Please enter a valid number");
            }
        });

        dialog.show();
    }
    
    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && 
            (requestCode == REQUEST_ADD_HABIT || requestCode == REQUEST_EDIT_HABIT)) {
            viewModel.refreshData();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        viewModel.refreshData();
    }

    private void onHabitStatusToggle(Habit habit) {
        String newStatus = habit.isActive() ? "Paused" : "Active";
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Toggle Habit Status");
        builder.setMessage("Change \"" + habit.getName() + "\" status to " + newStatus + "?");
        
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            habit.setActive(!habit.isActive());
            viewModel.updateHabit(habit);
            showToast("Habit status updated to " + newStatus);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
} 