package com.example.flowfits.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String email;
    private String displayName;
    private String profilePicture;
    private Date dateJoined;
    private Date createdAt;
    private Date lastLoginAt;
    private UserPreferences preferences;
    private UserStats stats;

    // Empty constructor required for Firestore
    public User() {}

    public User(String userId, String email, String displayName) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.dateJoined = new Date();
        this.preferences = new UserPreferences();
        this.stats = new UserStats();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getProfilePictureUrl() { return profilePicture; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePicture = profilePictureUrl; }

    public Date getDateJoined() { return dateJoined; }
    public void setDateJoined(Date dateJoined) { this.dateJoined = dateJoined; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Date lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public Date getUpdatedAt() { return lastLoginAt; }
    public void setUpdatedAt(Date updatedAt) { this.lastLoginAt = updatedAt; }

    public UserPreferences getPreferences() { return preferences; }
    public void setPreferences(UserPreferences preferences) { this.preferences = preferences; }

    public UserStats getStats() { return stats; }
    public void setStats(UserStats stats) { this.stats = stats; }

    // Nested Classes for User Preferences and Stats
    public static class UserPreferences {
        private boolean notifications = true;
        private String reminderTime = "08:00";
        private String units = "metric";

        public UserPreferences() {}

        public boolean isNotifications() { return notifications; }
        public void setNotifications(boolean notifications) { this.notifications = notifications; }

        public String getReminderTime() { return reminderTime; }
        public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

        public String getUnits() { return units; }
        public void setUnits(String units) { this.units = units; }
    }

    public static class UserStats {
        private int totalWorkouts = 0;
        private int totalCaloriesBurned = 0;
        private int goalsCompleted = 0;
        private int longestStreak = 0;
        private int totalGoals = 0;
        private int activeHabits = 0;
        private int currentStreak = 0;

        public UserStats() {}

        public int getTotalWorkouts() { return totalWorkouts; }
        public void setTotalWorkouts(int totalWorkouts) { this.totalWorkouts = totalWorkouts; }

        public int getTotalCaloriesBurned() { return totalCaloriesBurned; }
        public void setTotalCaloriesBurned(int totalCaloriesBurned) { this.totalCaloriesBurned = totalCaloriesBurned; }

        public int getGoalsCompleted() { return goalsCompleted; }
        public void setGoalsCompleted(int goalsCompleted) { this.goalsCompleted = goalsCompleted; }

        public int getLongestStreak() { return longestStreak; }
        public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
        
        public int getTotalGoals() { return totalGoals; }
        public void setTotalGoals(int totalGoals) { this.totalGoals = totalGoals; }
        
        public int getActiveHabits() { return activeHabits; }
        public void setActiveHabits(int activeHabits) { this.activeHabits = activeHabits; }
        
        public int getCurrentStreak() { return currentStreak; }
        public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    }
} 