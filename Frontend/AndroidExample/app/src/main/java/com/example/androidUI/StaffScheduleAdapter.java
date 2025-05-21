package com.example.androidUI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;
import com.example.androidexample.StaffSchedule;
import com.example.androidexample.TimeSlot;

import java.util.List;

public class StaffScheduleAdapter extends RecyclerView.Adapter<StaffScheduleAdapter.ViewHolder> {
    private List<StaffSchedule> staffScheduleList;
    private Context context;

    public StaffScheduleAdapter(Context context, List<StaffSchedule> staffScheduleList) {
        this.context = context;
        this.staffScheduleList = staffScheduleList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StaffSchedule staffSchedule = staffScheduleList.get(position);

        // Set the employee name
        holder.employeeNameText.setText(staffSchedule.getEmployeeName());

        // Handle the time slots
        if (staffSchedule.hasTimeSlots()) {
            // Show available times
            StringBuilder timesBuilder = new StringBuilder();
            for (int i = 0; i < staffSchedule.getTimeSlots().size(); i++) {
                TimeSlot slot = staffSchedule.getTimeSlots().get(i);
                timesBuilder.append(slot.getFormattedTimeRange());
                if (i < staffSchedule.getTimeSlots().size() - 1) {
                    timesBuilder.append("\n");
                }
            }
            holder.scheduleTimesText.setText(timesBuilder.toString());
            holder.scheduleStatusText.setText("Available");
            holder.scheduleStatusText.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            // No time slots available
            holder.scheduleTimesText.setText("No shifts scheduled");
            holder.scheduleStatusText.setText("Not Available");
            holder.scheduleStatusText.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public int getItemCount() {
        return staffScheduleList.size();
    }

    public void updateData(List<StaffSchedule> newStaffList) {
        this.staffScheduleList = newStaffList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView employeeNameText;
        TextView scheduleTimesText;
        TextView scheduleStatusText;

        ViewHolder(View itemView) {
            super(itemView);
            employeeNameText = itemView.findViewById(R.id.employeeNameText);
            scheduleTimesText = itemView.findViewById(R.id.scheduleTimesText);
            scheduleStatusText = itemView.findViewById(R.id.scheduleStatusText);
        }
    }
}
