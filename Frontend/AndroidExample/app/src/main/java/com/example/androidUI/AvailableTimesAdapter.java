package com.example.androidUI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

public class AvailableTimesAdapter extends RecyclerView.Adapter<AvailableTimesAdapter.TimeViewHolder> {

    private List<String> availableTimes;
    private OnTimeSelectedListener timeSelectedListener;
    private String selectedTime = "";

    public interface OnTimeSelectedListener {
        void onTimeSelected(String time);
    }

    public AvailableTimesAdapter(List<String> availableTimes, OnTimeSelectedListener listener) {
        this.availableTimes = availableTimes;
        this.timeSelectedListener = listener;
    }

    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_available_time, parent, false);
        return new TimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        String time = availableTimes.get(position);
        holder.tvTime.setText(time);

        // Set background based on selection state
        if (time.equals(selectedTime)) {
            holder.cardTime.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.selected_time_color)
            );
            holder.tvTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.white)
            );
        } else {
            holder.cardTime.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.white)
            );
            holder.tvTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_dark_purple)
            );
        }

        holder.itemView.setOnClickListener(v -> {
            // Update selected time
            String previouslySelected = selectedTime;
            selectedTime = time;

            // Notify adapter of changes
            if (!time.equals(previouslySelected)) {
                notifyDataSetChanged();
                timeSelectedListener.onTimeSelected(time);
            }
        });
    }

    @Override
    public int getItemCount() {
        return availableTimes.size();
    }

    public void setSelectedTime(String time) {
        this.selectedTime = time;
        notifyDataSetChanged();
    }

    static class TimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        CardView cardTime;

        TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            cardTime = itemView.findViewById(R.id.cardTime);
        }
    }
}