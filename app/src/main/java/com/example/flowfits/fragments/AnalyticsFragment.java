package com.example.flowfits.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.flowfits.R;
import com.example.flowfits.adapters.WeeklySummaryAdapter;
import com.example.flowfits.models.AnalyticsData;
import com.example.flowfits.viewmodels.AnalyticsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsFragment extends Fragment {
    private static final String TAG = "AnalyticsFragment";

    // UI Components
    private SwipeRefreshLayout swipeRefresh;
    private TextView titleText, subtitleText;
    private TextView totalWorkoutsText, thisWeekWorkoutsText, totalCaloriesText, currentStreakText;
    private TextView achievementTitleText, achievementMessageText;
    private MaterialButton getStartedButton;
    private MaterialCardView achievementBanner;
    private ProgressBar loadingProgress;
    private View emptyStateLayout, chartsContainer;
    
    // Charts
    private BarChart weeklyActivityChart;
    private PieChart workoutTypesChart;
    private LineChart progressTrendsChart;
    
    // RecyclerView
    private RecyclerView weeklyStatsRecyclerView;
    private WeeklySummaryAdapter weeklySummaryAdapter;
    
    // ViewModel
    private AnalyticsViewModel analyticsViewModel;
    
    // Date formatter
    private SimpleDateFormat dayFormatter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupViewModel();
        setupCharts();
        setupRecyclerView();
        setupClickListeners();
        observeData();
        
        dayFormatter = new SimpleDateFormat("EEE", Locale.getDefault());
        
        // Load initial data
        loadAnalyticsData();
    }

    private void initializeViews(View view) {
        // SwipeRefresh and loading
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        loadingProgress = view.findViewById(R.id.progress_loading);
        
        // Header components
        titleText = view.findViewById(R.id.tv_analytics_title);
        subtitleText = view.findViewById(R.id.tv_analytics_subtitle);
        
        // Stats components
        totalWorkoutsText = view.findViewById(R.id.tv_total_workouts);
        thisWeekWorkoutsText = view.findViewById(R.id.tv_this_week_workouts);
        totalCaloriesText = view.findViewById(R.id.tv_total_calories);
        currentStreakText = view.findViewById(R.id.tv_current_streak);
        
        // Achievement components
        achievementBanner = view.findViewById(R.id.card_achievement_banner);
        achievementTitleText = view.findViewById(R.id.tv_achievement_title);
        achievementMessageText = view.findViewById(R.id.tv_achievement_message);
        
        // Buttons
        getStartedButton = view.findViewById(R.id.btn_get_started);
        
        // Layout containers
        emptyStateLayout = view.findViewById(R.id.layout_empty_state);
        chartsContainer = view.findViewById(R.id.layout_charts_container);
        
        // Charts
        weeklyActivityChart = view.findViewById(R.id.chart_weekly_activity);
        workoutTypesChart = view.findViewById(R.id.chart_workout_types);
        progressTrendsChart = view.findViewById(R.id.chart_progress_trends);
        
        // RecyclerView
        weeklyStatsRecyclerView = view.findViewById(R.id.rv_weekly_stats);
    }

    private void setupViewModel() {
        analyticsViewModel = new ViewModelProvider(this).get(AnalyticsViewModel.class);
    }

    private void setupCharts() {
        setupWeeklyActivityChart();
        setupWorkoutTypesChart();
        setupProgressTrendsChart();
    }

    private void setupWeeklyActivityChart() {
        weeklyActivityChart.setDrawBarShadow(false);
        weeklyActivityChart.setDrawValueAboveBar(true);
        weeklyActivityChart.getDescription().setEnabled(false);
        weeklyActivityChart.setMaxVisibleValueCount(7);
        weeklyActivityChart.setPinchZoom(false);
        weeklyActivityChart.setDrawGridBackground(false);

        XAxis xAxis = weeklyActivityChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);

        YAxis leftAxis = weeklyActivityChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = weeklyActivityChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f);

        Legend legend = weeklyActivityChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setFormSize(9f);
        legend.setTextSize(11f);
        legend.setXEntrySpace(4f);
    }

    private void setupWorkoutTypesChart() {
        workoutTypesChart.setUsePercentValues(true);
        workoutTypesChart.getDescription().setEnabled(false);
        workoutTypesChart.setExtraOffsets(5, 10, 5, 5);
        
        workoutTypesChart.setDragDecelerationFrictionCoef(0.95f);
        workoutTypesChart.setDrawHoleEnabled(true);
        workoutTypesChart.setHoleColor(Color.WHITE);
        workoutTypesChart.setTransparentCircleColor(Color.WHITE);
        workoutTypesChart.setTransparentCircleAlpha(110);
        workoutTypesChart.setHoleRadius(58f);
        workoutTypesChart.setTransparentCircleRadius(61f);
        workoutTypesChart.setDrawCenterText(true);
        workoutTypesChart.setRotationAngle(0);
        workoutTypesChart.setRotationEnabled(true);
        workoutTypesChart.setHighlightPerTapEnabled(true);

        Legend legend = workoutTypesChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);
    }

    private void setupProgressTrendsChart() {
        progressTrendsChart.getDescription().setEnabled(false);
        progressTrendsChart.setTouchEnabled(true);
        progressTrendsChart.setDragEnabled(true);
        progressTrendsChart.setScaleEnabled(true);
        progressTrendsChart.setDrawGridBackground(false);
        progressTrendsChart.setPinchZoom(true);
        progressTrendsChart.setBackgroundColor(Color.WHITE);

        XAxis xAxis = progressTrendsChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = progressTrendsChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = progressTrendsChart.getAxisRight();
        rightAxis.setEnabled(false);

        Legend legend = progressTrendsChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(11f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    private void setupRecyclerView() {
        weeklySummaryAdapter = new WeeklySummaryAdapter();
        weeklyStatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        weeklyStatsRecyclerView.setAdapter(weeklySummaryAdapter);
        weeklyStatsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        // SwipeRefresh
        swipeRefresh.setOnRefreshListener(this::loadAnalyticsData);
        
        // Get Started button (redirects to workouts or goals)
        getStartedButton.setOnClickListener(v -> {
            // Navigate to workouts tab to start creating content
            if (getActivity() != null && getActivity() instanceof androidx.fragment.app.FragmentActivity) {
                // This could be improved to actually switch tabs
                Toast.makeText(getContext(), "Start by creating workouts and goals!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void observeData() {
        // Observe analytics data
        analyticsViewModel.getAnalyticsData().observe(getViewLifecycleOwner(), this::updateUI);

        // Observe loading state
        analyticsViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            loadingProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            swipeRefresh.setRefreshing(isLoading);
        });

        // Observe error messages
        analyticsViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Analytics error: " + error);
            }
        });
    }

    private void loadAnalyticsData() {
        if (analyticsViewModel != null) {
            analyticsViewModel.loadAnalytics();
            Log.d(TAG, "Loading analytics data...");
        }
    }

    private void updateUI(AnalyticsData data) {
        if (data == null) {
            showEmptyState(true);
            return;
        }

        boolean hasData = data.getTotalWorkouts() > 0 || data.getGoalsCompleted() > 0;
        showEmptyState(!hasData);
        
        if (hasData) {
        updateStatsCards(data);
        updateCharts(data);
        updateWeeklySummary(data);
            updateAchievements(data);
        }
        
        Log.d(TAG, "UI updated - Has data: " + hasData);
    }

    private void updateStatsCards(AnalyticsData data) {
        totalWorkoutsText.setText(String.valueOf(data.getTotalWorkouts()));
        totalCaloriesText.setText(String.valueOf(data.getTotalCaloriesBurned()));
        currentStreakText.setText(String.valueOf(data.getCurrentStreak()));
        
        // Calculate this week's workouts from weekly data
        int thisWeekWorkouts = 0;
        if (data.getWeeklyData() != null) {
            for (AnalyticsData.DayData dayData : data.getWeeklyData()) {
                thisWeekWorkouts += dayData.getWorkouts();
            }
        }
        thisWeekWorkoutsText.setText(String.valueOf(thisWeekWorkouts));
        
        Log.d(TAG, "Stats updated - Total: " + data.getTotalWorkouts() + ", This week: " + thisWeekWorkouts + ", Streak: " + data.getCurrentStreak());
    }

    private void updateCharts(AnalyticsData data) {
        updateWeeklyActivityChart(data.getWeeklyData());
        updateWorkoutTypesChart(data.getWorkoutTypeDistribution());
        updateProgressTrendsChart(data.getWorkoutProgress());
    }

    private void updateWeeklyActivityChart(List<AnalyticsData.DayData> weeklyData) {
        if (weeklyData == null || weeklyData.isEmpty()) {
            weeklyActivityChart.clear();
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < weeklyData.size(); i++) {
            AnalyticsData.DayData dayData = weeklyData.get(i);
            entries.add(new BarEntry(i, dayData.getWorkouts()));
            labels.add(dayFormatter.format(dayData.getDate()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Workouts");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        weeklyActivityChart.setData(barData);
        weeklyActivityChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        weeklyActivityChart.invalidate();
    }

    private void updateWorkoutTypesChart(Map<String, Integer> typeDistribution) {
        if (typeDistribution == null || typeDistribution.isEmpty()) {
            workoutTypesChart.clear();
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : typeDistribution.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), formatWorkoutType(entry.getKey())));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Workout Types");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData pieData = new PieData(dataSet);

        workoutTypesChart.setData(pieData);
        workoutTypesChart.setCenterText("Workout\nTypes");
        workoutTypesChart.invalidate();
    }

    private void updateProgressTrendsChart(List<AnalyticsData.ProgressPoint> progressData) {
        if (progressData == null || progressData.isEmpty()) {
            progressTrendsChart.clear();
            return;
        }

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
        
        for (int i = 0; i < progressData.size(); i++) {
            AnalyticsData.ProgressPoint point = progressData.get(i);
            entries.add(new Entry(i, (float) point.getValue()));
            labels.add(dateFormat.format(point.getDate()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Progress");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        
        progressTrendsChart.setData(lineData);
        progressTrendsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        progressTrendsChart.invalidate();
    }

    private void updateWeeklySummary(AnalyticsData data) {
        if (data.getWeeklyData() != null) {
            weeklySummaryAdapter.updateWeeklyData(data.getWeeklyData());
        }
    }

    private void updateAchievements(AnalyticsData data) {
        // Check for achievements and show banner
        String achievement = checkForAchievements(data);
        if (achievement != null && !achievement.isEmpty()) {
            achievementBanner.setVisibility(View.VISIBLE);
            achievementTitleText.setText("Achievement Unlocked! 🎉");
            achievementMessageText.setText(achievement);
        } else {
            achievementBanner.setVisibility(View.GONE);
        }
    }

    private String checkForAchievements(AnalyticsData data) {
        if (data.getCurrentStreak() >= 7) {
            return "You've maintained a 7-day workout streak! Keep it up! 🔥";
        } else if (data.getTotalWorkouts() >= 10) {
            return "You've completed 10 workouts! You're building great habits! 💪";
        } else if (data.getTotalCaloriesBurned() >= 1000) {
            return "You've burned over 1000 calories! Amazing progress! 🔥";
        }
        
        return null; // No achievement to show
    }

    private void showEmptyState(boolean show) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        chartsContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        Log.d(TAG, "Empty state: " + (show ? "shown" : "hidden"));
    }

    private String formatWorkoutType(String type) {
        if (type == null) return "Other";
        
        switch (type.toUpperCase()) {
            case "CARDIO": return "Cardio";
            case "STRENGTH": return "Strength";
            case "FLEXIBILITY": return "Flexibility";
            case "SPORTS": return "Sports";
            case "HIIT": return "HIIT";
            case "YOGA": return "Yoga";
            case "PILATES": return "Pilates";
            case "CROSSFIT": return "CrossFit";
            default: return type;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAnalyticsData(); // Refresh data when returning to fragment
    }
} 