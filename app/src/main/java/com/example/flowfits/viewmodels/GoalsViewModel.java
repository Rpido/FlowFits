package com.example.flowfits.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.flowfits.models.Goal;
import com.example.flowfits.repositories.GoalRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GoalsViewModel extends ViewModel {
    
    private GoalRepository goalRepository;
    private MutableLiveData<List<Goal>> goals;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<String> successMessage;

    public GoalsViewModel() {
        // Initialize repository
        goalRepository = new GoalRepository();
        
        // Initialize LiveData
        goals = new MutableLiveData<>(new ArrayList<>());
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        successMessage = new MutableLiveData<>();
        
        // Load goals from repository
        loadGoals();
    }

    public LiveData<List<Goal>> getGoals() {
        return goals;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public void loadGoals() {
        System.out.println("DEBUG: GoalsViewModel - loadGoals called");
        isLoading.setValue(true);
        
        // Load from repository
        goalRepository.getAllGoals(new GoalRepository.GoalsListCallback() {
            @Override
            public void onSuccess(List<Goal> goalsList) {
                System.out.println("DEBUG: GoalsViewModel - Goals loaded successfully, count: " + (goalsList != null ? goalsList.size() : 0));
                if (goalsList != null) {
                    for (Goal goal : goalsList) {
                        System.out.println("DEBUG: GoalsViewModel - Goal: " + goal.getTitle() + " (ID: " + goal.getGoalId() + ")");
                    }
                }
                goals.postValue(goalsList);
                isLoading.postValue(false);
            }
            
            @Override
            public void onFailure(String error) {
                System.out.println("DEBUG: GoalsViewModel - Failed to load goals: " + error);
                // Don't load sample data, just show empty list
                goals.postValue(new ArrayList<>());
                isLoading.postValue(false);
                errorMessage.postValue("Failed to load goals: " + error);
            }
        });
    }

    public void addGoal(Goal goal) {
        isLoading.setValue(true);
        
        goalRepository.addGoal(goal, new GoalRepository.GoalCallback() {
            @Override
            public void onSuccess() {
                // Optimized: Add the new goal to the top of the existing list immediately
                List<Goal> currentGoals = goals.getValue();
                if (currentGoals == null) {
                    currentGoals = new ArrayList<>();
                }
                // Add new goal at the beginning for immediate visibility
                currentGoals.add(0, goal);
                goals.postValue(currentGoals);
                isLoading.postValue(false);
                successMessage.postValue("Goal created successfully");
            }
            
            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue("Failed to add goal: " + error);
            }
        });
    }

    public void updateGoal(Goal goal) {
        isLoading.setValue(true);
        
        goalRepository.updateGoal(goal, new GoalRepository.GoalCallback() {
            @Override
            public void onSuccess() {
                // Optimized: Update the goal in the existing list immediately
                List<Goal> currentGoals = goals.getValue();
                if (currentGoals != null) {
                    for (int i = 0; i < currentGoals.size(); i++) {
                        if (currentGoals.get(i).getGoalId().equals(goal.getGoalId())) {
                            currentGoals.set(i, goal);
                            break;
                        }
                    }
                    goals.postValue(currentGoals);
                }
                isLoading.postValue(false);
                successMessage.postValue("Goal updated successfully");
            }
            
            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue("Failed to update goal: " + error);
            }
        });
    }

    public void deleteGoal(String goalId) {
        isLoading.setValue(true);
        
        goalRepository.deleteGoal(goalId, new GoalRepository.OnGoalDeletedListener() {
            @Override
            public void onSuccess() {
                // Remove from local list
                List<Goal> currentGoals = goals.getValue();
                if (currentGoals != null) {
                    currentGoals.removeIf(goal -> goal.getGoalId().equals(goalId));
                    goals.postValue(currentGoals);
                }
                isLoading.postValue(false);
                successMessage.postValue("Goal deleted successfully");
            }
            
            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue("Failed to delete goal: " + error);
            }
        });
    }

    public void clearMessages() {
        errorMessage.setValue(null);
        successMessage.setValue(null);
    }

    public void clearAllGoals() {
        isLoading.setValue(true);
        
        goalRepository.clearAllGoals(new GoalRepository.GoalCallback() {
            @Override
            public void onSuccess() {
                // Clear local list and reload
                goals.postValue(new ArrayList<>());
                loadGoals();
                successMessage.postValue("All goals cleared successfully");
            }
            
            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue("Failed to clear goals: " + error);
            }
        });
    }

    public List<Goal> getActiveGoals() {
        List<Goal> currentGoals = goals.getValue();
        if (currentGoals == null) return new ArrayList<>();
        
        List<Goal> activeGoals = new ArrayList<>();
        for (Goal goal : currentGoals) {
            if (goal.getStatus() == Goal.GoalStatus.ACTIVE) {
                activeGoals.add(goal);
            }
        }
        return activeGoals;
    }
} 