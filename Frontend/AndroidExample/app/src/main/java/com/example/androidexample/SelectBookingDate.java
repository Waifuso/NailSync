package com.example.androidexample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SelectBookingDate extends AppCompatActivity {

    // UI components
    private TextView tvMonth;
    private ImageButton btnPrevMonth, btnNextMonth;
    private Button btnContinue;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private GridLayout calendarGrid;

    // Technician info
    private long technicianId = 1;
    private String technicianName = "";
    private double technicianPrice = 0.0;
    private boolean isAnyTechnician = false;
    private long userID = -1;

    private Calendar currentCalendar;
    private String selectedDate = "";
    private List<TextView> dateTextViews = new ArrayList<>();

    private String rewardTitle, rewardImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_booking_date);

        // Catch crashes
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                Log.e("Crash", "Uncaught exception", throwable)
        );

        // Handle intent extras safely
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("TECHNICIAN_ID"))
                technicianId = intent.getLongExtra("TECHNICIAN_ID", -1);

            if (intent.hasExtra("TECHNICIAN_NAME"))
                technicianName = intent.getStringExtra("TECHNICIAN_NAME");

            if (intent.hasExtra("TECHNICIAN_PRICE"))
                technicianPrice = intent.getDoubleExtra("TECHNICIAN_PRICE", 0.0);

            if (intent.hasExtra("ANY_TECHNICIAN"))
                isAnyTechnician = intent.getBooleanExtra("ANY_TECHNICIAN", false);

            if (intent.hasExtra("userID"))
                userID = intent.getLongExtra("userID", -1);

            try {
                rewardTitle = getIntent().getStringExtra("REWARD_TITLE");
                rewardImageUrl = getIntent().getStringExtra("REWARD_IMAGE_URL");

                // log for debugging
                Log.d("ApplicationActivity", "Received reward title: " + rewardTitle);
                Log.d("ApplicationActivity", "Received reward image URL: " + rewardImageUrl);
            }
            catch (Exception e)
            {
                Log.d("ApplicationActivity", "No reward or discount used");
                // do nothing
            }
        }

        // Log values for debugging
        Log.d("BookingIntent", "Technician ID: " + technicianId);
        Log.d("BookingIntent", "Technician Name: " + technicianName);
        Log.d("BookingIntent", "Technician Price: " + technicianPrice);
        Log.d("BookingIntent", "Any Technician: " + isAnyTechnician);
        Log.d("BookingIntent", "User ID: " + userID);

        initViews();
        setupToolbar();

        currentCalendar = Calendar.getInstance();
        updateCalendarUI();

        setupClickListeners();
    }

    private void initViews() {
        tvMonth = findViewById(R.id.tvMonth);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnContinue = findViewById(R.id.btnContinue);
        calendarGrid = findViewById(R.id.calendarGrid);
        progressBar = findViewById(R.id.progressBar);
        toolbar = findViewById(R.id.toolbar);

        TextView tvTechnicianName = findViewById(R.id.tvTechnicianName);
        if (tvTechnicianName != null) {
            tvTechnicianName.setText(isAnyTechnician ? "Next Available Artist" :
                    (technicianName != null ? technicianName : "Technician"));
        }

        btnContinue.setEnabled(false);
        btnContinue.setAlpha(0.6f);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupClickListeners() {
        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendarUI();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendarUI();
        });

        btnContinue.setOnClickListener(v -> {
            if (!selectedDate.isEmpty()) {
                proceedToServiceSelection();
            } else {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            }
        });
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

        btnContinue.setEnabled(false);
        btnContinue.setAlpha(0.6f);

        Calendar tempCalendar = (Calendar) currentCalendar.clone();
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // Debug logs
        Log.d("Calendar", "Month: " + (tempCalendar.get(Calendar.MONTH) + 1));
        Log.d("Calendar", "First day of week: " + firstDayOfWeek);
        Log.d("Calendar", "Days in month: " + daysInMonth);

        // Calculate available width and ensure equal-sized cells
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        // Account for the parent layout's padding (16dp on each side)
        int parentPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());

        // Account for the card's padding (16dp on each side)
        int cardPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());

        int availableWidth = screenWidth - parentPadding - cardPadding;
        int cellSize = availableWidth / 7;

        // Reset the grid
        calendarGrid.removeAllViews();
        calendarGrid.setColumnCount(7);
        calendarGrid.setRowCount(6);

        // Create cells for the entire grid with precise spacing
        int day = 1;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                GridLayout.Spec rowSpec = GridLayout.spec(row);
                GridLayout.Spec colSpec = GridLayout.spec(col);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(0, 0, 0, 0); // Remove margins entirely

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

        // Set width and height explicitly to ensure consistency
        ViewGroup.LayoutParams gridParams = calendarGrid.getLayoutParams();
        gridParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        gridParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        calendarGrid.setLayoutParams(gridParams);
    }

    // Helper method to add an empty cell
    private void addEmptyCell(int col, int row, int cellSize) {
        TextView dateCell = new TextView(this);
        dateCell.setText("");
        dateCell.setBackgroundColor(Color.TRANSPARENT);

        GridLayout.Spec rowSpec = GridLayout.spec(row);
        GridLayout.Spec colSpec = GridLayout.spec(col);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
        params.width = cellSize;
        params.height = cellSize;
        params.setMargins(1, 1, 1, 1);

        calendarGrid.addView(dateCell, params);
    }

    // Helper method to add a day cell
    private void addDayCell(int col, int row, int day, int cellSize, Calendar today) {
        TextView dateCell = new TextView(this);
        dateCell.setText(String.valueOf(day));
        dateCell.setTextSize(14);
        dateCell.setGravity(Gravity.CENTER);
        dateCell.setBackgroundResource(R.drawable.calendar_day_background);

        GridLayout.Spec rowSpec = GridLayout.spec(row);
        GridLayout.Spec colSpec = GridLayout.spec(col);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
        params.width = cellSize;
        params.height = cellSize;
        params.setMargins(1, 1, 1, 1);

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
        btnContinue.setEnabled(true);
        btnContinue.setAlpha(1.0f);
    }



    private void proceedToServiceSelection() {
        Intent intent = new Intent(this, SelectServiceActivity.class);

        intent.putExtra("SELECTED_DATE", selectedDate);
        Log.d("BookingIntent", "Selected Date: " + selectedDate);
        intent.putExtra("userID", userID);

        if(rewardTitle != null && rewardImageUrl != null)
        {
            intent.putExtra("REWARD_TITLE", rewardTitle);
            intent.putExtra("REWARD_IMAGE_URL", rewardImageUrl);

            // Log values for debugging
            Log.d("BookingIntent", "REWARD_TITLE: " + rewardTitle);
            Log.d("BookingIntent", "REWARD_IMAGE_URL: " + rewardImageUrl);

        }
        if (isAnyTechnician) {
            intent.putExtra("ANY_TECHNICIAN", true);
        } else {
            intent.putExtra("TECHNICIAN_ID", technicianId);
            intent.putExtra("TECHNICIAN_NAME", technicianName);
            intent.putExtra("TECHNICIAN_PRICE", technicianPrice);

            Log.d("BookingIntent", "TECHNICIAN_ID: " + technicianId);
            Log.d("BookingIntent", "TECHNICIAN_NAME: " + technicianName);
            Log.d("BookingIntent", "TECHNICIAN_PRICE: " + technicianPrice);
        }

        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
