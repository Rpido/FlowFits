package com.example.flowfits.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.flowfits.MainActivity;
import com.example.flowfits.R;
import com.example.flowfits.adapters.RecentWorkoutsAdapter;
import com.example.flowfits.models.AnalyticsData;
import com.example.flowfits.viewmodels.DashboardViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    
    // UI Components
    private SwipeRefreshLayout swipeRefresh;
    
    // Header Components
    private TextView tvGreetingTime;
    private TextView tvGreeting;
    
    // Enhanced Progress Components
    private TextView tvUserLevel;
    private ProgressBar progressToday;
    private TextView tvTodayPercentage;
    private TextView tvTodayDescription;
    private TextView tvWeeklyWorkouts;
    private TextView tvWorkoutsProgress;
    private ProgressBar progressWorkouts;
    private TextView tvCurrentStreak;
    private TextView tvStreakStatus;
    private TextView tvWeeklyCalories;
    private TextView tvExerciseTime;
    private TextView tvPersonalBest;
    private ProgressBar progressWeeklyGoal;
    private TextView tvWeeklyGoalPercentage;
    
    // Quick Actions
    private MaterialButton btnQuickWorkout;
    private MaterialButton btnLogHabit;
    private MaterialButton btnViewGoals;
    
    // Stats and Activity
    private TextView tvViewAnalytics;
    private RecyclerView rvRecentWorkouts;
    
    // Adapters
    private RecentWorkoutsAdapter recentWorkoutsAdapter;
    
    // Empty state
    private LinearLayout layoutEmptyActivity;
    private MaterialButton btnStartFirstWorkout;
    private TextView btnViewAllWorkouts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        
        initializeViews(view);
        setupRecyclerViews();
        setupClickListeners();
        setupObservers();
        setupGreeting();
        
        // Load initial data
        refreshData();
    }

    private void initializeViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        
        // Header
        tvGreetingTime = view.findViewById(R.id.tv_greeting_time);
        tvGreeting = view.findViewById(R.id.tv_greeting);
        
        // Enhanced Progress Components
        tvUserLevel = view.findViewById(R.id.tv_user_level);
        progressToday = view.findViewById(R.id.progress_today);
        tvTodayPercentage = view.findViewById(R.id.tv_today_percentage);
        tvTodayDescription = view.findViewById(R.id.tv_today_description);
        tvWeeklyWorkouts = view.findViewById(R.id.tv_weekly_workouts);
        tvWorkoutsProgress = view.findViewById(R.id.tv_workouts_progress);
        progressWorkouts = view.findViewById(R.id.progress_workouts);
        tvCurrentStreak = view.findViewById(R.id.tv_current_streak);
        tvStreakStatus = view.findViewById(R.id.tv_streak_status);
        tvWeeklyCalories = view.findViewById(R.id.tv_weekly_calories);
        tvExerciseTime = view.findViewById(R.id.tv_exercise_time);
        tvPersonalBest = view.findViewById(R.id.tv_personal_best);
        progressWeeklyGoal = view.findViewById(R.id.progress_weekly_goal);
        tvWeeklyGoalPercentage = view.findViewById(R.id.tv_weekly_goal_percentage);
        
        // Quick Actions
        btnQuickWorkout = view.findViewById(R.id.btn_quick_workout);
        btnLogHabit = view.findViewById(R.id.btn_log_habit);
        btnViewGoals = view.findViewById(R.id.btn_view_goals);
        
        // Stats and Activity
        tvViewAnalytics = view.findViewById(R.id.tv_view_analytics);
        rvRecentWorkouts = view.findViewById(R.id.rv_recent_workouts);
        
        // Empty state
        layoutEmptyActivity = view.findViewById(R.id.layout_empty_activity);
        btnStartFirstWorkout = view.findViewById(R.id.btn_start_first_workout);
        btnViewAllWorkouts = view.findViewById(R.id.btn_view_all_workouts);
    }

    private void setupRecyclerViews() {
        // Recent Workouts RecyclerView
        recentWorkoutsAdapter = new RecentWorkoutsAdapter(getContext(), new ArrayList<>());
        rvRecentWorkouts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecentWorkouts.setAdapter(recentWorkoutsAdapter);
    }

    private void setupClickListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshData);
        
        // Quick Actions
        btnQuickWorkout.setOnClickListener(v -> navigateToWorkouts());
        btnLogHabit.setOnClickListener(v -> navigateToHabits());
        btnViewGoals.setOnClickListener(v -> navigateToGoals());
        
        // Navigation links
        tvViewAnalytics.setOnClickListener(v -> navigateToAnalytics());
        btnStartFirstWorkout.setOnClickListener(v -> navigateToWorkouts());
        btnViewAllWorkouts.setOnClickListener(v -> navigateToWorkouts());
    }

    private void setupObservers() {
        // Analytics data observer
        viewModel.getAnalyticsData().observe(getViewLifecycleOwner(), analyticsData -> {
            if (analyticsData != null) {
                updateEnhancedProgress(analyticsData);
            }
        });
        
        // Recent workouts observer
        viewModel.getRecentWorkouts().observe(getViewLifecycleOwner(), workouts -> {
            if (workouts != null && !workouts.isEmpty()) {
                layoutEmptyActivity.setVisibility(View.GONE);
                rvRecentWorkouts.setVisibility(View.VISIBLE);
                recentWorkoutsAdapter.updateWorkouts(workouts);
            } else {
                layoutEmptyActivity.setVisibility(View.VISIBLE);
                rvRecentWorkouts.setVisibility(View.GONE);
            }
        });
        
        // Motivational message observer
        viewModel.getMotivationalMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && tvGreeting != null) {
                tvGreeting.setText(message);
            }
        });
    }

    private void setupGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        String timeGreeting;
        if (hour < 12) {
            timeGreeting = "Good Morning";
        } else if (hour < 17) {
            timeGreeting = "Good Afternoon";
        } else {
            timeGreeting = "Good Evening";
        }
        
        tvGreetingTime.setText(timeGreeting);
    }

    private void updateEnhancedProgress(AnalyticsData analyticsData) {
        // Calculate weekly and daily progress
        int weeklyWorkouts = 0;
        int weeklyCalories = 0;
        int todaysWorkouts = 0;
        int totalExerciseMinutes = 0;
        
        if (analyticsData.getWeeklyData() != null) {
            Calendar today = Calendar.getInstance();
            for (AnalyticsData.DayData day : analyticsData.getWeeklyData()) {
                weeklyWorkouts += day.getWorkouts();
                weeklyCalories += day.getCalories();
                totalExerciseMinutes += day.getExerciseMinutes();
                
                // Check if this is today's data
                Calendar dayCalendar = Calendar.getInstance();
                dayCalendar.setTime(day.getDate());
                if (dayCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                    dayCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                    todaysWorkouts = day.getWorkouts();
                }
            }
        }
        
        // Update User Level based on total workouts
        updateUserLevel(analyticsData.getTotalWorkouts());
        
        // Update Today's Progress Card
        int dailyGoal = 3; // Assuming 3 daily goals (workout, habits, nutrition)
        int completedGoals = Math.min(todaysWorkouts + 1, dailyGoal); // +1 for mock habit completion
        int todayPercentage = (completedGoals * 100) / dailyGoal;
        
        progressToday.setProgress(todayPercentage);
        tvTodayPercentage.setText(todayPercentage + "%");
        tvTodayDescription.setText(completedGoals + " of " + dailyGoal + " daily goals completed");
        
        // Update Weekly Workouts Achievement
        int weeklyGoal = 7;
        tvWeeklyWorkouts.setText(String.valueOf(weeklyWorkouts));
        tvWorkoutsProgress.setText(weeklyWorkouts + "/" + weeklyGoal + " this week");
        progressWorkouts.setMax(weeklyGoal);
        progressWorkouts.setProgress(weeklyWorkouts);
        
        // Achievement badges would be shown if we had badge views in the layout
        
        // Update Streak Information
        int currentStreak = analyticsData.getCurrentStreak();
        tvCurrentStreak.setText(String.valueOf(currentStreak));
        
        // Update streak status
        if (currentStreak >= 7) {
            tvStreakStatus.setText("On fire! 🔥");
        } else if (currentStreak >= 3) {
            tvStreakStatus.setText("Great momentum! 💪");
        } else if (currentStreak >= 1) {
            tvStreakStatus.setText("Keep going! 🌟");
        } else {
            tvStreakStatus.setText("Start today! 🚀");
        }
        
        // Update Detailed Stats
        tvWeeklyCalories.setText(String.format(Locale.getDefault(), "%,d", weeklyCalories));
        
        // Convert minutes to hours and minutes
        int hours = totalExerciseMinutes / 60;
        int minutes = totalExerciseMinutes % 60;
        tvExerciseTime.setText(hours + "h " + minutes + "m");
        
        // Mock personal best count (would need to track actual PRs)
        int personalBests = Math.min(weeklyWorkouts, 3); // Mock: max 3 PRs per week
        tvPersonalBest.setText(String.valueOf(personalBests));
        
        // Update Weekly Goal Progress
        int weeklyGoalPercentage = Math.min(100, (weeklyWorkouts * 100) / weeklyGoal);
        progressWeeklyGoal.setProgress(weeklyGoalPercentage);
        tvWeeklyGoalPercentage.setText(weeklyGoalPercentage + "%");
    }

    private void updateUserLevel(int totalWorkouts) {
        String level;
        if (totalWorkouts >= 100) {
            level = "Elite Athlete";
        } else if (totalWorkouts >= 50) {
            level = "Advanced Athlete";
        } else if (totalWorkouts >= 25) {
            level = "Intermediate Athlete";
        } else if (totalWorkouts >= 10) {
            level = "Rising Athlete";
        } else if (totalWorkouts >= 5) {
            level = "Committed Beginner";
        } else {
            level = "Beginner Athlete";
        }
        
        tvUserLevel.setText(level);
    }

    private void refreshData() {
        if (viewModel != null) {
            viewModel.refreshData();
        }
        swipeRefresh.setRefreshing(false);
    }

    private void navigateToWorkouts() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToWorkouts();
        }
    }

    private void navigateToHabits() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToHabits();
        }
    }

    private void navigateToGoals() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToGoals();
        }
    }

    private void navigateToAnalytics() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToAnalytics();
        }
    }
}
