package com.example.flowfits.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowfits.R;
import com.example.flowfits.models.Habit;
import com.example.flowfits.models.HabitLog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitsAdapter extends RecyclerView.Adapter<HabitsAdapter.HabitViewHolder> {

    private List<Habit> habits = new ArrayList<>();
    private List<String> completedHabitIds = new ArrayList<>();
    private List<HabitLog> todaysLogs = new ArrayList<>();
    private Context context;
    private OnHabitClickListener listener;
    private boolean isCheckInMode = false; // Toggle between view and check-in modes

    public interface OnHabitClickListener {
        void onHabitClick(Habit habit);
        void onHabitLongClick(Habit habit);
        void onHabitToggleCompletion(Habit habit, boolean isCompleted);
        void onHabitEdit(Habit habit);
        void onHabitDelete(Habit habit);
        void onQuickCompleteHabit(Habit habit);
        void onUpdateProgress(Habit habit);
        void onMoreOptions(Habit habit);
    }

    public HabitsAdapter(Context context, OnHabitClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_habit_card, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        boolean isCompleted = completedHabitIds.contains(habit.getHabitId());
        
        holder.bind(habit, isCompleted);
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }
    
    public void setHabits(List<Habit> habits) {
        this.habits = habits != null ? habits : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setCompletedHabits(List<String> completedHabitIds) {
        this.completedHabitIds = completedHabitIds != null ? completedHabitIds : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setTodaysLogs(List<HabitLog> todaysLogs) {
        this.todaysLogs = todaysLogs != null ? todaysLogs : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setCheckInMode(boolean checkInMode) {
        this.isCheckInMode = checkInMode;
        notifyDataSetChanged();
    }
    
    public void updateHabitCompletion(String habitId, boolean isCompleted) {
        if (isCompleted && !completedHabitIds.contains(habitId)) {
            completedHabitIds.add(habitId);
        } else if (!isCompleted && completedHabitIds.contains(habitId)) {
            completedHabitIds.remove(habitId);
        }
        
        // Find and notify the specific item
        for (int i = 0; i < habits.size(); i++) {
            if (habits.get(i).getHabitId().equals(habitId)) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    class HabitViewHolder extends RecyclerView.ViewHolder {
        // Main card
        private MaterialCardView habitCardView;
        
        // Header elements
        private MaterialCardView categoryIconContainer;
        private ImageView categoryIcon;
        private TextView habitName;
        private Chip categoryChip;
        private Chip frequencyChip;
        private ImageView priorityIcon;
        private MaterialCardView quickCompleteButton;
        private ImageView checkIcon;
        
        // Content elements
        private TextView habitDescription;
        private LinearLayout targetProgressSection;
        private TextView targetText;
        private ProgressBar progressBar;
        private TextView progressText;
        
        // Streak section
        private TextView streakText;
        private TextView longestStreakText;
        private TextView lastCompletedText;
        
        // Reminder section
        private LinearLayout reminderSection;
        private TextView reminderText;
        
        // Action buttons
        private MaterialButton updateProgressButton;
        private MaterialButton editButton;
        private MaterialButton moreOptionsButton;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Initialize views with new layout
            habitCardView = itemView.findViewById(R.id.habitCardView);
            categoryIconContainer = itemView.findViewById(R.id.habitCardView).findViewById(R.id.categoryIcon).getParent() instanceof MaterialCardView ? 
                (MaterialCardView) itemView.findViewById(R.id.categoryIcon).getParent() : null;
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            habitName = itemView.findViewById(R.id.habitName);
            categoryChip = itemView.findViewById(R.id.categoryChip);
            frequencyChip = itemView.findViewById(R.id.frequencyChip);
            priorityIcon = itemView.findViewById(R.id.priorityIcon);
            quickCompleteButton = itemView.findViewById(R.id.quickCompleteButton);
            checkIcon = itemView.findViewById(R.id.checkIcon);
            
            habitDescription = itemView.findViewById(R.id.habitDescription);
            targetProgressSection = itemView.findViewById(R.id.targetProgressSection);
            targetText = itemView.findViewById(R.id.targetText);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressText = itemView.findViewById(R.id.progressText);
            
            streakText = itemView.findViewById(R.id.streakText);
            longestStreakText = itemView.findViewById(R.id.longestStreakText);
            lastCompletedText = itemView.findViewById(R.id.lastCompletedText);
            
            reminderSection = itemView.findViewById(R.id.reminderSection);
            reminderText = itemView.findViewById(R.id.reminderText);
            
            updateProgressButton = itemView.findViewById(R.id.updateProgressButton);
            editButton = itemView.findViewById(R.id.editButton);
            moreOptionsButton = itemView.findViewById(R.id.moreOptionsButton);
            
            setupClickListeners();
        }
        
        private void setupClickListeners() {
            // Main card click
            if (habitCardView != null) {
                habitCardView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onHabitClick(habits.get(position));
                    }
                });
                
                habitCardView.setOnLongClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onHabitLongClick(habits.get(position));
                        return true;
                    }
                    return false;
                });
            }
            
            // Quick complete button
            if (quickCompleteButton != null) {
                quickCompleteButton.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onQuickCompleteHabit(habits.get(position));
                    }
                });
            }
            
            // Update progress button
            if (updateProgressButton != null) {
                updateProgressButton.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onUpdateProgress(habits.get(position));
                    }
                });
            }
            
            // Edit button
            if (editButton != null) {
                editButton.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onHabitEdit(habits.get(position));
                    }
                });
            }
            
            // More options button
            if (moreOptionsButton != null) {
                moreOptionsButton.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onMoreOptions(habits.get(position));
                    }
                });
            }
        }
        
        public void bind(Habit habit, boolean isCompleted) {
            // Basic habit information
            if (habitName != null) {
                habitName.setText(habit.getName());
            }
            
            // Description
            if (habitDescription != null) {
                if (habit.getDescription() != null && !habit.getDescription().trim().isEmpty()) {
                    habitDescription.setVisibility(View.VISIBLE);
                    habitDescription.setText(habit.getDescription());
                } else {
                    habitDescription.setVisibility(View.GONE);
                }
            }
            
            // Category chip
            if (categoryChip != null) {
                categoryChip.setText(habit.getCategoryDisplayName());
                // Set category color
                if (habit.getCategoryEnum() != null) {
                    try {
                        int color = Color.parseColor(habit.getCategoryEnum().getColor());
                        categoryChip.setChipBackgroundColorResource(R.color.primary_container);
                    } catch (Exception e) {
                        categoryChip.setChipBackgroundColorResource(R.color.primary_container);
                    }
                }
            }
            
            // Frequency chip
            if (frequencyChip != null) {
                frequencyChip.setText(habit.getDisplayFrequency());
            }
            
            // Category icon
            setupCategoryIcon(habit.getCategoryEnum());
            
            // Priority indicator
            setupPriorityIndicator(habit);
            
            // Progress section
            setupProgressDisplay(habit, isCompleted);
            
            // Streak display
            setupStreakDisplay(habit);
            
            // Reminder display
            setupReminderDisplay(habit);
            
            // Last completed
            setupLastCompletedDisplay(habit);
            
            // Quick complete button state
            setupQuickCompleteButton(isCompleted);
            
            // Card styling
            setupCardStyling(habit, isCompleted);
        }
        
        private void setupCategoryIcon(Habit.HabitCategory category) {
            if (categoryIcon == null) return;
            
            // Set icon based on category
            switch (category) {
                case HEALTH:
                    categoryIcon.setImageResource(R.drawable.ic_health);
                    break;
                case FITNESS:
                    categoryIcon.setImageResource(R.drawable.ic_fitness);
                    break;
                case NUTRITION:
                    categoryIcon.setImageResource(R.drawable.ic_nutrition);
                    break;
                case MINDFULNESS:
                    categoryIcon.setImageResource(R.drawable.ic_mindfulness);
                    break;
                case PRODUCTIVITY:
                    categoryIcon.setImageResource(R.drawable.ic_productivity);
                    break;
                case LEARNING:
                    categoryIcon.setImageResource(R.drawable.ic_learning);
                    break;
                case SOCIAL:
                    categoryIcon.setImageResource(R.drawable.ic_social);
                    break;
                case CREATIVITY:
                    categoryIcon.setImageResource(R.drawable.ic_creativity);
                    break;
                case LIFESTYLE:
                    categoryIcon.setImageResource(R.drawable.ic_lifestyle);
                    break;
                default:
                    categoryIcon.setImageResource(R.drawable.ic_category);
                    break;
            }
        }
        
        private void setupPriorityIndicator(Habit habit) {
            if (priorityIcon != null) {
                if (habit.getPriority() != null && habit.getPriority() != Habit.HabitPriority.MEDIUM) {
                    priorityIcon.setVisibility(View.VISIBLE);
                    switch (habit.getPriority()) {
                        case HIGH:
                            priorityIcon.setImageResource(R.drawable.ic_priority_high);
                            break;
                        case LOW:
                            priorityIcon.setImageResource(R.drawable.ic_priority_low);
                            break;
                        default:
                            priorityIcon.setVisibility(View.GONE);
                            break;
                    }
                } else {
                    priorityIcon.setVisibility(View.GONE);
                }
            }
        }
        
        private void setupProgressDisplay(Habit habit, boolean isCompleted) {
            // Find today's log for this habit
            HabitLog todaysLog = null;
            for (HabitLog log : todaysLogs) {
                if (habit.getHabitId().equals(log.getHabitId())) {
                    todaysLog = log;
                    break;
                }
            }
            
            boolean hasTarget = habit.getTarget() > 0 && habit.getUnit() != null && !habit.getUnit().trim().isEmpty();
            
            if (hasTarget && targetProgressSection != null) {
                // Show target-based progress
                targetProgressSection.setVisibility(View.VISIBLE);
                
                if (targetText != null) {
                    targetText.setText(String.format(Locale.getDefault(), 
                        "Target: %.0f %s", habit.getTarget(), habit.getUnit()));
                }
                
                if (progressBar != null && progressText != null) {
                    double todaysProgress = todaysLog != null ? todaysLog.getProgressValue() : 0.0;
                    double target = habit.getTarget();
                    double progressPercentage = Math.min(100.0, (todaysProgress / target) * 100.0);
                    
                    progressBar.setProgress((int) progressPercentage);
                    progressText.setText(String.format(Locale.getDefault(), "%.0f%%", progressPercentage));
                }
            } else {
                // Hide target section for simple completion habits
                if (targetProgressSection != null) {
                    targetProgressSection.setVisibility(View.GONE);
                }
            }
        }
        
        private void setupStreakDisplay(Habit habit) {
            if (habit.getStreakData() != null) {
                if (streakText != null) {
                    streakText.setText(String.valueOf(habit.getStreakData().getCurrentStreak()));
                }
                
                if (longestStreakText != null) {
                    longestStreakText.setText(String.valueOf(habit.getStreakData().getLongestStreak()));
                }
            } else {
                if (streakText != null) streakText.setText("0");
                if (longestStreakText != null) longestStreakText.setText("0");
            }
        }
        
        private void setupReminderDisplay(Habit habit) {
            if (reminderSection != null && reminderText != null) {
                if (habit.isReminderEnabled() && habit.getReminderTime() != null) {
                    reminderSection.setVisibility(View.VISIBLE);
                    reminderText.setText("Reminder: " + habit.getReminderTime());
                } else {
                    reminderSection.setVisibility(View.GONE);
                }
            }
        }
        
        private void setupLastCompletedDisplay(Habit habit) {
            if (lastCompletedText != null) {
                if (habit.getStreakData() != null && habit.getStreakData().getLastCompletedDate() != null) {
                    Date lastCompleted = habit.getStreakData().getLastCompletedDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                    
                    // Check if it's today or yesterday
                    long daysDiff = (System.currentTimeMillis() - lastCompleted.getTime()) / (1000 * 60 * 60 * 24);
                    
                    String displayText;
                    if (daysDiff == 0) {
                        displayText = "Today";
                    } else if (daysDiff == 1) {
                        displayText = "Yesterday";
                    } else {
                        displayText = sdf.format(lastCompleted);
                    }
                    
                    lastCompletedText.setText(displayText);
                    lastCompletedText.setVisibility(View.VISIBLE);
                } else {
                    lastCompletedText.setVisibility(View.GONE);
                }
            }
        }
        
        private void setupQuickCompleteButton(boolean isCompleted) {
            if (quickCompleteButton != null && checkIcon != null) {
                if (isCompleted) {
                    // Show as completed
                    quickCompleteButton.setStrokeColor(context.getResources().getColor(R.color.success, null));
                    quickCompleteButton.setCardBackgroundColor(context.getResources().getColor(R.color.secondary_container, null));
                    checkIcon.setImageResource(R.drawable.ic_current);
                } else {
                    // Show as incomplete
                    quickCompleteButton.setStrokeColor(context.getResources().getColor(R.color.outline, null));
                    quickCompleteButton.setCardBackgroundColor(context.getResources().getColor(R.color.surface, null));
                    checkIcon.setImageResource(R.drawable.ic_add);
                }
            }
        }
        
        private void setupCardStyling(Habit habit, boolean isCompleted) {
            if (habitCardView != null) {
                if (isCompleted) {
                    // Completed styling
                    habitCardView.setStrokeColor(context.getResources().getColor(R.color.success, null));
                    habitCardView.setStrokeWidth(2);
                    habitCardView.setCardBackgroundColor(context.getResources().getColor(R.color.secondary_container, null));
                } else if (!habit.isActive()) {
                    // Inactive styling
                    habitCardView.setStrokeColor(context.getResources().getColor(R.color.outline, null));
                    habitCardView.setStrokeWidth(1);
                    habitCardView.setCardBackgroundColor(context.getResources().getColor(R.color.surface_variant, null));
                } else {
                    // Default styling
                    habitCardView.setStrokeColor(context.getResources().getColor(R.color.outline_variant, null));
                    habitCardView.setStrokeWidth(1);
                    habitCardView.setCardBackgroundColor(context.getResources().getColor(R.color.surface, null));
                }
            }
        }
    }
} 