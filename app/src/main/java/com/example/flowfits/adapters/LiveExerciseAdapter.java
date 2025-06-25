package com.example.flowfits.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flowfits.R;
import com.example.flowfits.models.Workout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LiveExerciseAdapter extends RecyclerView.Adapter<LiveExerciseAdapter.LiveExerciseViewHolder> {
    
    private Context context;
    private List<Workout.Exercise> exercises;
    private OnExerciseUpdateListener updateListener;
    private int currentExerciseIndex = -1;
    
    public interface OnExerciseUpdateListener {
        void onExerciseCompleted(int position);
        void onSetCompleted(int exercisePosition, int setNumber);
        void onExerciseDataChanged(int position, Workout.Exercise exercise);
    }
    
    public LiveExerciseAdapter(Context context, OnExerciseUpdateListener listener) {
        this.context = context;
        this.exercises = new ArrayList<>();
        this.updateListener = listener;
    }
    
    @NonNull
    @Override
    public LiveExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_live_exercise, parent, false);
        return new LiveExerciseViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LiveExerciseViewHolder holder, int position) {
        if (exercises != null && position < exercises.size()) {
            holder.bind(exercises.get(position), position);
        }
    }
    
    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }
    
    public void updateExercises(List<Workout.Exercise> newExercises) {
        this.exercises = newExercises != null ? newExercises : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setCurrentExercise(int index) {
        int previousIndex = currentExerciseIndex;
        currentExerciseIndex = index;
        
        if (previousIndex >= 0) {
            notifyItemChanged(previousIndex);
        }
        if (currentExerciseIndex >= 0) {
            notifyItemChanged(currentExerciseIndex);
        }
    }
    
    public class LiveExerciseViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView exerciseName;
        private TextView exerciseDetails;
        private LinearLayout setsContainer;
        private MaterialButton completeButton;
        private ImageView currentIndicator;
        private TextView setProgress;
        private EditText actualSetsInput;
        private EditText actualRepsInput;
        private EditText actualWeightInput;
        private LinearLayout actualValuesLayout;
        private MaterialCheckBox exerciseCheckbox;
        
        public LiveExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = (MaterialCardView) itemView;
            exerciseName = itemView.findViewById(R.id.tv_exercise_name);
            exerciseDetails = itemView.findViewById(R.id.tv_exercise_details);
            setsContainer = itemView.findViewById(R.id.container_sets);
            completeButton = itemView.findViewById(R.id.btn_complete_exercise);
            currentIndicator = itemView.findViewById(R.id.iv_current_indicator);
            setProgress = itemView.findViewById(R.id.tv_set_progress);
            actualSetsInput = itemView.findViewById(R.id.et_actual_sets);
            actualRepsInput = itemView.findViewById(R.id.et_actual_reps);
            actualWeightInput = itemView.findViewById(R.id.et_actual_weight);
            actualValuesLayout = itemView.findViewById(R.id.layout_actual_values);
            exerciseCheckbox = itemView.findViewById(R.id.cb_exercise_complete);
        }
        
        public void bind(Workout.Exercise exercise, int position) {
            exerciseName.setText(exercise.getName());
            
            // Build exercise details
            StringBuilder details = new StringBuilder();
            if (exercise.getSets() != null && exercise.getSets() > 0) {
                details.append(exercise.getSets()).append(" sets");
                if (exercise.getReps() != null && exercise.getReps() > 0) {
                    details.append(" × ").append(exercise.getReps()).append(" reps");
                }
                if (exercise.getWeight() != null && exercise.getWeight() > 0) {
                    details.append(" @ ").append(String.format(Locale.getDefault(), "%.1f kg", exercise.getWeight()));
                }
            } else {
                if (exercise.getReps() != null && exercise.getReps() > 0) {
                    details.append(exercise.getReps()).append(" reps");
                }
                if (exercise.getWeight() != null && exercise.getWeight() > 0) {
                    if (details.length() > 0) details.append(" @ ");
                    details.append(String.format(Locale.getDefault(), "%.1f kg", exercise.getWeight()));
                }
            }
            
            // Add distance if available for cardio exercises
            if (exercise.getDistance() != null && exercise.getDistance() > 0) {
                if (details.length() > 0) details.append(" • ");
                details.append(String.format(Locale.getDefault(), "%.1f", exercise.getDistance()));
                if (exercise.getUnit() != null && !exercise.getUnit().isEmpty()) {
                    details.append(" ").append(exercise.getUnit());
                } else {
                    details.append(" km");
                }
            }
            
            exerciseDetails.setText(details.toString());
            
            // Highlight current exercise
            if (position == currentExerciseIndex) {
                currentIndicator.setVisibility(View.VISIBLE);
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary_light));
            } else {
                currentIndicator.setVisibility(View.GONE);
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            }
            
            // FIXED: Remove listener before setting checked state to prevent recursive calls
            exerciseCheckbox.setOnCheckedChangeListener(null);
            exerciseCheckbox.setChecked(exercise.isCompleted());
            
            // Set up listener after setting the initial state
            exerciseCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                exercise.setCompleted(isChecked);
                if (updateListener != null) {
                    updateListener.onExerciseDataChanged(position, exercise);
                    if (isChecked) {
                        // Use post to avoid calling during layout
                        buttonView.post(() -> {
                            if (updateListener != null) {
                                updateListener.onExerciseCompleted(position);
                            }
                        });
                    }
                }
                updateCompleteButton(exercise);
            });
            
            // Setup sets tracking
            setupSetsTracking(exercise, position);
            
            // Setup actual values input
            setupActualValuesInput(exercise, position);
            
            // Update complete button
            updateCompleteButton(exercise);
            
            // Complete button click
            completeButton.setOnClickListener(v -> {
                exercise.setCompleted(true);
                // FIXED: Remove listener before programmatically setting checked state
                exerciseCheckbox.setOnCheckedChangeListener(null);
                exerciseCheckbox.setChecked(true);
                // Restore listener
                exerciseCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    exercise.setCompleted(isChecked);
                    if (updateListener != null) {
                        updateListener.onExerciseDataChanged(position, exercise);
                        if (isChecked) {
                            buttonView.post(() -> {
                                if (updateListener != null) {
                                    updateListener.onExerciseCompleted(position);
                                }
                            });
                        }
                    }
                    updateCompleteButton(exercise);
                });
                
                if (updateListener != null) {
                    // Use post to avoid calling during click handling
                    v.post(() -> {
                        if (updateListener != null) {
                            updateListener.onExerciseCompleted(position);
                        }
                    });
                }
                updateCompleteButton(exercise);
            });
        }
        
        private void setupSetsTracking(Workout.Exercise exercise, int position) {
            setsContainer.removeAllViews();
            
            if (exercise.getSets() != null && exercise.getSets() > 0) {
                // Initialize exercise sets if not already done
                if (exercise.getExerciseSets() == null || exercise.getExerciseSets().isEmpty()) {
                    exercise.setExerciseSets(new ArrayList<>());
                    for (int i = 0; i < exercise.getSets(); i++) {
                        Workout.ExerciseSet set = new Workout.ExerciseSet(
                            i + 1, 
                            exercise.getReps() != null ? exercise.getReps() : 0, 
                            exercise.getWeight() != null ? exercise.getWeight() : 0.0
                        );
                        exercise.getExerciseSets().add(set);
                    }
                }
                
                // Create UI for each set
                for (int i = 0; i < exercise.getExerciseSets().size(); i++) {
                    View setView = createSetView(exercise.getExerciseSets().get(i), i, exercise, position);
                    setsContainer.addView(setView);
                }
                
                setsContainer.setVisibility(View.VISIBLE);
                updateSetProgress(exercise);
            } else {
                setsContainer.setVisibility(View.GONE);
            }
        }
        
        private View createSetView(Workout.ExerciseSet exerciseSet, int setIndex, Workout.Exercise exercise, int exercisePosition) {
            View setView = LayoutInflater.from(context).inflate(R.layout.item_exercise_set, setsContainer, false);
            
            TextView setNumber = setView.findViewById(R.id.tv_set_number);
            EditText repsInput = setView.findViewById(R.id.et_set_reps);
            EditText weightInput = setView.findViewById(R.id.et_set_weight);
            MaterialCheckBox setCheckbox = setView.findViewById(R.id.cb_set_complete);
            
            setNumber.setText(String.valueOf(exerciseSet.getSetNumber()));
            
            // Set initial values
            repsInput.setText(String.valueOf(exerciseSet.getReps()));
            weightInput.setText(String.format(Locale.getDefault(), "%.1f", exerciseSet.getWeight()));
            
            // FIXED: Remove listener before setting checked state
            setCheckbox.setOnCheckedChangeListener(null);
            setCheckbox.setChecked(exerciseSet.isCompleted());
            
            // Set up listener after setting the initial state
            setCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                exerciseSet.setCompleted(isChecked);
                updateSetProgress(exercise);
                if (updateListener != null) {
                    // Use post to avoid calling during layout
                    buttonView.post(() -> {
                        if (updateListener != null) {
                            updateListener.onSetCompleted(exercisePosition, exerciseSet.getSetNumber());
                        }
                    });
                }
                
                // Check if all sets are completed
                boolean allSetsCompleted = exercise.getExerciseSets().stream()
                    .allMatch(Workout.ExerciseSet::isCompleted);
                if (allSetsCompleted && !exercise.isCompleted()) {
                    exercise.setCompleted(true);
                    // Remove and restore listener for the exercise checkbox
                    exerciseCheckbox.setOnCheckedChangeListener(null);
                    exerciseCheckbox.setChecked(true);
                    exerciseCheckbox.setOnCheckedChangeListener((buttonView2, isChecked2) -> {
                        exercise.setCompleted(isChecked2);
                        if (updateListener != null) {
                            updateListener.onExerciseDataChanged(exercisePosition, exercise);
                            if (isChecked2) {
                                buttonView2.post(() -> {
                                    if (updateListener != null) {
                                        updateListener.onExerciseCompleted(exercisePosition);
                                    }
                                });
                            }
                        }
                        updateCompleteButton(exercise);
                    });
                    
                    if (updateListener != null) {
                        // Use post to avoid calling during layout
                        buttonView.post(() -> {
                            if (updateListener != null) {
                                updateListener.onExerciseCompleted(exercisePosition);
                            }
                        });
                    }
                }
                updateCompleteButton(exercise);
            });
            
            return setView;
        }
        
        private void setupActualValuesInput(Workout.Exercise exercise, int position) {
            // Show actual values input for cardio exercises or when no sets are defined
            if (exercise.getSets() == null || exercise.getSets() <= 0) {
                actualValuesLayout.setVisibility(View.VISIBLE);
                
                // Set initial values
                if (exercise.getSets() != null) {
                    actualSetsInput.setText(String.valueOf(exercise.getSets()));
                }
                if (exercise.getReps() != null) {
                    actualRepsInput.setText(String.valueOf(exercise.getReps()));
                }
                if (exercise.getWeight() != null) {
                    actualWeightInput.setText(String.format(Locale.getDefault(), "%.1f", exercise.getWeight()));
                }
                
                // Add text watchers
                actualSetsInput.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        try {
                            int sets = Integer.parseInt(s.toString());
                            exercise.setSets(sets);
                            if (updateListener != null) {
                                updateListener.onExerciseDataChanged(position, exercise);
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid input
                        }
                    }
                });
                
                actualRepsInput.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        try {
                            int reps = Integer.parseInt(s.toString());
                            exercise.setReps(reps);
                            if (updateListener != null) {
                                updateListener.onExerciseDataChanged(position, exercise);
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid input
                        }
                    }
                });
                
                actualWeightInput.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        try {
                            double weight = Double.parseDouble(s.toString());
                            exercise.setWeight(weight);
                            if (updateListener != null) {
                                updateListener.onExerciseDataChanged(position, exercise);
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid input
                        }
                    }
                });
            } else {
                actualValuesLayout.setVisibility(View.GONE);
            }
        }
        
        private void updateSetProgress(Workout.Exercise exercise) {
            if (exercise.getExerciseSets() != null && !exercise.getExerciseSets().isEmpty()) {
                int completedSets = (int) exercise.getExerciseSets().stream()
                    .mapToInt(set -> set.isCompleted() ? 1 : 0)
                    .sum();
                int totalSets = exercise.getExerciseSets().size();
                
                String progressText = completedSets + " / " + totalSets + " sets completed";
                setProgress.setText(progressText);
                setProgress.setVisibility(View.VISIBLE);
            } else {
                setProgress.setVisibility(View.GONE);
            }
        }
        
        private void updateCompleteButton(Workout.Exercise exercise) {
            if (exercise.isCompleted()) {
                completeButton.setText("Completed ✓");
                completeButton.setEnabled(false);
                completeButton.setBackgroundColor(ContextCompat.getColor(context, R.color.success));
            } else {
                completeButton.setText("Mark Complete");
                completeButton.setEnabled(true);
                completeButton.setBackgroundColor(ContextCompat.getColor(context, R.color.primary));
            }
        }
    }
    
    // Helper class for simplified text watchers
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
} 