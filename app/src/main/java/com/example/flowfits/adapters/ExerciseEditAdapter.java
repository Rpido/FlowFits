package com.example.flowfits.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowfits.R;
import com.example.flowfits.models.Workout;
import com.example.flowfits.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class ExerciseEditAdapter extends RecyclerView.Adapter<ExerciseEditAdapter.ExerciseViewHolder> {
    
    private Context context;
    private List<Workout.Exercise> exercises;
    private OnExerciseChangeListener listener;
    
    // Common units for exercises
    private final String[] units = Constants.EXERCISE_UNITS;
    
    public ExerciseEditAdapter(Context context, OnExerciseChangeListener listener) {
        this.context = context;
        this.exercises = new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise_edit, parent, false);
        return new ExerciseViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        holder.bind(exercises.get(position), position);
    }
    
    @Override
    public int getItemCount() {
        return exercises.size();
    }
    
    public void setExercises(List<Workout.Exercise> exercises) {
        android.util.Log.d("ExerciseEditAdapter", "setExercises called with " + 
            (exercises != null ? exercises.size() : "null") + " exercises");
        
        this.exercises.clear();
        if (exercises != null) {
            this.exercises.addAll(exercises);
            android.util.Log.d("ExerciseEditAdapter", "Added exercises to adapter. Adapter now has " + 
                this.exercises.size() + " exercises");
            for (int i = 0; i < this.exercises.size(); i++) {
                Workout.Exercise ex = this.exercises.get(i);
                android.util.Log.d("ExerciseEditAdapter", "Adapter exercise " + i + ": " + ex.getName());
            }
        }
        notifyDataSetChanged();
        android.util.Log.d("ExerciseEditAdapter", "notifyDataSetChanged called. getItemCount() = " + getItemCount());
    }
    
    public void addExercise(Workout.Exercise exercise) {
        exercises.add(exercise);
        notifyItemInserted(exercises.size() - 1);
        if (listener != null) {
            listener.onExerciseListChanged(exercises);
        }
    }
    
    public void removeExercise(int position) {
        if (position >= 0 && position < exercises.size()) {
            exercises.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, exercises.size());
            if (listener != null) {
                listener.onExerciseListChanged(exercises);
            }
        }
    }
    
    public List<Workout.Exercise> getExercises() {
        return new ArrayList<>(exercises);
    }
    
    public class ExerciseViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvExerciseName;
        private TextInputEditText etSets, etReps, etWeight, etDistance, etExerciseNotes;
        private AutoCompleteTextView spinnerUnit;
        private MaterialButton btnRemoveExercise, btnToggleOptions;
        private View layoutAdditionalOptions;
        
        private boolean isExpanded = false;
        
        // Store TextWatcher references for proper cleanup
        private TextWatcher setsWatcher, repsWatcher, weightWatcher, distanceWatcher, notesWatcher;
        
        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            etSets = itemView.findViewById(R.id.etSets);
            etReps = itemView.findViewById(R.id.etReps);
            etWeight = itemView.findViewById(R.id.etWeight);
            etDistance = itemView.findViewById(R.id.etDistance);
            etExerciseNotes = itemView.findViewById(R.id.etExerciseNotes);
            spinnerUnit = itemView.findViewById(R.id.spinnerUnit);
            btnRemoveExercise = itemView.findViewById(R.id.btnRemoveExercise);
            btnToggleOptions = itemView.findViewById(R.id.btnToggleOptions);
            layoutAdditionalOptions = itemView.findViewById(R.id.layoutAdditionalOptions);
            
            setupUnitSpinner();
            setupClickListeners();
        }
        
        private void setupUnitSpinner() {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, 
                android.R.layout.simple_dropdown_item_1line, units);
            spinnerUnit.setAdapter(adapter);
        }
        
        private void setupClickListeners() {
            btnRemoveExercise.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    removeExercise(position);
                }
            });
            
            btnToggleOptions.setOnClickListener(v -> toggleAdditionalOptions());
            
            // Make exercise name editable when clicked
            tvExerciseName.setOnClickListener(v -> showEditExerciseNameDialog());
        }
        
        private void toggleAdditionalOptions() {
            isExpanded = !isExpanded;
            layoutAdditionalOptions.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            btnToggleOptions.setText(isExpanded ? "Less options" : "More options");
            btnToggleOptions.setIconResource(isExpanded ? R.drawable.ic_expand_less : R.drawable.ic_expand_more);
        }
        
        private void showEditExerciseNameDialog() {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;
            
            Workout.Exercise exercise = exercises.get(position);
            
            // Create input dialog
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle("Edit Exercise Name");
            
            // Set up the input
            final android.widget.EditText input = new android.widget.EditText(context);
            input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            input.setText(exercise.getName());
            input.selectAll();
            builder.setView(input);
            
            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (!android.text.TextUtils.isEmpty(newName)) {
                    exercise.setName(newName);
                    tvExerciseName.setText(newName);
                    if (listener != null) {
                        listener.onExerciseChanged(exercise, position);
                    }
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            
            android.app.AlertDialog dialog = builder.create();
            dialog.show();
            
            // Auto-focus and show keyboard
            input.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(input, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }
        
        public void bind(Workout.Exercise exercise, int position) {
            // Clear previous listeners to prevent issues
            clearTextWatchers();
            
            // Set exercise name with clickable styling
            tvExerciseName.setText(exercise.getName() != null ? exercise.getName() : "Exercise " + (position + 1));
            tvExerciseName.setTextColor(context.getResources().getColor(R.color.primary, null));
            tvExerciseName.setClickable(true);
            tvExerciseName.setFocusable(true);
            
            // Set values without triggering watchers
            etSets.setText(exercise.getSets() != null ? String.valueOf(exercise.getSets()) : "");
            etReps.setText(exercise.getReps() != null ? String.valueOf(exercise.getReps()) : "");
            etWeight.setText(exercise.getWeight() != null ? String.valueOf(exercise.getWeight()) : "");
            etDistance.setText(exercise.getDistance() != null ? String.valueOf(exercise.getDistance()) : "");
            if (!TextUtils.isEmpty(exercise.getUnit())) {
                spinnerUnit.setText(exercise.getUnit(), false);
            } else {
                spinnerUnit.setText("", false);
            }
            etExerciseNotes.setText(exercise.getNotes() != null ? exercise.getNotes() : "");
            
            // Setup text watchers for real-time updates
            setupTextWatchers(exercise);
        }
        
        private void clearTextWatchers() {
            // Remove existing text watchers properly
            if (setsWatcher != null) {
                etSets.removeTextChangedListener(setsWatcher);
                setsWatcher = null;
            }
            if (repsWatcher != null) {
                etReps.removeTextChangedListener(repsWatcher);
                repsWatcher = null;
            }
            if (weightWatcher != null) {
                etWeight.removeTextChangedListener(weightWatcher);
                weightWatcher = null;
            }
            if (distanceWatcher != null) {
                etDistance.removeTextChangedListener(distanceWatcher);
                distanceWatcher = null;
            }
            if (notesWatcher != null) {
                etExerciseNotes.removeTextChangedListener(notesWatcher);
                notesWatcher = null;
            }
        }
        
        private void setupTextWatchers(Workout.Exercise exercise) {
            setsWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        if (!TextUtils.isEmpty(s.toString())) {
                            exercise.setSets(Integer.parseInt(s.toString()));
                        } else {
                            exercise.setSets(null);
                        }
                        if (listener != null) {
                            listener.onExerciseChanged(exercise, getAdapterPosition());
                        }
                    } catch (NumberFormatException e) {
                        // Invalid number, ignore
                    }
                }
            };
            etSets.addTextChangedListener(setsWatcher);
            
            repsWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        if (!TextUtils.isEmpty(s.toString())) {
                            exercise.setReps(Integer.parseInt(s.toString()));
                        } else {
                            exercise.setReps(null);
                        }
                        if (listener != null) {
                            listener.onExerciseChanged(exercise, getAdapterPosition());
                        }
                    } catch (NumberFormatException e) {
                        // Invalid number, ignore
                    }
                }
            };
            etReps.addTextChangedListener(repsWatcher);
            
            weightWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        if (!TextUtils.isEmpty(s.toString())) {
                            exercise.setWeight(Double.parseDouble(s.toString()));
                        } else {
                            exercise.setWeight(null);
                        }
                        if (listener != null) {
                            listener.onExerciseChanged(exercise, getAdapterPosition());
                        }
                    } catch (NumberFormatException e) {
                        // Invalid number, ignore
                    }
                }
            };
            etWeight.addTextChangedListener(weightWatcher);
            
            distanceWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        if (!TextUtils.isEmpty(s.toString())) {
                            exercise.setDistance(Double.parseDouble(s.toString()));
                        } else {
                            exercise.setDistance(null);
                        }
                        if (listener != null) {
                            listener.onExerciseChanged(exercise, getAdapterPosition());
                        }
                    } catch (NumberFormatException e) {
                        // Invalid number, ignore
                    }
                }
            };
            etDistance.addTextChangedListener(distanceWatcher);
            
            notesWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                
                @Override
                public void afterTextChanged(Editable s) {
                    exercise.setNotes(s.toString());
                    if (listener != null) {
                        listener.onExerciseChanged(exercise, getAdapterPosition());
                    }
                }
            };
            etExerciseNotes.addTextChangedListener(notesWatcher);
            
            // Unit selection listener
            spinnerUnit.setOnItemClickListener((parent, view, position, id) -> {
                exercise.setUnit(units[position]);
                if (listener != null) {
                    listener.onExerciseChanged(exercise, getAdapterPosition());
                }
            });
        }
    }
    
    public interface OnExerciseChangeListener {
        void onExerciseChanged(Workout.Exercise exercise, int position);
        void onExerciseListChanged(List<Workout.Exercise> exercises);
    }
} 