package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
import com.example.androidUI.StaffScheduleAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// using the MPAndroidChart library found through google. -> https://github.com/PhilJay/MPAndroidChart/tree/master
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import androidx.core.content.ContextCompat;
import android.graphics.drawable.Drawable;
import android.widget.FrameLayout;
import android.graphics.Color;



public class AdminDashboardActivity extends AppCompatActivity {
    private static final String TAG = "AdminDashboardActivity";

    // UI Components - Profile Section
    private TextView adminNameText, adminRoleText;
    private ImageView adminProfileImage;

    // Stats Summary
    private TextView totalAppointmentsText, totalRevenueText, totalStaffText, totalCustomerCount;

    // Staff Schedule Components
    private RecyclerView staffScheduleRecyclerView;
    private MaterialButton viewFullScheduleButton, viewFullReportButton;
    private List<StaffSchedule> staffScheduleList = new ArrayList<>();
    private StaffScheduleAdapter staffScheduleAdapter;

    // Revenue Components
    private TextView currDateRevenueText, yesterdayRevenueText, revenueChangeText;

    // Quick Action Buttons
    private LinearLayout staffManagementAction, schedulingAction, inventoryAction, reportsAction;

    // Navigation
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    // Admin Data
    private long adminID;
    private String username;
    private String email;
    private String role;
    private String profileImageUrl;
    private Admin adminData; // Admin model to store complete data

    private List<EmployeeHomeActivity.Employee> employees = new ArrayList<>();


    // API URLS
    private static final String ADMIN_DETAILS_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/admin/";
    private static final String APPOINTMENTS = "http://coms-3090-020.class.las.iastate.edu:8080/api/admin/appointmentCount";
    private static final String STAFF_SCHEDULE_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/admin/allemployee/allappointment?localDate=";
    private static final String REVENUE_OVERVIEW_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/admin/finance?localDate=";
    private static final String REVENUE_WEEK_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/admin/week/revenue";
    private static final String ACTIVE_STAFF_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/admin/employee";
    private static final String EMPLOYEE_LIST_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/employee/info/name";
    private static final String NUM_CUSTOMER_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/admin/usercount";

    private static final String REPORTS_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/test/check/analyze";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        EdgeToEdge.enable(this);

        // Initialize UI components
        initializeUIComponents();

        // Setup toolbar
        setupToolbar();

        // Get admin data from intent
        getIntentData();

        // Fetch admin details
        fetchAdminDetails();

        // Setup quick action listeners
        setupQuickActionListeners();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void initializeUIComponents() {
        // Profile Section
        adminNameText = findViewById(R.id.adminNameText);
        adminRoleText = findViewById(R.id.adminRoleText);
        adminProfileImage = findViewById(R.id.adminProfileImage);

        // Stats Summary
        totalAppointmentsText = findViewById(R.id.totalAppointmentsText);
        totalRevenueText = findViewById(R.id.totalRevenueText);
        totalStaffText = findViewById(R.id.totalStaffText);
        totalCustomerCount = findViewById(R.id.totalCustomersText);

        // Staff Schedule
        staffScheduleRecyclerView = findViewById(R.id.staffScheduleRecyclerView);
        staffScheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        viewFullScheduleButton = findViewById(R.id.viewFullScheduleButton);

        // Revenue Overview
        currDateRevenueText = findViewById(R.id.todayRevenueText);
        yesterdayRevenueText = findViewById(R.id.yesterdayRevenueText);
        revenueChangeText = findViewById(R.id.revenueChangeText);
        viewFullReportButton = findViewById(R.id.viewDetailedReportButton);

        // Quick Action Buttons
        staffManagementAction = findViewById(R.id.staffManagementAction);
        schedulingAction = findViewById(R.id.schedulingAction);
        inventoryAction = findViewById(R.id.inventoryAction);
        reportsAction = findViewById(R.id.reportsAction);

        // Initialize staff schedule list and adapter
        staffScheduleRecyclerView = findViewById(R.id.staffScheduleRecyclerView);
        staffScheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        staffScheduleAdapter = new StaffScheduleAdapter(this, staffScheduleList);
        staffScheduleRecyclerView.setAdapter(staffScheduleAdapter);
    }

    private void getIntentData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            adminID = extras.getLong("adminID", -1);
            username = extras.getString("username", "");
            email = extras.getString("email", "");
            role = extras.getString("role", "Salon Administrator");

            // Set initial admin info if available
            if (username != null && !username.isEmpty()) {
                adminNameText.setText(username);
            }

