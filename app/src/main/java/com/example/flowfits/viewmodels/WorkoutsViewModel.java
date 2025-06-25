package com.example.flowfits.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.flowfits.models.Workout;
import com.example.flowfits.repositories.WorkoutRepository;

import java.util.Date;
import java.util.List;

public class WorkoutsViewModel extends ViewModel {
    private WorkoutRepository workoutRepository;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<String> successMessage;

    public WorkoutsViewModel() {
        workoutRepository = WorkoutRepository.getInstance();
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        successMessage = new MutableLiveData<>();
    }

    public LiveData<List<Workout>> getWorkouts() {
        return workoutRepository.getWorkoutsLiveData();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public void loadWorkouts() {
        isLoading.setValue(true);
        workoutRepository.loadUserWorkouts();
        isLoading.setValue(false);
    }

    public void saveWorkout(Workout workout) {
        isLoading.setValue(true);
        workoutRepository.saveWorkout(workout, new WorkoutRepository.WorkoutCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                successMessage.setValue("Workout saved successfully!");
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to save workout: " + error);
            }
        });
    }

    public void updateWorkout(Workout workout) {
        isLoading.setValue(true);
        workoutRepository.updateWorkout(workout, new WorkoutRepository.WorkoutCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                successMessage.setValue("Workout updated successfully!");
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to update workout: " + error);
            }
        });
    }

    public void deleteWorkout(String workoutId) {
        isLoading.setValue(true);
        workoutRepository.deleteWorkout(workoutId, new WorkoutRepository.WorkoutCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                successMessage.setValue("Workout deleted successfully!");
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to delete workout: " + error);
            }
        });
    }

    public void getWorkoutById(String workoutId, WorkoutRepository.WorkoutByIdCallback callback) {
        workoutRepository.getWorkoutById(workoutId, callback);
    }

    public void getWorkoutsByDateRange(Date startDate, Date endDate, WorkoutRepository.WorkoutListCallback callback) {
        workoutRepository.getWorkoutsByDateRange(startDate, endDate, callback);
    }

    public void clearMessages() {
        errorMessage.setValue(null);
        successMessage.setValue(null);
    }
} 