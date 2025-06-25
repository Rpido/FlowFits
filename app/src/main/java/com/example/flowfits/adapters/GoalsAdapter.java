package com.example.flowfits.adapters;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flowfits.R;
import com.example.flowfits.models.Goal;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.GoalViewHolder> {

    private List<Goal> goals;
    private List<Goal> allGoals; // Keep reference to all goals for filtering
    private OnGoalClickListener listener;
    private SimpleDateFormat dateFormat;
    private String currentFilter = null;

    public interface OnGoalClickListener {
        void onGoalClick(Goal goal);
        void onGoalEdit(Goal goal);
        void onGoalDelete(Goal goal);
        void onGoalUpdateProgress(Goal goal);
    }

    public GoalsAdapter() {
        this.goals = new ArrayList<>();
        this.allGoals = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    public void setOnGoalClickListener(OnGoalClickListener listener) {
        this.listener = listener;
    }

    public void setGoals(List<Goal> goals) {
        System.out.println("DEBUG: GoalsAdapter - setGoals called with " + (goals != null ? goals.size() : 0) + " goals");
        this.allGoals = goals != null ? new ArrayList<>(goals) : new ArrayList<>();
        applyFilter();
        System.out.println("DEBUG: GoalsAdapter - notifyDataSetChanged called");
    }
    
    public void addGoal(Goal goal) {
        System.out.println("DEBUG: GoalsAdapter - addGoal called for: " + goal.getTitle());
        allGoals.add(goal);
        
        // Check if goal should be visible with current filter
        if (currentFilter == null || currentFilter.isEmpty() || 
            (goal.getStatusString() != null && goal.getStatusString().equalsIgnoreCase(currentFilter))) {
            // Add to top of list for immediate visibility of new goals
            goals.add(0, goal);
            notifyItemInserted(0);
        }
    }
    
    public void updateGoal(Goal updatedGoal) {
        System.out.println("DEBUG: GoalsAdapter - updateGoal called for: " + updatedGoal.getTitle());
        
        // Update in allGoals
        for (int i = 0; i < allGoals.size(); i++) {
            if (allGoals.get(i).getGoalId().equals(updatedGoal.getGoalId())) {
                allGoals.set(i, updatedGoal);
                break;
            }
        }
        
        // Update in filtered goals
        for (int i = 0; i < goals.size(); i++) {
            if (goals.get(i).getGoalId().equals(updatedGoal.getGoalId())) {
                goals.set(i, updatedGoal);
                notifyItemChanged(i);
                return;
            }
        }
        
        // If not found in filtered list, re-apply filter
        applyFilter();
    }

    public void setFilter(String filter) {
        this.currentFilter = filter;
        applyFilter();
    }

    private void applyFilter() {
        if (currentFilter == null || currentFilter.isEmpty()) {
            this.goals = new ArrayList<>(allGoals);
        } else {
            this.goals = new ArrayList<>();
            for (Goal goal : allGoals) {
                if (goal.getStatusString() != null && 
                    goal.getStatusString().equalsIgnoreCase(currentFilter)) {
                    this.goals.add(goal);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        System.out.println("DEBUG: GoalsAdapter - onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal_card, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        System.out.println("DEBUG: GoalsAdapter - onBindViewHolder called for position: " + position);
        Goal goal = goals.get(position);
        System.out.println("DEBUG: GoalsAdapter - Binding goal: " + goal.getTitle());
        holder.bind(goal);
    }

    @Override
    public int getItemCount() {
        int count = goals.size();
        System.out.println("DEBUG: GoalsAdapter - getItemCount returning: " + count);
        return count;
    }

    class GoalViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView descriptionText;
        private TextView progressText;
        private TextView progressValuesText;
        private TextView remainingText;
        private TextView statusText;
        private TextView deadlineText;
        private TextView deadlineDateText;
        private TextView streakText;
        private TextView nextMilestoneText;
        private Chip priorityChip;
        private Chip categoryChip;
        private ProgressBar progressBar;
        private LinearLayout milestoneLayout;
        private LinearLayout streakLayout;
        private LinearLayout expandedActionsLayout;
        private MaterialButton updateProgressButton;
        private MaterialButton menuButton;
        private MaterialButton editButton;
        private MaterialButton deleteButton;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Initialize views with new IDs
            titleText = itemView.findViewById(R.id.tv_goal_title);
            descriptionText = itemView.findViewById(R.id.tv_goal_description);
            progressText = itemView.findViewById(R.id.tv_goal_progress);
            progressValuesText = itemView.findViewById(R.id.tv_progress_values);
            remainingText = itemView.findViewById(R.id.tv_remaining);
            statusText = itemView.findViewById(R.id.tv_goal_status);
            deadlineText = itemView.findViewById(R.id.tv_goal_deadline);
            deadlineDateText = itemView.findViewById(R.id.tv_deadline_date);
            streakText = itemView.findViewById(R.id.tv_goal_streak);
            nextMilestoneText = itemView.findViewById(R.id.tv_next_milestone);
            priorityChip = itemView.findViewById(R.id.chip_goal_priority);
            categoryChip = itemView.findViewById(R.id.chip_goal_category);
            progressBar = itemView.findViewById(R.id.progress_goal);
            milestoneLayout = itemView.findViewById(R.id.layout_milestone);
            streakLayout = itemView.findViewById(R.id.layout_streak);
            expandedActionsLayout = itemView.findViewById(R.id.layout_expanded_actions);
            updateProgressButton = itemView.findViewById(R.id.btn_update_progress);
            menuButton = itemView.findViewById(R.id.btn_menu);
            editButton = itemView.findViewById(R.id.btn_edit_goal);
            deleteButton = itemView.findViewById(R.id.btn_delete_goal);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onGoalClick(goals.get(getAdapterPosition()));
                }
            });

            updateProgressButton.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onGoalUpdateProgress(goals.get(getAdapterPosition()));
                }
            });

            // Menu toggle functionality
            menuButton.setOnClickListener(v -> {
                boolean isExpanded = expandedActionsLayout.getVisibility() == View.VISIBLE;
                if (isExpanded) {
                    expandedActionsLayout.setVisibility(View.GONE);
                    menuButton.setIconResource(R.drawable.ic_expand_more);
                } else {
                    expandedActionsLayout.setVisibility(View.VISIBLE);
                    menuButton.setIconResource(R.drawable.ic_expand_less);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onGoalEdit(goals.get(getAdapterPosition()));
                }
                // Collapse menu after action
                expandedActionsLayout.setVisibility(View.GONE);
                menuButton.setIconResource(R.drawable.ic_expand_more);
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onGoalDelete(goals.get(getAdapterPosition()));
                }
                // Collapse menu after action
                expandedActionsLayout.setVisibility(View.GONE);
                menuButton.setIconResource(R.drawable.ic_expand_more);
            });
        }

        public void bind(Goal goal) {
            System.out.println("DEBUG: GoalViewHolder - bind called for goal: " + goal.getTitle());
            
            // Reset expanded state
            expandedActionsLayout.setVisibility(View.GONE);
            menuButton.setIconResource(R.drawable.ic_expand_more);
            
            // Basic info
            titleText.setText(goal.getTitle());
            descriptionText.setText(goal.getDescription());
            
            // Category chip
            categoryChip.setText(goal.getCategory().replace("_", " ").toUpperCase());
            
            // Priority chip
            if (goal.getPriority() != null) {
                priorityChip.setText(goal.getPriority().getDisplayName().toUpperCase());
                priorityChip.setVisibility(View.VISIBLE);
                
                // Set priority chip colors based on priority level
                try {
                    switch (goal.getPriority()) {
                        case HIGH:
                        case CRITICAL:
                            priorityChip.setChipBackgroundColorResource(android.R.color.holo_red_dark);
                            break;
                        case MEDIUM:
                            priorityChip.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
                            break;
                        case LOW:
                            priorityChip.setChipBackgroundColorResource(android.R.color.holo_green_dark);
                            break;
                    }
                } catch (Exception e) {
                    // Fallback to default colors if theme attributes are not available
                    System.out.println("DEBUG: Using fallback colors for priority chip");
                }
            } else {
                priorityChip.setVisibility(View.GONE);
            }
            
            // Status
            if (goal.getStatus() != null) {
                statusText.setText(goal.getStatus().getDisplayName().toUpperCase());
                statusText.setVisibility(View.VISIBLE);
            } else {
                statusText.setVisibility(View.GONE);
            }
            
            // Progress
            double progress = goal.getProgressPercentage();
            progressText.setText(String.format("%.0f%%", progress));
            progressBar.setProgress((int) progress);
            
            // Progress values
            progressValuesText.setText(String.format("%.1f / %.1f %s", 
                goal.getCurrentValue(), goal.getTargetValue(), goal.getUnit()));
            
            // Remaining amount
            double remaining = goal.getTargetValue() - goal.getCurrentValue();
            if (remaining > 0) {
                remainingText.setText(String.format("%.1f %s to go", remaining, goal.getUnit()));
                remainingText.setVisibility(View.VISIBLE);
            } else {
                remainingText.setText("Goal achieved! 🎉");
                remainingText.setVisibility(View.VISIBLE);
            }
            
            // Deadline info
            updateDeadlineDisplay(goal);
            
            // Streak display
            updateStreakDisplay(goal);
            
            // Milestone display
            updateMilestoneDisplay(goal);
        }
        
        private void updateDeadlineDisplay(Goal goal) {
            if (deadlineText != null && deadlineDateText != null && goal.getDeadline() != null && !goal.getDeadline().isEmpty()) {
                try {
                    // Use the Goal's accurate getDaysRemaining() method
                    long daysRemaining = goal.getDaysRemaining();
                    
                    // Set the days remaining text
                    if (daysRemaining > 1) {
                        deadlineText.setText(daysRemaining + " days left");
                    } else if (daysRemaining == 1) {
                        deadlineText.setText("1 day left");
                    } else if (daysRemaining == 0) {
                        deadlineText.setText("Due today");
                    } else if (daysRemaining == -1) {
                        deadlineText.setText("1 day overdue");
                } else {
                        deadlineText.setText(Math.abs(daysRemaining) + " days overdue");
                    }
                    
                    // Set the actual deadline date
                    deadlineDateText.setText("Due " + goal.getDeadline());
                    
                    // Show both texts
                    deadlineText.setVisibility(View.VISIBLE);
                    deadlineDateText.setVisibility(View.VISIBLE);
                    
                    // Add debugging
                    System.out.println("DEBUG: Goal '" + goal.getTitle() + "' deadline: " + goal.getDeadline() + ", days remaining: " + daysRemaining);
                    
                } catch (Exception e) {
                    System.out.println("DEBUG: Error updating deadline display: " + e.getMessage());
                    // Fallback: just show the deadline string if calculation fails
                    deadlineText.setText("Due soon");
                    deadlineDateText.setText("Due " + goal.getDeadline());
                    deadlineText.setVisibility(View.VISIBLE);
                    deadlineDateText.setVisibility(View.VISIBLE);
                }
            } else {
                // Hide deadline info if no deadline is set
                deadlineText.setVisibility(View.GONE);
                deadlineDateText.setVisibility(View.GONE);
            }
        }
        
        private void updateMilestoneDisplay(Goal goal) {
            if (milestoneLayout != null && nextMilestoneText != null) {
                // Show milestone for goals that are < 50% complete
                if (goal.getProgressPercentage() < 50) {
                    double nextMilestone = Math.ceil(goal.getTargetValue() * 0.25); // 25% milestone
                    if (goal.getCurrentValue() < nextMilestone) {
                        nextMilestoneText.setText(String.format("🎯 Next milestone: %.0f %s", 
                            nextMilestone, goal.getUnit()));
                        milestoneLayout.setVisibility(View.VISIBLE);
                    } else {
                        milestoneLayout.setVisibility(View.GONE);
                    }
                } else {
            milestoneLayout.setVisibility(View.GONE);
                }
            }
        }
        
        private void updateStreakDisplay(Goal goal) {
            if (streakLayout != null && streakText != null) {
            if (goal.getStreakDays() > 0) {
                    streakText.setText(goal.getStreakDays() + " days");
                streakLayout.setVisibility(View.VISIBLE);
            } else {
                streakLayout.setVisibility(View.GONE);
            }
            }
        }
    }
} 