package com.example.flowfits.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.flowfits.firebase.FirebaseManager;
import com.example.flowfits.models.Habit;
import com.example.flowfits.models.HabitLog;
import com.example.flowfits.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HabitRepository {
    private static final String TAG = "HabitRepository";
    
    private final DatabaseReference databaseRef;
    private final FirebaseAuth auth;
    private final FirebaseManager firebaseManager;
    
    private final MutableLiveData<List<Habit>> habitsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<HabitLog>> habitLogsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    
    public HabitRepository() {
        firebaseManager = FirebaseManager.getInstance();
        databaseRef = firebaseManager.getDatabaseReference();
        auth = FirebaseAuth.getInstance();
    }
    
    // LiveData getters
    public LiveData<List<Habit>> getHabitsLiveData() { return habitsLiveData; }
    public LiveData<List<HabitLog>> getHabitLogsLiveData() { return habitLogsLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    
    // === HABIT CRUD OPERATIONS ===
    
    public void createHabit(Habit habit, RepositoryCallback<String> callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated. Please log in again.");
            return;
        }
        
        if (habit == null) {
            callback.onError("Habit data is invalid");
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        habit.setUserId(userId);
        habit.setCreatedAt(new Date());
        habit.setUpdatedAt(new Date());
        
        // Ensure required fields are set
        if (habit.getName() == null || habit.getName().trim().isEmpty()) {
            callback.onError("Habit name is required");
            return;
        }
        
        if (habit.getFrequency() == null) {
            callback.onError("Habit frequency is required");
            return;
        }
        
        Log.d(TAG, "Creating habit: " + habit.getName() + " for user: " + userId);
        
        // Generate a new key for the habit
        DatabaseReference newHabitRef = databaseRef.child(Constants.HABITS_COLLECTION).push();
        String habitId = newHabitRef.getKey();
        habit.setHabitId(habitId);
        
        newHabitRef.setValue(habit)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Habit created successfully: " + habitId);
                    callback.onSuccess(habitId);
                    loadUserHabits(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating habit", e);
                    callback.onError("Failed to create habit: " + e.getMessage());
                });
    }
    
    public void updateHabit(Habit habit, RepositoryCallback<Void> callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated. Please log in again.");
            return;
        }
        
        if (habit == null) {
            callback.onError("Habit data is invalid");
            return;
        }
        
        if (habit.getHabitId() == null || habit.getHabitId().trim().isEmpty()) {
            callback.onError("Habit ID is required for update");
            return;
        }
        
        // Ensure required fields are set
        if (habit.getName() == null || habit.getName().trim().isEmpty()) {
            callback.onError("Habit name is required");
            return;
        }
        
        if (habit.getFrequency() == null) {
            callback.onError("Habit frequency is required");
            return;
        }
        
        habit.setUpdatedAt(new Date());
        
        Log.d(TAG, "Updating habit: " + habit.getHabitId() + " - " + habit.getName());
        
        databaseRef.child(Constants.HABITS_COLLECTION)
                .child(habit.getHabitId())
                .setValue(habit)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Habit updated successfully: " + habit.getHabitId());
                    callback.onSuccess(null);
                    loadUserHabits(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating habit", e);
                    callback.onError("Failed to update habit: " + e.getMessage());
                });
    }
    
    public void deleteHabit(String habitId, RepositoryCallback<Void> callback) {
        if (habitId == null) {
            callback.onError("Habit ID is required");
            return;
        }
        
        // First delete all associated habit logs
        deleteAllHabitLogs(habitId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Then delete the habit itself
                databaseRef.child(Constants.HABITS_COLLECTION)
                        .child(habitId)
                        .removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Habit deleted successfully: " + habitId);
                            callback.onSuccess(null);
                            loadUserHabits(); // Refresh the list
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error deleting habit", e);
                            callback.onError("Failed to delete habit: " + e.getMessage());
                        });
            }
            
            @Override
            public void onError(String error) {
                callback.onError("Failed to delete habit logs: " + error);
            }
        });
    }
    
    public void loadUserHabits() {
        if (auth.getCurrentUser() == null) {
            errorLiveData.setValue("User not authenticated");
            return;
        }
        
        loadingLiveData.setValue(true);
        String userId = auth.getCurrentUser().getUid();
        
        databaseRef.child(Constants.HABITS_COLLECTION)
                .orderByChild("userId")
                .equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        loadingLiveData.setValue(false);
                        
                        List<Habit> habitsList = new ArrayList<>();
                        for (DataSnapshot habitSnapshot : dataSnapshot.getChildren()) {
                            try {
                                Habit habit = habitSnapshot.getValue(Habit.class);
                                if (habit != null) {
                                    habit.setHabitId(habitSnapshot.getKey());
                                    habitsList.add(habit);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error deserializing habit with key " + habitSnapshot.getKey(), e);
                            }
                        }
                        
                        Log.d(TAG, "Loaded " + habitsList.size() + " habits");
                        habitsLiveData.setValue(habitsList);
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        loadingLiveData.setValue(false);
                        Log.e(TAG, "Error loading habits", databaseError.toException());
                        errorLiveData.setValue("Failed to load habits: " + databaseError.getMessage());
                    }
                });
    }
    
    public void getHabitById(String habitId, RepositoryCallback<Habit> callback) {
        if (habitId == null) {
            callback.onError("Habit ID is required");
            return;
        }
        
        databaseRef.child(Constants.HABITS_COLLECTION)
                .child(habitId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Habit habit = dataSnapshot.getValue(Habit.class);
                        if (habit != null) {
                            habit.setHabitId(dataSnapshot.getKey());
                            callback.onSuccess(habit);
                        } else {
                            callback.onError("Habit not found");
                        }
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error getting habit by ID", databaseError.toException());
                        callback.onError("Failed to get habit: " + databaseError.getMessage());
                    }
                });
    }
    
    // === HABIT LOG OPERATIONS ===
    
    public void logHabitCompletion(String habitId, boolean completed, RepositoryCallback<String> callback) {
        logHabitCompletion(habitId, completed, 0.0, 0.0, null, callback);
    }
    
    public void logHabitCompletion(String habitId, boolean completed, double progressValue, double targetValue, String unit, RepositoryCallback<String> callback) {
        // First check if there's already a log for today
        getTodaysHabitLog(habitId, new RepositoryCallback<HabitLog>() {
            @Override
            public void onSuccess(HabitLog existingLog) {
                if (existingLog != null) {
                    // Update existing log
                    existingLog.setCompleted(completed);
                    existingLog.setProgressValue(progressValue);
                    existingLog.setTargetValue(targetValue);
                    existingLog.setUnit(unit);
                    existingLog.setLoggedAt(new Date());
                    
                    updateHabitLog(existingLog, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            updateHabitStreaks(habitId, completed, callback);
                        }
                        
                        @Override
                        public void onError(String error) {
                            callback.onError(error);
                        }
                    });
                } else {
                    // Create new log
                    HabitLog habitLog = createHabitLogObject(habitId, completed, progressValue, targetValue, unit);
                    createHabitLog(habitLog, new RepositoryCallback<String>() {
                        @Override
                        public void onSuccess(String logId) {
                            updateHabitStreaks(habitId, completed, callback);
                        }
                        
                        @Override
                        public void onError(String error) {
                            callback.onError(error);
                        }
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                callback.onError("Failed to check existing log: " + error);
            }
        });
    }
    
    private void createHabitLog(HabitLog habitLog, RepositoryCallback<String> callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        habitLog.setUserId(userId);
        habitLog.setLoggedAt(new Date());
        
        // Generate a new key for the habit log
        DatabaseReference newLogRef = databaseRef.child(Constants.HABIT_LOGS_COLLECTION).push();
        String logId = newLogRef.getKey();
        habitLog.setLogId(logId);
        
        newLogRef.setValue(habitLog)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Habit log created successfully: " + logId);
                    callback.onSuccess(logId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating habit log", e);
                    callback.onError("Failed to create habit log: " + e.getMessage());
                });
    }
    
    private void updateHabitLog(HabitLog habitLog, RepositoryCallback<Void> callback) {
        if (habitLog.getLogId() == null) {
            callback.onError("Log ID is required for update");
            return;
        }
        
        habitLog.setLoggedAt(new Date());
        
        databaseRef.child(Constants.HABIT_LOGS_COLLECTION)
                .child(habitLog.getLogId())
                .setValue(habitLog)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Habit log updated successfully: " + habitLog.getLogId());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating habit log", e);
                    callback.onError("Failed to update habit log: " + e.getMessage());
                });
    }
    
    public void getTodaysHabitLog(String habitId, RepositoryCallback<HabitLog> callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        databaseRef.child(Constants.HABIT_LOGS_COLLECTION)
                .orderByChild("userId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        HabitLog todaysLog = null;
                        
                        for (DataSnapshot logSnapshot : dataSnapshot.getChildren()) {
                            try {
                                HabitLog log = logSnapshot.getValue(HabitLog.class);
                                if (log != null && 
                                    habitId.equals(log.getHabitId()) &&
                                    todayDate.equals(log.getDateString())) {
                                    log.setLogId(logSnapshot.getKey());
                                    todaysLog = log;
                                    break;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error deserializing habit log", e);
                            }
                        }
                        
                        callback.onSuccess(todaysLog);
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error getting today's habit log", databaseError.toException());
                        callback.onError("Failed to get today's log: " + databaseError.getMessage());
                    }
                });
    }
    
    public void loadUserHabitLogs() {
        if (auth.getCurrentUser() == null) {
            errorLiveData.setValue("User not authenticated");
            return;
        }
        
        loadUserHabitLogs(null);
    }
    
    public void loadUserHabitLogs(String habitId) {
        if (auth.getCurrentUser() == null) {
            errorLiveData.setValue("User not authenticated");
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        
        databaseRef.child(Constants.HABIT_LOGS_COLLECTION)
                .orderByChild("userId")
                .equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<HabitLog> logsList = new ArrayList<>();
                        
                        for (DataSnapshot logSnapshot : dataSnapshot.getChildren()) {
                            try {
                                HabitLog log = logSnapshot.getValue(HabitLog.class);
                                if (log != null) {
                                    log.setLogId(logSnapshot.getKey());
                                    
                                    // Filter by habitId if specified
                                    if (habitId == null || habitId.equals(log.getHabitId())) {
                                        logsList.add(log);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error deserializing habit log", e);
                            }
                        }
                        
                        habitLogsLiveData.setValue(logsList);
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error loading habit logs", databaseError.toException());
                        errorLiveData.setValue("Failed to load habit logs: " + databaseError.getMessage());
                    }
                });
    }
    
    private void deleteAllHabitLogs(String habitId, RepositoryCallback<Void> callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        
        databaseRef.child(Constants.HABIT_LOGS_COLLECTION)
                .orderByChild("userId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> logsToDelete = new ArrayList<>();
                        
                        for (DataSnapshot logSnapshot : dataSnapshot.getChildren()) {
                            try {
                                HabitLog log = logSnapshot.getValue(HabitLog.class);
                                if (log != null && habitId.equals(log.getHabitId())) {
                                    logsToDelete.add(logSnapshot.getKey());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error checking habit log for deletion", e);
                            }
                        }
                        
                        if (logsToDelete.isEmpty()) {
                            callback.onSuccess(null);
                            return;
                        }
                        
                        // Delete all logs for this habit
                        Map<String, Object> updates = new HashMap<>();
                        for (String logId : logsToDelete) {
                            updates.put(Constants.HABIT_LOGS_COLLECTION + "/" + logId, null);
                        }
                        
                        databaseRef.updateChildren(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Deleted " + logsToDelete.size() + " habit logs");
                                    callback.onSuccess(null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error deleting habit logs", e);
                                    callback.onError("Failed to delete habit logs: " + e.getMessage());
                                });
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error querying habit logs for deletion", databaseError.toException());
                        callback.onError("Failed to query habit logs: " + databaseError.getMessage());
                    }
                });
    }
    
    private void updateHabitStreaks(String habitId, boolean completed, RepositoryCallback<String> callback) {
        getHabitById(habitId, new RepositoryCallback<Habit>() {
            @Override
            public void onSuccess(Habit habit) {
                if (habit.getStreakData() == null) {
                    habit.setStreakData(new Habit.StreakData());
                }
                
                Habit.StreakData streakData = habit.getStreakData();
                
                if (completed) {
                    // Increment current streak
                    streakData.setCurrentStreak(streakData.getCurrentStreak() + 1);
                    
                    // Update longest streak if necessary
                    if (streakData.getCurrentStreak() > streakData.getLongestStreak()) {
                        streakData.setLongestStreak(streakData.getCurrentStreak());
                    }
                    
                    streakData.setLastCompletedDate(new Date());
                } else {
                    // Reset current streak
                    streakData.setCurrentStreak(0);
                }
                
                habit.setUpdatedAt(new Date());
                
                // Update the habit in the database
                updateHabit(habit, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        callback.onSuccess("Habit streaks updated successfully");
                    }
                    
                    @Override
                    public void onError(String error) {
                        callback.onError("Failed to update streaks: " + error);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                callback.onError("Failed to get habit for streak update: " + error);
            }
        });
    }
    
    public void toggleHabitStatus(String habitId, RepositoryCallback<Void> callback) {
        getHabitById(habitId, new RepositoryCallback<Habit>() {
            @Override
            public void onSuccess(Habit habit) {
                habit.setActive(!habit.isActive());
                updateHabit(habit, callback);
            }
            
            @Override
            public void onError(String error) {
                callback.onError("Failed to get habit: " + error);
            }
        });
    }
    
    public void getHabitsForToday(RepositoryCallback<List<Habit>> callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String todayDayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date()).toLowerCase();
        
        databaseRef.child(Constants.HABITS_COLLECTION)
                .orderByChild("userId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Habit> todaysHabits = new ArrayList<>();
                        
                        for (DataSnapshot habitSnapshot : dataSnapshot.getChildren()) {
                            try {
                                Habit habit = habitSnapshot.getValue(Habit.class);
                                if (habit != null && habit.isActive()) {
                                    habit.setHabitId(habitSnapshot.getKey());
                                    
                                    // Check if habit is scheduled for today
                                    if (isHabitScheduledForToday(habit, todayDayOfWeek)) {
                                        todaysHabits.add(habit);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error deserializing habit", e);
                            }
                        }
                        
                        callback.onSuccess(todaysHabits);
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error getting today's habits", databaseError.toException());
                        callback.onError("Failed to get today's habits: " + databaseError.getMessage());
                    }
                });
    }
    
    // Helper methods
    private HabitLog createHabitLogObject(String habitId, boolean completed, double progressValue, double targetValue, String unit) {
        HabitLog habitLog = new HabitLog();
        habitLog.setHabitId(habitId);
        habitLog.setCompleted(completed);
        habitLog.setProgressValue(progressValue);
        habitLog.setTargetValue(targetValue);
        habitLog.setUnit(unit);
        habitLog.setDate(new Date());
        return habitLog;
    }
    
    private boolean isHabitScheduledForToday(Habit habit, String todayDayOfWeek) {
        if (habit.getFrequency() == null) {
            return false;
        }
        
        switch (habit.getFrequency()) {
            case DAILY:
                return true;
            case WEEKLY:
                return habit.getCustomDays() != null && habit.getCustomDays().contains(todayDayOfWeek);
            case CUSTOM:
                return habit.getCustomDays() != null && habit.getCustomDays().contains(todayDayOfWeek);
            default:
                return false;
        }
    }
    
    // Callback interface
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
} 