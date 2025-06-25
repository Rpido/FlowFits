package com.example.flowfits.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.flowfits.models.AnalyticsData;
import com.example.flowfits.repositories.AnalyticsRepository;

public class AnalyticsViewModel extends ViewModel {
    
    private AnalyticsRepository analyticsRepository;
    private MutableLiveData<AnalyticsData> analyticsData;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<String> warningMessage;

    public AnalyticsViewModel() {
        // Initialize repository
        analyticsRepository = AnalyticsRepository.getInstance();
        
        // Initialize LiveData
        analyticsData = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        warningMessage = new MutableLiveData<>();
        
        // Load analytics data
        loadAnalytics();
    }

    public LiveData<AnalyticsData> getAnalyticsData() {
        return analyticsData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getWarningMessage() {
        return warningMessage;
    }

    public void loadAnalytics() {
        isLoading.setValue(true);
        clearMessages();
        
        analyticsRepository.getAnalyticsData(new AnalyticsRepository.AnalyticsCallback() {
            @Override
            public void onSuccess(AnalyticsData data) {
                analyticsData.postValue(data);
                isLoading.postValue(false);
            }
            
            @Override
            public void onPartialSuccess(AnalyticsData data, String warning) {
                analyticsData.postValue(data);
                warningMessage.postValue(warning);
                isLoading.postValue(false);
            }
            
            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }

    public void refreshAnalytics() {
        loadAnalytics();
    }

    public void clearMessages() {
        errorMessage.setValue(null);
        warningMessage.setValue(null);
    }

    // Utility methods for quick access to key metrics
    public int getTotalWorkouts() {
        AnalyticsData data = analyticsData.getValue();
        return data != null ? data.getTotalWorkouts() : 0;
    }

    public int getTotalCalories() {
        AnalyticsData data = analyticsData.getValue();
        return data != null ? data.getTotalCaloriesBurned() : 0;
    }

    public int getGoalsCompleted() {
        AnalyticsData data = analyticsData.getValue();
        return data != null ? data.getGoalsCompleted() : 0;
    }

    public int getCurrentStreak() {
        AnalyticsData data = analyticsData.getValue();
        return data != null ? data.getCurrentStreak() : 0;
    }

    public String getFormattedExerciseTime() {
        AnalyticsData data = analyticsData.getValue();
        if (data == null) return "0 min";
        
        int totalMinutes = data.getTotalExerciseTime();
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%d min", minutes);
        }
    }

    public double getWeeklyAverage(String metric) {
        AnalyticsData data = analyticsData.getValue();
        if (data == null || data.getWeeklyData() == null) return 0.0;
        
        double total = 0;
        int count = 0;
        
        for (AnalyticsData.DayData dayData : data.getWeeklyData()) {
            switch (metric.toLowerCase()) {
                case "workouts":
                    total += dayData.getWorkouts();
                    break;
                case "calories":
                    total += dayData.getCalories();
                    break;
                case "minutes":
                    total += dayData.getExerciseMinutes();
                    break;
            }
            count++;
        }
        
        return count > 0 ? total / count : 0.0;
    }
} 