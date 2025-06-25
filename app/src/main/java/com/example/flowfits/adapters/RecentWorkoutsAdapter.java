package com.example.flowfits.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flowfits.R;
import com.example.flowfits.models.Workout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentWorkoutsAdapter extends RecyclerView.Adapter<RecentWorkoutsAdapter.RecentWorkoutViewHolder> {

    private Context context;
    private List<Workout> workouts;
    private OnWorkoutClickListener onWorkoutClickListener;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
    }

    public RecentWorkoutsAdapter(Context context, List<Workout> workouts) {
        this.context = context;
        this.workouts = workouts;
    }

    public void setOnWorkoutClickListener(OnWorkoutClickListener listener) {
        this.onWorkoutClickListener = listener;
    }

    @NonNull
    @Override
    public RecentWorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_workout, parent, false);
        return new RecentWorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentWorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.bind(workout);
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public void updateWorkouts(List<Workout> newWorkouts) {
        this.workouts = newWorkouts;
        notifyDataSetChanged();
    }

    class RecentWorkoutViewHolder extends RecyclerView.ViewHolder {
        private ImageView typeIconImageView;
        private TextView typeTextView;
        private TextView dateTextView;
        private TextView nameTextView;
        private TextView durationTextView;
        private TextView caloriesTextView;
        private TextView intensityTextView;

        public RecentWorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            typeIconImageView = itemView.findViewById(R.id.iv_workout_type_icon);
            typeTextView = itemView.findViewById(R.id.tv_workout_type);
            dateTextView = itemView.findViewById(R.id.tv_workout_date);
            nameTextView = itemView.findViewById(R.id.tv_workout_name);
            durationTextView = itemView.findViewById(R.id.tv_workout_duration);
            caloriesTextView = itemView.findViewById(R.id.tv_workout_calories);
            intensityTextView = itemView.findViewById(R.id.tv_workout_intensity);

            itemView.setOnClickListener(v -> {
                if (onWorkoutClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onWorkoutClickListener.onWorkoutClick(workouts.get(position));
                    }
                }
            });
        }

        public void bind(Workout workout) {
            // Set workout type and icon
            typeTextView.setText(workout.getType().toString());
            
            // Set appropriate icon for workout type
            switch (workout.getType()) {
                case HIIT:
                    typeIconImageView.setImageResource(R.drawable.ic_trending_up);
                    break;
                case YOGA:
                    typeIconImageView.setImageResource(R.drawable.ic_person);
                    break;
                case PILATES:
                    typeIconImageView.setImageResource(R.drawable.ic_person);
                    break;
                case CROSSFIT:
                    typeIconImageView.setImageResource(R.drawable.ic_workouts);
                    break;
                default:
                    typeIconImageView.setImageResource(R.drawable.ic_workouts);
                    break;
            }

            // Set workout name
            nameTextView.setText(workout.getName());

            // Set date (relative to today)
            dateTextView.setText(getRelativeDate(workout.getDate()));

            // Set duration
            durationTextView.setText(workout.getDuration() + " min");

            // Set calories
            caloriesTextView.setText(workout.getCaloriesBurned() + " kcal");

            // Set intensity badge
            intensityTextView.setText(workout.getIntensity().toString());
            
            // Set intensity color
            int intensityColor;
            switch (workout.getIntensity()) {
                case LOW:
                    intensityColor = R.color.success;
                    break;
                case MODERATE:
                    intensityColor = R.color.warning;
                    break;
                case HIGH:
                    intensityColor = R.color.error;
                    break;
                case VERY_HIGH:
                    intensityColor = R.color.purple;
                    break;
                default:
                    intensityColor = R.color.secondary_text;
                    break;
            }
            
            intensityTextView.setBackgroundTintList(
                ContextCompat.getColorStateList(context, intensityColor)
            );
        }

        private String getRelativeDate(Date date) {
            Calendar today = Calendar.getInstance();
            Calendar workoutDate = Calendar.getInstance();
            workoutDate.setTime(date);

            // Check if it's today
            if (isSameDay(today, workoutDate)) {
                return "Today";
            }

            // Check if it's yesterday
            today.add(Calendar.DAY_OF_YEAR, -1);
            if (isSameDay(today, workoutDate)) {
                return "Yesterday";
            }

            // Check if it's within the last week
            today = Calendar.getInstance();
            long diffInMillis = today.getTimeInMillis() - workoutDate.getTimeInMillis();
            long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
            
            if (diffInDays < 7) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                return dayFormat.format(date);
            }

            // For older dates, show the date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            return dateFormat.format(date);
        }

        private boolean isSameDay(Calendar cal1, Calendar cal2) {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }
    }
} 