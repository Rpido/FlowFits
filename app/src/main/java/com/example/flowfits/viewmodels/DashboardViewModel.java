package com.example.flowfits.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.flowfits.models.AnalyticsData;
import com.example.flowfits.models.Goal;
import com.example.flowfits.models.Habit;
import com.example.flowfits.models.Workout;
import com.example.flowfits.repositories.AnalyticsRepository;
import com.example.flowfits.repositories.GoalRepository;
import com.example.flowfits.repositories.HabitRepository;
import com.example.flowfits.repositories.WorkoutRepository;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DashboardViewModel extends AndroidViewModel {
    
    private final AnalyticsRepository analyticsRepository;
    private final GoalRepository goalRepository;
    private final HabitRepository habitRepository;
    private final WorkoutRepository workoutRepository;
    
    // Live data for dashboard sections
    private final MutableLiveData<AnalyticsData> analyticsData = new MutableLiveData<>();
    private final MutableLiveData<List<Goal>> urgentGoals = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Habit>> todaysHabits = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Workout>> recentWorkouts = new MutableLiveData<>();
    private final MutableLiveData<DashboardSummary> dashboardSummary = new MutableLiveData<>();
    private final MutableLiveData<String> motivationalMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final String[] motivationalMessages = {
        "Ready to crush your goals today?",
        "Every workout counts! 💪",
        "You're stronger than yesterday",
        "Progress, not perfection ✨",
        "Your future self will thank you",
        "Make today count! 🔥",
        "Consistency is key to success",
        "Turn your dreams into reality",
        "You've got this! 💯",
        "Champions train when nobody's watching"
    };

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        // Initialize repositories
        analyticsRepository = AnalyticsRepository.getInstance();
        goalRepository = new GoalRepository();
        habitRepository = new HabitRepository();
        workoutRepository = WorkoutRepository.getInstance();
        
        // Load dashboard data
        loadInitialData();
    }

    // Getters for LiveData
    public LiveData<AnalyticsData> getAnalyticsData() { return analyticsData; }
    public LiveData<List<Goal>> getUrgentGoals() { return urgentGoals; }
    public LiveData<List<Habit>> getTodaysHabits() { return todaysHabits; }
    public LiveData<List<Workout>> getRecentWorkouts() { return recentWorkouts; }
    public LiveData<DashboardSummary> getDashboardSummary() { return dashboardSummary; }
    public LiveData<String> getMotivationalMessage() { return motivationalMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // Additional getter for weekly summary (delegates to analytics data)
    public LiveData<List<AnalyticsData.DayData>> getWeeklySummary() {
        MutableLiveData<List<AnalyticsData.DayData>> weeklySummary = new MutableLiveData<>();
        analyticsData.observeForever(data -> {
            if (data != null && data.getWeeklyData() != null) {
                weeklySummary.postValue(data.getWeeklyData());
            } else {
                weeklySummary.postValue(new ArrayList<>());
            }
        });
        return weeklySummary;
    }

    private void loadInitialData() {
        isLoading.setValue(true);
        generateMotivationalMessage();
        loadAnalyticsData();
        loadUrgentGoals();
        loadTodaysHabits();
        loadRecentWorkouts();
    }

    private void loadAnalyticsData() {
        analyticsRepository.getAnalyticsData(new AnalyticsRepository.AnalyticsCallback() {
            @Override
            public void onSuccess(AnalyticsData data) {
                analyticsData.postValue(data);
                generateDashboardSummary(data);
            }
            
            @Override
            public void onPartialSuccess(AnalyticsData data, String warning) {
                analyticsData.postValue(data);
                generateDashboardSummary(data);
            }
            
            @Override
            public void onFailure(String error) {
                // Provide default data if loading fails
                AnalyticsData defaultData = createDefaultAnalyticsData();
                analyticsData.postValue(defaultData);
                generateDashboardSummary(defaultData);
            }
        });
    }

    private void loadUrgentGoals() {
        goalRepository.getAllGoals(new GoalRepository.GoalsListCallback() {
            @Override
            public void onSuccess(List<Goal> goals) {
                // Filter for urgent goals (due soon or overdue)
                List<Goal> urgent = new ArrayList<>();
                Date today = new Date();
                
                for (Goal goal : goals) {
                    if (!goal.isCompleted() && goal.getDaysRemaining() <= 7) {
                        urgent.add(goal);
                    }
                }
                
                // Sort by days remaining
                urgent.sort((g1, g2) -> {
                    return Long.compare(g1.getDaysRemaining(), g2.getDaysRemaining());
                });
                urgentGoals.postValue(urgent.size() > 5 ? urgent.subList(0, 5) : urgent);
            }
            
            @Override
            public void onFailure(String error) {
                urgentGoals.postValue(new ArrayList<>());
            }
        });
    }

    private void loadTodaysHabits() {
        habitRepository.getHabitsForToday(new HabitRepository.RepositoryCallback<List<Habit>>() {
            @Override
            public void onSuccess(List<Habit> habits) {
                todaysHabits.postValue(habits != null ? habits : new ArrayList<>());
            }
            
            @Override
            public void onError(String error) {
                todaysHabits.postValue(new ArrayList<>());
            }
        });
    }

    private void loadRecentWorkouts() {
        workoutRepository.loadUserWorkouts();
        
        // Get recent workouts from LiveData
        workoutRepository.getWorkoutsLiveData().observeForever(workouts -> {
            if (workouts != null && !workouts.isEmpty()) {
                // Get last 3 workouts
                List<Workout> recent = new ArrayList<>(workouts);
                Collections.sort(recent, (w1, w2) -> w2.getDate().compareTo(w1.getDate()));
                
                // Limit to 3 recent workouts
                if (recent.size() > 3) {
                    recent = recent.subList(0, 3);
                }
                recentWorkouts.postValue(recent);
            } else {
                recentWorkouts.postValue(new ArrayList<>());
            }
        });
    }

    private void generateDashboardSummary(AnalyticsData data) {
        DashboardSummary summary = new DashboardSummary();
        
        // Calculate today's progress
        if (data.getWeeklyData() != null && !data.getWeeklyData().isEmpty()) {
            // Find the DayData for today
            Calendar todayCal = Calendar.getInstance();
            AnalyticsData.DayData todayData = null;
            for (AnalyticsData.DayData day : data.getWeeklyData()) {
                Calendar dayCal = Calendar.getInstance();
                dayCal.setTime(day.getDate());
                if (dayCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                    dayCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)) {
                    todayData = day;
                    break;
                }
            }
            if (todayData != null) {
                summary.setTodayWorkouts(todayData.getWorkouts());
                summary.setTodayCalories(todayData.getCalories());
                summary.setTodayMinutes(todayData.getExerciseMinutes());
                summary.setTodayGoalAchieved(todayData.isGoalAchieved());
            } else {
                summary.setTodayWorkouts(0);
                summary.setTodayCalories(0);
                summary.setTodayMinutes(0);
                summary.setTodayGoalAchieved(false);
            }
        }
        
        // Set overall stats
        summary.setTotalWorkouts(data.getTotalWorkouts());
        summary.setTotalCalories(data.getTotalCaloriesBurned());
        summary.setActiveGoals(data.getActiveGoals());
        summary.setCurrentStreak(data.getCurrentStreak());
        summary.setLongestStreak(data.getLongestStreak());
        
        // Calculate weekly average
        if (data.getWeeklyData() != null && !data.getWeeklyData().isEmpty()) {
            int weeklyWorkouts = 0;
            int weeklyCalories = 0;
            for (AnalyticsData.DayData day : data.getWeeklyData()) {
                weeklyWorkouts += day.getWorkouts();
                weeklyCalories += day.getCalories();
            }
            summary.setWeeklyAvgWorkouts(weeklyWorkouts / 7.0);
            summary.setWeeklyAvgCalories(weeklyCalories / 7.0);
        }
        
        dashboardSummary.postValue(summary);
    }

    private void generateMotivationalMessage() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        String timeBasedMessage;
        if (hour < 12) {
            timeBasedMessage = getRandomFromArray(new String[]{
                "Start your day strong! ☀️",
                "Morning energy is the best energy! ⚡",
                "Rise and shine, champion! 🌅",
                "Great mornings create great days! ✨"
            });
        } else if (hour < 17) {
            timeBasedMessage = getRandomFromArray(new String[]{
                "Keep the momentum going! 🚀",
                "Afternoon power boost time! ⚡",
                "You're halfway there! 💪",
                "Push through to greatness! 🔥"
            });
        } else {
            timeBasedMessage = getRandomFromArray(new String[]{
                "Finish strong tonight! 🌙",
                "Evening victories count double! ⭐",
                "End your day on a high note! 🎯",
                "Tonight's effort, tomorrow's results! 💫"
            });
        }
        
        // 70% chance for time-based message, 30% for general motivational
        String selectedMessage;
        if (new Random().nextFloat() < 0.7f) {
            selectedMessage = timeBasedMessage;
        } else {
            selectedMessage = getRandomFromArray(motivationalMessages);
        }
        
        motivationalMessage.postValue(selectedMessage);
    }

    private String getRandomFromArray(String[] array) {
        return array[new Random().nextInt(array.length)];
    }

    private AnalyticsData createDefaultAnalyticsData() {
        AnalyticsData data = new AnalyticsData();
        data.setTotalWorkouts(0);
        data.setTotalCalories(0);
        data.setCurrentStreak(0);
        data.setActiveGoals(0);
        data.setTodayWorkouts(0);
        return data;
    }

    public void refreshData() {
        isLoading.setValue(true);
        generateMotivationalMessage();
        loadAnalyticsData();
        loadRecentWorkouts();
        isLoading.setValue(false);
    }

    // Methods for habit interactions from DashboardFragment
    public void toggleHabitCompletion(Habit habit, boolean isCompleted) {
        if (isCompleted) {
            // Log habit completion
            habitRepository.logHabitCompletion(habit.getHabitId(), true, new HabitRepository.RepositoryCallback<String>() {
                @Override
                public void onSuccess(String logId) {
                    // Refresh today's habits to update UI
                    loadTodaysHabits();
                }
                
                @Override
                public void onError(String error) {
                    errorMessage.postValue("Failed to log habit completion: " + error);
                }
            });
        }
    }
    
    public void toggleHabitStatus(Habit habit) {
        habitRepository.toggleHabitStatus(habit.getHabitId(), new HabitRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Refresh today's habits to update UI
                loadTodaysHabits();
            }
            
            @Override
            public void onError(String error) {
                errorMessage.postValue("Failed to toggle habit status: " + error);
            }
        });
    }

    // Dashboard Summary Model
    public static class DashboardSummary {
        private int todayWorkouts;
        private int todayCalories;
        private int todayMinutes;
        private boolean todayGoalAchieved;
        private int totalWorkouts;
        private int totalCalories;
        private int activeGoals;
        private int currentStreak;
        private int longestStreak;
        private double weeklyAvgWorkouts;
        private double weeklyAvgCalories;

        // Getters and Setters
        public int getTodayWorkouts() { return todayWorkouts; }
        public void setTodayWorkouts(int todayWorkouts) { this.todayWorkouts = todayWorkouts; }
        
        public int getTodayCalories() { return todayCalories; }
        public void setTodayCalories(int todayCalories) { this.todayCalories = todayCalories; }
        
        public int getTodayMinutes() { return todayMinutes; }
        public void setTodayMinutes(int todayMinutes) { this.todayMinutes = todayMinutes; }
        
        public boolean isTodayGoalAchieved() { return todayGoalAchieved; }
        public void setTodayGoalAchieved(boolean todayGoalAchieved) { this.todayGoalAchieved = todayGoalAchieved; }
        
        public int getTotalWorkouts() { return totalWorkouts; }
        public void setTotalWorkouts(int totalWorkouts) { this.totalWorkouts = totalWorkouts; }
        
        public int getTotalCalories() { return totalCalories; }
        public void setTotalCalories(int totalCalories) { this.totalCalories = totalCalories; }
        
        public int getActiveGoals() { return activeGoals; }
        public void setActiveGoals(int activeGoals) { this.activeGoals = activeGoals; }
        
        public int getCurrentStreak() { return currentStreak; }
        public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
        
        public int getLongestStreak() { return longestStreak; }
        public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
        
        public double getWeeklyAvgWorkouts() { return weeklyAvgWorkouts; }
        public void setWeeklyAvgWorkouts(double weeklyAvgWorkouts) { this.weeklyAvgWorkouts = weeklyAvgWorkouts; }
        
        public double getWeeklyAvgCalories() { return weeklyAvgCalories; }
        public void setWeeklyAvgCalories(double weeklyAvgCalories) { this.weeklyAvgCalories = weeklyAvgCalories; }
    }
} 