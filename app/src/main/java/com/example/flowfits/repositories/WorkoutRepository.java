package com.example.flowfits.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.example.flowfits.firebase.FirebaseManager;
import com.example.flowfits.models.Workout;
import com.example.flowfits.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkoutRepository {
    private static WorkoutRepository instance;
    private DatabaseReference databaseRef;
    private FirebaseManager firebaseManager;
    private MutableLiveData<List<Workout>> workoutsLiveData;

    public WorkoutRepository() {
        firebaseManager = FirebaseManager.getInstance();
        databaseRef = firebaseManager.getDatabaseReference();
        workoutsLiveData = new MutableLiveData<>();
    }

    public static synchronized WorkoutRepository getInstance() {
        if (instance == null) {
            instance = new WorkoutRepository();
        }
        return instance;
    }

    public MutableLiveData<List<Workout>> getWorkoutsLiveData() {
        return workoutsLiveData;
    }

    public void saveWorkout(Workout workout, WorkoutCallback callback) {
        String userId = firebaseManager.getCurrentUserId();
        if (userId == null) {
            if (callback != null) callback.onFailure("User not authenticated");
            return;
        }

        if (workout.getWorkoutId() == null) {
            // Generate new workout ID
            String workoutId = databaseRef.child(Constants.WORKOUTS_COLLECTION).push().getKey();
            workout.setWorkoutId(workoutId);
        }

        workout.setUserId(userId);
        if (workout.getDate() == null) {
            workout.setDate(new Date());
        }
        
        // Ensure exercise totals are calculated
        if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
            workout.calculateTotals();
        }

        databaseRef.child(Constants.WORKOUTS_COLLECTION)
                .child(workout.getWorkoutId())
                .setValue(workout)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                    // Refresh workout list
                    loadUserWorkouts();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void updateWorkout(Workout workout, WorkoutCallback callback) {
        if (workout.getWorkoutId() == null) {
            if (callback != null) callback.onFailure("Workout ID is required for update");
            return;
        }

        databaseRef.child(Constants.WORKOUTS_COLLECTION)
                .child(workout.getWorkoutId())
                .setValue(workout)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                    // Refresh workout list
                    loadUserWorkouts();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void deleteWorkout(String workoutId, WorkoutCallback callback) {
        databaseRef.child(Constants.WORKOUTS_COLLECTION)
                .child(workoutId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                    // Refresh workout list
                    loadUserWorkouts();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void loadUserWorkouts() {
        String userId = firebaseManager.getCurrentUserId();
        if (userId == null) {
            workoutsLiveData.setValue(new ArrayList<>());
            return;
        }

        Query query = databaseRef.child(Constants.WORKOUTS_COLLECTION)
                .orderByChild("userId")
                .equalTo(userId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Workout> workouts = new ArrayList<>();
                for (DataSnapshot workoutSnapshot : dataSnapshot.getChildren()) {
                    Workout workout = workoutSnapshot.getValue(Workout.class);
                    if (workout != null) {
                        workout.setWorkoutId(workoutSnapshot.getKey());
                        
                        // Ensure totals are calculated for display
                        if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
                            workout.calculateTotals();
                        }
                        
                        workouts.add(workout);
                    }
                }
                
                // Sort workouts by date (newest first)
                workouts.sort((w1, w2) -> {
                    if (w1.getDate() == null && w2.getDate() == null) return 0;
                    if (w1.getDate() == null) return 1;
                    if (w2.getDate() == null) return -1;
                    return w2.getDate().compareTo(w1.getDate());
                });
                
                workoutsLiveData.setValue(workouts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                workoutsLiveData.setValue(new ArrayList<>());
            }
        });
    }

    public void getWorkoutById(String workoutId, WorkoutByIdCallback callback) {
        databaseRef.child(Constants.WORKOUTS_COLLECTION)
                .child(workoutId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Workout workout = dataSnapshot.getValue(Workout.class);
                        if (workout != null) {
                            workout.setWorkoutId(dataSnapshot.getKey());
                            if (callback != null) callback.onSuccess(workout);
                        } else {
                            if (callback != null) callback.onFailure("Workout not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if (callback != null) callback.onFailure(databaseError.getMessage());
                    }
                });
    }

    public void getWorkoutsByDateRange(Date startDate, Date endDate, WorkoutListCallback callback) {
        String userId = firebaseManager.getCurrentUserId();
        if (userId == null) {
            if (callback != null) callback.onFailure("User not authenticated");
            return;
        }

        Query query = databaseRef.child(Constants.WORKOUTS_COLLECTION)
                .orderByChild("userId")
                .equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Workout> workouts = new ArrayList<>();
                for (DataSnapshot workoutSnapshot : dataSnapshot.getChildren()) {
                    Workout workout = workoutSnapshot.getValue(Workout.class);
                    if (workout != null && workout.getDate() != null) {
                        if (workout.getDate().compareTo(startDate) >= 0 && 
                            workout.getDate().compareTo(endDate) <= 0) {
                            workout.setWorkoutId(workoutSnapshot.getKey());
                            workouts.add(workout);
                        }
                    }
                }
                
                // Sort by date
                workouts.sort((w1, w2) -> w2.getDate().compareTo(w1.getDate()));
                
                if (callback != null) callback.onSuccess(workouts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null) callback.onFailure(databaseError.getMessage());
            }
        });
    }

    // Callback interfaces
    public interface WorkoutCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface WorkoutByIdCallback {
        void onSuccess(Workout workout);
        void onFailure(String error);
    }

    public interface WorkoutListCallback {
        void onSuccess(List<Workout> workouts);
        void onFailure(String error);
    }
    
    // New method to get workouts by exercise name for analytics
    public void getWorkoutsByExercise(String exerciseName, WorkoutListCallback callback) {
        String userId = firebaseManager.getCurrentUserId();
        if (userId == null) {
            if (callback != null) callback.onFailure("User not authenticated");
            return;
        }

        Query query = databaseRef.child(Constants.WORKOUTS_COLLECTION)
                .orderByChild("userId")
                .equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Workout> matchingWorkouts = new ArrayList<>();
                for (DataSnapshot workoutSnapshot : dataSnapshot.getChildren()) {
                    Workout workout = workoutSnapshot.getValue(Workout.class);
                    if (workout != null && workout.getExercises() != null) {
                        workout.setWorkoutId(workoutSnapshot.getKey());
                        
                        // Check if any exercise matches the search
                        for (Workout.Exercise exercise : workout.getExercises()) {
                            if (exercise.getName() != null && 
                                exercise.getName().toLowerCase().contains(exerciseName.toLowerCase())) {
                                workout.calculateTotals();
                                matchingWorkouts.add(workout);
                                break; // Found match, move to next workout
                            }
                        }
                    }
                }
                
                if (callback != null) callback.onSuccess(matchingWorkouts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null) callback.onFailure(databaseError.getMessage());
            }
        });
    }

    public void fetchAllWorkouts(WorkoutListCallback callback) {
        String userId = firebaseManager.getCurrentUserId();
        if (userId == null) {
            if (callback != null) callback.onFailure("User not authenticated");
            return;
        }

        Query query = databaseRef.child(Constants.WORKOUTS_COLLECTION)
                .orderByChild("userId")
                .equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Workout> workouts = new ArrayList<>();
                for (DataSnapshot workoutSnapshot : dataSnapshot.getChildren()) {
                    Workout workout = workoutSnapshot.getValue(Workout.class);
                    if (workout != null) {
                        workout.setWorkoutId(workoutSnapshot.getKey());
                        // Ensure totals are calculated for display
                        if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
                            workout.calculateTotals();
                        }
                        workouts.add(workout);
                    }
                }
                // Sort workouts by date (newest first)
                workouts.sort((w1, w2) -> {
                    if (w1.getDate() == null && w2.getDate() == null) return 0;
                    if (w1.getDate() == null) return 1;
                    if (w2.getDate() == null) return -1;
                    return w2.getDate().compareTo(w1.getDate());
                });
                if (callback != null) callback.onSuccess(workouts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null) callback.onFailure(databaseError.getMessage());
            }
        });
    }
} 