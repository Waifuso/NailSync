package com.example.androidUI;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

public class DatePickerAdapter extends RecyclerView.Adapter<DatePickerAdapter.DateViewHolder> {
    private List<String> dates;
    private int selectedPosition = -1;
    private final OnDateSelectedListener listener;

    public interface OnDateSelectedListener {
        void onDateSelected(String date);
    }

    public DatePickerAdapter(List<String> dates, OnDateSelectedListener listener) {
        this.dates = dates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        int currentPosition = holder.getAdapterPosition();
        if (dates == null || dates.isEmpty() || currentPosition < 0 || currentPosition >= dates.size()) {
            Log.e("DatePickerAdapter", "Invalid position: " + position);
            return;
        }

        String date = dates.get(currentPosition);
        String dayNumber = date.substring(date.lastIndexOf('-') + 1); // Extract day number
        holder.dateText.setText(dayNumber);

        // Highlight selected date
        if (selectedPosition == currentPosition) {
            holder.dateText.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary));
            holder.dateText.setTextColor(Color.WHITE);
        } else {
            holder.dateText.setBackgroundColor(Color.TRANSPARENT);
            holder.dateText.setTextColor(Color.BLACK);
        }

        // Click listener for date selection
        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = currentPosition;

            // Notify previous and new selection to update UI
            if (previousSelected >= 0) {
                notifyItemChanged(previousSelected);
            }
            notifyItemChanged(selectedPosition);

            // Trigger callback to load appointments for selected date
            listener.onDateSelected(date);
        });
    }

    public void updateDates(List<String> newDates) {
        this.dates.clear();
        this.dates.addAll(newDates);
        notifyDataSetChanged();  // 🔄 Refresh the RecyclerView
    }



    @Override
    public int getItemCount() {
        return dates.size();
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }
}
