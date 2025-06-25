package com.example.flowfits.repositories;

import com.example.flowfits.firebase.FirebaseManager;
import com.example.flowfits.models.AnalyticsData;
import com.example.flowfits.models.Goal;
import com.example.flowfits.models.Habit;
import com.example.flowfits.models.Workout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class AnalyticsRepository {
    private static AnalyticsRepository instance;
    private FirebaseManager firebaseManager;
    
    // Repository dependencies
    private GoalRepository goalRepository;
    private WorkoutRepository workoutRepository;
    private HabitRepository habitRepository;

    public AnalyticsRepository() {
        firebaseManager = FirebaseManager.getInstance();
        goalRepository = GoalRepository.getInstance();
        workoutRepository = WorkoutRepository.getInstance();
        habitRepository = new HabitRepository();
    }

    public static synchronized AnalyticsRepository getInstance() {
        if (instance == null) {
            instance = new AnalyticsRepository();
        }
        return instance;
    }

    public void getAnalyticsData(AnalyticsCallback callback) {
        if (firebaseManager.getCurrentUserId() == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        AnalyticsData analyticsData = new AnalyticsData();
        CountDownLatch latch = new CountDownLatch(4); // goals, all workouts, weekly workouts, habits
        List<String> errors = new ArrayList<>();

        // Load Goals Data
        goalRepository.getAllGoals(new GoalRepository.GoalsListCallback() {
            @Override
            public void onSuccess(List<Goal> goals) {
                processGoalsData(goals, analyticsData);
                latch.countDown();
            }
            
            @Override
            public void onFailure(String error) {
                errors.add("Goals: " + error);
                latch.countDown();
            }
        });

        // Fetch all workouts for total count
        workoutRepository.fetchAllWorkouts(new WorkoutRepository.WorkoutListCallback() {
            @Override
            public void onSuccess(List<Workout> allWorkouts) {
                analyticsData.setTotalWorkouts(allWorkouts.size());
                latch.countDown();
            }
            @Override
            public void onFailure(String error) {
                errors.add("All Workouts: " + error);
                analyticsData.setTotalWorkouts(0);
                latch.countDown();
            }
        });

        // Fetch weekly workouts for weekly/today stats
        workoutRepository.getWorkoutsByDateRange(getWeekStartDate(), new Date(), new WorkoutRepository.WorkoutListCallback() {
            @Override
            public void onSuccess(List<Workout> weeklyWorkouts) {
                processWeeklyWorkoutsData(weeklyWorkouts, analyticsData);
                latch.countDown();
            }
            @Override
            public void onFailure(String error) {
                errors.add("Weekly Workouts: " + error);
                latch.countDown();
            }
        });

        // Load Habits Data
        habitRepository.getHabitsForToday(new HabitRepository.RepositoryCallback<List<Habit>>() {
            @Override
            public void onSuccess(List<Habit> habits) {
                processHabitsData(habits, analyticsData);
                latch.countDown();
            }
            @Override
            public void onError(String error) {
                errors.add("Habits: " + error);
                latch.countDown();
            }
        });

        // Wait for all data in a background thread
        new Thread(() -> {
            try {
                latch.await();
                // Calculate derived metrics
                calculateDerivedMetrics(analyticsData);
                if (errors.isEmpty()) {
                    callback.onSuccess(analyticsData);
                } else {
                    callback.onPartialSuccess(analyticsData, "Some data couldn't be loaded: " + String.join(", ", errors));
                }
            } catch (InterruptedException e) {
                callback.onFailure("Data loading interrupted: " + e.getMessage());
            }
        }).start();
    }
    
    private Date getWeekStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        return calendar.getTime();
    }
    
    private void processGoalsData(List<Goal> goals, AnalyticsData analyticsData) {
        int completedGoals = 0;
        int activeGoals = 0;
        Map<String, Integer> categoryDistribution = new HashMap<>();
        List<AnalyticsData.ProgressPoint> weightProgress = new ArrayList<>();
        
        for (Goal goal : goals) {
            if (goal.isCompleted()) {
                completedGoals++;
            } else if (goal.getStatus() == Goal.GoalStatus.ACTIVE) {
                activeGoals++;
            }
            
            // Category distribution
            String category = goal.getCategory();
            categoryDistribution.put(category, categoryDistribution.getOrDefault(category, 0) + 1);
            
            // Weight progress tracking
            if ("weight_loss".equals(category) || "weight_gain".equals(category)) {
                weightProgress.add(new AnalyticsData.ProgressPoint(
                    goal.getUpdatedAtDate(), 
                    goal.getCurrentValue(), 
                    goal.getUnit(), 
                    "weight"
                ));
            }
        }
        
        analyticsData.setGoalsCompleted(completedGoals);
        analyticsData.setActiveGoals(activeGoals);
        analyticsData.setGoalCategoryDistribution(categoryDistribution);
        analyticsData.setWeightProgress(weightProgress);
    }
    
    private void processWeeklyWorkoutsData(List<Workout> workouts, AnalyticsData analyticsData) {
        int totalCalories = 0;
        int totalMinutes = 0;
        double totalDistance = 0;
        Map<String, Integer> typeDistribution = new HashMap<>();
        List<AnalyticsData.DayData> weeklyData = new ArrayList<>();
        List<AnalyticsData.ProgressPoint> workoutProgress = new ArrayList<>();
        List<AnalyticsData.ProgressPoint> calorieProgress = new ArrayList<>();
        
        // Initialize weekly data for the last 7 days
        Calendar calendar = Calendar.getInstance();
        for (int i = 6; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -i);
            weeklyData.add(new AnalyticsData.DayData(calendar.getTime(), 0, 0, 0, false));
        }
        
        for (Workout workout : workouts) {
            // Aggregate totals
            totalCalories += workout.getCaloriesBurned();
            totalMinutes += workout.getDuration(); // Using getDuration() instead of getDurationMinutes()
            
            // Type distribution - convert enum to string
            String type = workout.getType().name();
            typeDistribution.put(type, typeDistribution.getOrDefault(type, 0) + 1);
            
            // Weekly data processing
            updateWeeklyData(workout, weeklyData);
            
            // Progress points
            workoutProgress.add(new AnalyticsData.ProgressPoint(
                workout.getDate(), 
                workout.getDuration(), // Using getDuration() instead of getDurationMinutes()
                "minutes", 
                "workout_duration"
            ));
            
            calorieProgress.add(new AnalyticsData.ProgressPoint(
                workout.getDate(), 
                workout.getCaloriesBurned(), 
                "kcal", 
                "calories"
            ));
        }
        
        analyticsData.setTotalCaloriesBurned(totalCalories);
        analyticsData.setTotalExerciseTime(totalMinutes);
        analyticsData.setTotalDistance(totalDistance);
        analyticsData.setWorkoutTypeDistribution(typeDistribution);
        analyticsData.setWeeklyData(weeklyData);
        analyticsData.setWorkoutProgress(workoutProgress);
        analyticsData.setCalorieProgress(calorieProgress);
    }
    
    private void processHabitsData(List<Habit> habits, AnalyticsData analyticsData) {
        int currentStreak = 0;
        int longestStreak = 0;
        
        for (Habit habit : habits) {
            // Access streak data through the nested StreakData object
            if (habit.getStreakData() != null) {
                if (habit.getStreakData().getCurrentStreak() > currentStreak) {
                    currentStreak = habit.getStreakData().getCurrentStreak();
                }
                if (habit.getStreakData().getLongestStreak() > longestStreak) {
                    longestStreak = habit.getStreakData().getLongestStreak();
                }
            }
        }
        
        analyticsData.setCurrentStreak(currentStreak);
        analyticsData.setLongestStreak(longestStreak);
    }
    
    private void updateWeeklyData(Workout workout, List<AnalyticsData.DayData> weeklyData) {
        Calendar workoutCal = Calendar.getInstance();
        workoutCal.setTime(workout.getDate());
        
        for (AnalyticsData.DayData dayData : weeklyData) {
            Calendar dayCal = Calendar.getInstance();
            dayCal.setTime(dayData.getDate());
            
            if (isSameDay(workoutCal, dayCal)) {
                dayData.setWorkouts(dayData.getWorkouts() + 1);
                dayData.setCalories(dayData.getCalories() + workout.getCaloriesBurned());
                dayData.setExerciseMinutes(dayData.getExerciseMinutes() + workout.getDuration()); // Using getDuration()
                break;
            }
        }
    }
    
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    
    private void calculateDerivedMetrics(AnalyticsData analyticsData) {
        // Calculate weekly averages, trends, etc.
        List<AnalyticsData.DayData> weeklyData = analyticsData.getWeeklyData();
        if (weeklyData != null && !weeklyData.isEmpty()) {
            // Set goal achievement flags based on criteria
            for (AnalyticsData.DayData dayData : weeklyData) {
                // Goal achieved if user did at least 1 workout or burned 200+ calories
                dayData.setGoalAchieved(dayData.getWorkouts() > 0 || dayData.getCalories() >= 200);
            }
        }
    }
    
    public void getUserStats(String userId, UserStatsCallback callback) {
        if (userId == null) {
            callback.onFailure("Invalid user ID");
            return;
        }

        com.example.flowfits.models.User.UserStats stats = new com.example.flowfits.models.User.UserStats();
        CountDownLatch latch = new CountDownLatch(3);
        List<String> errors = new ArrayList<>();

        // Get total workouts - use current user's workouts from LiveData
        List<Workout> currentWorkouts = workoutRepository.getWorkoutsLiveData().getValue();
        if (currentWorkouts != null) {
            stats.setTotalWorkouts(currentWorkouts.size());
            latch.countDown();
        } else {
            // Load workouts if not available
            workoutRepository.loadUserWorkouts();
            // For now, set to 0 and count down
            stats.setTotalWorkouts(0);
            latch.countDown();
        }

        // Get total goals
        goalRepository.getAllGoals(new GoalRepository.GoalsListCallback() {
            @Override
            public void onSuccess(List<Goal> goals) {
                stats.setTotalGoals(goals.size());
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                errors.add("Goals: " + error);
                latch.countDown();
            }
        });

        // Get active habits and current streak - use getHabitsForToday method
        habitRepository.getHabitsForToday(new HabitRepository.RepositoryCallback<List<Habit>>() {
            @Override
            public void onSuccess(List<Habit> habits) {
                int activeHabits = 0;
                int maxStreak = 0;
                
                for (Habit habit : habits) {
                    if (habit.isActive()) {
                        activeHabits++;
                    }
                    if (habit.getStreakData() != null) {
                        maxStreak = Math.max(maxStreak, habit.getStreakData().getCurrentStreak());
                    }
                }
                
                stats.setActiveHabits(activeHabits);
                stats.setCurrentStreak(maxStreak);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                errors.add("Habits: " + error);
                latch.countDown();
            }
        });

        // Wait for all data in background thread
        new Thread(() -> {
            try {
                latch.await();
                
                if (errors.isEmpty()) {
                    callback.onSuccess(stats);
                } else {
                    callback.onFailure("Failed to load some stats: " + String.join(", ", errors));
                }
            } catch (InterruptedException e) {
                callback.onFailure("Stats loading interrupted: " + e.getMessage());
            }
        }).start();
    }

    public interface UserStatsCallback {
        void onSuccess(com.example.flowfits.models.User.UserStats stats);
        void onFailure(String error);
    }

    public interface AnalyticsCallback {
        void onSuccess(AnalyticsData analyticsData);
        void onPartialSuccess(AnalyticsData analyticsData, String warning);
        void onFailure(String error);
    }
} 