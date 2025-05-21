package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmployeeHomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "EmployeeHomeActivity";

    // UI Components
    private TextView employeeNameText, employeeRoleText;
    private ImageView employeeProfileImage;
    private TextView appointmentsCountText, dailyRevenueText, clientsServedText;
    private RecyclerView upcomingAppointmentsRecyclerView;
    private LinearLayout manageAppointmentsAction, clientListAction, notesAction, reportAction;
    private BottomNavigationView bottomNavigationView;
    private MaterialButton viewAllAppointmentsButton;
    private Toolbar toolbar;

    // Employee Data
    private long userID;
    private String username;
    private String email;
    private String role;
    private String profileImageUrl;
    private Employee employeeData; // Employee model to store complete data

    // Upcoming Appointments Adapter
    private UpcomingAppointmentsAdapter appointmentsAdapter;
    private List<AppointmentItem> appointmentsList;

    // Constants
    private static final String EMPLOYEE_DASHBOARD_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/employee/dashboard/";
    private static final String EMPLOYEE_DETAILS_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/employee/";
    private static final String EMPLOYEE_APPOINTMENTS_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/employee/appointments/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_home);
        EdgeToEdge.enable(this);

        // Initialize UI components
        initializeUIComponents();

        // Setup toolbar
        setupToolbar();

        // Get employee data from intent
        getIntentData();

        // Fetch employee details first
        fetchEmployeeDetails();

        // Setup quick action listeners
        setupQuickActionListeners();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void setupToolbar()
    {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void initializeUIComponents() {
        // Profile Section
        employeeNameText = findViewById(R.id.employeeNameText);
        employeeRoleText = findViewById(R.id.employeeRoleText);
        employeeProfileImage = findViewById(R.id.employeeProfileImage);

        // Quick Stats
        appointmentsCountText = findViewById(R.id.appointmentsCountText);
        dailyRevenueText = findViewById(R.id.dailyRevenueText);
        clientsServedText = findViewById(R.id.clientsServedText);

        // Upcoming Appointments
        upcomingAppointmentsRecyclerView = findViewById(R.id.upcomingAppointmentsRecyclerView);
        upcomingAppointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        viewAllAppointmentsButton = findViewById(R.id.viewAllAppointmentsButton);

        // Quick Action Buttons
        manageAppointmentsAction = findViewById(R.id.manageAppointmentsAction);
        clientListAction = findViewById(R.id.clientListAction);
        notesAction = findViewById(R.id.notesAction);
        reportAction = findViewById(R.id.reportAction);

        // Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Initialize appointments list and adapter
        appointmentsList = new ArrayList<>();
        appointmentsAdapter = new UpcomingAppointmentsAdapter(appointmentsList);
        upcomingAppointmentsRecyclerView.setAdapter(appointmentsAdapter);
    }

    private void getIntentData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userID = extras.getLong("userID", -1);
            username = extras.getString("username", "");
            email = extras.getString("email", "");
            role = extras.getString("role", "Nail Technician");

            // Set initial employee info if available
            if (username != null && !username.isEmpty()) {
                employeeNameText.setText(username);
            }

            if (role != null && !role.isEmpty()) {
                employeeRoleText.setText(role);
            }

            // Log received data
            Log.d(TAG, "From Intent - id: " + userID);
            Log.d(TAG, "From Intent - Username: " + username);
            Log.d(TAG, "From Intent - Email: " + email);
            Log.d(TAG, "From Intent - Role: " + role);
        }
        else {
            // Try to get data from stored preferences if not in intent
            PrefManager prefManager = PrefManager.getInstance(this);
            userID = prefManager.getUserId();
            username = prefManager.getUsername();
            email = prefManager.getEmail();

            // Set initial employee info if available
            if (username != null) {
                employeeNameText.setText(username);
            }

            Log.d(TAG, "Using data from preferences - id: " + userID);
        }
    }

    /**
     * Fetches employee details from the server using the employee ID
     */
    private void fetchEmployeeDetails() {
        // Check if we have a valid userID
        if (userID <= 0) {
            Log.e(TAG, "Invalid userID: " + userID);
//            Toast.makeText(this, "Invalid employee ID", Toast.LENGTH_SHORT).show();

            // Try to fetch dashboard data anyway
            fetchEmployeeDashboardData();

            // Also fetch upcoming appointments directly
            fetchUpcomingAppointments();
            return;
        }

        String url = EMPLOYEE_DETAILS_URL + userID;
        Log.d(TAG, "Fetching employee details from: " + url);

        JsonObjectRequest employeeRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Parse employee data from response
                            employeeData = parseEmployeeData(response);

                            // Update UI with employee data
                            updateEmployeeUI();

                            // After loading employee data, fetch dashboard data
                            fetchEmployeeDashboardData();

                            // Also fetch upcoming appointments directly
                            fetchUpcomingAppointments();

                            Log.d(TAG, "Employee data loaded successfully");
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing employee data", e);
//                            Toast.makeText(EmployeeHomeActivity.this,
//                                    "Failed to process employee data",
//                                    Toast.LENGTH_SHORT).show();

                            // Fetch dashboard data anyway if employee parsing fails
                            fetchEmployeeDashboardData();

                            // Also fetch upcoming appointments directly
                            fetchUpcomingAppointments();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching employee data", error);

                        // Get error details
                        String errorMessage = "Failed to load employee data";
                        int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : 0;

                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                errorMessage = data.optString("message", errorMessage);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error response", e);
                            }
                        }

