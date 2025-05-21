package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.androidUI.AppointmentAdapter;
import com.example.androidUI.AppointmentDetailsDialog;
import com.example.androidUI.AppointmentModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViewAllAppointmentsActivity extends AppCompatActivity
        implements AppointmentDetailsDialog.AppointmentActionListener {

    // UI Components
    private RecyclerView recyclerViewAppointments;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private AppointmentAdapter adapter;
    private TextView tvDateFilter;

    // Data
    private List<AppointmentModel> appointmentsList = new ArrayList<>();
    private long employeeID = -1;
    private String currentDate;

    // Tag for logging
    private static final String TAG = "EmployeeViewAppointments";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_appointments);

        // Initialize views
        initializeViews();

        // Extract employee ID from intent
        extractIntentData();

        // Set up RecyclerView
        setupRecyclerView();

        // Set up date filtering
        setupDateFilter();

        // Fetch appointments data for today
        fetchEmployeeAppointments();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Today's Appointments");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewAppointments = findViewById(R.id.recyclerViewAppointments);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Date filter - assuming you have this TextView in your layout or will add it
        tvDateFilter = findViewById(R.id.tvDateFilter);
        if (tvDateFilter == null) {
            Log.w(TAG, "Date filter TextView not found in layout");
        }
    }

    private void extractIntentData() {
        // Extract employee ID from intent
        employeeID = getIntent().getLongExtra("employeeID", -1);
        Log.d(TAG, "Employee ID extracted: " + employeeID);

        // Set current date to today
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currentDate = dateFormat.format(new Date());
    }

    private void setupRecyclerView() {
        adapter = new AppointmentAdapter(
                this,
                appointmentsList,
                this::onViewDetailsClicked,
                null); // Employees don't cancel appointments

        recyclerViewAppointments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAppointments.setAdapter(adapter);
    }

    private void setupDateFilter() {
        if (tvDateFilter != null) {
            // Format today's date for display
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            tvDateFilter.setText(displayFormat.format(new Date()));

            // Set up date navigation buttons if they exist
            View btnPrevDay = findViewById(R.id.btnPrevDay);
            View btnNextDay = findViewById(R.id.btnNextDay);

            if (btnPrevDay != null) {
                btnPrevDay.setOnClickListener(v -> {
                    changeDate(-1);
                });
            }

            if (btnNextDay != null) {
                btnNextDay.setOnClickListener(v -> {
                    changeDate(1);
                });
            }
        }
    }

    private void changeDate(int dayOffset) {
        // Parse current date
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

            Date date = dateFormat.parse(currentDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // Add or subtract days
            calendar.add(Calendar.DAY_OF_MONTH, dayOffset);

            // Update current date
            currentDate = dateFormat.format(calendar.getTime());

            // Update UI
            if (tvDateFilter != null) {
                tvDateFilter.setText(displayFormat.format(calendar.getTime()));
            }

            // Update title based on date
            Date today = new Date();
            if (dateFormat.format(today).equals(currentDate)) {
                getSupportActionBar().setTitle("Today's Appointments");
            } else {
                getSupportActionBar().setTitle("Appointments");
            }

            // Fetch appointments for new date
            fetchEmployeeAppointments();

        } catch (Exception e) {
            Log.e(TAG, "Error changing date: " + e.getMessage());
        }
    }

    private void fetchEmployeeAppointments() {
        // Show loading state
        showLoading(true);

        // If no valid employeeID, use sample data
        if (employeeID <= 0) {
            Log.d(TAG, "No valid employeeID found, using sample data");
            fallbackToSampleData();
            return;
        }

        // http://coms-3090-020.class.las.iastate.edu:8080/api/timeframe/get/appointment/employee?id=1&localDate=2025-04-11
        // Create the URL with employeeID and date as parameters
        String url = "http://coms-3090-020.class.las.iastate.edu:8080/api/timeframe/get/appointment/employee?id="
                + employeeID + "&localDate=" + currentDate;

        // Create the request
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null, // No request body needed for GET request
                response -> {
                    Log.d(TAG, "Response: " + response.toString());
                    try {
                        // Process the JSON array response
                        processAppointmentsResponse(response);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
                        fallbackToSampleData();
                    }
                },
                error -> {
                    Log.e(TAG, "Network error: " + error.toString(), error);
                    fallbackToSampleData();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Add any required headers here
                return headers;
            }
        };

        // Set timeout for the request
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }


    private void processAppointmentsResponse(JSONArray response) throws JSONException {
        // Clear existing list
        appointmentsList.clear();

        Log.d(TAG, "Processing " + response.length() + " appointments from server");

        // Process each appointment in the response
        for (int i = 0; i < response.length(); i++) {
            JSONObject appointmentObj = response.getJSONObject(i);

            // Log the appointment object to inspect its structure
            Log.d(TAG, "Appointment JSON: " + appointmentObj.toString());

            // Create a new appointment model
            AppointmentModel appointment = new AppointmentModel();

            // Parse basic details - ID is the only field we can be sure exists based on your sample
            appointment.setId(appointmentObj.getLong("id"));

            // Get date (using the current date filter)
            appointment.setDate(currentDate);

            // Extract start and end time - these are the key fields in your response
            String startTime = appointmentObj.getString("subStartTime");
            String endTime = appointmentObj.getString("subEndTime");

            // Set the time (just using start time)
            appointment.setTime(startTime.substring(0, 5)); // Format from "15:18:00" to "15:18"

            // Calculate duration in minutes
            int durationMinutes = calculateDurationInMinutes(startTime, endTime);
            appointment.setDuration(durationMinutes);

            // Set availability status from the response
            boolean isAvailable = appointmentObj.getBoolean("available");
            appointment.setStatus(isAvailable ? "Available" : "Booked");

            // Since we don't have client info, set a placeholder
            appointment.setEmployeeName("Time Slot #" + appointmentObj.getInt("id"));

            // Set notes about the time slot
            appointment.setNotes("Start: " + startTime + "\nEnd: " + endTime +
                    "\nAvailable: " + (isAvailable ? "Yes" : "No"));

            // Add placeholder for services since they're not in the response
            List<String> serviceNames = new ArrayList<>();
            serviceNames.add("Regular Appointment");
            appointment.setServiceNames(serviceNames);

            // hide pay button because reused
            appointment.setShowPayButton(false);

            // Add to the list
            appointmentsList.add(appointment);
        }

        // Sort appointments by time
        appointmentsList.sort((a1, a2) -> a1.getTime().compareTo(a2.getTime()));

        // Update UI based on response
        showLoading(false);
        if (appointmentsList.isEmpty()) {
            showEmptyState("No appointments found for this date");
        } else {
            adapter.notifyDataSetChanged();
            showContent();
        }

        Log.d(TAG, "Successfully loaded " + appointmentsList.size() + " appointments");
    }

    // Helper method to calculate duration in minutes between two time strings in format "HH:mm:ss"
    private int calculateDurationInMinutes(String startTime, String endTime) {
        try {
            // Parse the hours and minutes
            int startHour = Integer.parseInt(startTime.substring(0, 2));
            int startMinute = Integer.parseInt(startTime.substring(3, 5));

            int endHour = Integer.parseInt(endTime.substring(0, 2));
            int endMinute = Integer.parseInt(endTime.substring(3, 5));

            // Calculate total minutes
            int startTotalMinutes = startHour * 60 + startMinute;
            int endTotalMinutes = endHour * 60 + endMinute;

            // Return the difference
            return endTotalMinutes - startTotalMinutes;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating duration: " + e.getMessage());
            return 30; // Default to 30 minutes on error
        }
    }

    // Helper method for fallback to sample data
    private void fallbackToSampleData() {
        appointmentsList.clear();

        // Generate appointments for the current date
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(apiDateFormat.parse(currentDate));
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date for sample data: " + e.getMessage());
            // Just use today if there's an error
        }

        // Generate 5 sample appointments throughout the day
        for (int i = 0; i < 5; i++) {
            AppointmentModel appointment = new AppointmentModel();

            // Set ID
            appointment.setId(System.currentTimeMillis() + i);

            // Set date
            appointment.setDate(apiDateFormat.format(calendar.getTime()));

            // Set client name (for employees)
            appointment.setEmployeeName("Client: Sample Client " + (i + 1));

            // Set time slots throughout the day
            calendar.set(Calendar.HOUR_OF_DAY, 9 + i * 2); // 9AM, 11AM, 1PM, 3PM, 5PM
            calendar.set(Calendar.MINUTE, 0);
            appointment.setTime(timeFormat.format(calendar.getTime()));

            // Set duration
            appointment.setDuration((i % 2 == 0) ? 30 : 60);

            // Set services
            List<String> services = new ArrayList<>();
            if (i % 3 == 0) {
                services.add("SAMPLE");
                services.add("SAMPLE");
            } else if (i % 3 == 1) {
                services.add("SAMPLE");
                services.add("SAMPLE");
            } else {
                services.add("SAMPLE");
                services.add("SAMPLE");
            }
            appointment.setServiceNames(services);

            // Set status
            String[] statuses = {"Confirmed", "Upcoming", "Scheduled"};
            appointment.setStatus(statuses[i % statuses.length]);

            // Set notes
            appointment.setNotes("Confirmation #: " + (10000 + i) +
                    "\n\nClient Notes: Sample client preferences");

            // hide the payment button
            appointment.setShowPayButton(false);

            appointmentsList.add(appointment);
        }

        showLoading(false);
        if (appointmentsList.isEmpty()) {
            showEmptyState("No appointments found for this date");
        } else {
            adapter.notifyDataSetChanged();
            showContent();
        }

        Log.d(TAG, "Using simulated appointment data for date: " + currentDate);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        if (isLoading) {
            recyclerViewAppointments.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void showContent() {
        recyclerViewAppointments.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState(String message) {
        recyclerViewAppointments.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
        tvEmptyState.setText(message);
    }

    private void onViewDetailsClicked(AppointmentModel appointment) {
        // Find the position of the appointment in the list
        int position = appointmentsList.indexOf(appointment);

        // Create and show appointment details dialog with employee-specific options
        AppointmentDetailsDialog dialog = new AppointmentDetailsDialog(appointment);
        dialog.setActionListener(this);
        dialog.setAdapterPosition(position);
        // dialog.setEmployeeMode(true); // Enable employee mode if you add this flag
        dialog.show(getSupportFragmentManager(), "AppointmentDetails");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Implementation of AppointmentActionListener interface
    @Override
    public void onAppointmentCancelled(AppointmentModel appointment, int position) {
        // Typically employees don't cancel appointments, but you could implement
        // a status change here if needed
    }

    @Override
    public void onAppointmentRescheduled(AppointmentModel appointment, int position) {
        // Update the adapter
        adapter.updateItem(appointment, position);
    }

    @Override
    public void onRescheduleRequested(AppointmentModel appointment, int position) {

    }

    // New method to update appointment status (for employee actions)
    public void updateAppointmentStatus(AppointmentModel appointment, String newStatus, int position) {
        String url = "http://coms-3090-020.class.las.iastate.edu:8080/api/appointments/"
                + appointment.getId() + "/status";

        try {
            JSONObject statusUpdate = new JSONObject();
            statusUpdate.put("status", newStatus);

            JsonArrayRequest updateRequest = new JsonArrayRequest(
                    Request.Method.PUT,
                    url,
                    null,
                    response -> {
                        // Update successful
                        appointment.setStatus(newStatus);
                        adapter.updateItem(appointment, position);
                        Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating status: " + error.toString());
                    }
            ) {
                @Override
                public byte[] getBody() {
                    return statusUpdate.toString().getBytes();
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(updateRequest);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating status update request: " + e.getMessage());
        }
    }
}