//package com.example.androidexample;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.appcompat.app.AlertDialog;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.android.volley.Request;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.JsonObjectRequest;
//import com.example.androidUI.AppointmentAdapter;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.chip.Chip;
//import com.google.android.material.chip.ChipGroup;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class EmployeeAppointmentsActivity extends AppCompatActivity {
//    private static final String TAG = "EmployeeAppointmentActivity";
//
//    // API URL
//    private static final String EMPLOYEE_APPOINTMENTS_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/employee/appointments/";
//
//    // UI Components
//    private RecyclerView appointmentsRecyclerView;
//    private AppointmentAdapter appointmentAdapter;
//    private List<Appointment> appointmentList;
//    private View emptyStateContainer;
//    private TextView appointmentCountText;
//    private ChipGroup filterChipGroup;
//    private MaterialButton dateRangeButton;
//    private FloatingActionButton addAppointmentFab;
//    private Toolbar toolbar;
//
//    // Data
//    private int employeeId;
//    private String filterStatus = "all"; // all, confirmed, pending, cancelled
//    private String dateRange = "week"; // day, week, month, all
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_employee_appointment);
//
//        // Get employee ID from intent
//        employeeId = getIntent().getIntExtra("id", -1);
//
//        if (employeeId <= 0) {
//            // Try to get from PrefManager if not in intent
//            PrefManager prefManager = PrefManager.getInstance(this);
//            employeeId = prefManager.getUserId();
//
//            if (employeeId <= 0) {
//                Toast.makeText(this, "Invalid employee ID", Toast.LENGTH_SHORT).show();
//                //finish();
//                return;
//            }
//        }
//
//        // Initialize UI components
//        initializeUI();
//
//        // Set up listeners
//        setupListeners();
//
//        // Fetch appointments
//        fetchAppointments();
//    }
//
//    private void initializeUI() {
//        // Toolbar setup
//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        // Find views
//        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView);
//        emptyStateContainer = findViewById(R.id.emptyStateContainer);
//        appointmentCountText = findViewById(R.id.appointmentCountText);
//        filterChipGroup = findViewById(R.id.filterChipGroup);
//        dateRangeButton = findViewById(R.id.dateRangeButton);
//        addAppointmentFab = findViewById(R.id.addAppointmentFab);
//
//        // Set up RecyclerView
//        appointmentList = new ArrayList<>();
//        appointmentAdapter = new AppointmentAdapter(appointmentList);
//        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        appointmentsRecyclerView.setAdapter(appointmentAdapter);
//    }
//
//    private void setupListeners() {
//        // Handle back button in toolbar
//        toolbar.setNavigationOnClickListener(v -> onBackPressed());
//
//        // Filter chip selection changes
//        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
//            if (checkedId == R.id.allChip) {
//                filterStatus = "all";
//            } else if (checkedId == R.id.confirmedChip) {
//                filterStatus = "confirmed";
//            } else if (checkedId == R.id.pendingChip) {
//                filterStatus = "pending";
//            } else if (checkedId == R.id.cancelledChip) {
//                filterStatus = "cancelled";
//            } else if (checkedId == R.id.todayChip) {
//                filterStatus = "all";
//                dateRange = "today";
//            }
//
//            // Apply filters
//            fetchAppointments();
//        });
//
//        // Date range button
//        dateRangeButton.setOnClickListener(v -> {
//            // Show date range selection dialog
//            showDateRangeDialog();
//        });
//
//        // Add appointment FAB
//        addAppointmentFab.setOnClickListener(v -> {
//            // Navigate to add appointment screen
//            // Currently a placeholder
//            Toast.makeText(this, "Add appointment feature coming soon", Toast.LENGTH_SHORT).show();
//        });
//    }
//
//    private void showDateRangeDialog() {
//        // Placeholder for date range dialog
//        // Show a menu/dialog to select date range (Today, This Week, This Month, All)
//        String[] options = {"Today", "This Week", "This Month", "All"};
//
//        new AlertDialog.Builder(this)
//                .setTitle("Select Date Range")
//                .setItems(options, (dialog, which) -> {
//                    switch (which) {
//                        case 0:
//                            dateRange = "today";
//                            dateRangeButton.setText("Today");
//                            break;
//                        case 1:
//                            dateRange = "week";
//                            dateRangeButton.setText("This Week");
//                            break;
//                        case 2:
//                            dateRange = "month";
//                            dateRangeButton.setText("This Month");
//                            break;
//                        case 3:
//                            dateRange = "all";
//                            dateRangeButton.setText("All Time");
//                            break;
//                    }
//
//                    // Apply the new date filter
//                    fetchAppointments();
//                })
//                .show();
//    }
//
//    private void fetchAppointments() {
//        String url = EMPLOYEE_APPOINTMENTS_URL + employeeId;
//
//        // Add query parameters for filtering
//        url += "?status=" + filterStatus + "&range=" + dateRange;
//
//        JsonObjectRequest request = new JsonObjectRequest(
//                Request.Method.GET,
//                url,
//                null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            parseAppointmentsResponse(response);
//                        } catch (JSONException e) {
//                            Log.e(TAG, "Error parsing appointments", e);
//                            Toast.makeText(EmployeeAppointmentsActivity.this,
//                                    "Error loading appointments", Toast.LENGTH_SHORT).show();
//                            showEmptyState();
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e(TAG, "Error fetching appointments", error);
//                        Toast.makeText(EmployeeAppointmentsActivity.this,
//                                "Failed to load appointments", Toast.LENGTH_SHORT).show();
//                        showEmptyState();
//                    }
//                }
//        ) {
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> headers = new HashMap<>();
//                // Add any headers here if needed
//                return headers;
//            }
//        };
//
//        // Add the request to the queue
//        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
//    }
//
//    private void parseAppointmentsResponse(JSONObject response) throws JSONException {
//        // Clear existing appointments
//        appointmentList.clear();
//
//        // Get appointments array
//        JSONArray appointmentsArray = response.getJSONArray("appointments");
//
//        // Update count
//        int totalCount = appointmentsArray.length();
//        appointmentCountText.setText(totalCount + " Total");
//
//        // Parse each appointment
//        for (int i = 0; i < appointmentsArray.length(); i++) {
//            JSONObject appointmentJson = appointmentsArray.getJSONObject(i);
//
//            Appointment appointment = new Appointment(
//                    appointmentJson.getInt("id"),
//                    appointmentJson.getString("clientName"),
//                    appointmentJson.getString("serviceName"),
//                    appointmentJson.getString("appointmentDate"),
//                    appointmentJson.getString("appointmentTime"),
//                    appointmentJson.getString("endTime"),
//                    appointmentJson.getString("status"),
//                    appointmentJson.optDouble("price", 0.0),
//                    appointmentJson.optString("notes", "")
//            );
//
//            appointmentList.add(appointment);
//        }
//
//        // Update UI
//        appointmentAdapter.notifyDataSetChanged();
//
//        // Show empty state if no appointments
//        if (appointmentList.isEmpty()) {
//            showEmptyState();
//        } else {
//            hideEmptyState();
//        }
//    }
//
//    private void showEmptyState() {
//        appointmentsRecyclerView.setVisibility(View.GONE);
//        emptyStateContainer.setVisibility(View.VISIBLE);
//    }
//
//    private void hideEmptyState() {
//        appointmentsRecyclerView.setVisibility(View.VISIBLE);
//        emptyStateContainer.setVisibility(View.GONE);
//    }
//
//    // Data model for an appointment
//    public static class Appointment {
//        private int id;
//        private String clientName;
//        private String serviceName;
//        private String appointmentDate;
//        private String startTime;
//        private String endTime;
//        private String status;
//        private double price;
//        private String notes;
//
//        public Appointment(int id, String clientName, String serviceName,
//                           String appointmentDate, String startTime, String endTime,
//                           String status, double price, String notes) {
//            this.id = id;
//            this.clientName = clientName;
//            this.serviceName = serviceName;
//            this.appointmentDate = appointmentDate;
//            this.startTime = startTime;
//            this.endTime = endTime;
//            this.status = status;
//            this.price = price;
//            this.notes = notes;
//        }
//
//        // Getters
//        public int getId() { return id; }
//        public String getClientName() { return clientName; }
//        public String getServiceName() { return serviceName; }
//        public String getAppointmentDate() { return appointmentDate; }
//        public String getStartTime() { return startTime; }
//        public String getEndTime() { return endTime; }
//        public String getStatus() { return status; }
//        public double getPrice() { return price; }
//        public String getNotes() { return notes; }
//
//        // Get formatted price
//        public String getFormattedPrice() {
//            return String.format("$%.2f", price);
//        }
//
//        // Get day part of the date (e.g., "15")
//        public String getDay() {
//            // Assumes date format is "yyyy-MM-dd"
//            String[] parts = appointmentDate.split("-");
//            if (parts.length == 3) {
//                return parts[2]; // Day is the third part
//            }
//            return "";
//        }
//
//        // Get month part of the date (e.g., "MAR")
//        public String getMonth() {
//            // Assumes date format is "yyyy-MM-dd"
//            String[] parts = appointmentDate.split("-");
//            if (parts.length == 3) {
//                int month = Integer.parseInt(parts[1]);
//                String[] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN",
//                        "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
//                return months[month - 1]; // Month is the second part (0-indexed in array)
//            }
//            return "";
//        }
//
//        // Get formatted time range (e.g., "2:30 PM - 3:45 PM")
//        public String getTimeRange() {
//            return startTime + " - " + endTime;
//        }
//    }
//}