package com.example.flowfits.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flowfits.models.Habit;
import com.example.flowfits.models.HabitLog;
import com.example.flowfits.repositories.HabitRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HabitsViewModel extends ViewModel {
    private final HabitRepository habitRepository;
    
    // LiveData for UI observation
    private final MutableLiveData<List<Habit>> allHabitsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Habit>> filteredHabitsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Habit>> todaysHabitsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<HabitLog>> habitLogsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> successLiveData = new MutableLiveData<>();
    
    // UI state tracking
    private final MutableLiveData<Boolean> isCreatingHabit = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUpdatingHabit = new MutableLiveData<>();
    private final MutableLiveData<String> selectedFilter = new MutableLiveData<>();
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>();
    
    // Store original unfiltered data
    private List<Habit> originalHabits = new ArrayList<>();
    
    public HabitsViewModel() {
        habitRepository = new HabitRepository();
        loadingLiveData.setValue(false);
        isCreatingHabit.setValue(false);
        isUpdatingHabit.setValue(false);
        selectedFilter.setValue("ALL");
        selectedCategory.setValue("ALL");
        
        // Initialize repository observers
        setupRepositoryObservers();
        
        // Initialize with empty filtered list
        filteredHabitsLiveData.setValue(new ArrayList<>());
    }
    
    private void setupRepositoryObservers() {
        habitRepository.getHabitsLiveData().observeForever(habits -> {
            if (habits != null) {
                allHabitsLiveData.setValue(habits);
                originalHabits = new ArrayList<>(habits);
                filterTodaysHabits(habits);
                applyFilters();
            }
        });
        
        habitRepository.getHabitLogsLiveData().observeForever(logs -> {
            if (logs != null) {
                habitLogsLiveData.setValue(logs);
            }
        });
        
        habitRepository.getLoadingLiveData().observeForever(isLoading -> {
            if (isLoading != null) {
                loadingLiveData.setValue(isLoading);
            }
        });
        
        habitRepository.getErrorLiveData().observeForever(error -> {
            if (error != null) {
                errorLiveData.setValue(error);
            }
        });
    }
    
    // === GETTERS FOR LIVEDATA ===
    
    public LiveData<List<Habit>> getAllHabitsLiveData() { return filteredHabitsLiveData; }
    public LiveData<List<Habit>> getTodaysHabitsLiveData() { return todaysHabitsLiveData; }
    public LiveData<List<HabitLog>> getHabitLogsLiveData() { return habitLogsLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<String> getSuccessLiveData() { return successLiveData; }
    public LiveData<Boolean> getIsCreatingHabit() { return isCreatingHabit; }
    public LiveData<Boolean> getIsUpdatingHabit() { return isUpdatingHabit; }
    public LiveData<String> getSelectedFilter() { return selectedFilter; }
    public LiveData<String> getSelectedCategory() { return selectedCategory; }
    
    // === HABIT CRUD OPERATIONS ===
    
    public void createHabit(Habit habit) {
        if (habit == null) {
            errorLiveData.setValue("Invalid habit data");
            return;
        }
        
        isCreatingHabit.setValue(true);
        
        habitRepository.createHabit(habit, new HabitRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String habitId) {
                isCreatingHabit.setValue(false);
                successLiveData.setValue("Habit created successfully!");
                refreshData();
            }
            
            @Override
            public void onError(String error) {
                isCreatingHabit.setValue(false);
                errorLiveData.setValue("Failed to create habit: " + error);
            }
        });
    }
    
    public void updateHabit(Habit habit) {
        if (habit == null || habit.getHabitId() == null) {
            errorLiveData.setValue("Invalid habit data for update");
            return;
        }
        
        isUpdatingHabit.setValue(true);
        
        habitRepository.updateHabit(habit, new HabitRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isUpdatingHabit.setValue(false);
                successLiveData.setValue("Habit updated successfully!");
                refreshData();
            }
            
            @Override
            public void onError(String error) {
                isUpdatingHabit.setValue(false);
                errorLiveData.setValue("Failed to update habit: " + error);
            }
        });
    }
    
    public void deleteHabit(String habitId) {
        if (habitId == null) {
            errorLiveData.setValue("Invalid habit ID");
            return;
        }
        
        loadingLiveData.setValue(true);
        
        habitRepository.deleteHabit(habitId, new HabitRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                successLiveData.setValue("Habit deleted successfully!");
                refreshData();
            }
            
            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Failed to delete habit: " + error);
            }
        });
    }
    
    public void toggleHabitStatus(String habitId) {
        if (habitId == null) {
            errorLiveData.setValue("Invalid habit ID");
            return;
        }
        
        habitRepository.toggleHabitStatus(habitId, new HabitRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                successLiveData.setValue("Habit status updated!");
                refreshData();
            }
            
            @Override
            public void onError(String error) {
                errorLiveData.setValue("Failed to update habit status: " + error);
            }
        });
    }
    
    // === HABIT LOGGING OPERATIONS ===
    
    public void logHabitCompletion(String habitId, boolean completed) {
        logHabitCompletion(habitId, completed, 0, 0, null);
    }
    
    public void logHabitCompletion(String habitId, boolean completed, double progressValue, double targetValue, String unit) {
        if (habitId == null) {
            errorLiveData.setValue("Invalid habit ID");
            return;
        }
        
        habitRepository.logHabitCompletion(habitId, completed, progressValue, targetValue, unit, 
                new HabitRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String result) {
                String message = completed ? "Habit completed! 🎉" : "Habit unchecked";
                successLiveData.setValue(message);
                refreshData();
            }
            
            @Override
            public void onError(String error) {
                errorLiveData.setValue("Failed to log habit: " + error);
            }
        });
    }
    
    public void checkTodaysHabitCompletion(String habitId, HabitCompletionCallback callback) {
        habitRepository.getTodaysHabitLog(habitId, new HabitRepository.RepositoryCallback<HabitLog>() {
            @Override
            public void onSuccess(HabitLog habitLog) {
                callback.onResult(habitLog != null && habitLog.isCompleted());
            }
            
            @Override
            public void onError(String error) {
                callback.onResult(false);
            }
        });
    }
    
    // === DATA LOADING ===
    
    public void loadUserHabits() {
        habitRepository.loadUserHabits();
    }
    
    public void loadUserHabitLogs() {
        habitRepository.loadUserHabitLogs();
    }
    
    public void loadUserHabitLogs(String habitId) {
        habitRepository.loadUserHabitLogs(habitId);
    }
    
    public void refreshData() {
        loadUserHabits();
        loadUserHabitLogs();
        loadTodaysHabits();
    }
    
    public void loadTodaysHabits() {
        habitRepository.getHabitsForToday(new HabitRepository.RepositoryCallback<List<Habit>>() {
            @Override
            public void onSuccess(List<Habit> habits) {
                todaysHabitsLiveData.setValue(habits);
            }
            
            @Override
            public void onError(String error) {
                errorLiveData.setValue("Failed to load today's habits: " + error);
            }
        });
    }
    
    private void filterTodaysHabits(List<Habit> allHabits) {
        if (allHabits == null) return;
        
        List<Habit> todaysHabits = new ArrayList<>();
        for (Habit habit : allHabits) {
            if (habit.isActive() && habit.isScheduledForToday()) {
                todaysHabits.add(habit);
            }
        }
        todaysHabitsLiveData.setValue(todaysHabits);
    }
    
    // === FILTERING AND SEARCH ===
    
    public void setFilter(String filter) {
        selectedFilter.setValue(filter);
        applyFilters();
    }
    
    public void setCategory(String category) {
        selectedCategory.setValue(category);
        applyFilters();
    }
    
    private void applyFilters() {
        if (originalHabits.isEmpty()) return;
        
        String filter = selectedFilter.getValue();
        String category = selectedCategory.getValue();
        
        List<Habit> filteredHabits = new ArrayList<>();
        
        for (Habit habit : originalHabits) {
            // Apply status filter
            boolean matchesFilter = true;
            if ("ACTIVE".equals(filter)) {
                matchesFilter = habit.isActive();
            } else if ("INACTIVE".equals(filter)) {
                matchesFilter = !habit.isActive();
            } else if ("TODAY".equals(filter)) {
                matchesFilter = habit.isActive() && habit.isScheduledForToday();
            }
            
            // Apply category filter
            boolean matchesCategory = true;
            if (!"ALL".equals(category)) {
                matchesCategory = category.equals(habit.getCategory());
            }
            
            if (matchesFilter && matchesCategory) {
                filteredHabits.add(habit);
            }
        }
        
        filteredHabitsLiveData.setValue(filteredHabits);
    }
    
    public void searchHabits(String query) {
        if (originalHabits.isEmpty() || query == null) return;
        
        String lowerQuery = query.toLowerCase().trim();
        if (lowerQuery.isEmpty()) {
            // Reset to apply current filters
            applyFilters();
            return;
        }
        
        List<Habit> searchResults = new ArrayList<>();
        for (Habit habit : originalHabits) {
            if (habit.getName().toLowerCase().contains(lowerQuery) ||
                (habit.getDescription() != null && habit.getDescription().toLowerCase().contains(lowerQuery)) ||
                (habit.getCategory() != null && habit.getCategory().toLowerCase().contains(lowerQuery))) {
                searchResults.add(habit);
            }
        }
        
        filteredHabitsLiveData.setValue(searchResults);
    }
    
    // === UTILITY METHODS ===
    
    public void getHabitById(String habitId, HabitCallback callback) {
        habitRepository.getHabitById(habitId, new HabitRepository.RepositoryCallback<Habit>() {
            @Override
            public void onSuccess(Habit habit) {
                callback.onResult(habit);
            }
            
            @Override
            public void onError(String error) {
                errorLiveData.setValue("Failed to load habit: " + error);
                callback.onResult(null);
            }
        });
    }
    
    public void clearError() {
        errorLiveData.setValue(null);
    }
    
    public void clearSuccess() {
        successLiveData.setValue(null);
    }
    
    // === ANALYTICS METHODS ===
    
    public int getTotalActiveHabits() {
        if (originalHabits.isEmpty()) return 0;
        
        int activeCount = 0;
        for (Habit habit : originalHabits) {
            if (habit.isActive()) {
                activeCount++;
            }
        }
        return activeCount;
    }
    
    public int getTodaysCompletedHabits() {
        List<HabitLog> logs = habitLogsLiveData.getValue();
        if (logs == null) return 0;
        
        int completedToday = 0;
        for (HabitLog log : logs) {
            if (log.isCompletedToday()) {
                completedToday++;
            }
        }
        return completedToday;
    }
    
    public int getTodaysScheduledHabits() {
        List<Habit> todaysHabits = todaysHabitsLiveData.getValue();
        return todaysHabits != null ? todaysHabits.size() : 0;
    }
    
    public double getTodaysCompletionRate() {
        int scheduled = getTodaysScheduledHabits();
        if (scheduled == 0) return 0.0;
        
        int completed = getTodaysCompletedHabits();
        return (completed * 100.0) / scheduled;
    }
    
    public int getLongestStreakOverall() {
        if (originalHabits.isEmpty()) return 0;
        
        int longest = 0;
        for (Habit habit : originalHabits) {
            if (habit.getStreakData() != null) {
                longest = Math.max(longest, habit.getStreakData().getLongestStreak());
            }
        }
        return longest;
    }
    
    public int getCurrentStreaksTotal() {
        List<Habit> habits = allHabitsLiveData.getValue();
        if (habits == null) return 0;
        
        int total = 0;
        for (Habit habit : habits) {
            if (habit.getStreakData() != null && habit.getStreakData().isStreakActive()) {
                total += habit.getStreakData().getCurrentStreak();
            }
        }
        return total;
    }
    
    // === VALIDATION HELPERS ===
    
    public boolean validateHabit(Habit habit) {
        if (habit == null) return false;
        
        if (habit.getName() == null || habit.getName().trim().isEmpty()) {
            errorLiveData.setValue("Habit name is required");
            return false;
        }
        
        if (habit.getFrequency() == null) {
            errorLiveData.setValue("Habit frequency is required");
            return false;
        }
        
        if (habit.getFrequency() == Habit.HabitFrequency.CUSTOM || 
            habit.getFrequency() == Habit.HabitFrequency.WEEKLY) {
            if (habit.getCustomDays() == null || habit.getCustomDays().isEmpty()) {
                errorLiveData.setValue("Please select days for this habit");
                return false;
            }
        }
        
        return true;
    }
    
    // === CALLBACK INTERFACES ===
    
    public interface HabitCallback {
        void onResult(Habit habit);
    }
    
    public interface HabitCompletionCallback {
        void onResult(boolean isCompleted);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up any observers if needed
    }
} 