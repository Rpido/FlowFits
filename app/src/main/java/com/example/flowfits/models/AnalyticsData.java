package com.example.flowfits.models;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AnalyticsData {
    
    // Overall Stats
    private int totalWorkouts;
    private int totalCaloriesBurned;
    private int goalsCompleted;
    private int activeGoals;
    private int currentStreak;
    private int longestStreak;
    private double totalDistance;
    private int totalExerciseTime; // in minutes
    private int todayWorkouts;
    
    // Weekly/Monthly data for charts
    private List<DayData> weeklyData;
    private List<WeekData> monthlyData;
    private Map<String, Integer> workoutTypeDistribution;
    private Map<String, Integer> goalCategoryDistribution;
    
    // Progress tracking
    private List<ProgressPoint> weightProgress;
    private List<ProgressPoint> calorieProgress;
    private List<ProgressPoint> workoutProgress;
    
    public AnalyticsData() {
        // Default constructor
    }
    
    // Overall Stats Getters and Setters
    public int getTotalWorkouts() { return totalWorkouts; }
    public void setTotalWorkouts(int totalWorkouts) { this.totalWorkouts = totalWorkouts; }
    
    public int getTotalCaloriesBurned() { return totalCaloriesBurned; }
    public void setTotalCaloriesBurned(int totalCaloriesBurned) { this.totalCaloriesBurned = totalCaloriesBurned; }
    
    public int getTotalCalories() { return totalCaloriesBurned; }
    public void setTotalCalories(int totalCalories) { this.totalCaloriesBurned = totalCalories; }
    
    public int getGoalsCompleted() { return goalsCompleted; }
    public void setGoalsCompleted(int goalsCompleted) { this.goalsCompleted = goalsCompleted; }
    
    public int getActiveGoals() { return activeGoals; }
    public void setActiveGoals(int activeGoals) { this.activeGoals = activeGoals; }
    
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    
    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
    
    public double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(double totalDistance) { this.totalDistance = totalDistance; }
    
    public int getTotalExerciseTime() { return totalExerciseTime; }
    public void setTotalExerciseTime(int totalExerciseTime) { this.totalExerciseTime = totalExerciseTime; }
    
    public int getTodayWorkouts() { return todayWorkouts; }
    public void setTodayWorkouts(int todayWorkouts) { this.todayWorkouts = todayWorkouts; }
    
    // Chart Data Getters and Setters
    public List<DayData> getWeeklyData() { return weeklyData; }
    public void setWeeklyData(List<DayData> weeklyData) { this.weeklyData = weeklyData; }
    
    public List<WeekData> getMonthlyData() { return monthlyData; }
    public void setMonthlyData(List<WeekData> monthlyData) { this.monthlyData = monthlyData; }
    
    public Map<String, Integer> getWorkoutTypeDistribution() { return workoutTypeDistribution; }
    public void setWorkoutTypeDistribution(Map<String, Integer> workoutTypeDistribution) { 
        this.workoutTypeDistribution = workoutTypeDistribution; 
    }
    
    public Map<String, Integer> getGoalCategoryDistribution() { return goalCategoryDistribution; }
    public void setGoalCategoryDistribution(Map<String, Integer> goalCategoryDistribution) { 
        this.goalCategoryDistribution = goalCategoryDistribution; 
    }
    
    public List<ProgressPoint> getWeightProgress() { return weightProgress; }
    public void setWeightProgress(List<ProgressPoint> weightProgress) { this.weightProgress = weightProgress; }
    
    public List<ProgressPoint> getCalorieProgress() { return calorieProgress; }
    public void setCalorieProgress(List<ProgressPoint> calorieProgress) { this.calorieProgress = calorieProgress; }
    
    public List<ProgressPoint> getWorkoutProgress() { return workoutProgress; }
    public void setWorkoutProgress(List<ProgressPoint> workoutProgress) { this.workoutProgress = workoutProgress; }
    
    // Inner Classes for Chart Data
    public static class DayData {
        private Date date;
        private int workouts;
        private int calories;
        private int exerciseMinutes;
        private boolean goalAchieved;
        
        public DayData() {}
        
        public DayData(Date date, int workouts, int calories, int exerciseMinutes, boolean goalAchieved) {
            this.date = date;
            this.workouts = workouts;
            this.calories = calories;
            this.exerciseMinutes = exerciseMinutes;
            this.goalAchieved = goalAchieved;
        }
        
        // Getters and Setters
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
        
        public int getWorkouts() { return workouts; }
        public void setWorkouts(int workouts) { this.workouts = workouts; }
        
        public int getCalories() { return calories; }
        public void setCalories(int calories) { this.calories = calories; }
        
        public int getExerciseMinutes() { return exerciseMinutes; }
        public void setExerciseMinutes(int exerciseMinutes) { this.exerciseMinutes = exerciseMinutes; }
        
        public boolean isGoalAchieved() { return goalAchieved; }
        public void setGoalAchieved(boolean goalAchieved) { this.goalAchieved = goalAchieved; }
    }
    
    public static class WeekData {
        private Date weekStart;
        private int totalWorkouts;
        private int totalCalories;
        private int totalMinutes;
        private int goalsCompleted;
        
        public WeekData() {}
        
        public WeekData(Date weekStart, int totalWorkouts, int totalCalories, int totalMinutes, int goalsCompleted) {
            this.weekStart = weekStart;
            this.totalWorkouts = totalWorkouts;
            this.totalCalories = totalCalories;
            this.totalMinutes = totalMinutes;
            this.goalsCompleted = goalsCompleted;
        }
        
        // Getters and Setters
        public Date getWeekStart() { return weekStart; }
        public void setWeekStart(Date weekStart) { this.weekStart = weekStart; }
        
        public int getTotalWorkouts() { return totalWorkouts; }
        public void setTotalWorkouts(int totalWorkouts) { this.totalWorkouts = totalWorkouts; }
        
        public int getTotalCalories() { return totalCalories; }
        public void setTotalCalories(int totalCalories) { this.totalCalories = totalCalories; }
        
        public int getTotalMinutes() { return totalMinutes; }
        public void setTotalMinutes(int totalMinutes) { this.totalMinutes = totalMinutes; }
        
        public int getGoalsCompleted() { return goalsCompleted; }
        public void setGoalsCompleted(int goalsCompleted) { this.goalsCompleted = goalsCompleted; }
    }
    
    public static class ProgressPoint {
        private Date date;
        private double value;
        private String unit;
        private String type; // "weight", "calories", "workouts", etc.
        
        public ProgressPoint() {}
        
        public ProgressPoint(Date date, double value, String unit, String type) {
            this.date = date;
            this.value = value;
            this.unit = unit;
            this.type = type;
        }
        
        // Getters and Setters
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
        
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    // QuickStat class for dashboard
    public static class QuickStat {
        private String title;
        private String value;
        private String change;
        private String label;
        
        public QuickStat() {}
        
        public QuickStat(String title, String value, String change, String label) {
            this.title = title;
            this.value = value;
            this.change = change;
            this.label = label;
        }
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        
        public String getChange() { return change; }
        public void setChange(String change) { this.change = change; }
        
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
} 