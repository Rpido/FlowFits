package com.example.flowfits.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flowfits.R;
import com.example.flowfits.models.Workout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {
    private List<Workout> workouts;
    private OnWorkoutClickListener onWorkoutClickListener;
    private OnWorkoutEditListener onWorkoutEditListener;
    private OnWorkoutDeleteListener onWorkoutDeleteListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

    // New constructor for lambda functions
    public WorkoutAdapter(List<Workout> workouts, OnWorkoutClickListener clickListener, 
                         OnWorkoutEditListener editListener, OnWorkoutDeleteListener deleteListener) {
        this.workouts = workouts != null ? workouts : new ArrayList<>();
        this.onWorkoutClickListener = clickListener;
        this.onWorkoutEditListener = editListener;
        this.onWorkoutDeleteListener = deleteListener;
    }

    // Legacy constructor for backward compatibility
    public WorkoutAdapter(Context context, OldOnWorkoutClickListener listener) {
        this.workouts = new ArrayList<>();
        // Convert old interface to new ones
        this.onWorkoutClickListener = workout -> {
            if (listener != null) listener.onWorkoutClick(workout, 0);
        };
        this.onWorkoutEditListener = workout -> {
            if (listener != null) listener.onEditClick(workout, 0);
        };
        this.onWorkoutDeleteListener = workout -> {
            if (listener != null) listener.onDeleteClick(workout, 0);
        };
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_card, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.bind(workout);
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public void updateWorkouts(List<Workout> newWorkouts) {
        this.workouts.clear();
        if (newWorkouts != null) {
            this.workouts.addAll(newWorkouts);
        }
        notifyDataSetChanged();
    }

    public void addWorkout(Workout workout) {
        workouts.add(0, workout); // Add to top
        notifyItemInserted(0);
    }

    public void removeWorkout(int position) {
        if (position >= 0 && position < workouts.size()) {
            workouts.remove(position);
            notifyItemRemoved(position);
        }
    }

    public class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView workoutTypeIcon;
        private TextView workoutName;
        private TextView workoutType;
        private TextView workoutIntensity;
        private TextView workoutDate;
        private TextView workoutDuration;
        private TextView workoutCalories;
        private TextView exercisesCount;
        private TextView workoutNotes;
        private ProgressBar progressWorkout;
        private LinearLayout strengthStats;
        private TextView totalSets;
        private TextView totalReps;
        private TextView totalWeight;
        private LinearLayout additionalStats;
        private TextView workoutLocation;
        private TextView heartRate;
        private MaterialButton startButton;
        private MaterialButton editButton;
        private MaterialButton deleteButton;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = (MaterialCardView) itemView;
            workoutTypeIcon = itemView.findViewById(R.id.iv_workout_type);
            workoutName = itemView.findViewById(R.id.tv_workout_name);
            workoutType = itemView.findViewById(R.id.tv_workout_type);
            workoutIntensity = itemView.findViewById(R.id.tv_workout_intensity);
            workoutDate = itemView.findViewById(R.id.tv_workout_date);
            workoutDuration = itemView.findViewById(R.id.tv_workout_duration);
            workoutCalories = itemView.findViewById(R.id.tv_workout_calories);
            exercisesCount = itemView.findViewById(R.id.tv_exercises_count);
            workoutNotes = itemView.findViewById(R.id.tv_workout_notes);
            progressWorkout = itemView.findViewById(R.id.progress_workout);
            strengthStats = itemView.findViewById(R.id.layout_strength_stats);
            totalSets = itemView.findViewById(R.id.tv_total_sets);
            totalReps = itemView.findViewById(R.id.tv_total_reps);
            totalWeight = itemView.findViewById(R.id.tv_total_weight);
            additionalStats = itemView.findViewById(R.id.layout_additional_stats);
            workoutLocation = itemView.findViewById(R.id.tv_workout_location);
            heartRate = itemView.findViewById(R.id.tv_heart_rate);
            startButton = itemView.findViewById(R.id.btn_start_workout);
            editButton = itemView.findViewById(R.id.btn_edit_workout);
            deleteButton = itemView.findViewById(R.id.btn_delete_workout);
        }

        public void bind(Workout workout) {
            // Set workout name
            workoutName.setText(workout.getName() != null ? workout.getName() : "Workout");
            
            // Set workout type with enhanced display
            if (workout.getType() != null) {
                workoutType.setText(workout.getType().getDisplayName());
            } else {
                workoutType.setText("General");
            }
            
            // Set workout intensity
            if (workout.getIntensity() != null) {
                workoutIntensity.setText(workout.getIntensity().getDisplayName());
                workoutIntensity.setVisibility(View.VISIBLE);
                // Set color based on intensity
                int color = getIntensityColor(workout.getIntensity());
                workoutIntensity.setTextColor(color);
            } else {
                workoutIntensity.setVisibility(View.GONE);
            }
            
            // Set workout type icon based on type
            setWorkoutTypeIcon(workout.getType());
            
            // Show start button for workouts with exercises
                if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
                    startButton.setVisibility(View.VISIBLE);
                } else {
                    startButton.setVisibility(View.GONE);
            }
            
            // Set progress for ongoing workouts
            if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
                double completionPercentage = workout.getCompletionPercentage();
                if (completionPercentage > 0 && completionPercentage < 100) {
                    progressWorkout.setProgress((int) completionPercentage);
                    progressWorkout.setVisibility(View.VISIBLE);
                } else {
                    progressWorkout.setVisibility(View.GONE);
                }
            } else {
                progressWorkout.setVisibility(View.GONE);
            }
            
            // Set date
            if (workout.getDate() != null) {
                workoutDate.setText(dateFormat.format(workout.getDate()));
            } else {
                workoutDate.setText("");
            }
            
            // Set duration
            if (workout.getDuration() > 0) {
                workoutDuration.setText(String.valueOf(workout.getDuration()));
            } else {
                workoutDuration.setText("0");
            }
            
            // Set calories
            if (workout.getCaloriesBurned() > 0) {
                workoutCalories.setText(String.valueOf(workout.getCaloriesBurned()));
            } else {
                workoutCalories.setText("0");
            }
            
            // Set exercises count with better formatting
            if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
                int count = workout.getExercises().size();
                exercisesCount.setText(String.valueOf(count));
                exercisesCount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success));
                exercisesCount.setTypeface(null, Typeface.BOLD);
            } else {
                exercisesCount.setText("0");
                exercisesCount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.secondary_text));
                exercisesCount.setTypeface(null, Typeface.NORMAL);
            }
            
            // Show strength stats for strength workouts
            if (workout.getType() == Workout.WorkoutType.STRENGTH || 
                workout.getType() == Workout.WorkoutType.CROSSFIT ||
                workout.getType() == Workout.WorkoutType.HIIT) {
                
                // Calculate totals if not already calculated
                if (workout.getTotalSets() == 0 && workout.getExercises() != null) {
                    workout.calculateTotals();
                }
                
                strengthStats.setVisibility(View.VISIBLE);
                totalSets.setText(String.valueOf(workout.getTotalSets()));
                totalReps.setText(String.valueOf(workout.getTotalReps()));
                
                if (workout.getTotalWeight() > 0) {
                    // Format weight nicely
                    String weightText = String.format(Locale.getDefault(), "%.0f kg", workout.getTotalWeight());
                    totalWeight.setText(weightText);
                } else {
                    totalWeight.setText("0 kg");
                }
            } else {
                strengthStats.setVisibility(View.GONE);
            }
            
            // Show additional stats if available
            boolean showAdditionalStats = false;
            
            if (!TextUtils.isEmpty(workout.getLocation())) {
                workoutLocation.setText(workout.getLocation());
                workoutLocation.setVisibility(View.VISIBLE);
                showAdditionalStats = true;
            } else {
                workoutLocation.setVisibility(View.GONE);
            }
            
            if (workout.getAverageHeartRate() > 0) {
                String heartRateText = String.format(Locale.getDefault(), "%.0f avg BPM", workout.getAverageHeartRate());
                heartRate.setText(heartRateText);
                heartRate.setVisibility(View.VISIBLE);
                showAdditionalStats = true;
            } else {
                heartRate.setVisibility(View.GONE);
            }
            
            additionalStats.setVisibility(showAdditionalStats ? View.VISIBLE : View.GONE);
            
            // Set notes
            if (!TextUtils.isEmpty(workout.getNotes())) {
                workoutNotes.setText(workout.getNotes());
                workoutNotes.setVisibility(View.VISIBLE);
            } else {
                workoutNotes.setVisibility(View.GONE);
            }
            
            // Set click listeners
            cardView.setOnClickListener(v -> {
                if (onWorkoutClickListener != null) {
                    onWorkoutClickListener.onWorkoutClick(workout);
                }
            });
            
            // Add long click to show exercise summary
            cardView.setOnLongClickListener(v -> {
                showExercisesSummary(workout);
                return true;
            });
            
            startButton.setOnClickListener(v -> {
                if (onWorkoutClickListener != null) {
                    onWorkoutClickListener.onWorkoutClick(workout);
                }
            });
            
            editButton.setOnClickListener(v -> {
                if (onWorkoutEditListener != null) {
                    onWorkoutEditListener.onWorkoutEdit(workout);
                }
            });
            
            deleteButton.setOnClickListener(v -> {
                if (onWorkoutDeleteListener != null) {
                    onWorkoutDeleteListener.onWorkoutDelete(workout);
                }
            });
        }

        private int getIntensityColor(Workout.WorkoutIntensity intensity) {
            Context context = itemView.getContext();
            switch (intensity) {
                case LOW:
                    return ContextCompat.getColor(context, android.R.color.holo_green_light);
                case MODERATE:
                    return ContextCompat.getColor(context, android.R.color.holo_orange_light);
                case HIGH:
                    return ContextCompat.getColor(context, android.R.color.holo_red_light);
                case VERY_HIGH:
                    return ContextCompat.getColor(context, android.R.color.holo_red_dark);
                default:
                    return ContextCompat.getColor(context, android.R.color.darker_gray);
            }
        }

        private void setWorkoutTypeIcon(Workout.WorkoutType type) {
            int iconResource = R.drawable.ic_workouts; // Default icon
            
            if (type != null) {
                switch (type) {
                    case CARDIO:
                        iconResource = R.drawable.ic_trending_up;
                        break;
                    case STRENGTH:
                        iconResource = R.drawable.ic_target;
                        break;
                    case FLEXIBILITY:
                    case YOGA:
                    case PILATES:
                        iconResource = R.drawable.ic_workouts;
                        break;
                    case SPORTS:
                        iconResource = R.drawable.ic_flag;
                        break;
                    case HIIT:
                    case CROSSFIT:
                        iconResource = R.drawable.ic_trending_up;
                        break;
                    default:
                        iconResource = R.drawable.ic_workouts;
                        break;
                }
            }
            
            workoutTypeIcon.setImageResource(iconResource);
        }
        
        private void showExercisesSummary(Workout workout) {
            if (workout.getExercises() == null || workout.getExercises().isEmpty()) {
                return;
            }
            
            StringBuilder summary = new StringBuilder();
            summary.append("Exercises in this workout:\n\n");
            
            for (int i = 0; i < workout.getExercises().size(); i++) {
                Workout.Exercise exercise = workout.getExercises().get(i);
                summary.append(i + 1).append(". ").append(exercise.getName());
                
                // Add sets/reps info if available
                if (exercise.getSets() != null && exercise.getReps() != null) {
                    summary.append(" - ").append(exercise.getSets()).append(" sets × ")
                           .append(exercise.getReps()).append(" reps");
                }
                
                // Add weight if available
                if (exercise.getWeight() != null && exercise.getWeight() > 0) {
                    summary.append(" @ ").append(exercise.getWeight()).append(" kg");
                }
                
                // Add distance if available
                if (exercise.getDistance() != null && exercise.getDistance() > 0) {
                    summary.append(" - ").append(exercise.getDistance());
                    if (!TextUtils.isEmpty(exercise.getUnit())) {
                        summary.append(" ").append(exercise.getUnit());
                    }
                }
                
                summary.append("\n");
            }
            
            // Create and show dialog with exercise summary
            Context context = itemView.getContext();
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle("Workout Details")
                   .setMessage(summary.toString())
                   .setPositiveButton("OK", null)
                   .setNeutralButton("Edit", (dialog, which) -> {
                       if (onWorkoutEditListener != null) {
                           onWorkoutEditListener.onWorkoutEdit(workout);
                       }
                   })
                   .show();
        }
    }

    // New functional interfaces for lambda support
    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
    }
    
    public interface OnWorkoutEditListener {
        void onWorkoutEdit(Workout workout);
    }
    
    public interface OnWorkoutDeleteListener {
        void onWorkoutDelete(Workout workout);
    }

    // Legacy interface for backward compatibility
    public interface OldOnWorkoutClickListener {
        void onWorkoutClick(Workout workout, int position);
        void onStartClick(Workout workout, int position);
        void onEditClick(Workout workout, int position);
        void onDeleteClick(Workout workout, int position);
    }
} 