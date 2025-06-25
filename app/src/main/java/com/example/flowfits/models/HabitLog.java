package com.example.flowfits.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HabitLog implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String logId;
    private String userId;
    private String habitId;
    private Date date;
    private boolean completed;
    private Date completedAt;
    private Date loggedAt;
    private String notes;
    private double progressValue; // actual value achieved (e.g., 8000 steps out of 10000 target)
    private double targetValue; // target value for that day
    private String unit; // unit of measurement
    private HabitMood mood; // how the user felt about completing this habit
    private int difficulty; // 1-5 scale of how difficult it was
    private String location; // where the habit was performed
    private int timeSpentMinutes; // time spent on the habit
    private boolean isPartialCompletion; // true if not fully completed but some progress made
    private String completionMethod; // how the habit was completed (manual, automatic, etc.)

    public enum HabitMood {
        EXCELLENT("Excellent", "😍", "#4CAF50"),
        GOOD("Good", "😊", "#8BC34A"),
        NEUTRAL("Neutral", "😐", "#FFC107"),
        DIFFICULT("Difficult", "😓", "#FF9800"),
        STRUGGLED("Struggled", "😩", "#F44336");
        
        private final String displayName;
        private final String emoji;
        private final String color;
        
        HabitMood(String displayName, String emoji, String color) {
            this.displayName = displayName;
            this.emoji = emoji;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getEmoji() { return emoji; }
        public String getColor() { return color; }
    }

    // Empty constructor for Firebase
    public HabitLog() {
        this.loggedAt = new Date();
    }

    public HabitLog(String userId, String habitId, Date date, boolean completed) {
        this();
        this.userId = userId;
        this.habitId = habitId;
        this.date = date;
        this.completed = completed;
        if (completed) {
            this.completedAt = new Date();
        }
    }
    
    public HabitLog(String userId, String habitId, Date date, boolean completed, double progressValue, double targetValue, String unit) {
        this(userId, habitId, date, completed);
        this.progressValue = progressValue;
        this.targetValue = targetValue;
        this.unit = unit;
        
        // Auto-determine completion based on progress
        if (targetValue > 0) {
            this.completed = progressValue >= targetValue;
            this.isPartialCompletion = progressValue > 0 && progressValue < targetValue;
        }
    }

    // Core Getters and Setters
    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getHabitId() { return habitId; }
    public void setHabitId(String habitId) { this.habitId = habitId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { 
        this.completed = completed;
        if (completed && this.completedAt == null) {
            this.completedAt = new Date();
        }
    }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    public Date getLoggedAt() { return loggedAt; }
    public void setLoggedAt(Date loggedAt) { this.loggedAt = loggedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public double getProgressValue() { return progressValue; }
    public void setProgressValue(double progressValue) { 
        this.progressValue = progressValue;
        
        // Auto-update completion status
        if (targetValue > 0) {
            this.completed = progressValue >= targetValue;
            this.isPartialCompletion = progressValue > 0 && progressValue < targetValue;
        }
    }

    public double getTargetValue() { return targetValue; }
    public void setTargetValue(double targetValue) { this.targetValue = targetValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public HabitMood getMood() { return mood; }
    public void setMood(HabitMood mood) { this.mood = mood; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { 
        this.difficulty = Math.max(1, Math.min(5, difficulty)); // Clamp between 1-5
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getTimeSpentMinutes() { return timeSpentMinutes; }
    public void setTimeSpentMinutes(int timeSpentMinutes) { this.timeSpentMinutes = timeSpentMinutes; }

    public boolean isPartialCompletion() { return isPartialCompletion; }
    public void setPartialCompletion(boolean partialCompletion) { this.isPartialCompletion = partialCompletion; }

    public String getCompletionMethod() { return completionMethod; }
    public void setCompletionMethod(String completionMethod) { this.completionMethod = completionMethod; }

    // Utility methods
    public String getDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }
    
    public String getDisplayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(date);
    }
    
    public String getCompletedTimeString() {
        if (completedAt == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(completedAt);
    }
    
    public double getCompletionPercentage() {
        if (targetValue <= 0) return completed ? 100.0 : 0.0;
        return Math.min(100.0, (progressValue / targetValue) * 100.0);
    }
    
    public String getProgressDisplay() {
        if (targetValue <= 0) {
            return completed ? "Completed" : "Not completed";
        }
        
        String unitDisplay = (unit != null && !unit.isEmpty()) ? " " + unit : "";
        return String.format(Locale.getDefault(), "%.1f / %.1f%s", progressValue, targetValue, unitDisplay);
    }
    
    public String getCompletionStatusDisplay() {
        if (completed) {
            return "✅ Completed";
        } else if (isPartialCompletion) {
            return "🔶 Partial (" + String.format(Locale.getDefault(), "%.0f%%", getCompletionPercentage()) + ")";
        } else {
            return "❌ Not completed";
        }
    }
    
    public String getMoodDisplay() {
        if (mood == null) return "";
        return mood.getEmoji() + " " + mood.getDisplayName();
    }
    
    public String getDifficultyDisplay() {
        if (difficulty <= 0) return "";
        
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < difficulty) {
                stars.append("⭐");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
    
    public String getTimeSpentDisplay() {
        if (timeSpentMinutes <= 0) return "";
        
        if (timeSpentMinutes < 60) {
            return timeSpentMinutes + " min";
        } else {
            int hours = timeSpentMinutes / 60;
            int minutes = timeSpentMinutes % 60;
            if (minutes == 0) {
                return hours + "h";
            } else {
                return hours + "h " + minutes + "m";
            }
        }
    }
    
    public boolean isCompletedToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayString = sdf.format(new Date());
        String logDateString = sdf.format(date);
        return todayString.equals(logDateString) && completed;
    }
    
    public boolean isFromToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayString = sdf.format(new Date());
        String logDateString = sdf.format(date);
        return todayString.equals(logDateString);
    }
    
    // Deprecated method for backward compatibility
    @Deprecated
    public double getProgress() { 
        return progressValue; 
    }
    
    @Deprecated
    public void setProgress(double progress) { 
        setProgressValue(progress); 
    }
    
    // Legacy method for backward compatibility
    @Deprecated
    public String getId() { 
        return logId; 
    }
    
    @Deprecated
    public void setId(String logId) { 
        this.logId = logId; 
    }
} 