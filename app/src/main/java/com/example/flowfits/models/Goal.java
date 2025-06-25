package com.example.flowfits.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Calendar;

public class Goal implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String goalId;
    private String userId;
    private String title;
    private String description;
    private double targetValue;
    private double currentValue;
    private String unit;
    private String category;
    private String type; // "Increase", "Decrease", "Maintain", "Achieve"
    private String deadline; // Store as string for Firebase compatibility
    private long createdAt; // Store as timestamp
    private long updatedAt; // Store as timestamp
    private String status; // Store enum as string
    private List<String> linkedHabits;
    
    // Enhanced fields (simplified for Firebase)
    private String priority; // Store enum as string
    private List<String> tags;
    private String notes;
    private boolean isArchived;
    private long completedAt; // Store as timestamp (0 if not completed)
    private int streakDays;
    private String reminderTime; // e.g., "09:00"

    public enum GoalStatus {
        ACTIVE("Active", "#4CAF50"), 
        COMPLETED("Completed", "#2196F3"), 
        PAUSED("Paused", "#FF9800"), 
        IN_PROGRESS("In Progress", "#9C27B0"),
        OVERDUE("Overdue", "#F44336"),
        CANCELLED("Cancelled", "#757575");
        
        private final String displayName;
        private final String colorCode;
        
        GoalStatus(String displayName, String colorCode) {
            this.displayName = displayName;
            this.colorCode = colorCode;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColorCode() { return colorCode; }
        
        public static GoalStatus fromString(String status) {
            if (status == null) return ACTIVE;
            try {
                return GoalStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ACTIVE;
            }
        }
    }
    
    public enum GoalPriority {
        LOW("Low", 1, "#4CAF50"),
        MEDIUM("Medium", 2, "#FF9800"), 
        HIGH("High", 3, "#F44336"),
        CRITICAL("Critical", 4, "#9C27B0");
        
        private final String displayName;
        private final int level;
        private final String colorCode;
        
        GoalPriority(String displayName, int level, String colorCode) {
            this.displayName = displayName;
            this.level = level;
            this.colorCode = colorCode;
        }
        
        public String getDisplayName() { return displayName; }
        public int getLevel() { return level; }
        public String getColorCode() { return colorCode; }
        
        public static GoalPriority fromString(String priority) {
            if (priority == null) return MEDIUM;
            try {
                return GoalPriority.valueOf(priority.toUpperCase());
            } catch (IllegalArgumentException e) {
                return MEDIUM;
            }
        }
    }

    // Empty constructor for Firebase
    public Goal() {
        this.linkedHabits = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.priority = "MEDIUM";
        this.status = "ACTIVE";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isArchived = false;
        this.streakDays = 0;
        this.completedAt = 0;
    }

    public Goal(String userId, String title, String description, double targetValue, String unit, String category, Date deadline) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.targetValue = targetValue;
        this.currentValue = 0.0;
        this.unit = unit;
        this.category = category;
        this.type = "Achieve"; // Default type
        this.deadline = deadline != null ? new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(deadline) : null;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.status = "ACTIVE";
        this.priority = "MEDIUM";
        this.linkedHabits = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.isArchived = false;
        this.streakDays = 0;
        this.completedAt = 0;
    }

    // Getters and Setters
    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }
    
    // Add getId() for compatibility
    public String getId() { return goalId; }
    public void setId(String id) { this.goalId = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getTargetValue() { return targetValue; }
    public void setTargetValue(double targetValue) { this.targetValue = targetValue; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { 
        this.currentValue = currentValue;
        this.updatedAt = System.currentTimeMillis();
        
        // Auto-complete if target reached
        if (currentValue >= targetValue && !isCompleted()) {
            this.status = "COMPLETED";
            this.completedAt = System.currentTimeMillis();
        }
    }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // Status handling
    public String getStatusString() { return status; }
    public void setStatusString(String status) { this.status = status; }

    public GoalStatus getStatus() { return GoalStatus.fromString(status); }
    public void setStatusEnum(GoalStatus status) { this.status = status.name(); }
    
    // Priority handling
    public String getPriorityString() { return priority; }
    public void setPriorityString(String priority) { this.priority = priority; }

    public GoalPriority getPriority() { return GoalPriority.fromString(priority); }
    public void setPriorityEnum(GoalPriority priority) { this.priority = priority.name(); }

    public List<String> getLinkedHabits() { return linkedHabits; }
    public void setLinkedHabits(List<String> linkedHabits) { this.linkedHabits = linkedHabits; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    // Helper methods
    public double getProgressPercentage() {
        if (targetValue <= 0) return 0;
        return Math.min(100, (currentValue / targetValue) * 100);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public long getDaysRemaining() {
        if (deadline == null || deadline.isEmpty()) return -1;
        
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
            Date deadlineDate = sdf.parse(deadline);
            
            if (deadlineDate == null) return -1;
            
            // Get current date at start of day (00:00:00)
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            // Get deadline date at end of day (23:59:59) to give user full day
            Calendar deadlineCalendar = Calendar.getInstance();
            deadlineCalendar.setTime(deadlineDate);
            deadlineCalendar.set(Calendar.HOUR_OF_DAY, 23);
            deadlineCalendar.set(Calendar.MINUTE, 59);
            deadlineCalendar.set(Calendar.SECOND, 59);
            deadlineCalendar.set(Calendar.MILLISECOND, 999);
            
            // Calculate the difference in milliseconds
            long diffInMillis = deadlineCalendar.getTimeInMillis() - today.getTimeInMillis();
            
            // Convert to days and round properly
            long daysRemaining = diffInMillis / (24 * 60 * 60 * 1000L);
            
            // Add debugging
            System.out.println("DEBUG: Goal '" + title + "' - Today: " + today.getTime() + 
                             ", Deadline: " + deadlineCalendar.getTime() + 
                             ", Diff in millis: " + diffInMillis + 
                             ", Days remaining: " + daysRemaining);
            
            return daysRemaining;
            
        } catch (Exception e) {
            System.out.println("DEBUG: Error calculating days remaining for goal '" + title + "': " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public boolean isOverdue() {
        return getDaysRemaining() < 0;
    }

    public boolean isDueSoon() {
        long daysRemaining = getDaysRemaining();
        return daysRemaining >= 0 && daysRemaining <= 7;
    }

    public String getTimeFrameDescription() {
        long daysRemaining = getDaysRemaining();
        
        if (isCompleted()) {
            return "Completed";
        } else if (daysRemaining < -1) {
            return "Overdue by " + Math.abs(daysRemaining) + " days";
        } else if (daysRemaining == -1) {
            return "Overdue by 1 day";
        } else if (daysRemaining == 0) {
            return "Due today";
        } else if (daysRemaining == 1) {
            return "Due tomorrow";
        } else if (daysRemaining <= 7) {
            return "Due in " + daysRemaining + " days";
        } else if (daysRemaining <= 30) {
            return "Due in " + daysRemaining + " days";
        } else {
            long weeks = daysRemaining / 7;
            return "Due in " + weeks + " week" + (weeks != 1 ? "s" : "");
    }
    }

    // Legacy date methods for backwards compatibility
    public Date getCreatedAtDate() { return new Date(createdAt); }
    public void setCreatedAtDate(Date createdAt) { this.createdAt = createdAt != null ? createdAt.getTime() : System.currentTimeMillis(); }
    
    public Date getUpdatedAtDate() { return new Date(updatedAt); }
    public void setUpdatedAtDate(Date updatedAt) { this.updatedAt = updatedAt != null ? updatedAt.getTime() : System.currentTimeMillis(); }
    
    public Date getCompletedAtDate() { return completedAt > 0 ? new Date(completedAt) : null; }
    public void setCompletedAtDate(Date completedAt) { this.completedAt = completedAt != null ? completedAt.getTime() : 0; }
} 