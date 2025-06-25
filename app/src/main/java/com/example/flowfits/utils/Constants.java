package com.example.flowfits.utils;

public class Constants {
    // Firebase Collections
    public static final String USERS_COLLECTION = "users";
    public static final String GOALS_COLLECTION = "goals";
    public static final String WORKOUTS_COLLECTION = "workouts";
    public static final String HABITS_COLLECTION = "habits";
    public static final String HABIT_LOGS_COLLECTION = "habitLogs";
    public static final String ACHIEVEMENTS_COLLECTION = "achievements";
    public static final String ANALYTICS_COLLECTION = "analytics";

    // Goal Categories
    public static final String GOAL_CATEGORY_WEIGHT_LOSS = "weight_loss";
    public static final String GOAL_CATEGORY_MUSCLE_GAIN = "muscle_gain";
    public static final String GOAL_CATEGORY_ENDURANCE = "endurance";
    public static final String GOAL_CATEGORY_STRENGTH = "strength";
    public static final String GOAL_CATEGORY_FLEXIBILITY = "flexibility";
    
    // Goal Categories Array
    public static final String[] GOAL_CATEGORIES = {
        "Weight Loss", "Muscle Gain", "Endurance", "Strength", "Flexibility", "Cardio", "Nutrition", "Sleep", "Mindfulness"
    };

    // Habit Categories
    public static final String HABIT_CATEGORY_HEALTH = "health";
    public static final String HABIT_CATEGORY_FITNESS = "fitness";
    public static final String HABIT_CATEGORY_NUTRITION = "nutrition";
    public static final String HABIT_CATEGORY_MINDFULNESS = "mindfulness";
    
    // Habit Categories Array
    public static final String[] HABIT_CATEGORIES = {
        "Fitness", "Nutrition", "Mindfulness", "Health", "Productivity", "Learning", "Sleep", "Social"
    };

    // Achievement Types
    public static final String ACHIEVEMENT_TYPE_STREAK = "streak";
    public static final String ACHIEVEMENT_TYPE_GOAL = "goal";
    public static final String ACHIEVEMENT_TYPE_WORKOUT = "workout";
    public static final String ACHIEVEMENT_TYPE_MILESTONE = "milestone";

    // Shared Preferences
    public static final String PREFS_NAME = "FlowFitsPrefs";
    public static final String PREF_USER_LOGGED_IN = "user_logged_in";
    public static final String PREF_REMINDER_TIME = "reminder_time";
    public static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";

    // Date Formats
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";
    public static final String DATE_FORMAT_API = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";

    // Intent Keys
    public static final String EXTRA_GOAL_ID = "goal_id";
    public static final String EXTRA_WORKOUT_ID = "workout_id";
    public static final String EXTRA_HABIT_ID = "habit_id";
    public static final String EXTRA_EXERCISE_NAME = "exercise_name";
    public static final String EXTRA_SHOW_EXERCISES = "show_exercises";

    // Request Codes
    public static final int REQUEST_CODE_LOGIN = 1001;
    public static final int REQUEST_CODE_ADD_GOAL = 1002;
    public static final int REQUEST_CODE_ADD_WORKOUT = 1003;
    public static final int REQUEST_CODE_ADD_HABIT = 1004;

    // Notification IDs
    public static final int NOTIFICATION_ID_HABIT_REMINDER = 2001;
    public static final int NOTIFICATION_ID_GOAL_DEADLINE = 2002;

    // Exercise Categories
    public static final String[] EXERCISE_CATEGORIES = {
        "Push", "Pull", "Legs", "Core", "Cardio", "Full Body", "Arms", "Shoulders"
    };
    
    // Muscle Groups
    public static final String[] MUSCLE_GROUPS = {
        "Chest", "Back", "Shoulders", "Arms", "Legs", "Glutes", "Core", "Cardio", "Full Body"
    };
    
    // Common Exercise Units
    public static final String[] EXERCISE_UNITS = {
        "lbs", "kg", "km", "miles", "minutes", "seconds", "reps", "sets"
    };

    // Error Messages
    public static final String ERROR_NETWORK = "Network error. Please check your internet connection.";
    public static final String ERROR_GENERIC = "Something went wrong. Please try again.";
    public static final String ERROR_LOGIN_FAILED = "Login failed. Please check your credentials.";
    public static final String ERROR_EXERCISE_NAME_REQUIRED = "Exercise name is required.";
    public static final String ERROR_EXERCISE_SAVE_FAILED = "Failed to save exercise. Please try again.";
} 