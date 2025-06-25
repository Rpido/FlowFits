package com.example.flowfits.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowfits.R;
import com.example.flowfits.models.AnalyticsData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeeklySummaryAdapter extends RecyclerView.Adapter<WeeklySummaryAdapter.WeeklySummaryViewHolder> {

    private List<AnalyticsData.DayData> weeklyData;
    private SimpleDateFormat dateFormatter;

    public WeeklySummaryAdapter() {
        this.weeklyData = new ArrayList<>();
        this.dateFormatter = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
    }

    public void setWeeklyData(List<AnalyticsData.DayData> weeklyData) {
        this.weeklyData = weeklyData != null ? weeklyData : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateWeeklyData(List<AnalyticsData.DayData> newWeeklyData) {
        this.weeklyData = newWeeklyData != null ? newWeeklyData : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WeeklySummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weekly_summary, parent, false);
        return new WeeklySummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeeklySummaryViewHolder holder, int position) {
        AnalyticsData.DayData dayData = weeklyData.get(position);
        holder.bind(dayData);
    }

    @Override
    public int getItemCount() {
        return weeklyData.size();
    }

    class WeeklySummaryViewHolder extends RecyclerView.ViewHolder {
        private TextView dateText;
        private TextView workoutsText;
        private TextView caloriesText;
        private TextView minutesText;
        private View goalIndicator;

        public WeeklySummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.tv_day_date);
            workoutsText = itemView.findViewById(R.id.tv_day_workouts);
            caloriesText = itemView.findViewById(R.id.tv_day_calories);
            minutesText = itemView.findViewById(R.id.tv_day_minutes);
            goalIndicator = itemView.findViewById(R.id.indicator_goal_achieved);
        }

        public void bind(AnalyticsData.DayData dayData) {
            dateText.setText(dateFormatter.format(dayData.getDate()));
            workoutsText.setText(String.valueOf(dayData.getWorkouts()));
            caloriesText.setText(String.valueOf(dayData.getCalories()));
            minutesText.setText(String.valueOf(dayData.getExerciseMinutes()));
            
            // Show/hide goal achievement indicator
            goalIndicator.setVisibility(dayData.isGoalAchieved() ? View.VISIBLE : View.INVISIBLE);
            
            // Set background tint based on activity level
            if (dayData.getWorkouts() > 0) {
                itemView.setBackgroundResource(R.drawable.background_active_day);
            } else {
                itemView.setBackgroundResource(R.drawable.background_inactive_day);
            }
        }
    }
} 