            if (role != null && !role.isEmpty()) {
                adminRoleText.setText(role);
            }

            // Log received data
            Log.d(TAG, "From Intent - id: " + adminID);
            Log.d(TAG, "From Intent - Username: " + username);
            Log.d(TAG, "From Intent - Email: " + email);
            Log.d(TAG, "From Intent - Role: " + role);
        }
        else {
            // Try to get data from stored preferences if not in intent
            PrefManager prefManager = PrefManager.getInstance(this);
            adminID = prefManager.getUserId();
            username = prefManager.getUsername();
            email = prefManager.getEmail();
            role = "Salon Administrator"; // Default role for admin

            // Set initial admin info if available
            if (username != null) {
                adminNameText.setText(username);
            }

            Log.d(TAG, "Using data from preferences - id: " + adminID);
        }
    }

    /**
     * Fetches admin details from the server using the admin ID
     */
    private void fetchAdminDetails() {
        // Check if we have a valid adminID
        if (adminID <= 0) {
            Log.e(TAG, "Invalid adminID: " + adminID);
            // Toast.makeText(this, "Invalid admin ID", Toast.LENGTH_SHORT).show();

            // try to fetch dashboard data anyway
            fetchAdminDashboardData();

            // fetch staff schedule directly

            // fetch revenue overview
            fetchRevenueOverview();
            return;
        }

        String url = ADMIN_DETAILS_URL + adminID;
        Log.d(TAG, "Fetching admin details from: " + url);

        JsonObjectRequest adminRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Parse admin data from response
                            adminData = parseAdminData(response);

                            // Update UI with admin data
                            updateAdminUI();

                            // After loading admin data, fetch dashboard data
                            fetchAdminDashboardData();

                            // fetch staff schedule directly

                            // And revenue overview
                            fetchRevenueOverview();

                            Log.d(TAG, "Admin data loaded successfully");
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing admin data", e);
                            Toast.makeText(AdminDashboardActivity.this,
                                    "Failed to process admin data",
                                    Toast.LENGTH_SHORT).show();

                            // Fetch dashboard data anyway if admin parsing fails
                            fetchAdminDashboardData();

                            // fetch staff schedule directly

                            // And revenue overview
                            fetchRevenueOverview();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching admin data", error);

                        // Get error details
                        String errorMessage = "Failed to load admin data";
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

                        Toast.makeText(AdminDashboardActivity.this,
                                errorMessage + (statusCode > 0 ? " (" + statusCode + ")" : ""),
                                Toast.LENGTH_SHORT).show();

                        // Fetch dashboard data anyway if admin fetch fails
                        fetchAdminDashboardData();

                        // fetch staff schedule directly

                        // And revenue overview
                        fetchRevenueOverview();
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
        adminRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000, // 15 seconds timeout
                1, // Max retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(adminRequest);
    }

    /**
     * Parse admin data from JSON response
     */
    private Admin parseAdminData(JSONObject response) throws JSONException {
        // Create a new Admin object
        Admin admin = new Admin();

        // Parse basic information
        admin.setId(response.getInt("id"));
        admin.setUsername(response.getString("username"));
        admin.setEmail(response.getString("email"));
        admin.setFullName(response.optString("fullName", username != null ? username : ""));
        admin.setRole(response.optString("role", "Salon Administrator"));
        admin.setPhoneNumber(response.optString("phoneNumber", ""));
        admin.setProfileImageUrl(response.optString("profileImageUrl", ""));

        return admin;
    }

    /**
     * Update UI with admin data
     */
    private void updateAdminUI() {
        if (adminData == null) return;

        // Update profile section
        if (adminData.getFullName() != null && !adminData.getFullName().isEmpty()) {
            adminNameText.setText(adminData.getFullName());
        } else {
            adminNameText.setText(adminData.getUsername());
        }

        adminRoleText.setText(adminData.getRole());

        // Update class variables
        username = adminData.getUsername();
        email = adminData.getEmail();
        role = adminData.getRole();
        profileImageUrl = adminData.getProfileImageUrl();

        // Load profile image if available
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile))
                    .into(adminProfileImage);
        }

        // Store admin data in shared preferences
        PrefManager prefManager = PrefManager.getInstance(this);
        prefManager.setUserId(adminData.getId());
        prefManager.setUsername(adminData.getUsername());
        prefManager.setEmail(adminData.getEmail());
    }

    /**
     * Setup click listeners
     */
    private void setupQuickActionListeners() {
        staffManagementAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminDashboardActivity.this, "Staff Management Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        schedulingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminDashboardActivity.this, "Scheduling Feature Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        inventoryAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminDashboardActivity.this, "Inventory Management Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        reportsAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminDashboardActivity.this, "Reports Feature Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        viewFullScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminDashboardActivity.this, "Full Schedule View Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        viewFullReportButton.setOnClickListener(v -> {
            JsonObjectRequest loadSummary = new JsonObjectRequest(
                    Request.Method.GET,
                    REPORTS_URL,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // parse messeage
                            String msg = response.optString("message", "error");
                            showReportDialog(msg);

                            Log.d(TAG, "Received");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error", error);
                            Toast.makeText(AdminDashboardActivity.this,
                                    "Error",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            // add the request to the RequestQueue
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(loadSummary);
        });
    }

    /**
     * Shows a dialog with the report message
     * @param message The report message to display
     */
    private void showReportDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Weekly Report Summary");

        // Set up the message
        builder.setMessage(message);

        // Add a button to dismiss the dialog
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void fetchAdminDashboardData() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        Log.d(TAG, "Fetching today's revenue from: " + REVENUE_OVERVIEW_URL + currentDate);
        JsonObjectRequest dashboardRequest = new JsonObjectRequest(
                Request.Method.GET,
                REVENUE_OVERVIEW_URL + currentDate,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // update summary stats
                        double revenue = response.optDouble("object", 0.0);
                        String formattedRevenue;

                        // truncate the value so that its compact
                        if (revenue >= 1000) {
                            formattedRevenue = String.format(Locale.US, "$%.1fK", revenue / 1000.0);
                        } else {
                            formattedRevenue = String.format(Locale.US, "$%.2f", revenue);
                        }

                        totalRevenueText.setText(formattedRevenue);
                        Log.d(TAG, "Today's revenue is " + revenue);
                        Log.d(TAG, "Dashboard data loaded successfully");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching dashboard data", error);
                        Toast.makeText(AdminDashboardActivity.this,
                                "Failed to load dashboard data",
                                Toast.LENGTH_SHORT).show();

                        // Load sample data if there's an error
                        // loadSampleDashboardData();
                    }
                }
        );
        // add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(dashboardRequest);

        getNumAppointments();
        updateNumStaff();
        updateNumCustomers();
        updateStaffSchedule();
    }

    /**
     * Fetch revenue overview data
     */
    private void fetchRevenueOverview() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        Log.d(TAG, "Fetching revenue overview from: " + REVENUE_OVERVIEW_URL + currentDate);

        JsonObjectRequest revenueRequest = new JsonObjectRequest(
                Request.Method.GET,
                REVENUE_OVERVIEW_URL + currentDate,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // update revenue overview
                            updateRevenueOverview(response, dateFormat);
                            Log.d(TAG, "Revenue overview loaded successfully");
                        } catch (JSONException e) {
                            Log.e(TAG, "Error processing revenue data", e);
                            Toast.makeText(AdminDashboardActivity.this,
                                    "Error loading revenue data",
                                    Toast.LENGTH_SHORT).show();

                            // load sample data if there's an error
                            loadSampleRevenueData();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching revenue overview", error);
                        Toast.makeText(AdminDashboardActivity.this,
                                "Failed to load revenue overview",
                                Toast.LENGTH_SHORT).show();

                        // load sample data if there's an error
                        loadSampleRevenueData();
                    }
                }
        );

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(revenueRequest);

        fetchRevenueWeek();
    }

    private void fetchRevenueWeek() {
        JsonObjectRequest revenueRequest = new JsonObjectRequest(
                Request.Method.GET,
                REVENUE_WEEK_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        setupRevenueChart(response.toString()); // update revenue graph for week
                        Log.d(TAG, "Revenue loaded successfully");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching revenue overview", error);
                        Toast.makeText(AdminDashboardActivity.this,
                                "Failed to load revenue overview",
                                Toast.LENGTH_SHORT).show();

                        // load sample data if there's an error
                        loadSampleRevenueData();
                    }
                }
        );
        // add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(revenueRequest);
    }

    private void updateRevenueOverview(JSONObject response, SimpleDateFormat date) throws JSONException {
        // extract current date revenue
        double currDateRevenue = response.optDouble("object", 0.0);

        // format and display current date revenue
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        currDateRevenueText.setText(currencyFormat.format(currDateRevenue));

        // get yesterday's revenue and update the UI after it's available
        getYesterdayRevenue(date, currDateRevenue);
    }

    private void getYesterdayRevenue(SimpleDateFormat dateFormat, final double currDateRevenue) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String yesterday = dateFormat.format(calendar.getTime());
        JsonObjectRequest yesterdayRevenueRequest = new JsonObjectRequest(
                Request.Method.GET,
                REVENUE_OVERVIEW_URL + yesterday,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        double yesterdayRevenue = response.optDouble("object", 0.0);
                        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
                        yesterdayRevenueText.setText(currencyFormat.format(yesterdayRevenue));
                        Log.d(TAG, "Yesterday's revenue was " + yesterdayRevenue);

                        // now that we have yesterday's revenue, calculate and display percentage change
                        updatePercentageChange(currDateRevenue, yesterdayRevenue);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching revenue overview", error);
                        Toast.makeText(AdminDashboardActivity.this,
                                "Failed to load revenue overview",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
        // add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(yesterdayRevenueRequest);
    }

    private void updatePercentageChange(double currDateRevenue, double yesterdayRevenue) {
        // calculate and display percentage change
        if (yesterdayRevenue > 0) {
            double percentChange = ((currDateRevenue - yesterdayRevenue) / yesterdayRevenue) * 100;
            String changeText = String.format("%+.1f%%", percentChange);
            revenueChangeText.setText(changeText);

            // set text color based on change
            if (percentChange >= 0) {
                revenueChangeText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                revenueChangeText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        } else {
            revenueChangeText.setText("+0%");
            revenueChangeText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void updateStaffSchedule() {
        fetchEmployees(); // GET THE LIST OF EMPLOYEES FIRST

        // check if adapter is initialized - initialize if needed
        // if this isnt done, the app will crash because its trying to append to a null element
        if (staffScheduleAdapter == null) {
            staffScheduleList = new ArrayList<>();
            staffScheduleAdapter = new StaffScheduleAdapter(this, staffScheduleList);
            staffScheduleRecyclerView.setAdapter(staffScheduleAdapter);
        }

        JsonObjectRequest staffScheduleReq = new JsonObjectRequest(
                Request.Method.GET,
                STAFF_SCHEDULE_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject scheduleObj = response.getJSONObject("object");
                            staffScheduleList.clear();

                            // iterate through all employees to ensure we include those with no schedule
                            Map<String, EmployeeHomeActivity.Employee> employeeMap = new HashMap<>();
                            for (EmployeeHomeActivity.Employee employee : employees) {
                                employeeMap.put(employee.getFullName().toLowerCase().replace(" ", ""), employee);
                            }

                            // process the schedule data from the API
                            Iterator<String> employeeKeys = scheduleObj.keys();
                            while (employeeKeys.hasNext()) {
                                String employeeKey = employeeKeys.next();
                                String displayName = employeeKey; // Default to the key

                                // try to find the proper display name from employee list
                                for (EmployeeHomeActivity.Employee emp : employees) {
                                    String normalizedName = emp.getFullName().toLowerCase().replace(" ", "");
                                    if (normalizedName.equals(employeeKey.toLowerCase())) {
                                        displayName = emp.getFullName();
                                        break;
                                    }
                                }

                                // get the time slots for this employee
                                JSONArray timeSlotsArray = scheduleObj.getJSONArray(employeeKey);
                                List<TimeSlot> timeSlots = new ArrayList<>();

                                for (int i = 0; i < timeSlotsArray.length(); i++) {
                                    JSONObject slotObj = timeSlotsArray.getJSONObject(i);
                                    int id = slotObj.getInt("id");
                                    String startTime = slotObj.getString("subStartTime");
                                    String endTime = slotObj.getString("subEndTime");
                                    boolean available = slotObj.getBoolean("available");

                                    timeSlots.add(new TimeSlot(id, startTime, endTime, available));
                                }

                                // add this employee's schedule to our list
                                staffScheduleList.add(new StaffSchedule(displayName, timeSlots));

                                // remove this employee from the map since we've processed them
                                employeeMap.remove(employeeKey.toLowerCase());
                            }

                            // add employees with no schedule
                            for (EmployeeHomeActivity.Employee emp : employeeMap.values()) {
                                staffScheduleList.add(new StaffSchedule(emp.getFullName(), new ArrayList<>()));
                            }

                            // Make sure adapter exists before updating
                            if (staffScheduleAdapter != null) {
                                staffScheduleAdapter.updateData(staffScheduleList);

                                // Show/hide RecyclerView based on data availability
                                if (staffScheduleList.isEmpty()) {
                                    staffScheduleRecyclerView.setVisibility(View.GONE);
                                } else {
                                    staffScheduleRecyclerView.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Log.e(TAG, "Staff schedule adapter is null");
                                // Initialize adapter if it's still null
                                staffScheduleAdapter = new StaffScheduleAdapter(AdminDashboardActivity.this, staffScheduleList);
                                staffScheduleRecyclerView.setAdapter(staffScheduleAdapter);
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing schedule data", e);
                            Toast.makeText(AdminDashboardActivity.this,
                                    "Error parsing schedule data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching staff schedule", error);
                        Toast.makeText(AdminDashboardActivity.this,
                                "Failed to load staff schedule",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // add timeouts to prevent hanging
        staffScheduleReq.setRetryPolicy(new DefaultRetryPolicy(
                15000, // 15 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(staffScheduleReq);
    }

    private void updateNumStaff() {
        JsonObjectRequest getStaffCount = new JsonObjectRequest(
                Request.Method.GET,
                ACTIVE_STAFF_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        long staffCount = response.optLong("object", 0);

                        totalStaffText.setText(String.valueOf(staffCount));
                        Log.d(TAG, "Staff count was " + staffCount);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching staff count", error);
                        Toast.makeText(AdminDashboardActivity.this,
                                "Failed to load staff count",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(getStaffCount);
    }

    private void updateNumCustomers() {
        JsonObjectRequest getCustomerCount = new JsonObjectRequest(
                Request.Method.GET,
                NUM_CUSTOMER_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        long customerCount = response.optLong("object", 0);

                        totalCustomerCount.setText(String.valueOf(customerCount));
                        Log.d(TAG, "Customer count was " + customerCount);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching customer count", error);
                        Toast.makeText(AdminDashboardActivity.this,
                                "Failed to load customer count",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(getCustomerCount);
    }

    private void getNumAppointments() {
        JsonObjectRequest apptReq = new JsonObjectRequest(
                Request.Method.GET,
                APPOINTMENTS,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // update total appointments for today
                        long todayAppointments = response.optLong("object", 2);
                        totalAppointmentsText.setText(String.valueOf(todayAppointments));
                        totalAppointmentsText.setText(String.valueOf(2));
                        Log.d(TAG, "appointment count loaded successfully");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching number of appointments", error);
                        Toast.makeText(AdminDashboardActivity.this,
                                "Failed to load appointments",
                                Toast.LENGTH_SHORT).show();

                        // load sample data if there's an error
                        loadSampleRevenueData();
                    }
                }
        );
        // add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(apptReq);
    }

    private void fetchEmployees() {
        JsonObjectRequest employeeListReq = new JsonObjectRequest(
                Request.Method.GET,
                EMPLOYEE_LIST_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Fetching employees from " + EMPLOYEE_LIST_URL);
                            // Get the "object" JSON array from the response
                            JSONArray employeeArr = response.getJSONArray("object");

                            // Create a list to store staff members
                            List<EmployeeHomeActivity.Employee> staffList = new ArrayList<>();

                            // Parse each staff member
                            for (int i = 0; i < employeeArr.length(); i++) {
                                JSONObject staffObj = employeeArr.getJSONObject(i);

                                EmployeeHomeActivity.Employee staff = new EmployeeHomeActivity.Employee();
                                staff.setId(staffObj.getInt("id"));
                                staff.setFullName(staffObj.getString("message"));
                                staffList.add(staff);
                            }

                            // Store the staff list for later use
                            employees = staffList;
                            Log.d(TAG, "Employee list made!");

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing staff list data", e);
                            Toast.makeText(AdminDashboardActivity.this,
                                    "Failed to parse staff list data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching staff list data", error);
                        Toast.makeText(AdminDashboardActivity.this,
                                "Failed to load staff list data",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(employeeListReq);
    }

    // sample data loaders for backup purposes
    private void loadSampleDashboardData() {
        // Set sample values for dashboard stats
        totalAppointmentsText.setText("15");
        totalRevenueText.setText("$1,250");
        totalStaffText.setText("5");
    }

    private void loadSampleRevenueData() {
        // Set sample values for revenue overview
        currDateRevenueText.setText("$8,750");
        yesterdayRevenueText.setText("$7,890");
        revenueChangeText.setText("+10.9%");
        revenueChangeText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh dashboard data when returning to this screen
        fetchAdminDashboardData();

        // Refresh staff schedule

        // Refresh revenue overview
        fetchRevenueOverview();

        // Make sure home tab is selected
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    // Admin Data Model
    public static class Admin {
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

    /**
     * sets up the revenue chart with data from the API response for the last 7 days including today
     * @param jsonResponse the JSON response with previous 6 days data
     */
    private void setupRevenueChart(String jsonResponse) {
        try {
            // parse JSON response to get the revenue data array for previous 6 days
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String revenueArrayString = jsonObject.getString("object");

            // remove brackets and separate elements by commas
            revenueArrayString = revenueArrayString.replace("[", "").replace("]", "");
            String[] revenueStrings = revenueArrayString.split(", ");

            // reverse the array to display oldest to newest (previous 6 days)
            String[] reversedRevenueStrings = new String[revenueStrings.length];
            for (int i = 0; i < revenueStrings.length; i++) {
                reversedRevenueStrings[i] = revenueStrings[revenueStrings.length - 1 - i];
            }

            // create entries for the chart using the reversed array (previous 6 days)
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < reversedRevenueStrings.length; i++) {
                float value = Float.parseFloat(reversedRevenueStrings[i]);
                entries.add(new Entry(i, value));
            }

            // add today's revenue data as the 7th entry
            // get value from the TextView (removing currency symbol and formatting)
            String todayRevenueText = currDateRevenueText.getText().toString()
                    .replace("$", "")
                    .replace(",", "");
            float todayRevenue;
            try {
                todayRevenue = Float.parseFloat(todayRevenueText);
            } catch (NumberFormatException e) {
                todayRevenue = 0f; // Default if parsing fails
            }

            // add today's revenue as the last point on the chart
            entries.add(new Entry(reversedRevenueStrings.length, todayRevenue));

            // create a data set with the entries
            LineDataSet dataSet = new LineDataSet(entries, "Revenue");
            styleDataSet(dataSet);

            // create a data object with the data set
            LineData lineData = new LineData(dataSet);
            lineData.setValueTextColor(Color.BLACK);
            lineData.setValueTextSize(12f);
            lineData.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "$" + (int)value;
                }
            });

            // get chart view
            LineChart chart = new LineChart(this);
            styleChart(chart, reversedRevenueStrings.length + 1); // +1 for today

            // append data
            chart.setData(lineData);

            // update chart
            chart.invalidate();

            // add the chart to the container view
            FrameLayout container = findViewById(R.id.revenueChartContainer);
            container.removeAllViews();
            container.addView(chart);

        } catch (JSONException e) {
            Log.e("AdminDashboard", "Error parsing revenue data", e);
            Toast.makeText(this, "Failed to load revenue data", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * helper method to style the chart view with dynamic labels for the last 7 days including today
     * @param chart the chart to style
     * @param dataPoints the number of data points
     */
    private void styleChart(LineChart chart, int dataPoints) {
        // appearance config
        chart.setBackgroundColor(Color.WHITE);
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);

        // set padding
        chart.setExtraOffsets(10f, 10f, 10f, 10f);

        // X-axis styling to show all 7 days
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(dataPoints);

        // get day names for the last 7 days including today
        final String[] dayNames = getLastSevenDayNames(dataPoints - 1); // -1 because dataPoints includes today

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < dayNames.length) {
                    return dayNames[index];
                } else if (index == dayNames.length) {
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                    return dayFormat.format(calendar.getTime()); // label for today's data point
                }
                return "";
            }
        });

        // Y-axis styling
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setGridLineWidth(0.5f);
        leftAxis.setAxisLineWidth(1f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "$" + (int)value;
            }
        });

        // disable right Y-axis (because i want this to work similar to a bar graph
        chart.getAxisRight().setEnabled(false);

        // legend styling
        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
        legend.setTextColor(Color.BLACK);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    /**
     * Gets the names of the last n days (excluding today)
     * @param numDays the number of days to include
     * @return an array of day names
     */
    private String[] getLastSevenDayNames(int numDays) {
        String[] dayNames = new String[numDays];

        Calendar calendar = Calendar.getInstance();
        // move back to the first day in data array (earliest day)
        calendar.add(Calendar.DAY_OF_WEEK, -numDays);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        for (int i = 0; i < numDays; i++) {
            dayNames[i] = dayFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }

        return dayNames;
    }
    /**
     * helper method to style the dataset for the revenue chart
     * @param dataSet the dataset to style
     */
    private void styleDataSet(LineDataSet dataSet) {
        dataSet.setColor(Color.rgb(33, 150, 243)); // Blue color
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.rgb(33, 150, 243));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.rgb(33, 150, 243));
        dataSet.setFillAlpha(50);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
    }
}
