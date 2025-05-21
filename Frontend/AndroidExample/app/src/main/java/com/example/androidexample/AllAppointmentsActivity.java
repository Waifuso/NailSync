package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// import com.android.volley.Request;
// import com.android.volley.toolbox.JsonArrayRequest;
// import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidUI.AppointmentAdapter;
import com.example.androidUI.AppointmentDetailsDialog;
import com.example.androidUI.AppointmentModel;

// import org.json.JSONArray;
// import org.json.JSONException;
// import org.json.JSONObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;



public class AllAppointmentsActivity extends AppCompatActivity
        implements AppointmentDetailsDialog.AppointmentActionListener {

    private static final int REQUEST_CODE_RESCHEDULE = 102;

    // UI Components
    private RecyclerView recyclerViewAppointments;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private AppointmentAdapter adapter;

    // Data
    private List<AppointmentModel> appointmentsList = new ArrayList<>();
    private long userID = -1;

    private long employeeId = -1;
    private CardView rewardBannerCard;
    private TextView tvRewardTitle;
    private String rewardTitle;
    private static final int REQUEST_PAYMENT_CODE = 103;
    private static final int REQUEST_REVIEW_CODE = 104;

    // Service names mapping (for 1-7 based service IDs)
    private static final String[] SERVICE_NAMES = {
            "", // Empty element at index 0 (not used)
            "Polish Change",
            "Gel Builder",
            "Solar Acrylic",
            "Manicure",
            "Pedicure",
            "Dipping Powder",
            "Extras"
    };

    // Tag for logging
    private static final String TAG = "AllAppointmentsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_appointments);

        long userID = getIntent().getLongExtra("userID", -1);
        String username = getIntent().getStringExtra("username");
        int userPoints = getIntent().getIntExtra("userPoints", -1);

        try
        {
            // Get reward title if available
            rewardTitle = getIntent().getStringExtra("rewardTitle");
            // Initialize reward banner views
            rewardBannerCard = findViewById(R.id.rewardBannerCard);
            tvRewardTitle = findViewById(R.id.tvRewardTitle);

            Log.d("AllAppointmentsActivity", "Received reward title: " + rewardTitle);
        }
        catch (Exception e)
        // Handle the case where rewardTitle is not available
        {
            Log.d("AllAppointmentsActivity", "No reward title available");
        }


        // Initialize views
        initializeViews();

        // Extract user ID from intent
        extractIntentData();

        // Set up RecyclerView
        setupRecyclerView();

        setupRewardBanner();

        // Fetch appointments data
        fetchAppointments();
    }


    /**
     * Set up the reward banner visibility based on whether user has an active reward
     */
    private void setupRewardBanner() {
        if (rewardTitle != null && !rewardTitle.isEmpty()) {
            // User has a reward, show the banner
            rewardBannerCard.setVisibility(View.VISIBLE);
            tvRewardTitle.setText(rewardTitle);

            // Log for debugging
            Log.d("AllAppointmentsActivity", "Showing reward banner with title: " + rewardTitle);
        } else {
            // No reward, hide the banner
            rewardBannerCard.setVisibility(View.GONE);
            Log.d("AllAppointmentsActivity", "No reward found, hiding banner");
        }
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewAppointments = findViewById(R.id.recyclerViewAppointments);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void extractIntentData() {
        // Extract user ID (or use a default/hardcoded value for testing)
        userID = getIntent().getLongExtra("userID", -1); // Default to -1 for testing
    }

    private void setupRecyclerView() {
        adapter = new AppointmentAdapter(
                this,
                appointmentsList,
                this::onViewDetailsClicked,
                this::onCancelAppointmentClicked,
                this::handlePaymentRequest,
                this::handleReviewRequest); // Add the review callback

        recyclerViewAppointments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAppointments.setAdapter(adapter);
    }

    private void handlePaymentRequest(AppointmentModel appointment, int position, String paymentMethod) {
        // Instead of making the POST request here, we'll pass the data to CompletePaymentActivity

        // Create intent for CompletePaymentActivity
        Intent intent = new Intent(this, CompletePaymentActivity.class);

        // Pass the appointment object
        intent.putExtra("appointment", appointment);

        // Pass payment method
        intent.putExtra("paymentMethod", paymentMethod);

        // Pass appointment position for updating status later
        intent.putExtra("position", position);

        // Pass user ID
        intent.putExtra("userId", userID);

        // Pass employee ID (in a real app, you'd get this from the appointment)
        long employeeId = 1; // Default value, you may need to adjust this
        intent.putExtra("employeeId", employeeId);

        // Extract service IDs from the appointment's service names
        try {
            List<String> serviceNames = appointment.getServiceNames();
            int[] serviceIds = new int[serviceNames.size()];

            for (int i = 0; i < serviceNames.size(); i++) {
                String serviceName = serviceNames.get(i);
                serviceIds[i] = getServiceIdFromName(serviceName);
            }

            // Pass service IDs array
            intent.putExtra("serviceIds", serviceIds);
        } catch (Exception e) {
            Log.e(TAG, "Error extracting service IDs: " + e.getMessage());
            // Default to basic services
            intent.putExtra("serviceIds", new int[]{1, 2});
        }

        // Pass service names as ArrayList
        ArrayList<String> serviceNames = new ArrayList<>(appointment.getServiceNames());
        intent.putExtra("serviceNames", serviceNames);

        // Start the activity
        startActivityForResult(intent, REQUEST_PAYMENT_CODE);
    }
    private void handleReviewRequest(AppointmentModel appointment, int position) {
        Log.d(TAG, "Review requested for appointment: " + appointment.getId());

        // Check if the appointment status is "Paid" to determine payment status
        boolean isPaid = "Paid".equalsIgnoreCase(appointment.getStatus());

        // Create intent for ReviewActivity
        Intent intent = new Intent(this, ReviewActivity.class);

        // Pass appointment details
        intent.putExtra("appointmentId", appointment.getId());
        intent.putExtra("userId", userID);

        // Find employee ID based on employee name (in a real app, you'd have a proper mapping)
        // For now, use a default value

        intent.putExtra("employeeId", employeeId);

        // Pass employee name and appointment details
        intent.putExtra("technicianName", appointment.getEmployeeName());
        intent.putExtra("appointmentDate", appointment.getDate());
        intent.putExtra("appointmentTime", appointment.getTime());
        intent.putExtra("duration", appointment.getDuration());

        // Pass payment status
        intent.putExtra("isPaid", isPaid);

        // Extract service IDs from the appointment's service names
        try {
            List<String> serviceNames = appointment.getServiceNames();
            int[] serviceIds = new int[serviceNames.size()];

            for (int i = 0; i < serviceNames.size(); i++) {
                String serviceName = serviceNames.get(i);
                serviceIds[i] = getServiceIdFromName(serviceName);
            }

            // Pass service IDs array
            intent.putExtra("serviceIds", serviceIds);
        } catch (Exception e) {
            Log.e(TAG, "Error extracting service IDs: " + e.getMessage());
            // Default to basic services
            intent.putExtra("serviceIds", new int[]{1, 2});
        }

        // Pass service names as ArrayList
        ArrayList<String> serviceNames = new ArrayList<>(appointment.getServiceNames());
        intent.putExtra("serviceNames", serviceNames);

        // Pass position for potential updates
        intent.putExtra("position", position);

        // Start the activity
        startActivityForResult(intent, REQUEST_REVIEW_CODE);
    }
    private void fetchAppointments()
    {
        // Show loading state
        showLoading(true);

        // Get the user ID from intent
        long userID = getIntent().getLongExtra("userID", -1);

        // Create the URL with userID as parameter
        String url = "http://coms-3090-020.class.las.iastate.edu:8080/api/appointments/user/" + userID;

        // Create the request
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null, // No request body needed for GET request
                response -> {
                    Log.d(TAG, "Response: " + response.toString());

                    try {
                        // Clear existing list
                        appointmentsList.clear();

                        // Process the JSON array response
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject appointmentWrapper = response.getJSONObject(i);

                            // Extract the nested appointment object
                            JSONObject appointmentObj = appointmentWrapper.getJSONObject("appointment");

                            // Get employee name from the wrapper
                            String employeeName = appointmentWrapper.getString("employeeName");

                            // Get service IDs from the wrapper
                            JSONArray serviceIdArray = appointmentWrapper.getJSONArray("serviceId");
                            List<String> serviceNames = new ArrayList<>();
                            for (int j = 0; j < serviceIdArray.length(); j++) {
                                int serviceId = serviceIdArray.getInt(j);
                                // Convert service ID to actual service name
                                serviceNames.add(getServiceName(serviceId));
                            }

                            // Create a new appointment model
                            AppointmentModel appointment = new AppointmentModel();

                            // Parse basic details
                            appointment.setId(appointmentObj.getLong("id"));

                            // Get date and times
                            appointment.setDate(appointmentObj.getString("date"));

                            // Extract start and end time
                            String startTime = appointmentObj.getString("startTime");
                            String endTime = appointmentObj.getString("endTime");

                            // Set the time (just using start time)
                            appointment.setTime(startTime.substring(0, 5)); // Format from "09:46:00" to "09:46"

                            // Calculate duration in minutes
                            int durationMinutes = calculateDurationInMinutes(startTime, endTime);
                            appointment.setDuration(durationMinutes);

                            // Set status
                            appointment.setStatus(appointmentObj.getString("status"));

                            // Set confirmation number as notes
                            int confirmationNumber = appointmentObj.getInt("confirmationNumber");
                            appointment.setNotes("Confirmation #: " + confirmationNumber);

                            // Set employee name from wrapper
                            appointment.setEmployeeName(employeeName);

                            // Set service names from the service ID array
                            appointment.setServiceNames(serviceNames);

                            // Add to the list
                            appointmentsList.add(appointment);
                        }

                        // Update UI based on response
                        showLoading(false);
                        if (appointmentsList.isEmpty()) {
                            showEmptyState("No appointments found");
                        } else {
                            adapter.notifyDataSetChanged();
                            showContent();
                        }

                        Log.d(TAG, "Successfully loaded " + appointmentsList.size() + " appointments");

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);

                        // Fallback to sample data
                        Toast.makeText(AllAppointmentsActivity.this,
                                "Error parsing appointment data. Using sample data.",
                                Toast.LENGTH_SHORT).show();

                        fallbackToSampleData();
                    }
                },
                error -> {
                    Log.e(TAG, "Network error: " + error.toString(), error);

                    String errorMessage = "Failed to load appointments";
                    if (error.networkResponse != null) {
                        errorMessage += " (Error " + error.networkResponse.statusCode + ")";
                    }

                    // Show error toast
                    Toast.makeText(AllAppointmentsActivity.this,
                            errorMessage + ". Using sample data instead.",
                            Toast.LENGTH_SHORT).show();

                    // Fallback to sample data
                    // only for testing - jacob
                    //fallbackToSampleData();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Add authorization headers if needed
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

    // Helper method to get service name from service ID
    private String getServiceName(int serviceId) {
        if (serviceId >= 1 && serviceId < SERVICE_NAMES.length) {
            return SERVICE_NAMES[serviceId];
        } else {
            return "Unknown Service";
        }
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
        appointmentsList.addAll(generateSimulatedAppointments());

        showLoading(false);
        if (appointmentsList.isEmpty()) {
            showEmptyState("No appointments found");
        } else {
            adapter.notifyDataSetChanged();
            showContent();
        }

        Log.d(TAG, "Using simulated appointment data");
    }

    private List<AppointmentModel> generateSimulatedAppointments() {
        List<AppointmentModel> simulatedAppointments = new ArrayList<>();
        Random random = new Random();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Properly named services using SERVICE_NAMES array
        List<String> services = Arrays.asList(
                SERVICE_NAMES[1], SERVICE_NAMES[2], SERVICE_NAMES[3],
                SERVICE_NAMES[4], SERVICE_NAMES[5], SERVICE_NAMES[6], SERVICE_NAMES[7]
        );

        // Possible employee names
        List<String> employees = Arrays.asList(
                "Emma Johnson", "Michael Rodriguez",
                "Sophia Lee", "David Kim", "Olivia Martinez"
        );

        // Generate 3-6 appointments
        int appointmentCount = random.nextInt(4) + 3;

        for (int i = 0; i < appointmentCount; i++) {
            AppointmentModel appointment = new AppointmentModel();

            // Set ID
            appointment.setId(System.currentTimeMillis() + i);

            // Set employee
            String employeeName = employees.get(random.nextInt(employees.size()));
            appointment.setEmployeeName(employeeName);

            // Set date (next few days)
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, random.nextInt(7));
            appointment.setDate(dateFormat.format(calendar.getTime()));

            // Set time
            calendar.set(Calendar.HOUR_OF_DAY, 9 + random.nextInt(8)); // 9 AM to 5 PM
            calendar.set(Calendar.MINUTE, random.nextInt(2) * 30); // On the hour or half hour
            appointment.setTime(timeFormat.format(calendar.getTime()));

            // Set duration
            int[] durations = {30, 45, 60, 90};
            appointment.setDuration(durations[random.nextInt(durations.length)]);

            // Set services
            int serviceCount = random.nextInt(2) + 1;
            List<String> appointmentServices = new ArrayList<>();
            for (int j = 0; j < serviceCount; j++) {
                String service = services.get(random.nextInt(services.size()));
                if (!appointmentServices.contains(service)) {
                    appointmentServices.add(service);
                }
            }
            appointment.setServiceNames(appointmentServices);

            // Set status
            String[] statuses = {"Confirmed", "Upcoming", "Scheduled"};
            appointment.setStatus(statuses[random.nextInt(statuses.length)]);

            // Optional: Add some notes
            if (random.nextBoolean()) {
                appointment.setNotes("Additional notes for the appointment");
            }

            simulatedAppointments.add(appointment);
        }

        return simulatedAppointments;
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

        // Create and show appointment details dialog
        AppointmentDetailsDialog dialog = new AppointmentDetailsDialog(appointment);
        dialog.setActionListener(this);
        dialog.setAdapterPosition(position);
        dialog.show(getSupportFragmentManager(), "AppointmentDetails");
    }

    private void onCancelAppointmentClicked(AppointmentModel appointment, int position) {
        // First update the UI to show "Cancelling..." status
        appointment.setStatus("Cancelling...");
        adapter.notifyItemChanged(position);

        // Then show the confirmation dialog
        showCancelConfirmationDialog(appointment, position);
    }

    private void showCancelConfirmationDialog(AppointmentModel appointment, int position) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Cancel Appointment");
        builder.setMessage("Are you sure you want to cancel this appointment?");
        builder.setPositiveButton("Cancel Appointment", (dialog, which) -> {
            cancelAppointment(appointment, position);
        });
        builder.setNegativeButton("Keep Appointment", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void cancelAppointment(AppointmentModel appointment, int position) {
        // Show loading indicator
        showLoading(true);

        // Create the URL with appointment ID as parameter
        String url = "http://coms-3090-020.class.las.iastate.edu:8080/api/appointments/cancel/" + appointment.getId();

        Log.d(TAG, "Cancel appointment URL: " + url);

        // Create a JsonObjectRequest for DELETE method
        JsonObjectRequest deleteRequest = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null, // No body for DELETE request
                response -> {
                    try {
                        Log.d(TAG, "Response: " + response.toString());
                        // Parse response message
                        String message = response.getString("message");

                        // Show success message (using the actual message from the server)
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                        // Hide loading indicator before removing the item (so we don't hide the progress bar
                        // that's associated with the whole screen right before animation)
                        showLoading(false);

                        // Remove the appointment from the list and update UI with animation
                        removeAppointmentFromUI(position);
                    }
                    catch (JSONException e) {
                        Log.e(TAG, "Error parsing response JSON: " + e.getMessage());
                        Toast.makeText(this, "Appointment cancelled successfully",
                                Toast.LENGTH_SHORT).show();

                        // Hide loading indicator
                        showLoading(false);

                        // Even if we can't parse the response, the DELETE was successful
                        // so remove the appointment from the UI with animation
                        removeAppointmentFromUI(position);
                    }
                },
                error -> {
                    // Handle error
                    Log.e(TAG, "Error cancelling appointment: " + error.toString());

                    String errorMessage = "Failed to cancel appointment";
                    if (error.networkResponse != null) {
                        errorMessage += " (Error " + error.networkResponse.statusCode + ")";
                    }

                    // Show error message
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();

                    // Reset the appointment status since cancellation failed
                    appointment.setStatus("Confirmed"); // Or whatever the original status was
                    adapter.notifyItemChanged(position);

                    // Hide loading indicator
                    showLoading(false);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Add any necessary headers here
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Set timeout for the request
        deleteRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(deleteRequest);
    }

    // Helper method to ensure UI updates consistently
    private void removeAppointmentFromUI(int position) {
        // Remove from data source
        if (position >= 0 && position < appointmentsList.size()) {
            // Get the view for the position we're about to remove
            RecyclerView.ViewHolder viewHolder = recyclerViewAppointments.findViewHolderForAdapterPosition(position);

            if (viewHolder != null && viewHolder.itemView != null) {
                // Apply a fade out animation
                viewHolder.itemView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .setListener(new android.animation.AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(android.animation.Animator animation) {
                                // After the animation ends, remove the item and update the adapter
                                appointmentsList.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, appointmentsList.size());

                                // Check if list is now empty
                                if (appointmentsList.isEmpty()) {
                                    showEmptyState("No appointments found");
                                }
                            }
                        });
            } else {
                // If view holder isn't available, just remove it directly
                appointmentsList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, appointmentsList.size());

                // Check if list is now empty
                if (appointmentsList.isEmpty()) {
                    showEmptyState("No appointments found");
                }
            }
        }
    }
    @Override
    public void onRescheduleRequested(AppointmentModel appointment, int position) {
        // Launch the RescheduleActivity
        Intent intent = new Intent(this, RescheduleAppointmentActivity.class);

        // Extract service IDs from the appointment's service names
        int[] serviceIds;
        try {
            List<String> serviceNames = appointment.getServiceNames();
            serviceIds = new int[serviceNames.size()];

            for (int i = 0; i < serviceNames.size(); i++) {
                String serviceName = serviceNames.get(i);
                serviceIds[i] = getServiceIdFromName(serviceName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting service IDs: " + e.getMessage());
            // Default to some basic services
            serviceIds = new int[]{1, 2};
        }

        // Find employee ID - in a real app, you would have a proper mapping
        // For now, we'll use a default ID
        long employeeId = 1;

        // Pass all required data to the activity
        intent.putExtra("APPOINTMENT_ID", appointment.getId());
        intent.putExtra("USER_ID", userID);
        intent.putExtra("EMPLOYEE_ID", employeeId);
        intent.putExtra("TECHNICIAN_NAME", appointment.getEmployeeName());
        intent.putExtra("APPOINTMENT_DATE", appointment.getDate());
        intent.putExtra("APPOINTMENT_TIME", appointment.getTime());
        intent.putExtra("SERVICE_IDS", serviceIds);

        // Store position for result handling
        intent.putExtra("POSITION", position);

        // Start the activity for result
        startActivityForResult(intent, REQUEST_CODE_RESCHEDULE);
    }

    // Helper method to get service ID from name
    private int getServiceIdFromName(String serviceName) {
        for (int i = 1; i < SERVICE_NAMES.length; i++) {
            if (SERVICE_NAMES[i].equals(serviceName)) {
                return i;
            }
        }

        // If it's still in the "Service #X" format, extract the number
        if (serviceName.contains("#")) {
            try {
                String idPart = serviceName.substring(serviceName.indexOf("#") + 1);
                return Integer.parseInt(idPart.trim());
            } catch (NumberFormatException e) {
                return 1; // Default to first service if parsing fails
            }
        }

        return 1; // Default to first service
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_RESCHEDULE && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("RESCHEDULED", false)) {
                // Get the new date and time
                String newDate = data.getStringExtra("NEW_DATE");
                String newTime = data.getStringExtra("NEW_TIME");
                int position = data.getIntExtra("POSITION", -1);

                if (position >= 0 && position < appointmentsList.size()) {
                    // Update the appointment in our list
                    AppointmentModel appointment = appointmentsList.get(position);

                    // Save the original time for logging
                    String oldDate = appointment.getDate();
                    String oldTime = appointment.getTime();

                    // Update the appointment with new date and time
                    appointment.setDate(newDate);
                    appointment.setTime(newTime);

                    Log.d(TAG, "Appointment rescheduled - From: " + oldDate + " " + oldTime
                            + " To: " + newDate + " " + newTime);

                    // Update the adapter to refresh the UI
                    adapter.notifyItemChanged(position);

                    // Show success message
                    Toast.makeText(this, "Appointment successfully rescheduled!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        // Handle payment result
        else if (requestCode == REQUEST_PAYMENT_CODE && resultCode == RESULT_OK) {
            // Check if payment was successful
            if (data != null && data.getBooleanExtra("PAYMENT_SUCCESSFUL", false)) {
                // Get position of the appointment
                int position = data.getIntExtra("POSITION", -1);

                // Update appointment status if position is valid
                if (position >= 0 && position < appointmentsList.size()) {
                    AppointmentModel appointment = appointmentsList.get(position);
                    appointment.setStatus("Paid");

                    // Update the adapter
                    adapter.updateItem(appointment, position);

                    // Show success message
                    Toast.makeText(this, "Payment completed successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }
        // Handle review request
        else if (requestCode == REQUEST_REVIEW_CODE && resultCode == RESULT_OK) {
            // Check if review was submitted successfully
            if (data != null && data.getBooleanExtra("REVIEW_SUBMITTED", false)) {
                long appointmentId = data.getLongExtra("APPOINTMENT_ID", -1);

                // You could update the appointment model to mark it as reviewed if needed
                // For now, just show a success message
                Toast.makeText(this, "Review submitted successfully!", Toast.LENGTH_SHORT).show();

                Log.d(TAG, "Review submitted for appointment ID: " + appointmentId);
            }
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Implementation of AppointmentActionListener interface
    @Override
    public void onAppointmentCancelled(AppointmentModel appointment, int position) {
        // Update the adapter
        removeAppointmentFromUI(position);

    }

    @Override
    public void onAppointmentRescheduled(AppointmentModel appointment, int position) {
        // Update the adapter
        adapter.updateItem(appointment, position);
    }
}