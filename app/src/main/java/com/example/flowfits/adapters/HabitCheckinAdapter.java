package com.example.flowfits.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowfits.R;
import com.example.flowfits.models.Habit;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class HabitCheckinAdapter extends RecyclerView.Adapter<HabitCheckinAdapter.HabitCheckinViewHolder> {

    private Context context;
    private List<Habit> habits = new ArrayList<>();
    private OnHabitCheckinListener listener;

    public interface OnHabitCheckinListener {
        void onHabitClick(Habit habit);
        void onHabitToggle(Habit habit, boolean isCompleted);
    }

    public HabitCheckinAdapter(Context context, List<Habit> habits) {
        this.context = context;
        this.habits = habits != null ? habits : new ArrayList<>();
    }

    public void setOnHabitCheckinListener(OnHabitCheckinListener listener) {
        this.listener = listener;
    }

    public void updateHabits(List<Habit> newHabits) {
        this.habits = newHabits != null ? newHabits : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitCheckinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_habit_checkin, parent, false);
        return new HabitCheckinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitCheckinViewHolder holder, int position) {
        if (position < habits.size()) {
        Habit habit = habits.get(position);
            holder.bind(habit);
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(habits.size(), 5); // Show max 5 habits for quick access
    }

    class HabitCheckinViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cvCompletionBadge;
        private ImageView ivHabitIcon;
        private TextView tvHabitName;
        private TextView tvHabitProgress;

        public HabitCheckinViewHolder(@NonNull View itemView) {
            super(itemView);
            cvCompletionBadge = itemView.findViewById(R.id.cv_completion_badge);
            ivHabitIcon = itemView.findViewById(R.id.iv_habit_icon);
            tvHabitName = itemView.findViewById(R.id.tv_habit_name);
            tvHabitProgress = itemView.findViewById(R.id.tv_habit_progress);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < habits.size() && listener != null) {
                    listener.onHabitClick(habits.get(position));
                }
            });
        }

        public void bind(Habit habit) {
            tvHabitName.setText(habit.getName());

            // Set category icon
            setHabitIcon(habit);
            
            // Set progress text and completion status
            updateProgressDisplay(habit);
        }

        private void setHabitIcon(Habit habit) {
            int iconResource = R.drawable.ic_category;
            
            if (habit.getCategory() != null) {
                switch (habit.getCategory().toUpperCase()) {
                    case "HEALTH":
                        iconResource = R.drawable.ic_health;
                        break;
                    case "FITNESS":
                        iconResource = R.drawable.ic_fitness;
                        break;
                    case "NUTRITION":
                        iconResource = R.drawable.ic_nutrition;
                        break;
                    case "MINDFULNESS":
                        iconResource = R.drawable.ic_mindfulness;
                        break;
                    case "PRODUCTIVITY":
                        iconResource = R.drawable.ic_productivity;
                        break;
                    case "LEARNING":
                        iconResource = R.drawable.ic_learning;
                        break;
                    case "SOCIAL":
                        iconResource = R.drawable.ic_social;
                        break;
                    case "CREATIVITY":
                        iconResource = R.drawable.ic_creativity;
                        break;
                    case "LIFESTYLE":
                        iconResource = R.drawable.ic_lifestyle;
                        break;
                    default:
                        iconResource = R.drawable.ic_habits;
                        break;
                        }
            }
            
            ivHabitIcon.setImageResource(iconResource);
                    }

        private void updateProgressDisplay(Habit habit) {
            // Check if habit is completed today (this would need actual implementation)
            boolean isCompletedToday = false; // Mock for now
            
            if (habit.getTarget() > 0 && habit.getUnit() != null && !habit.getUnit().trim().isEmpty()) {
                // Habit with target - show progress
                double currentProgress = 0; // This would come from today's habit log
                tvHabitProgress.setText(String.format("%.0f/%.0f %s", 
                    currentProgress, habit.getTarget(), habit.getUnit()));
            } else {
                // Simple habit - show completion status
                tvHabitProgress.setText(isCompletedToday ? "Completed" : "Pending");
            }

            // Show/hide completion badge
            cvCompletionBadge.setVisibility(isCompletedToday ? View.VISIBLE : View.GONE);
        }
    }
} 