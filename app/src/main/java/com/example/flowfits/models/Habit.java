package com.example.flowfits.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Habit implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String habitId;
    private String userId;
    private String name;
    private String description;
    private HabitFrequency frequency;
    private List<String> customDays; // for custom frequency (MON, TUE, etc.)
    private String category;
    private double target;
    private String unit;
    private String reminderTime; // HH:mm format
    private boolean reminderEnabled;
    private String linkedGoalId;
    private Date createdAt;
    private Date updatedAt;
    private boolean isActive;
    private HabitPriority priority;
    private String color; // for visual organization
    private StreakData streakData;
    private ProgressData progressData;

    public enum HabitFrequency {
        DAILY("Daily", "Every day"),
        WEEKLY("Weekly", "Specific days of the week"),
        CUSTOM("Custom", "Custom schedule");
        
        private final String displayName;
        private final String description;
        
        HabitFrequency(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public enum HabitCategory {
        HEALTH("Health", "#4CAF50", "🏥"),
        FITNESS("Fitness", "#FF9800", "💪"),
        NUTRITION("Nutrition", "#8BC34A", "🥗"),
        MINDFULNESS("Mindfulness", "#9C27B0", "🧘"),
        PRODUCTIVITY("Productivity", "#2196F3", "⚡"),
        LEARNING("Learning", "#FF5722", "📚"),
        SOCIAL("Social", "#E91E63", "👥"),
        CREATIVITY("Creativity", "#673AB7", "🎨"),
        LIFESTYLE("Lifestyle", "#607D8B", "🌟"),
        OTHER("Other", "#9E9E9E", "📝");
        
        private final String displayName;
        private final String color;
        private final String icon;
        
        HabitCategory(String displayName, String color, String icon) {
            this.displayName = displayName;
            this.color = color;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
        public String getIcon() { return icon; }
    }
    
    public enum HabitPriority {
        LOW("Low", 1),
        MEDIUM("Medium", 2),
        HIGH("High", 3);
        
        private final String displayName;
        private final int level;
        
        HabitPriority(String displayName, int level) {
            this.displayName = displayName;
            this.level = level;
        }
        
        public String getDisplayName() { return displayName; }
        public int getLevel() { return level; }
    }

    // Empty constructor for Firebase
    public Habit() {
        this.customDays = new ArrayList<>();
        this.streakData = new StreakData();
        this.progressData = new ProgressData();
        this.isActive = true;
        this.reminderEnabled = false;
        this.priority = HabitPriority.MEDIUM;
        this.color = "#2196F3";
    }

    public Habit(String userId, String name, String description, HabitFrequency frequency, String category) {
        this();
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.frequency = frequency;
        this.category = category;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Core Getters and Setters
    public String getHabitId() { return habitId; }
    public void setHabitId(String habitId) { this.habitId = habitId; }
    
    // Alias for getId() - used by adapters
    public String getId() { return habitId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        this.updatedAt = new Date();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.updatedAt = new Date();
    }

    public HabitFrequency getFrequency() { return frequency; }
    public void setFrequency(HabitFrequency frequency) { 
        this.frequency = frequency;
        this.updatedAt = new Date();
    }

    public List<String> getCustomDays() { 
        if (customDays == null) {
            customDays = new ArrayList<>();
        }
        return customDays; 
    }
    public void setCustomDays(List<String> customDays) { 
        this.customDays = customDays;
        this.updatedAt = new Date();
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category;
        this.updatedAt = new Date();
    }

    public double getTarget() { return target; }
    public void setTarget(double target) { 
        this.target = target;
        this.updatedAt = new Date();
    }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { 
        this.unit = unit;
        this.updatedAt = new Date();
    }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { 
        this.reminderTime = reminderTime;
        this.updatedAt = new Date();
    }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { 
        this.reminderEnabled = reminderEnabled;
        this.updatedAt = new Date();
    }

    public String getLinkedGoalId() { return linkedGoalId; }
    public void setLinkedGoalId(String linkedGoalId) { this.linkedGoalId = linkedGoalId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { 
        this.isActive = active;
        this.updatedAt = new Date();
    }

    public HabitPriority getPriority() { return priority; }
    public void setPriority(HabitPriority priority) { 
        this.priority = priority;
        this.updatedAt = new Date();
    }

    public String getColor() { return color; }
    public void setColor(String color) { 
        this.color = color;
        this.updatedAt = new Date();
    }

    public StreakData getStreakData() { 
        if (streakData == null) {
            streakData = new StreakData();
        }
        return streakData; 
    }
    public void setStreakData(StreakData streakData) { this.streakData = streakData; }

    public ProgressData getProgressData() { 
        if (progressData == null) {
            progressData = new ProgressData();
        }
        return progressData; 
    }
    public void setProgressData(ProgressData progressData) { this.progressData = progressData; }

    // Utility methods
    public boolean isScheduledForToday() {
        if (frequency == null) return true; // Default to true if frequency is not set
        
        Calendar today = Calendar.getInstance();
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        String todayName = getDayName(dayOfWeek);
        
        switch (frequency) {
            case DAILY:
                return true;
            case WEEKLY:
            case CUSTOM:
                return customDays != null && customDays.contains(todayName);
            default:
                return false;
        }
    }
    
    public String getDisplayFrequency() {
        if (frequency == null) return "Unknown";
        
        switch (frequency) {
            case DAILY:
                return "Daily";
            case WEEKLY:
                if (customDays != null) {
                    return "Weekly (" + customDays.size() + " days)";
                } else {
                    return "Weekly";
                }
            case CUSTOM:
                if (customDays != null && !customDays.isEmpty()) {
                    return String.join(", ", customDays);
                }
                return "Custom";
            default:
                return "Unknown";
        }
    }
    
    public HabitCategory getCategoryEnum() {
        if (category == null) return HabitCategory.OTHER;
        try {
            return HabitCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return HabitCategory.OTHER;
        }
    }
    
    public String getCategoryDisplayName() {
        return getCategoryEnum().getDisplayName();
    }
    
    public String getCategoryIcon() {
        return getCategoryEnum().getIcon();
    }
    
    public String getStreakDisplay() {
        if (streakData == null) return "0 days";
        int current = streakData.getCurrentStreak();
        return current + (current == 1 ? " day" : " days");
    }
    
    public String getLongestStreakDisplay() {
        if (streakData == null) return "0 days";
        int longest = streakData.getLongestStreak();
        return longest + (longest == 1 ? " day" : " days");
    }
    
    public double getCompletionRate() {
        if (progressData == null || progressData.getTotalDays() == 0) return 0.0;
        return (progressData.getCompletedDays() * 100.0) / progressData.getTotalDays();
    }
    
    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "SUN";
            case Calendar.MONDAY: return "MON";
            case Calendar.TUESDAY: return "TUE";
            case Calendar.WEDNESDAY: return "WED";
            case Calendar.THURSDAY: return "THU";
            case Calendar.FRIDAY: return "FRI";
            case Calendar.SATURDAY: return "SAT";
            default: return "";
        }
    }

    // Enhanced Streak Data
    public static class StreakData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private int currentStreak = 0;
        private int longestStreak = 0;
        private int totalCompletions = 0;
        private Date lastCompletedDate;
        private Date streakStartDate;
        private int weeklyStreak = 0; // for weekly habits
        private int monthlyStreak = 0; // for monthly tracking

        public StreakData() {}

        public int getCurrentStreak() { return currentStreak; }
        public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

        public int getLongestStreak() { return longestStreak; }
        public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

        public int getTotalCompletions() { return totalCompletions; }
        public void setTotalCompletions(int totalCompletions) { this.totalCompletions = totalCompletions; }

        public Date getLastCompletedDate() { return lastCompletedDate; }
        public void setLastCompletedDate(Date lastCompletedDate) { this.lastCompletedDate = lastCompletedDate; }

        public Date getStreakStartDate() { return streakStartDate; }
        public void setStreakStartDate(Date streakStartDate) { this.streakStartDate = streakStartDate; }

        public int getWeeklyStreak() { return weeklyStreak; }
        public void setWeeklyStreak(int weeklyStreak) { this.weeklyStreak = weeklyStreak; }

        public int getMonthlyStreak() { return monthlyStreak; }
        public void setMonthlyStreak(int monthlyStreak) { this.monthlyStreak = monthlyStreak; }
        
        public void incrementStreak() {
            currentStreak++;
            totalCompletions++;
            lastCompletedDate = new Date();
            
            if (currentStreak > longestStreak) {
                longestStreak = currentStreak;
            }
            
            if (currentStreak == 1) {
                streakStartDate = new Date();
            }
        }
        
        public void resetStreak() {
            currentStreak = 0;
            streakStartDate = null;
        }
        
        public boolean isStreakActive() {
            if (lastCompletedDate == null) return false;
            
            Calendar today = Calendar.getInstance();
            Calendar lastCompleted = Calendar.getInstance();
            lastCompleted.setTime(lastCompletedDate);
            
            // Check if last completed was yesterday or today
            long diffInMillis = today.getTimeInMillis() - lastCompleted.getTimeInMillis();
            long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
            
            return diffInDays <= 1;
        }
    }

    // Progress Data for analytics
    public static class ProgressData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private int completedDays = 0;
        private int totalDays = 0;
        private double currentValue = 0.0; // for target-based habits
        private Date lastUpdated;
        private List<String> completedDates; // ISO date strings for analytics

        public ProgressData() {
            this.completedDates = new ArrayList<>();
        }

        public int getCompletedDays() { return completedDays; }
        public void setCompletedDays(int completedDays) { this.completedDays = completedDays; }

        public int getTotalDays() { return totalDays; }
        public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

        public double getCurrentValue() { return currentValue; }
        public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

        public Date getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }

        public List<String> getCompletedDates() { return completedDates; }
        public void setCompletedDates(List<String> completedDates) { this.completedDates = completedDates; }
        
        public void addCompletedDate(Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateString = sdf.format(date);
            
            if (!completedDates.contains(dateString)) {
                completedDates.add(dateString);
                completedDays++;
            }
            
            totalDays++;
            lastUpdated = new Date();
        }
        
        public boolean isCompletedToday() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String todayString = sdf.format(new Date());
            return completedDates.contains(todayString);
        }
    }
} 