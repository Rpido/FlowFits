package com.example.flowfits.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowfits.R;
import com.example.flowfits.models.AnalyticsData;

import java.util.ArrayList;
import java.util.List;

public class QuickStatsAdapter extends RecyclerView.Adapter<QuickStatsAdapter.QuickStatViewHolder> {

    private List<? extends AnalyticsData.QuickStat> stats = new ArrayList<>();

    public void updateStats(List<? extends AnalyticsData.QuickStat> newStats) {
        this.stats = newStats != null ? newStats : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuickStatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stat_card, parent, false);
        return new QuickStatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuickStatViewHolder holder, int position) {
        AnalyticsData.QuickStat stat = stats.get(position);
        holder.bind(stat);
    }

    @Override
    public int getItemCount() {
        return stats.size();
    }

    public static class QuickStatViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivStatIcon;
        private final TextView tvStatValue;
        private final TextView tvStatLabel;

        public QuickStatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStatIcon = itemView.findViewById(R.id.iv_stat_icon);
            tvStatValue = itemView.findViewById(R.id.tv_stat_value);
            tvStatLabel = itemView.findViewById(R.id.tv_stat_label);
        }

        public void bind(AnalyticsData.QuickStat stat) {
            tvStatValue.setText(stat.getValue());
            tvStatLabel.setText(stat.getLabel());
            if (ivStatIcon != null) {
                if (stat instanceof StatWithIcon) {
                    ivStatIcon.setImageResource(((StatWithIcon) stat).getIconResId());
                } else {
                    ivStatIcon.setImageResource(R.drawable.ic_analytics); // fallback icon
                }
            }
        }
    }

    // New stat model for icon support
    public static class StatWithIcon extends AnalyticsData.QuickStat {
        private final int iconResId;
        public StatWithIcon(String title, String value, String label, int iconResId) {
            super(title, value, "", label);
            this.iconResId = iconResId;
        }
        public int getIconResId() { return iconResId; }
    }
} 