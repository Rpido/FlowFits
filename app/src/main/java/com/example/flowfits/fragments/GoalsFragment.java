package com.example.flowfits.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flowfits.R;
import com.example.flowfits.activities.AddEditGoalActivity;
import com.example.flowfits.adapters.GoalsAdapter;
import com.example.flowfits.models.Goal;
import com.example.flowfits.viewmodels.GoalsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;
import android.text.InputType;

public class GoalsFragment extends Fragment implements GoalsAdapter.OnGoalClickListener {

    private TextView titleText;
    private TextView subtitleText;
    private RecyclerView goalsRecyclerView;
    private FloatingActionButton fabAddGoal;
    private View emptyStateLayout;
    private ChipGroup filterChipGroup;
    private MaterialButton createFirstGoalButton;
    
    private GoalsAdapter goalsAdapter;
    private GoalsViewModel goalsViewModel;
    private ActivityResultLauncher<Intent> addEditGoalLauncher;

    public GoalsFragment() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Register activity result launcher
        addEditGoalLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK) {
                    // Goals are now updated automatically in ViewModel, no need to reload
                    System.out.println("DEBUG: GoalsFragment - Goal activity completed successfully");
                    
                    // Smooth scroll to top to show the newly added goal
                    if (goalsRecyclerView != null) {
                        goalsRecyclerView.post(() -> {
                            goalsRecyclerView.smoothScrollToPosition(0);
                        });
                    }
                }
            }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goals, container, false);
        
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupFab();
        setupFilterChips();
        setupEmptyStateButton();
        observeData();
        
        // Explicitly load goals when fragment is created
        loadGoals();
        
        return view;
    }

    private void initViews(View view) {
        titleText = view.findViewById(R.id.tv_goals_title);
        subtitleText = view.findViewById(R.id.tv_goals_subtitle);
        goalsRecyclerView = view.findViewById(R.id.rv_goals);
        fabAddGoal = view.findViewById(R.id.fab_add_goal);
        emptyStateLayout = view.findViewById(R.id.layout_empty_state);
        filterChipGroup = view.findViewById(R.id.chip_group_filters);
        createFirstGoalButton = view.findViewById(R.id.btn_create_first_goal);
    }

    private void setupViewModel() {
        goalsViewModel = new ViewModelProvider(this).get(GoalsViewModel.class);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        goalsRecyclerView.setLayoutManager(layoutManager);
        
        // Performance optimizations for faster scrolling and animations
        goalsRecyclerView.setHasFixedSize(true);
        goalsRecyclerView.setItemViewCacheSize(20);
        goalsRecyclerView.setDrawingCacheEnabled(true);
        goalsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        
        goalsAdapter = new GoalsAdapter();
        goalsAdapter.setOnGoalClickListener(this);
        goalsRecyclerView.setAdapter(goalsAdapter);
    }

    private void setupFab() {
        fabAddGoal.setOnClickListener(v -> openAddGoalActivity());
        
        // Remove debug feature for production
        // Enhanced feedback for user action
        fabAddGoal.setOnLongClickListener(v -> {
            Toast.makeText(getContext(), "Create a new goal to start your journey!", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void setupFilterChips() {
        if (filterChipGroup != null) {
            filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty()) {
                    int checkedId = checkedIds.get(0);
                    filterGoals(checkedId);
                }
            });
        }
    }

    private void setupEmptyStateButton() {
        if (createFirstGoalButton != null) {
            createFirstGoalButton.setOnClickListener(v -> {
                // Add a subtle animation or feedback
                openAddGoalActivity();
            });
        }
    }

    private void openAddGoalActivity() {
        Intent intent = new Intent(getActivity(), AddEditGoalActivity.class);
        intent.putExtra(AddEditGoalActivity.EXTRA_IS_EDIT_MODE, false);
        addEditGoalLauncher.launch(intent);
    }

    private void filterGoals(int chipId) {
        // Filter logic based on selected chip
        if (chipId == R.id.chip_all) {
            goalsAdapter.setFilter(null);
        } else if (chipId == R.id.chip_active) {
            goalsAdapter.setFilter("ACTIVE");
        } else if (chipId == R.id.chip_completed) {
            goalsAdapter.setFilter("COMPLETED");
        }
    }

    private void updateUIBasedOnGoalsCount(int goalsCount) {
        if (goalsCount > 0) {
            // Show filter chips when there are goals
            if (filterChipGroup != null) {
                filterChipGroup.setVisibility(View.VISIBLE);
            }
            // Update subtitle with count
            if (subtitleText != null) {
                String subtitle = goalsCount == 1 ? 
                    "1 goal in progress" : 
                    goalsCount + " goals in progress";
                subtitleText.setText(subtitle);
            }
        } else {
            // Hide filter chips when empty
            if (filterChipGroup != null) {
                filterChipGroup.setVisibility(View.GONE);
            }
            // Reset subtitle
            if (subtitleText != null) {
                subtitleText.setText("Turn your dreams into achievements");
            }
        }
    }

    private void observeData() {
        // Observe goals data
        goalsViewModel.getGoals().observe(getViewLifecycleOwner(), goals -> {
            System.out.println("DEBUG: GoalsFragment - Goals observed, count: " + (goals != null ? goals.size() : 0));
            if (goals != null && !goals.isEmpty()) {
                System.out.println("DEBUG: GoalsFragment - Showing goals list");
                goalsAdapter.setGoals(goals);
                
                // Smooth transition to show list
                if (goalsRecyclerView.getVisibility() != View.VISIBLE) {
                    goalsRecyclerView.setVisibility(View.VISIBLE);
                    emptyStateLayout.setVisibility(View.GONE);
                    
                    // Scroll to top to show newly added goals
                    goalsRecyclerView.smoothScrollToPosition(0);
                }
                
                updateUIBasedOnGoalsCount(goals.size());
            } else {
                System.out.println("DEBUG: GoalsFragment - Showing empty state");
                goalsRecyclerView.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.VISIBLE);
                updateUIBasedOnGoalsCount(0);
            }
        });

        // Observe loading state
        goalsViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            System.out.println("DEBUG: GoalsFragment - Loading state: " + isLoading);
            // TODO: Show/hide loading indicator with shimmer effect
        });

        // Observe error messages
        goalsViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                System.out.println("DEBUG: GoalsFragment - Error: " + errorMessage);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                goalsViewModel.clearMessages();
            }
        });

        // Observe success messages
        goalsViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                System.out.println("DEBUG: GoalsFragment - Success: " + successMessage);
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                goalsViewModel.clearMessages();
            }
        });
    }

    private void loadGoals() {
        System.out.println("DEBUG: GoalsFragment - Loading goals...");
        if (goalsViewModel != null) {
            goalsViewModel.loadGoals();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("DEBUG: GoalsFragment - onResume called");
        // Only load goals if list is empty (first time loading)
        if (goalsViewModel.getGoals().getValue() == null || goalsViewModel.getGoals().getValue().isEmpty()) {
            System.out.println("DEBUG: GoalsFragment - Goals list is empty, loading goals");
            loadGoals();
        } else {
            System.out.println("DEBUG: GoalsFragment - Goals already loaded, skipping reload");
        }
    }

    // GoalsAdapter.OnGoalClickListener implementation
    @Override
    public void onGoalClick(Goal goal) {
        // Open goal for editing
        Intent intent = new Intent(getActivity(), AddEditGoalActivity.class);
        intent.putExtra(AddEditGoalActivity.EXTRA_GOAL, goal);
        intent.putExtra(AddEditGoalActivity.EXTRA_IS_EDIT_MODE, true);
        addEditGoalLauncher.launch(intent);
    }

    @Override
    public void onGoalEdit(Goal goal) {
        // Open edit goal dialog
        Intent intent = new Intent(getActivity(), AddEditGoalActivity.class);
        intent.putExtra(AddEditGoalActivity.EXTRA_GOAL, goal);
        intent.putExtra(AddEditGoalActivity.EXTRA_IS_EDIT_MODE, true);
        addEditGoalLauncher.launch(intent);
    }

    @Override
    public void onGoalUpdateProgress(Goal goal) {
        // Show enhanced progress update dialog
        showProgressUpdateDialog(goal);
    }

    @Override
    public void onGoalDelete(Goal goal) {
        if (goal.getGoalId() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Delete Goal");
            builder.setMessage("Are you sure you want to delete \"" + goal.getTitle() + "\"? This action cannot be undone.");
            builder.setPositiveButton("Delete", (dialog, which) -> {
                goalsViewModel.deleteGoal(goal.getGoalId());
                Toast.makeText(getContext(), "Goal deleted", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                // User cancelled the dialog
            });
            builder.show();
        }
    }
    
    private void showProgressUpdateDialog(Goal goal) {
        // Create an enhanced dialog for updating progress
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update Progress");
        
        // Create a custom view for better UX
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_progress, null);
        
        // Find views in custom dialog
        TextView goalTitle = dialogView.findViewById(R.id.tv_goal_title);
        TextView currentProgress = dialogView.findViewById(R.id.tv_current_progress);
        EditText newValueInput = dialogView.findViewById(R.id.et_new_value);
        TextView unitLabel = dialogView.findViewById(R.id.tv_unit);
        MaterialButton minusOneBtn = dialogView.findViewById(R.id.btn_minus_one);
        MaterialButton plusOneBtn = dialogView.findViewById(R.id.btn_plus_one);
        MaterialButton plusFiveBtn = dialogView.findViewById(R.id.btn_plus_five);
        
        // Set up dialog content
        goalTitle.setText(goal.getTitle());
        currentProgress.setText(String.format("Current: %.1f / %.1f %s", 
            goal.getCurrentValue(), goal.getTargetValue(), goal.getUnit()));
        unitLabel.setText(goal.getUnit());
        newValueInput.setHint("Enter new value");
        newValueInput.setText(String.valueOf(goal.getCurrentValue()));
        
        // Set up quick adjustment buttons
        minusOneBtn.setOnClickListener(v -> {
            try {
                double currentVal = Double.parseDouble(newValueInput.getText().toString());
                double newVal = Math.max(0, currentVal - 1);
                newValueInput.setText(String.valueOf(newVal));
            } catch (NumberFormatException e) {
                newValueInput.setText("0");
            }
        });
        
        plusOneBtn.setOnClickListener(v -> {
            try {
                double currentVal = Double.parseDouble(newValueInput.getText().toString());
                double newVal = currentVal + 1;
                newValueInput.setText(String.valueOf(newVal));
            } catch (NumberFormatException e) {
                newValueInput.setText("1");
            }
        });
        
        plusFiveBtn.setOnClickListener(v -> {
            try {
                double currentVal = Double.parseDouble(newValueInput.getText().toString());
                double newVal = currentVal + 5;
                newValueInput.setText(String.valueOf(newVal));
            } catch (NumberFormatException e) {
                newValueInput.setText("5");
            }
        });
        
        builder.setView(dialogView);
        
        builder.setPositiveButton("Update", (dialog, which) -> {
            String valueStr = newValueInput.getText().toString().trim();
            if (!valueStr.isEmpty()) {
                try {
                    double newValue = Double.parseDouble(valueStr);
                    if (newValue >= 0) {
                        // Update the goal progress
                        goal.setCurrentValue(newValue);
                        goalsViewModel.updateGoal(goal);
                        
                        // Show encouraging message
                        if (newValue >= goal.getTargetValue()) {
                            Toast.makeText(getContext(), "🎉 Congratulations! Goal achieved!", Toast.LENGTH_LONG).show();
                        } else {
                            double progress = (newValue / goal.getTargetValue()) * 100;
                            Toast.makeText(getContext(), 
                                String.format("Progress updated! %.1f%% complete", progress), 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Please enter a positive value", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
} 