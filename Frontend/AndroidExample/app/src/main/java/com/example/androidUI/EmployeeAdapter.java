package com.example.androidUI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    private final Context context;
    private final List<Employee> employeeList;
    private final OnEmployeeClickListener listener;

    public interface OnEmployeeClickListener {
        void onEmployeeClick(Employee employee);
    }

    public EmployeeAdapter(Context context, List<Employee> employeeList, OnEmployeeClickListener listener) {
        this.context = context;
        this.employeeList = employeeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_employee, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        Employee employee = employeeList.get(position);

        // Set the employee name
        holder.employeeName.setText(employee.getName());

        // Generate a first letter for the avatar using the employee name
        String firstLetter = "";
        if (employee.getName() != null && !employee.getName().isEmpty()) {
            firstLetter = employee.getName().substring(0, 1).toUpperCase();
        }

        // Customize the avatar based on the employee (this could be enhanced with real profile pics)
        holder.employeeAvatar.setImageResource(R.drawable.ic_profile_pic);

        // Set up click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEmployeeClick(employee);
            }
        });
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        ImageView employeeAvatar;
        TextView employeeName;
        TextView employeeRole;

        EmployeeViewHolder(View itemView) {
            super(itemView);
            employeeAvatar = itemView.findViewById(R.id.employeeAvatar);
            employeeName = itemView.findViewById(R.id.employeeName);
            employeeRole = itemView.findViewById(R.id.employeeRole);
        }
    }
}