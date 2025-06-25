package com.example.flowfits.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.example.flowfits.firebase.FirebaseManager;
import com.example.flowfits.models.Goal;
import com.example.flowfits.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class GoalRepository {
    private static GoalRepository instance;
    private DatabaseReference databaseRef;
    private MutableLiveData<List<Goal>> goals;
    private FirebaseManager firebaseManager;

    public GoalRepository() {
        firebaseManager = FirebaseManager.getInstance();
        databaseRef = firebaseManager.getDatabaseReference();
        goals = new MutableLiveData<>();
    }

    public static synchronized GoalRepository getInstance() {
        if (instance == null) {
            instance = new GoalRepository();
        }
        return instance;
    }

    public MutableLiveData<List<Goal>> getGoals() {
        return goals;
    }
    
    public void getAllGoals(GoalsListCallback callback) {
        String currentUserId = firebaseManager.getCurrentUserId();
        System.out.println("DEBUG: GoalRepository - getAllGoals called for user: " + currentUserId);
        
        if (currentUserId == null) {
            System.out.println("DEBUG: GoalRepository - User not logged in");
            if (callback != null) {
                callback.onFailure("User not logged in");
            }
            return;
        }
        
        System.out.println("DEBUG: GoalRepository - Querying goals with userId: " + currentUserId);
        databaseRef.child(Constants.GOALS_COLLECTION)
                .orderByChild("userId")
                .equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        System.out.println("DEBUG: GoalRepository - Firebase response received, children count: " + dataSnapshot.getChildrenCount());
                        List<Goal> goalsList = new ArrayList<>();
                        for (DataSnapshot goalSnapshot : dataSnapshot.getChildren()) {
                            try {
                                System.out.println("DEBUG: GoalRepository - Processing snapshot with key: " + goalSnapshot.getKey());
                                System.out.println("DEBUG: GoalRepository - Raw data: " + goalSnapshot.getValue());
                                
                                Goal goal = goalSnapshot.getValue(Goal.class);
                                if (goal != null) {
                                    goal.setGoalId(goalSnapshot.getKey());
                                    goalsList.add(goal);
                                    System.out.println("DEBUG: GoalRepository - Successfully loaded goal: " + goal.getTitle() + " (ID: " + goal.getGoalId() + ") (UserId: " + goal.getUserId() + ")");
                                } else {
                                    System.out.println("DEBUG: GoalRepository - Null goal encountered in snapshot with key: " + goalSnapshot.getKey());
                                }
                            } catch (Exception e) {
                                System.out.println("DEBUG: GoalRepository - Error deserializing goal with key " + goalSnapshot.getKey() + ": " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        System.out.println("DEBUG: GoalRepository - Total goals loaded: " + goalsList.size());
                        if (callback != null) {
                            callback.onSuccess(goalsList);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("DEBUG: GoalRepository - Firebase query cancelled: " + databaseError.getMessage());
                        if (callback != null) {
                            callback.onFailure(databaseError.getMessage());
                        }
                    }
                });
    }

    public void loadUserGoals(String userId) {
        databaseRef.child(Constants.GOALS_COLLECTION)
                .orderByChild("userId")
                .equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Goal> goalsList = new ArrayList<>();
                        for (DataSnapshot goalSnapshot : dataSnapshot.getChildren()) {
                            Goal goal = goalSnapshot.getValue(Goal.class);
                            if (goal != null) {
                                goal.setGoalId(goalSnapshot.getKey());
                                goalsList.add(goal);
                            }
                        }
                        goals.setValue(goalsList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                        goals.setValue(null);
                    }
                });
    }

    public void addGoal(Goal goal, GoalCallback callback) {
        String currentUserId = firebaseManager.getCurrentUserId();
        System.out.println("DEBUG: GoalRepository - addGoal called for user: " + currentUserId);
        
        if (currentUserId == null) {
            System.out.println("DEBUG: GoalRepository - User not logged in");
            if (callback != null) {
                callback.onFailure("User not logged in");
            }
            return;
        }
        
        // Set user ID if not already set
        if (goal.getUserId() == null) {
            goal.setUserId(currentUserId);
        }
        
        // Generate a new key for the goal
        DatabaseReference newGoalRef = databaseRef.child(Constants.GOALS_COLLECTION).push();
        String goalId = newGoalRef.getKey();
        goal.setGoalId(goalId);
        
        System.out.println("DEBUG: GoalRepository - Attempting to save goal:");
        System.out.println("  Goal ID: " + goal.getGoalId());
        System.out.println("  User ID: " + goal.getUserId());
        System.out.println("  Title: " + goal.getTitle());
        System.out.println("  Priority: " + goal.getPriorityString());
        System.out.println("  Status: " + goal.getStatusString());
        System.out.println("  CreatedAt: " + goal.getCreatedAt());
        System.out.println("  UpdatedAt: " + goal.getUpdatedAt());
        
        newGoalRef.setValue(goal)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("DEBUG: GoalRepository - Goal saved successfully to Firebase");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("DEBUG: GoalRepository - Failed to save goal: " + e.getMessage());
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void updateGoal(Goal goal, GoalCallback callback) {
        if (goal.getGoalId() == null) {
            if (callback != null) {
                callback.onFailure("Goal ID is required for update");
            }
            return;
        }

        databaseRef.child(Constants.GOALS_COLLECTION)
                .child(goal.getGoalId())
                .setValue(goal)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void deleteGoal(String goalId, OnGoalDeletedListener listener) {
        databaseRef.child(Constants.GOALS_COLLECTION)
                .child(goalId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    // Debug method to clear all goals for current user
    public void clearAllGoals(GoalCallback callback) {
        String currentUserId = firebaseManager.getCurrentUserId();
        System.out.println("DEBUG: GoalRepository - clearAllGoals called for user: " + currentUserId);
        
        if (currentUserId == null) {
            if (callback != null) {
                callback.onFailure("User not logged in");
            }
            return;
        }
        
        databaseRef.child(Constants.GOALS_COLLECTION)
                .orderByChild("userId")
                .equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        System.out.println("DEBUG: GoalRepository - Found " + dataSnapshot.getChildrenCount() + " goals to delete");
                        
                        if (dataSnapshot.getChildrenCount() == 0) {
                            if (callback != null) {
                                callback.onSuccess();
                            }
                            return;
                        }
                        
                        int totalGoals = (int) dataSnapshot.getChildrenCount();
                        int[] deletedCount = {0};
                        
                        for (DataSnapshot goalSnapshot : dataSnapshot.getChildren()) {
                            goalSnapshot.getRef().removeValue()
                                    .addOnCompleteListener(task -> {
                                        deletedCount[0]++;
                                        if (deletedCount[0] >= totalGoals) {
                                            System.out.println("DEBUG: GoalRepository - All goals cleared successfully");
                                            if (callback != null) {
                                                callback.onSuccess();
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("DEBUG: GoalRepository - Failed to clear goals: " + databaseError.getMessage());
                        if (callback != null) {
                            callback.onFailure(databaseError.getMessage());
                        }
                    }
                });
    }

    // Callback interfaces
    public interface GoalCallback {
        void onSuccess();
        void onFailure(String error);
    }
    
    public interface GoalsListCallback {
        void onSuccess(List<Goal> goals);
        void onFailure(String error);
    }

    public interface OnGoalAddedListener {
        void onSuccess(Goal goal);
        void onFailure(String error);
    }

    public interface OnGoalUpdatedListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnGoalDeletedListener {
        void onSuccess();
        void onFailure(String error);
    }
} 