package com.example.androidexample;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidUI.AppointmentModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RescheduleAppointmentActivity extends AppCompatActivity {

    // UI Components - Calendar
    private TextView tvMonth;
    private ImageButton btnPrevMonth, btnNextMonth;
    private Button btnSelectTime, btnReschedule;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private GridLayout calendarGrid;
    private CardView timeSelectionCard;

    // UI Components - Time Selection
    private TextView tvAvailableTimeRange;
    private GridLayout timeSlotsGrid;
    private ScrollView scrollViewTimeSlots;
    private LinearLayout layoutPreferredTime;
    private EditText etPreferredTime;
    private TextView tvSelectFromGrid, tvEnterPreferredTime;

    // UI Components - Current Appointment
    private TextView tvCurrentTechnician, tvCurrentDate, tvCurrentTime, tvCurrentServices;

    // Calendar Variables

    private Calendar currentCalendar;
    private String selectedDate = "";
    private List<TextView> dateTextViews = new ArrayList<>();

    // Time Selection Variables
    private Button selectedTimeSlotButton = null;
    private boolean isGridMode = true;
    private String selectedTime = "";

    // Original Appointment Data
    private long appointmentId = -1;
    private long userId = -1;
    private long employeeId = -1;
    private String originalDate = "";
    private String originalTime = "";
    private int[] serviceIds;
    private String technicianName = "";

    // Time formats
    private static final DateTimeFormatter API_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    // Service names mapping (for 1-7 based service IDs)
    public static final String[] SERVICE_NAMES = {
            "", // Empty element at index 0 (not used)
            "Polish Change",
            "Gel Builder",
            "Solar Acrylic",
            "Manicure",
            "Pedicure",
            "Dipping Powder",
            "Extras"
    };

    // API Endpoints
    private static final String BASE_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api";
    private static final String CHECK_AVAILABILITY_ENDPOINT = "/timeframe/checkAvailability";
    private static final String RESCHEDULE_ENDPOINT = "/appointments/reschedule";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reschedule_appointment);

        // Initialize views
        initViews();

        // Extract intent data
        extractIntentData();

        // Display current appointment info
        displayCurrentAppointmentInfo();

        // Setup toolbar
        setupToolbar();

        // Initialize calendar
        currentCalendar = Calendar.getInstance();
        updateCalendarUI();

        // Setup event listeners
        setupListeners();
    }

    private void initViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);

        // Current appointment info
        tvCurrentTechnician = findViewById(R.id.tvCurrentTechnician);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvCurrentServices = findViewById(R.id.tvCurrentServices);

        // Calendar views
        tvMonth = findViewById(R.id.tvMonth);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        calendarGrid = findViewById(R.id.calendarGrid);

        // Time selection views
        timeSelectionCard = findViewById(R.id.timeSelectionCard);
        tvAvailableTimeRange = findViewById(R.id.tvAvailableTimeRange);
        timeSlotsGrid = findViewById(R.id.timeSlotsGrid);
        scrollViewTimeSlots = findViewById(R.id.scrollViewTimeSlots);
        layoutPreferredTime = findViewById(R.id.layoutPreferredTime);
        etPreferredTime = findViewById(R.id.etPreferredTime);
        tvSelectFromGrid = findViewById(R.id.tvSelectFromGrid);
        tvEnterPreferredTime = findViewById(R.id.tvEnterPreferredTime);

        // Buttons
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnReschedule = findViewById(R.id.btnReschedule);
        progressBar = findViewById(R.id.progressBar);

        // Initially disable buttons
        btnSelectTime.setEnabled(false);
        btnSelectTime.setAlpha(0.6f);
        btnReschedule.setEnabled(false);
        btnReschedule.setAlpha(0.6f);
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            appointmentId = intent.getLongExtra("APPOINTMENT_ID", -1);
            userId = intent.getLongExtra("USER_ID", -1);
            employeeId = intent.getLongExtra("EMPLOYEE_ID", -1);
            technicianName = intent.getStringExtra("TECHNICIAN_NAME");
            originalDate = intent.getStringExtra("APPOINTMENT_DATE");
            originalTime = intent.getStringExtra("APPOINTMENT_TIME");
            serviceIds = intent.getIntArrayExtra("SERVICE_IDS");

            // Log received data for debugging
            Log.d("RescheduleActivity", "Appointment ID: " + appointmentId);
            Log.d("RescheduleActivity", "User ID: " + userId);
            Log.d("RescheduleActivity", "Employee ID: " + employeeId);
            Log.d("RescheduleActivity", "Technician: " + technicianName);
            Log.d("RescheduleActivity", "Original Date: " + originalDate);
            Log.d("RescheduleActivity", "Original Time: " + originalTime);
        } else {
            Toast.makeText(this, "Error retrieving appointment data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void displayCurrentAppointmentInfo() {
        tvCurrentTechnician.setText(technicianName);
        tvCurrentDate.setText(formatDate(originalDate));
        tvCurrentTime.setText(originalTime);

        // Format service IDs into readable text
        StringBuilder servicesText = new StringBuilder();
        if (serviceIds != null && serviceIds.length > 0) {
            for (int i = 0; i < serviceIds.length; i++) {
                int serviceId = serviceIds[i];
                if (serviceId >= 1 && serviceId < SERVICE_NAMES.length) {
                    servicesText.append(SERVICE_NAMES[serviceId]);
                    if (i < serviceIds.length - 1) {
                        servicesText.append(", ");
                    }
                }
            }
        }
        tvCurrentServices.setText(servicesText.toString());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupListeners() {
        // Month navigation buttons
        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendarUI();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendarUI();
        });

        // Continue to time selection button
        btnSelectTime.setOnClickListener(v -> {
            if (!selectedDate.isEmpty()) {
                // Show time selection card and hide date selection button
                timeSelectionCard.setVisibility(View.VISIBLE);
                btnSelectTime.setVisibility(View.GONE);
                btnReschedule.setVisibility(View.VISIBLE);

                // Fetch available times for the selected date
                fetchAvailableTimes();
            } else {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            }
        });

        // Reschedule button
        btnReschedule.setOnClickListener(v -> {
            if (validateSelection()) {
                rescheduleAppointment();
            } else {
                Toast.makeText(this, "Please select both date and time", Toast.LENGTH_SHORT).show();
            }
        });

        // Mode switch listeners for time selection
        tvSelectFromGrid.setOnClickListener(v -> {
            if (!isGridMode) {
                isGridMode = true;
                updateModeUI();
                clearTimeSelection();
                validateAndEnableRescheduleButton();
            }
        });

        tvEnterPreferredTime.setOnClickListener(v -> {
            if (isGridMode) {
                isGridMode = false;
                updateModeUI();
                clearTimeSelection();
                validateAndEnableRescheduleButton();
            }
        });

        // Preferred time input listener
        etPreferredTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isGridMode) {
                    validateAndEnableRescheduleButton();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void updateCalendarUI() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonth.setText(monthFormat.format(currentCalendar.getTime()));
        generateCalendarGrid();
    }

    private void generateCalendarGrid() {
        selectedDate = "";
        dateTextViews.clear();
        calendarGrid.removeAllViews();

        // Reset Continue button
        btnSelectTime.setEnabled(false);
        btnSelectTime.setAlpha(0.6f);

        // Get calendar properties
        Calendar tempCalendar = (Calendar) currentCalendar.clone();
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Get today's date for comparison
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // Debug logs
        Log.d("Calendar", "Month: " + (tempCalendar.get(Calendar.MONTH) + 1));
        Log.d("Calendar", "First day of week: " + firstDayOfWeek);
        Log.d("Calendar", "Days in month: " + daysInMonth);

        // Calculate cell size
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int parentPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
        int cardPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
        int availableWidth = screenWidth - parentPadding - cardPadding;
        int cellSize = availableWidth / 7;

        // Set grid properties
        calendarGrid.setColumnCount(7);
        calendarGrid.setRowCount(6);

        // Generate calendar cells
        int day = 1;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                GridLayout.Spec rowSpec = GridLayout.spec(row);
                GridLayout.Spec colSpec = GridLayout.spec(col);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(0, 0, 0, 0);

                // For first row, handle days before the first of month
                if (row == 0 && col < firstDayOfWeek - 1) {
                    TextView emptyCell = new TextView(this);
                    emptyCell.setBackgroundColor(Color.TRANSPARENT);
                    calendarGrid.addView(emptyCell, params);
                    continue;
                }

                // For cells after the last day of month
                if (day > daysInMonth) {
                    TextView emptyCell = new TextView(this);
                    emptyCell.setBackgroundColor(Color.TRANSPARENT);
                    calendarGrid.addView(emptyCell, params);
                    continue;
                }

                // For actual day cells
                TextView dateCell = new TextView(this);
                dateCell.setText(String.valueOf(day));
                dateCell.setTextSize(14);
                dateCell.setGravity(Gravity.CENTER);
                dateCell.setBackgroundResource(R.drawable.calendar_day_background);

                Calendar thisDate = (Calendar) currentCalendar.clone();
                thisDate.set(Calendar.DAY_OF_MONTH, day);

                boolean isPast = thisDate.before(today);

                if (isPast) {
                    dateCell.setTextColor(ContextCompat.getColor(this, R.color.lightGray));
                    dateCell.setClickable(false);
                } else {
                    dateCell.setTextColor(ContextCompat.getColor(this, R.color.textDarkPurple));

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String dateString = dateFormat.format(thisDate.getTime());

                    dateCell.setOnClickListener(v -> selectDate(dateCell, dateString));
                    dateTextViews.add(dateCell);
                }

                calendarGrid.addView(dateCell, params);
                day++;
            }
        }
    }

    private void selectDate(TextView dateView, String dateString) {
        // Reset all date views to default state
        for (TextView tv : dateTextViews) {
            tv.setBackgroundResource(R.drawable.calendar_day_background);
            tv.setTextColor(ContextCompat.getColor(this, R.color.textDarkPurple));
        }

        // Highlight the selected one
        dateView.setBackgroundResource(R.drawable.selected_date_background);
        dateView.setTextColor(Color.WHITE);

        // Store the selected date
        selectedDate = dateString;

        // Enable the continue button
        btnSelectTime.setEnabled(true);
        btnSelectTime.setAlpha(1.0f);

        // Reset time selection if date changes
        timeSelectionCard.setVisibility(View.GONE);
        btnSelectTime.setVisibility(View.VISIBLE);
        btnReschedule.setVisibility(View.GONE);
        clearTimeSelection();
    }

    private void fetchAvailableTimes() {
        showLoading(true);
        timeSlotsGrid.removeAllViews(); // Clear previous slots

        // Prepare request data
        JSONObject data = new JSONObject();
        JSONArray jsonarray = new JSONArray();

        // Add the service IDs from the intent
        try {
            if (serviceIds != null) {
                for (int serviceId : serviceIds) {
                    jsonarray.put(serviceId);
                }
            } else {
                // Fallback in case serviceIds is null
                Log.e("RescheduleActivity", "serviceIds is null");
                Toast.makeText(this, "Error: Service information missing", Toast.LENGTH_SHORT).show();
                showLoading(false);
                return;
            }

            data.put("serviceIds", jsonarray);
            Log.d("RescheduleActivity", "employeeId: " + employeeId);
            Log.d("RescheduleActivity", "serviceIds: " + jsonarray);
            Log.d("RescheduleActivity", "date: " + selectedDate);
        } catch (JSONException e) {
            e.printStackTrace();
            showLoading(false);
            Toast.makeText(this, "Error preparing data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare API URL
        String url = BASE_URL + CHECK_AVAILABILITY_ENDPOINT + "?employeeId=" + employeeId + "&date=" + selectedDate;
        Log.d("RescheduleActivity", "url: " + url);

        // Make the request to get available time slots
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                data,
                response -> {
                    showLoading(false);
                    Log.d("RescheduleActivity", "Response: " + response.toString());
                    try {
                        // Parse the response which should have a "slots" array
                        JSONArray slotsArray = response.getJSONArray("slots");
                        List<String> allTimeSlots = new ArrayList<>();

                        if (slotsArray.length() == 0) {
                            Toast.makeText(this, "No available time slots found for this date.", Toast.LENGTH_LONG).show();
                            tvAvailableTimeRange.setText("No available times for this date");
                        } else {
                            // Find earliest start time and latest end time across all slots
                            LocalTime earliestStartTime = null;
                            LocalTime latestEndTime = null;

                            for (int i = 0; i < slotsArray.length(); i++) {
                                JSONObject slot = slotsArray.getJSONObject(i);
                                String start = slot.getString("availableStart");
                                String end = slot.getString("availableEnd");

                                LocalTime currentStartTime = LocalTime.parse(start, API_TIME_FORMAT);
                                LocalTime currentEndTime = LocalTime.parse(end, API_TIME_FORMAT);

                                // Initialize or update earliest/latest times
                                if (earliestStartTime == null || currentStartTime.isBefore(earliestStartTime)) {
                                    earliestStartTime = currentStartTime;
                                }

                                if (latestEndTime == null || currentEndTime.isAfter(latestEndTime)) {
                                    latestEndTime = currentEndTime;
                                }

                                allTimeSlots.addAll(generateTimeSlots(start, end));
                            }

                            // Update the time range display with the overall range
                            if (earliestStartTime != null && latestEndTime != null) {
                                String displayStart = earliestStartTime.format(DISPLAY_TIME_FORMAT);
                                String displayEnd = latestEndTime.format(DISPLAY_TIME_FORMAT);
                                tvAvailableTimeRange.setText("Available between " + displayStart + " - " + displayEnd);
                            }

                            populateTimeSlotsGrid(allTimeSlots);
                        }
                    } catch (JSONException e) {
                        Log.e("RescheduleActivity", "Error parsing JSON response", e);
                        Toast.makeText(this, "Error processing available times", Toast.LENGTH_SHORT).show();
                        tvAvailableTimeRange.setText("Error loading available times");
                    }
                },
                error -> {
                    showLoading(false);
                    Log.e("RescheduleActivity", "Error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("RescheduleActivity", "Status Code: " + error.networkResponse.statusCode);
                    }
                    Toast.makeText(this, "Error fetching available times", Toast.LENGTH_SHORT).show();
                    tvAvailableTimeRange.setText("Error loading available times");
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private List<String> generateTimeSlots(String startStr, String endStr) {
        List<String> slots = new ArrayList<>();
        try {
            LocalTime startTime = LocalTime.parse(startStr, API_TIME_FORMAT);
            LocalTime endTime = LocalTime.parse(endStr, API_TIME_FORMAT);

            LocalTime currentTime = startTime;
            while (currentTime.isBefore(endTime)) {
                slots.add(currentTime.format(DISPLAY_TIME_FORMAT));
                currentTime = currentTime.plusMinutes(30); // 30-minute slots
            }
        } catch (DateTimeParseException e) {
            Log.e("RescheduleActivity", "Error parsing time: " + e.getMessage());
        }
        return slots;
    }

    private void populateTimeSlotsGrid(List<String> timeSlots) {
        timeSlotsGrid.removeAllViews();

        if (timeSlots.isEmpty()) {
            TextView noSlotsMsg = new TextView(this);
            noSlotsMsg.setText("No available time slots found.");
            noSlotsMsg.setGravity(Gravity.CENTER);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.columnSpec = GridLayout.spec(0, timeSlotsGrid.getColumnCount());
            noSlotsMsg.setLayoutParams(params);
            timeSlotsGrid.addView(noSlotsMsg);
            return;
        }

        int columnCount = 3; // Use 3 columns for the grid
        timeSlotsGrid.setColumnCount(columnCount);

        for (String time : timeSlots) {
            Button timeButton = new Button(this);
            timeButton.setText(time);
            timeButton.setTextColor(ContextCompat.getColorStateList(this, R.color.time_slot_text_selector));
            timeButton.setBackgroundResource(R.drawable.time_slot_button);
            timeButton.setTextSize(14);
            timeButton.setAllCaps(false);
            timeButton.setElevation(2f);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            timeButton.setLayoutParams(params);

            timeButton.setOnClickListener(v -> {
                if (selectedTimeSlotButton != null) {
                    selectedTimeSlotButton.setSelected(false);
                }
                v.setSelected(true);
                selectedTimeSlotButton = (Button) v;
                selectedTime = time;
                validateAndEnableRescheduleButton();
            });

            timeSlotsGrid.addView(timeButton);
        }
    }

    private void updateModeUI() {
        if (isGridMode) {
            scrollViewTimeSlots.setVisibility(View.VISIBLE);
            layoutPreferredTime.setVisibility(View.GONE);

            tvSelectFromGrid.setBackgroundResource(R.drawable.tab_selected_background);
            tvSelectFromGrid.setTextColor(ContextCompat.getColor(this, R.color.text_dark_purple));
            tvSelectFromGrid.setTypeface(null, android.graphics.Typeface.BOLD);

            tvEnterPreferredTime.setBackgroundResource(R.drawable.tab_unselected_background);
            tvEnterPreferredTime.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tvEnterPreferredTime.setTypeface(null, android.graphics.Typeface.NORMAL);
        } else {
            scrollViewTimeSlots.setVisibility(View.GONE);
            layoutPreferredTime.setVisibility(View.VISIBLE);

            tvSelectFromGrid.setBackgroundResource(R.drawable.tab_unselected_background);
            tvSelectFromGrid.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tvSelectFromGrid.setTypeface(null, android.graphics.Typeface.NORMAL);

            tvEnterPreferredTime.setBackgroundResource(R.drawable.tab_selected_background);
            tvEnterPreferredTime.setTextColor(ContextCompat.getColor(this, R.color.text_dark_purple));
            tvEnterPreferredTime.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void clearTimeSelection() {
        selectedTime = "";
        if (selectedTimeSlotButton != null) {
            selectedTimeSlotButton.setSelected(false);
            selectedTimeSlotButton = null;
        }
        etPreferredTime.setText("");
    }

    private boolean validateTimeInput() {
        if (isGridMode) {
            return selectedTimeSlotButton != null && !selectedTime.isEmpty();
        } else {
            String preferredTime = etPreferredTime.getText().toString().trim();
            if (preferredTime.matches("^([01]?[0-9]|2[0-3]):([0-5][0-9])$")) {
                selectedTime = preferredTime;
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean validateSelection() {
        return !selectedDate.isEmpty() && validateTimeInput();
    }

    private void validateAndEnableRescheduleButton() {
        boolean isValid = validateSelection();
        btnReschedule.setEnabled(isValid);
        btnReschedule.setAlpha(isValid ? 1.0f : 0.6f);
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(dateString));
        } catch (Exception e) {
            return dateString;
        }
    }

    private void rescheduleAppointment() {
        if (!validateSelection()) {
            Toast.makeText(this, "Please select both date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // Prepare the JSON data for the request
        JSONObject requestData = new JSONObject();
        try {
            // Only include necessary fields for the updated endpoint
            // We don't need to include appointmentId in the body since it's in the URL
            requestData.put("userId", userId);
            requestData.put("employeeId", employeeId);

            // Add service IDs if needed by your API
            JSONArray serviceIdsArray = new JSONArray();
            if (serviceIds != null) {
                for (int serviceId : serviceIds) {
                    serviceIdsArray.put(serviceId);
                }
            }
            requestData.put("serviceIds", serviceIdsArray);

            // Log request data for debugging
            Log.d("RescheduleActivity", "Request data: " + requestData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            showLoading(false);
            Toast.makeText(this, "Error preparing rescheduling data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format the URL with path parameters
        String url = BASE_URL + "/appointments/change/" + appointmentId + "/" + selectedDate + "/" + selectedTime;
        Log.d("RescheduleActivity", "Reschedule URL: " + url);

        // Send the PUT request to reschedule the appointment
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                requestData,
                response -> {
                    showLoading(false);
                    Log.d("RescheduleActivity", "Reschedule response: " + response.toString());

                    try {

                        String message = response.optString("message", "Unknown response");

                        if (!message.equals("Unknown response")) {
                            Toast.makeText(this, "Appointment successfully rescheduled!", Toast.LENGTH_LONG).show();

                            // Navigate back to appointment details or appointments list
                            Intent intent = new Intent();
                            intent.putExtra("RESCHEDULED", true);
                            intent.putExtra("NEW_DATE", selectedDate);
                            intent.putExtra("NEW_TIME", selectedTime);

                            // Add position to intent to update correct appointment in list
                            int position = getIntent().getIntExtra("POSITION", -1);
                            intent.putExtra("POSITION", position);

                            setResult(RESULT_OK, intent);
                            finish();
                        }
                        else {
                            Toast.makeText(this, "Failed to reschedule: " + message, Toast.LENGTH_LONG).show();
                        }
                    }
                    catch (Exception e)
                    {
                        Log.e("RescheduleActivity", "Error processing response", e);
                        Toast.makeText(this, "Error processing server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    showLoading(false);
                    Log.e("RescheduleActivity", "Error: " + error.toString());

                    if (error.networkResponse != null) {
                        Log.e("RescheduleActivity", "Status Code: " + error.networkResponse.statusCode);
                    }

                    Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Add the request to the request queue
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSelectTime.setEnabled(!isLoading && !selectedDate.isEmpty());
        btnReschedule.setEnabled(!isLoading && validateSelection());
    }
}