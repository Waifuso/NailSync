package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingConfirmationActivity extends AppCompatActivity {

    private static final String TAG = "BookingConfirmation";
    private TextView tvConfirmationTitle;
    private TextView tvConfirmationMessage;
    private Button btnBackToHome;
    private long userID = -1;

    private String rewardTitle, rewardImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        // Find views
        tvConfirmationTitle = findViewById(R.id.tvConfirmationTitle);
        tvConfirmationMessage = findViewById(R.id.tvConfirmationMessage);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        // Get userID from intent
        userID = getIntent().getLongExtra("userID", -1);
        Log.d(TAG, "Received userID: " + userID);

        try {
            rewardTitle = getIntent().getStringExtra("REWARD_TITLE");
            rewardImageUrl = getIntent().getStringExtra("REWARD_IMAGE_URL");

            // log for debugging
            Log.d(TAG, "Received reward title: " + rewardTitle);
            Log.d(TAG, "Received reward image URL: " + rewardImageUrl);
        }
        catch (Exception e) {
            Log.d(TAG, "No reward or discount used");
            // do nothing
        }

        // Get booking details from intent
        String bookingDetailsStr = getIntent().getStringExtra("bookingDetails");

        // Get additional data passed directly
        int[] selectedServices = getIntent().getIntArrayExtra("SELECTED_SERVICES");
        String technicianName = getIntent().getStringExtra("TECHNICIAN_NAME");

        if (bookingDetailsStr != null && !bookingDetailsStr.isEmpty()) {
            try {
                // Parse booking details
                JSONObject bookingDetails = new JSONObject(bookingDetailsStr);

                // Format date and time
                String dateStr = bookingDetails.getString("date");
                String timeStr = bookingDetails.getString("time");

                // Format the date for display
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateStr);
                String formattedDate = outputFormat.format(date);

                // Process selected services
                StringBuilder servicesString = new StringBuilder();

                if (selectedServices != null && selectedServices.length > 0) {
                    for (int i = 0; i < selectedServices.length; i++) {
                        int serviceId = selectedServices[i];
                        String serviceName = getServiceName(serviceId);
                        servicesString.append(serviceName);
                        if (i < selectedServices.length - 1) {
                            servicesString.append(", ");
                        }
                    }
                } else {
                    try {
                        // Try to extract services from bookingDetails
                        JSONArray serviceArray = bookingDetails.getJSONArray("serviceId");
                        for (int i = 0; i < serviceArray.length(); i++) {
                            int serviceId = serviceArray.getInt(i);
                            String serviceName = getServiceName(serviceId);
                            servicesString.append(serviceName);
                            if (i < serviceArray.length() - 1) {
                                servicesString.append(", ");
                            }
                        }
                    } catch (JSONException e) {
                        servicesString.append("Services not specified");
                    }
                }

                // Get technician info
                String technicianInfo = "";
                if (technicianName != null && !technicianName.isEmpty()) {
                    technicianInfo = "Technician: " + technicianName;
                } else if (bookingDetails.has("employeeId")) {
                    technicianInfo = "Technician ID: " + bookingDetails.getLong("employeeId");
                } else {
                    technicianInfo = "Technician: Not specified";
                }

                // Create confirmation message
                String confirmationMessage = String.format(
                        "Your appointment is confirmed!\n\n" +
                                "Date: %s\n" +
                                "Time: %s\n\n" +
                                "Services: %s\n\n" +
                                "%s",
                        formattedDate,
                        timeStr,
                        servicesString.toString(),
                        technicianInfo
                );

                // Set confirmation message
                tvConfirmationMessage.setText(confirmationMessage);
                tvConfirmationTitle.setText("Booking Confirmed!");

            } catch (JSONException | ParseException e) {
                e.printStackTrace();
                tvConfirmationMessage.setText("Booking details could not be processed.");
                tvConfirmationTitle.setText("Something went wrong");
            }
        } else {
            // No booking details passed
            showNoBookingsMessage();
        }

        // Back to home button
        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, ApplicationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("userID", userID);

            if (rewardTitle != null && rewardImageUrl != null) {
                intent.putExtra("REWARD_TITLE", rewardTitle);
                intent.putExtra("REWARD_IMAGE_URL", rewardImageUrl);
            }

            // Logging values
            Log.d(TAG, "userID: " + userID);
            Log.d(TAG, "REWARD_TITLE: " + rewardTitle);
            Log.d(TAG, "REWARD_IMAGE_URL: " + rewardImageUrl);

            startActivity(intent);
            finish();
        });
    }

    private void showNoBookingsMessage() {
        tvConfirmationTitle.setText("No Bookings");
        tvConfirmationMessage.setText("No appointments booked.");
    }

    // Helper method to get service name from service ID
    private String getServiceName(int serviceId) {
        // Array of service names (matches the SERVICE_NAMES array in SelectBookingTimeActivity)
        String[] SERVICE_NAMES = {
                "", // Empty element at index 0 (not used)
                "Polish Change",
                "Gel Builder",
                "Solar Acrylic",
                "Manicure",
                "Pedicure",
                "Dipping Powder",
                "Extras"
        };

        if (serviceId >= 1 && serviceId < SERVICE_NAMES.length) {
            return SERVICE_NAMES[serviceId];
        } else {
            return "Unknown Service";
        }
    }
}