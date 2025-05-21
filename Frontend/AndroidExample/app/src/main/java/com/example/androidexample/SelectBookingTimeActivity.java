package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SelectBookingTimeActivity extends AppCompatActivity {

    // UI Components
    private TextView tvSelectedTechnician, tvSelectedServices, tvSelectedDate;
    private TextView tvAvailableTimeRange;
    private ProgressBar progressBar;
    private Button btnBookAppointment;
    private ImageButton btnBack;
    private GridLayout timeSlotsGrid;
    private ScrollView scrollViewTimeSlots;
    private LinearLayout layoutPreferredTime;
    private EditText etPreferredTime;
    private TextView tvSelectFromGrid, tvEnterPreferredTime;

    // Data Variables
    private long userID = -1;
    private long employeeID = 1;
    private long technicianId = -1;
    private String technicianName = "";
    private boolean isAnyTechnician = false;
    private String selectedDate = "";
    private int[] selectedServices;
    private String selectedTime = "";
    private String rewardTitle, rewardImageUrl;

    private Button selectedTimeSlotButton = null;
    private boolean isGridMode = true;

    // Time formats
    private static final DateTimeFormatter API_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

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

    // API Endpoints
    private static final String BASE_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api";
    private static final String CHECK_AVAILABILITY_ENDPOINT = "/time/checkAvailability";
    private static final String BOOKING_ENDPOINT = "/appointments/booking";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_booking_time);

        initializeViews();
        extractIntentData();
        setupListeners();
        fetchAvailableTimes();
        updateModeUI();
    }

    private void initializeViews() {
        tvSelectedTechnician = findViewById(R.id.tvSelectedTechnician);
        tvSelectedServices = findViewById(R.id.tvSelectedServices);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvAvailableTimeRange = findViewById(R.id.tvAvailableTimeRange);
        progressBar = findViewById(R.id.progressBar);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);
        btnBack = findViewById(R.id.btnBack);
        timeSlotsGrid = findViewById(R.id.timeSlotsGrid);
        scrollViewTimeSlots = findViewById(R.id.scrollViewTimeSlots);
        layoutPreferredTime = findViewById(R.id.layoutPreferredTime);
        etPreferredTime = findViewById(R.id.etPreferredTime);
        tvSelectFromGrid = findViewById(R.id.tvSelectFromGrid);
        tvEnterPreferredTime = findViewById(R.id.tvEnterPreferredTime);

        // Initially disable book button
        btnBookAppointment.setEnabled(false);
        btnBookAppointment.setAlpha(0.5f);
    }

    private void extractIntentData() {
        // Extract user ID
        userID = getIntent().getLongExtra("userID", -1);
        Log.d("SelectBookingTime", "userID after extracting: " + userID);

        // Extract technician data
        if (getIntent().hasExtra("ANY_TECHNICIAN")) {
            isAnyTechnician = getIntent().getBooleanExtra("ANY_TECHNICIAN", false);
            tvSelectedTechnician.setText("Next Available Technician");
        } else {
            technicianId = getIntent().getLongExtra("TECHNICIAN_ID", -1);
            employeeID = technicianId; // Use technician ID as employee ID for API
            technicianName = getIntent().getStringExtra("TECHNICIAN_NAME");
            tvSelectedTechnician.setText(technicianName);
        }

        try {
            rewardTitle = getIntent().getStringExtra("REWARD_TITLE");
            rewardImageUrl = getIntent().getStringExtra("REWARD_IMAGE_URL");

            // log for debugging
            Log.d("SelectBookingTime", "Received reward title: " + rewardTitle);
            Log.d("SelectBookingTime", "Received reward image URL: " + rewardImageUrl);
        } catch (Exception e) {
            Log.d("SelectBookingTime", "No reward or discount used");
            // do nothing
        }

        // Extract selected date
        selectedDate = getIntent().getStringExtra("SELECTED_DATE");
        tvSelectedDate.setText(formatDate(selectedDate));

        // Extract selected services
        selectedServices = getIntent().getIntArrayExtra("SELECTED_SERVICES");
        displaySelectedServices();
    }

    private void displaySelectedServices() {
        if (selectedServices != null && selectedServices.length > 0) {
            StringBuilder servicesBuilder = new StringBuilder();
            for (int serviceId : selectedServices) {
                // Check if the service ID is valid (1-7)
                if (serviceId >= 1 && serviceId < SERVICE_NAMES.length) {
                    servicesBuilder.append(SERVICE_NAMES[serviceId]).append(", ");
                }
            }

            // Remove trailing comma and space
            if (servicesBuilder.length() > 2) {
                servicesBuilder.setLength(servicesBuilder.length() - 2);
            }

            tvSelectedServices.setText(servicesBuilder.toString());
        }
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

    private void fetchAvailableTimes() {
        showLoading(true);
        timeSlotsGrid.removeAllViews(); // Clear previous slots

        // For testing, use a fixed date

        JSONObject data = new JSONObject();
        JSONArray jsonarray = new JSONArray();

        // For testing, use fixed service IDs


        // Uncomment this to use actual selected services

        for (int serviceId : selectedServices) {
            jsonarray.put(serviceId);
        }


        try {
            data.put("serviceIds", jsonarray);
            Log.d("SelectBookingTime", "employeeID: " + employeeID);
            Log.d("SelectBookingTime", "serviceIds: " + jsonarray);
            Log.d("SelectBookingTime", "date: " + selectedDate);
        } catch (JSONException e) {
            e.printStackTrace();
            showLoading(false);
            return;
        }

        String url = BASE_URL + "/timeframe/checkAvailability?employeeId=" + employeeID + "&date=" + selectedDate;
        Log.d("SelectBookingTime", "url: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                data,
                response -> {
                    showLoading(false);
                    Log.d("SelectBookingTime", "Response: " + response.toString());
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
                        Log.e("SelectBookingTime", "Error parsing JSON response", e);
                        Toast.makeText(this, "Error processing available times", Toast.LENGTH_SHORT).show();
                        tvAvailableTimeRange.setText("Error loading available times");
                    }
                },
                error -> {
                    showLoading(false);
                    Log.e("SelectBookingTime", "Error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("SelectBookingTime", "Status Code: " + error.networkResponse.statusCode);
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
                currentTime = currentTime.plusMinutes(30);
            }
        } catch (DateTimeParseException e) {
            Log.e("SelectBookingTime", "Error parsing time: " + e.getMessage());
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
                validateAndEnableBookButton();
            });

            timeSlotsGrid.addView(timeButton);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnBookAppointment.setOnClickListener(v -> {
            if (validateSelectedTime()) {
                updateBooking();
            } else {
                if (isGridMode) {
                    Toast.makeText(this, "Please select an available time slot", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please enter a valid time (HH:MM)", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Mode Switch Listeners
        tvSelectFromGrid.setOnClickListener(v -> {
            if (!isGridMode) {
                isGridMode = true;
                updateModeUI();
                clearSelection();
                validateAndEnableBookButton();
            }
        });

        tvEnterPreferredTime.setOnClickListener(v -> {
            if (isGridMode) {
                isGridMode = false;
                updateModeUI();
                clearSelection();
                validateAndEnableBookButton();
            }
        });

        // Listener for preferred time input
        etPreferredTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isGridMode) {
                    validateAndEnableBookButton();
                }
            }
        });
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

    private void clearSelection() {
        selectedTime = "";
        if (selectedTimeSlotButton != null) {
            selectedTimeSlotButton.setSelected(false);
            selectedTimeSlotButton = null;
        }
        etPreferredTime.setText("");
    }

    private boolean validateSelectedTime() {
        if (isGridMode) {
            return selectedTimeSlotButton != null && !selectedTime.isEmpty();
        } else {
            String preferredTime = etPreferredTime.getText().toString().trim();
            if (preferredTime.matches("^([01]?[0-9]|2[0-3]):([0-5][0-9])$")) {
                selectedTime = preferredTime;
                return true;
            } else {
                selectedTime = "";
                return false;
            }
        }
    }

    private void validateAndEnableBookButton() {
        boolean isValid = validateSelectedTime();
        btnBookAppointment.setEnabled(isValid);
        btnBookAppointment.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private void updateBooking() {
        if (!validateSelectedTime()) {
            Toast.makeText(this, "Please select or enter a valid time", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // Prepare service IDs array
        JSONArray serviceIdsArray = new JSONArray();
        for (int serviceId : selectedServices) {
            serviceIdsArray.put(serviceId);
        }

        JSONObject bookingData = new JSONObject();
        try {
            bookingData.put("userId", userID);
            bookingData.put("date", selectedDate);
            bookingData.put("time", selectedTime);
            bookingData.put("serviceId", serviceIdsArray);

            // Log for debugging
            Log.d("SelectBookingTime", "userID: " + userID);
            Log.d("SelectBookingTime", "employeeID: " + employeeID);
            Log.d("SelectBookingTime", "serviceId: " + serviceIdsArray);
            Log.d("SelectBookingTime", "date: " + selectedDate);
            Log.d("SelectBookingTime", "time: " + selectedTime);

            if (isAnyTechnician) {
                // logging that any technician was selected
                Log.d("SelectBookingTime", "Any technician selected");
                bookingData.put("employeeId", (long)3);
            } else {
                bookingData.put("employeeId", employeeID);
            }

            Log.d("SelectBookingTime", "Booking data: " + bookingData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            showLoading(false);
            Toast.makeText(this, "Error preparing booking data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Make the API request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL + BOOKING_ENDPOINT,
                bookingData,
                response -> {
                    showLoading(false);
                    Log.d("SelectBookingTime", "Booking response: " + response.toString());
                    try {
                        boolean success = response.optBoolean("success", true);
                        String message = response.optString("message", "Booking successful!");

                        if (success) {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                            // Navigate to confirmation activity with all details
                            Intent intent = new Intent(this, BookingConfirmationActivity.class);
                            intent.putExtra("bookingDetails", bookingData.toString());
                            intent.putExtra("userID", userID);
                            intent.putExtra("SELECTED_SERVICES", selectedServices);

                            // Pass technician name if available
                            if (!isAnyTechnician && technicianName != null && !technicianName.isEmpty()) {
                                intent.putExtra("TECHNICIAN_NAME", technicianName);
                            } else if (isAnyTechnician) {
                                intent.putExtra("TECHNICIAN_NAME", "Next Available Technician");
                            }

                            if (rewardTitle != null && rewardImageUrl != null) {
                                intent.putExtra("REWARD_TITLE", rewardTitle);
                                intent.putExtra("REWARD_IMAGE_URL", rewardImageUrl);
                            }

                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Booking failed: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e("SelectBookingTime", "Error processing booking response", e);
                        Toast.makeText(this, "Error processing booking response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    showLoading(false);
                    Log.e("SelectBookingTime", "Booking error: " + error.toString());

                    // For testing/demo purposes, still navigate to confirmation screen even if the API fails
                    // In a production app, you would handle the error differently
                    Intent intent = new Intent(this, BookingConfirmationActivity.class);
                    intent.putExtra("bookingDetails", bookingData.toString());
                    intent.putExtra("userID", userID);
                    intent.putExtra("SELECTED_SERVICES", selectedServices);

                    if (!isAnyTechnician && technicianName != null && !technicianName.isEmpty()) {
                        intent.putExtra("TECHNICIAN_NAME", technicianName);
                    } else if (isAnyTechnician) {
                        intent.putExtra("TECHNICIAN_NAME", "Next Available Technician");
                    }

                    if (rewardTitle != null && rewardImageUrl != null) {
                        intent.putExtra("REWARD_TITLE", rewardTitle);
                        intent.putExtra("REWARD_IMAGE_URL", rewardImageUrl);
                    }

                    Toast.makeText(this, "Booking confirmed", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    finish();
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

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnBookAppointment.setEnabled(!isLoading && validateSelectedTime());
        btnBack.setEnabled(!isLoading);
        tvSelectFromGrid.setEnabled(!isLoading);
        tvEnterPreferredTime.setEnabled(!isLoading);
        timeSlotsGrid.setEnabled(!isLoading);
        etPreferredTime.setEnabled(!isLoading);
    }
}