//                        Toast.makeText(EmployeeHomeActivity.this,
//                                errorMessage + (statusCode > 0 ? " (" + statusCode + ")" : ""),
//                                Toast.LENGTH_SHORT).show();

                        // Fetch dashboard data anyway if employee fetch fails
                        fetchEmployeeDashboardData();

                        // Also fetch upcoming appointments directly
                        fetchUpcomingAppointments();
                    }
                }
        ) {
            // Optional: Add headers if needed
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Add any headers like authorization tokens if required
                // headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        // Optional: Set request timeout
        employeeRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000, // 15 seconds timeout
                1, // Max retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(employeeRequest);
    }

    /**
     * Parse employee data from JSON response
     */
    private Employee parseEmployeeData(JSONObject response) throws JSONException
    {
        // Create a new Employee object
        Employee employee = new Employee();

        // Parse basic information
        employee.setId(response.getInt("id"));
        employee.setUsername(response.getString("username"));
        employee.setEmail(response.getString("email"));
        employee.setFullName(response.optString("fullName", username != null ? username : ""));
        employee.setRole(response.optString("role", "Nail Technician"));
        employee.setPhoneNumber(response.optString("phoneNumber", ""));
        employee.setProfileImageUrl(response.optString("profileImageUrl", ""));

        return employee;
    }

    /**
     * Update UI with employee data
     */
    private void updateEmployeeUI() {
        if (employeeData == null) return;

        // Update profile section
        if (employeeData.getFullName() != null && !employeeData.getFullName().isEmpty()) {
            employeeNameText.setText(employeeData.getFullName());
        } else {
            employeeNameText.setText(employeeData.getUsername());
        }

        employeeRoleText.setText(employeeData.getRole());

        // Update class variables
        username = employeeData.getUsername();
        email = employeeData.getEmail();
        role = employeeData.getRole();
        profileImageUrl = employeeData.getProfileImageUrl();

        // Load profile image if available
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile))
                    .into(employeeProfileImage);
        }

        // Store employee data in shared preferences
        PrefManager prefManager = PrefManager.getInstance(this);
        prefManager.setUserId(employeeData.getId());
        prefManager.setUsername(employeeData.getUsername());
        prefManager.setEmail(employeeData.getEmail());
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) {
            Log.e(TAG, "Cannot setup bottom navigation - view is null");
            return;
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Set home as selected since we're on the home page
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Make sure it's visible
        bottomNavigationView.setVisibility(View.VISIBLE);

        Log.d(TAG, "Bottom navigation setup complete with home selected");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            // Already on home, do nothing or refresh
            return true;
        }
        else if (itemId == R.id.nav_appointments) {
//            Intent intent = new Intent(EmployeeHomeActivity.this, EmployeeAppointmentsActivity.class);
//            startActivity(intent);
            return true;
        }
        else if (itemId == R.id.nav_timeline) {
            // Navigate to timeline
            Intent intent = new Intent(EmployeeHomeActivity.this, TimelineActivity.class);
            intent.putExtra("userID", userID);
            intent.putExtra("username", username);
            intent.putExtra("email", email);
            startActivity(intent);
            return true;
        }
        else if (itemId == R.id.nav_profile) {
            // Navigate to profile
            Intent profileIntent = new Intent(this, EmployeeViewProfileActivity.class);
            profileIntent.putExtra("id", userID);
            profileIntent.putExtra("username", username);
            profileIntent.putExtra("email", email);
            profileIntent.putExtra("role", role);
            startActivity(profileIntent);
            return true;
        }
        return false;
    }

    private void setupQuickActionListeners() {
        manageAppointmentsAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(EmployeeHomeActivity.this, EmployeeAppointmentsActivity.class);
//                intent.putExtra("id", userID);
//                startActivity(intent);
            }
        });

        clientListAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement client list activity
                Toast.makeText(EmployeeHomeActivity.this, "Client List Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        notesAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement inventory management activity
                Intent intent = new Intent(EmployeeHomeActivity.this, EmployeeNotesActivity.class);
                intent.putExtra("employeeID", userID);
                startActivity(intent);

            }
        });

        reportAction.setOnClickListener(v -> {
                EmployeeFinanceDialog dialog = new EmployeeFinanceDialog(this, userID);
                dialog.show();
        });

        viewAllAppointmentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeHomeActivity.this, ViewAllAppointmentsActivity.class);
                intent.putExtra("employeeID", userID);
                startActivity(intent);
            }
        });
    }

    private void fetchEmployeeDashboardData() {
        if (userID <= 0) {
            Log.e(TAG, "Invalid employeeId for dashboard data: " + userID);
            return;
        }

        String url = EMPLOYEE_DASHBOARD_URL + userID;
        Log.d(TAG, "Fetching dashboard data from: " + url);

        JsonObjectRequest dashboardRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Update profile information
                            updateProfileInfo(response);

                            // Update quick stats
                            updateQuickStats(response);

                            Log.d(TAG, "Dashboard data loaded successfully");
                        } catch (JSONException e) {
                            Log.e(TAG, "Error processing dashboard data", e);
//                            Toast.makeText(EmployeeHomeActivity.this,
//                                    "Error processing dashboard data",
//                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching dashboard data", error);
//                        Toast.makeText(EmployeeHomeActivity.this,
//                                "Failed to load dashboard data",
//                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(dashboardRequest);
    }

    /**
     * Fetch upcoming appointments directly from appointments endpoint
     */
    private void fetchUpcomingAppointments() {
        if (userID <= 0) {
            Log.e(TAG, "Invalid userID for appointments: " + userID);
            return;
        }

        // URL with query parameters to get only upcoming appointments
        String url = EMPLOYEE_APPOINTMENTS_URL + userID + "?status=confirmed&range=week&limit=5";
        Log.d(TAG, "Fetching upcoming appointments from: " + url);

        JsonObjectRequest appointmentsRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Update upcoming appointments
                            updateUpcomingAppointmentsFromDirectCall(response);
                            Log.d(TAG, "Upcoming appointments loaded successfully");
                        } catch (JSONException e) {
                            Log.e(TAG, "Error processing appointments data", e);
//                            Toast.makeText(EmployeeHomeActivity.this,
//                                    "Error loading upcoming appointments",
//                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching upcoming appointments", error);
//                        Toast.makeText(EmployeeHomeActivity.this,
//                                "Failed to load upcoming appointments",
//                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(appointmentsRequest);
    }

    private void updateProfileInfo(JSONObject response) throws JSONException {
        // Update employee name
        String fullName = response.optString("fullName", username);
        employeeNameText.setText(fullName);

        // Update role
        role = response.optString("role", "Nail Technician");
        employeeRoleText.setText(role);

        // Update profile image
        profileImageUrl = response.optString("profileImageUrl", null);
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile))
                    .into(employeeProfileImage);
        }
    }

    private void updateQuickStats(JSONObject response) throws JSONException {
        // Update appointments count
        int appointmentsCount = response.optInt("todayAppointmentsCount", 0);
        appointmentsCountText.setText(String.valueOf(appointmentsCount));

        // Update daily revenue
        double dailyRevenue = response.optDouble("dailyRevenue", 0.0);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        dailyRevenueText.setText(currencyFormat.format(dailyRevenue));

        // Update clients served
        int clientsServed = response.optInt("clientsServedToday", 0);
        clientsServedText.setText(String.valueOf(clientsServed));
    }

    /**
     * Update upcoming appointments from direct appointments endpoint call
     */
    private void updateUpcomingAppointmentsFromDirectCall(JSONObject response) throws JSONException {
        // Clear existing appointments
        appointmentsList.clear();

        // Parse upcoming appointments - note different structure from the appointments endpoint
        JSONArray appointmentsArray = response.optJSONArray("appointments");
        if (appointmentsArray != null) {
            for (int i = 0; i < Math.min(appointmentsArray.length(), 5); i++) { // Only show up to 5
                JSONObject appointmentJson = appointmentsArray.getJSONObject(i);

                // Create AppointmentItem from JSON
                AppointmentItem appointment = new AppointmentItem(
                        appointmentJson.optInt("id"),
                        appointmentJson.optString("clientName"),
                        appointmentJson.optString("serviceName"),
                        appointmentJson.optString("appointmentTime", appointmentJson.optString("startTime"))
                );

                appointmentsList.add(appointment);
            }
        }

        // Notify adapter of data changes
        appointmentsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh dashboard data when returning to this screen
        fetchEmployeeDashboardData();

        // Refresh upcoming appointments
        fetchUpcomingAppointments();

        // Make sure home tab is selected
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    // Upcoming Appointments Data Model
    public static class AppointmentItem {
        private int id;
        private String clientName;
        private String serviceName;
        private String appointmentTime;

        public AppointmentItem(int id, String clientName, String serviceName, String appointmentTime) {
            this.id = id;
            this.clientName = clientName;
            this.serviceName = serviceName;
            this.appointmentTime = appointmentTime;
        }

        // Getters for the fields
        public int getId() { return id; }
        public String getClientName() { return clientName; }
        public String getServiceName() { return serviceName; }
        public String getAppointmentTime() { return appointmentTime; }
    }

    // Upcoming Appointments RecyclerView Adapter
    public static class UpcomingAppointmentsAdapter extends RecyclerView.Adapter<UpcomingAppointmentsAdapter.AppointmentViewHolder> {
        private List<AppointmentItem> appointments;

        public UpcomingAppointmentsAdapter(List<AppointmentItem> appointments) {
            this.appointments = appointments;
        }

        @NonNull
        @Override
        public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_upcoming_appointment, parent, false);
            return new AppointmentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
            AppointmentItem appointment = appointments.get(position);
            holder.clientNameText.setText(appointment.getClientName());
            holder.serviceNameText.setText(appointment.getServiceName());
            holder.appointmentTimeText.setText(appointment.getAppointmentTime());
        }

        @Override
        public int getItemCount() {
            return appointments.size();
        }

        public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
            TextView clientNameText;
            TextView serviceNameText;
            TextView appointmentTimeText;

            public AppointmentViewHolder(@NonNull View itemView)
            {
                super(itemView);
                clientNameText = itemView.findViewById(R.id.clientName);
                serviceNameText = itemView.findViewById(R.id.serviceName);
                appointmentTimeText = itemView.findViewById(R.id.appointmentTime);
            }
        }
    }



    // Employee Data Model
    public static class Employee {
        private int id;
        private String username;
        private String email;
        private String fullName;
        private String role;
        private String phoneNumber;
        private String profileImageUrl;

        // Add getters and setters for all fields
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getProfileImageUrl() { return profileImageUrl; }
        public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    }
}