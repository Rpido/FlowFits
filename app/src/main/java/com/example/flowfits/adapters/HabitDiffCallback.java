package com.example.flowfits.adapters;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import com.example.flowfits.models.Habit;
import com.example.flowfits.models.HabitLog;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HabitDiffCallback extends DiffUtil.Callback {

    private final List<Habit> oldHabits;
    private final List<Habit> newHabits;
    private final Map<String, HabitLog> oldLogs;
    private final Map<String, HabitLog> newLogs;

    public HabitDiffCallback(List<Habit> oldHabits, List<Habit> newHabits, Map<String, HabitLog> oldLogs, Map<String, HabitLog> newLogs) {
        this.oldHabits = oldHabits;
        this.newHabits = newHabits;
        this.oldLogs = oldLogs;
        this.newLogs = newLogs;
    }

    @Override
    public int getOldListSize() {
        return oldHabits.size();
    }

    @Override
    public int getNewListSize() {
        return newHabits.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldHabits.get(oldItemPosition).getHabitId().equals(newHabits.get(newItemPosition).getHabitId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Habit oldHabit = oldHabits.get(oldItemPosition);
        Habit newHabit = newHabits.get(newItemPosition);

        HabitLog oldLog = oldLogs != null ? oldLogs.get(oldHabit.getHabitId()) : null;
        HabitLog newLog = newLogs != null ? newLogs.get(newHabit.getHabitId()) : null;

        double oldProgress = oldLog != null ? oldLog.getProgress() : 0;
        double newProgress = newLog != null ? newLog.getProgress() : 0;

        return oldHabit.getName().equals(newHabit.getName()) &&
               oldHabit.getStreakData().getCurrentStreak() == newHabit.getStreakData().getCurrentStreak() &&
               oldProgress == newProgress;
    }
} 