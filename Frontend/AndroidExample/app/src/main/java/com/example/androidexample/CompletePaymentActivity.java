package com.example.androidexample;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidUI.AppointmentModel;
import com.example.androidUI.ReceiptServiceAdapter;
import com.example.androidUI.ServiceModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class CompletePaymentActivity extends AppCompatActivity {

    private static final String TAG = "CompletePaymentActivity";
    private static final int REQUEST_PAYMENT_CODE = 103;
    private static final int REQUEST_REVIEW_CODE = 104;

    // UI Components
    private TextView tvDateTime;
    private TextView tvConfirmationNumber;
    private TextView tvPaymentMethod;
    private TextView tvSubtotal;
    private TextView tvTax;
    private TextView tvTotal;
    private RecyclerView recyclerViewServices;
    private Button btnDone;
    private Button btnLeaveReview;

    // Service adapter
    private ReceiptServiceAdapter serviceAdapter;
    private List<ServiceModel> serviceModelList = new ArrayList<>();
    private List<Double> servicePrices = new ArrayList<>();

    // Data from intent
    private AppointmentModel appointment;
    private String paymentMethod;
    private int position;
    private long userId;
    private long employeeId;
    private int[] serviceIds;
    private double tipping = 0.0; // Default tipping amount

    // Service prices mapping to service IDs
    private static final double[] SERVICE_PRICES =
            {
                    0.00,       // Index 0 (not used)
                    100.00,      // Polish Change (ID: 1)
                    110.00,      // Gel Builder (ID: 2)
                    150.00,      // Solar Acrylic (ID: 3)
                    70.00,      // Manicure (ID: 4)
                    50.00,      // Pedicure (ID: 5)
                    120.00,      // Dipping Powder (ID: 6)
                    70.00       // Extras (ID: 7)
            };

    // Service icons mapping to service IDs
    private static final int[] SERVICE_ICONS = {
            R.drawable.ic_launcher_foreground, // Index 0 (not used)
            android.R.drawable.ic_menu_edit,   // Polish Change (ID: 1)
            android.R.drawable.ic_menu_edit,   // Gel Builder (ID: 2)
            android.R.drawable.ic_menu_edit,   // Solar Acrylic (ID: 3)
            android.R.drawable.ic_menu_edit,   // Manicure (ID: 4)
            android.R.drawable.ic_menu_edit,   // Pedicure (ID: 5)
            android.R.drawable.ic_menu_edit,   // Dipping Powder (ID: 6)
            android.R.drawable.ic_menu_edit    // Extras (ID: 7)
    };

    // Add SERVICE_NAMES array
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

    // Tax rate (0%)
    private static final double TAX_RATE = 0.00;

    // Payment successfully processed
    private boolean paymentSuccessful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_payment);

        // Initialize views
        initializeViews();

        // Get data from intent
        getIntentData();

        // Process the payment
        processPayment();

        // Generate the services list from the appointment
        generateServicesList();

        // Set up the RecyclerView for services
        setupRecyclerView();

        // Calculate and display totals
        calculateAndDisplayTotals();

        // Set current date and time
        setCurrentDateTime();

        // Set a confirmation number
        setConfirmationNumber();

        // Add animations for a better user experience
        animateContent();

        // Set button click listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        // Get references to your new UI components
        tvDateTime = findViewById(R.id.tvDateTime);
        tvConfirmationNumber = findViewById(R.id.tvConfirmationNumber);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);
        recyclerViewServices = findViewById(R.id.recyclerViewServices);
        btnDone = findViewById(R.id.btnDone);
        btnLeaveReview = findViewById(R.id.btnLeaveReview);

        // Add back button functionality
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupButtonListeners() {
        // Done button click listener
        btnDone.setOnClickListener(view -> {
            // Create a short fade out animation
            view.animate()
                    .alpha(0.7f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        view.setAlpha(1.0f);

                        // Return result to AllAppointmentsActivity
                        if (paymentSuccessful) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("PAYMENT_SUCCESSFUL", true);
                            resultIntent.putExtra("POSITION", position);
                            setResult(RESULT_OK, resultIntent);
                        }

                        finish(); // Close the activity and return to previous screen
                    })
                    .start();
        });

        // Leave Review button click listener
        btnLeaveReview.setOnClickListener(view -> {
            // Create a short fade out animation
            view.animate()
                    .alpha(0.7f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        view.setAlpha(1.0f);

                        // Launch the ReviewActivity
                        launchReviewActivity();
                    })
                    .start();
        });
    }

    private void launchReviewActivity() {
        if (paymentSuccessful) {
            // Create intent for ReviewActivity
            Intent intent = new Intent(CompletePaymentActivity.this, ReviewActivity.class);

            // Pass necessary data for review
            intent.putExtra("appointmentId", appointment != null ? appointment.getId() : -1);
            intent.putExtra("employeeId", employeeId);
            intent.putExtra("userId", userId);

            // Pass service information
            if (serviceModelList != null && !serviceModelList.isEmpty()) {
                ArrayList<String> serviceNames = new ArrayList<>();
                for (ServiceModel service : serviceModelList) {
                    serviceNames.add(service.getTitle()); // Use getTitle() instead of getName()
                }
                intent.putExtra("serviceNames", serviceNames);
            }

            intent.putExtra("serviceIds", serviceIds);

            // Pass technician name if available in appointment
            if (appointment != null) {
                intent.putExtra("technicianName", appointment.getEmployeeName());
                intent.putExtra("appointmentDate", appointment.getDate());
                intent.putExtra("appointmentTime", appointment.getTime());
                intent.putExtra("duration", appointment.getDuration());
            } else {
                // Default values if appointment is null
                intent.putExtra("technicianName", "Technician");

                // Get current date and time if appointment is null
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
                Date now = new Date();
                intent.putExtra("appointmentDate", dateFormat.format(now));
                intent.putExtra("appointmentTime", timeFormat.format(now));
                intent.putExtra("duration", 60); // Default duration in minutes
            }

            // Set payment status to true since payment is complete
            intent.putExtra("isPaid", true);

            // Start ReviewActivity for result
            startActivityForResult(intent, REQUEST_REVIEW_CODE);
        } else {
            // If payment wasn't successful, show an error message
            Toast.makeText(this, "Please complete payment before leaving a review", Toast.LENGTH_SHORT).show();
        }
    }

    private void getIntentData() {
        // Get appointment data
        appointment = (AppointmentModel) getIntent().getSerializableExtra("appointment");

        // Get payment method
        paymentMethod = getIntent().getStringExtra("paymentMethod");
        if (paymentMethod == null) {
            paymentMethod = "Credit Card"; // Default value
        }

        // Get position
        position = getIntent().getIntExtra("position", -1);

        // Get user ID
        userId = getIntent().getLongExtra("userId", 1);

        // Get employee ID
        employeeId = getIntent().getLongExtra("employeeId", 1);

        // Get service IDs
        serviceIds = getIntent().getIntArrayExtra("serviceIds");
        if (serviceIds == null) {
            // Default to some basic services if not provided
            serviceIds = new int[]{1, 4}; // Polish Change and Manicure
        }

        // Set the payment method in the UI
        tvPaymentMethod.setText(paymentMethod);

        Log.d(TAG, "Received payment method: " + paymentMethod);
        Log.d(TAG, "Received service IDs: " + Arrays.toString(serviceIds));
        Log.d(TAG, "Received employee ID: " + employeeId);
        Log.d(TAG, "Received user ID: " + userId);
        Log.d(TAG, "Received position: " + position);
        Log.d(TAG, "Received appointment: " + appointment);
    }

    private String convertPaymentMethodForApi(String paymentMethod) {
        if (paymentMethod == null) {
            return "CREDIT_CARD"; // Default value
        }

        // Convert to uppercase for comparison
        String method = paymentMethod.toUpperCase();

        // Map user-friendly text to API enum values
        switch (method) {
            case "CREDIT/DEBIT CARD":
            case "CREDIT CARD":
            case "DEBIT CARD":
            case "CARD":
                return "CREDIT_CARD";

            case "CASH":
                return "CASH";

            case "MOBILE PAYMENT":
            case "MOBILE":
            case "PHONE PAYMENT":
                return "MOBILE_PAYMENT";

            case "BANK TRANSFER":
            case "TRANSFER":
            case "WIRE TRANSFER":
                return "BANK_TRANSFER";

            default:
                // If nothing matches, default to CREDIT_CARD
                return "CREDIT_CARD";
        }
    }

    private void processPayment() {
        // Create payment URL
        String url = "http://coms-3090-020.class.las.iastate.edu:8080/api/payment/pay";

        try
        {
            JSONObject paymentData = new JSONObject();

            // Add user ID
            paymentData.put("userId", userId);

            // Add service IDs as JSON array
            JSONArray serviceIdArray = new JSONArray();
            for (int serviceId : serviceIds) {
                serviceIdArray.put(serviceId);
            }
            paymentData.put("serviceId", serviceIdArray);

            // Add other required fields
            paymentData.put("couponCode", ""); // Empty coupon code
            paymentData.put("paymentMethod", convertPaymentMethodForApi(paymentMethod));
            paymentData.put("employeeId", employeeId);
            paymentData.put("tipping", tipping);

            Log.d(TAG, "Payment data: " + paymentData.toString());
            Log.d(TAG, "Payment URL: " + url);
            Log.d(TAG, "Payment method: " + paymentMethod);
            Log.d(TAG, "Service IDs: " + Arrays.toString(serviceIds));
            Log.d(TAG, "Employee ID: " + employeeId);
            Log.d(TAG, "User ID: " + userId);
            Log.d(TAG, "Tipping: " + tipping);

            // Create and send the request
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    paymentData,
                    response -> {
                        // Handle success
                        Log.d(TAG, "Payment successful: " + response.toString());

                        // Update payment status
                        paymentSuccessful = true;

                        // If we have an appointment object, update its status
                        if (appointment != null) {
                            appointment.setStatus("Paid");
                        }

                        // Extract confirmation number if available
                        try {
                            if (response.has("confirmationNumber")) {
                                int confirmationNumber = response.getInt("confirmationNumber");
                                tvConfirmationNumber.setText(String.valueOf(confirmationNumber));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing confirmation number: " + e.getMessage());
                        }

                        // Show success message
                        Toast.makeText(this, "Payment processed successfully", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        // Handle error
                        Log.e(TAG, "Payment failed: " + error.toString());

                        // Show error message
                        Toast.makeText(this, "Payment processing failed: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();

                        // We'll still show the receipt, but mark that payment wasn't successful
                        paymentSuccessful = false;
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
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

        } catch (JSONException e) {
            Log.e(TAG, "Error creating payment JSON: " + e.getMessage());
            Toast.makeText(this, "Error processing payment", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateServicesList() {
        serviceModelList.clear();
        servicePrices.clear();

        if (serviceIds != null && serviceIds.length > 0) {
            // Create service models from service IDs
            for (int serviceId : serviceIds) {
                // Get service name and price
                String serviceName = getServiceName(serviceId);
                double price = getServicePrice(serviceId);

                // Create a ServiceModel
                ServiceModel serviceModel = new ServiceModel(
                        serviceId,
                        serviceName,
                        "Service", // Short description
                        "Service details", // Long description
                        getServiceIcon(serviceId)
                );

                // Add to the lists
                serviceModelList.add(serviceModel);
                servicePrices.add(price);
            }
        } else if (appointment != null) {
            // If we have an appointment but no service IDs, use service names from appointment
            List<String> serviceNames = appointment.getServiceNames();

            if (serviceNames != null && !serviceNames.isEmpty()) {
                for (String serviceName : serviceNames) {
                    // Find the service ID and price based on name
                    int serviceId = getServiceIdFromName(serviceName);
                    double price = getServicePrice(serviceId);

                    // Create a ServiceModel
                    ServiceModel serviceModel = new ServiceModel(
                            serviceId,
                            serviceName,
                            "Service", // Short description
                            "Service details", // Long description
                            getServiceIcon(serviceId)
                    );

                    // Add to the lists
                    serviceModelList.add(serviceModel);
                    servicePrices.add(price);
                }
            }
        }

        // If no services were added (fallback)
        if (serviceModelList.isEmpty()) {
            // Add default services
            ServiceModel manicure = new ServiceModel(
                    4, // Manicure ID
                    "Manicure",
                    "Basic manicure service",
                    "Complete professional manicure service",
                    getServiceIcon(4)
            );
            ServiceModel polishChange = new ServiceModel(
                    1, // Polish Change ID
                    "Polish Change",
                    "Quick polish change",
                    "Quick change of nail polish",
                    getServiceIcon(1)
            );

            serviceModelList.add(manicure);
            serviceModelList.add(polishChange);

            servicePrices.add(SERVICE_PRICES[4]); // Manicure price
            servicePrices.add(SERVICE_PRICES[1]); // Polish Change price
        }
    }

    private String getServiceName(int serviceId) {
        // Helper method to get service name from ID
        if (serviceId >= 1 && serviceId < SERVICE_NAMES.length) {
            return SERVICE_NAMES[serviceId];
        } else {
            return "Unknown Service";
        }
    }

    private int getServiceIdFromName(String serviceName) {
        // Match service name to ID
        for (int i = 1; i < SERVICE_NAMES.length; i++) {
            if (SERVICE_NAMES[i].equals(serviceName)) {
                return i;
            }
        }

        // Default to Manicure if not found
        return 4;
    }

    private double getServicePrice(int serviceId) {
        // Get price based on service ID
        if (serviceId >= 1 && serviceId < SERVICE_PRICES.length) {
            return SERVICE_PRICES[serviceId];
        } else {
            // For unknown service IDs, return a default price
            return 20.00;
        }
    }

    private int getServiceIcon(int serviceId) {
        // Get icon resource ID based on service ID
        if (serviceId >= 1 && serviceId < SERVICE_ICONS.length) {
            return SERVICE_ICONS[serviceId];
        } else {
            // For unknown service IDs, return a default icon
            return android.R.drawable.ic_menu_edit;
        }
    }

    private void setupRecyclerView() {
        serviceAdapter = new ReceiptServiceAdapter(this, serviceModelList, servicePrices);
        recyclerViewServices.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewServices.setAdapter(serviceAdapter);
    }

    private void calculateAndDisplayTotals() {
        double subtotal = 0.0;

        // Calculate subtotal from service prices
        for (Double price : servicePrices) {
            subtotal += price;
        }

        // Calculate tax
        double tax = subtotal * TAX_RATE;

        // Calculate total (including tip)
        double total = subtotal + tax + tipping;

        // Display values with animation
        animateTextView(tvSubtotal, 0, (float) subtotal, String.format(Locale.US, "$%.2f", subtotal));
        animateTextView(tvTax, 0, (float) tax, String.format(Locale.US, "$%.2f", tax));
        animateTextView(tvTotal, 0, (float) total, String.format(Locale.US, "$%.2f", total));
    }

    private void animateTextView(final TextView textView, float startValue, float endValue, final String finalText) {
        ValueAnimator animator = ValueAnimator.ofFloat(startValue, endValue);
        animator.setDuration(1000); // 1 second duration
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            textView.setText(String.format(Locale.US, "$%.2f", animatedValue));
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                textView.setText(finalText); // Set the final text
            }
        });

        animator.start();
    }

    private void setCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.US);
        String currentDateTime = dateFormat.format(new Date());
        tvDateTime.setText(currentDateTime);
    }

    private void setConfirmationNumber() {
        // If appointment has a confirmation number, use that
        if (appointment != null && appointment.getNotes() != null &&
                appointment.getNotes().contains("Confirmation #:")) {
            String confirmationText = appointment.getNotes();
            String confirmationNumber = confirmationText.substring(
                    confirmationText.indexOf(":") + 1).trim();
            tvConfirmationNumber.setText(confirmationNumber);
        } else {
            // Generate a random 5-digit confirmation number
            Random random = new Random();
            int confirmationNumber = 10000 + random.nextInt(90000);
            tvConfirmationNumber.setText(String.valueOf(confirmationNumber));
        }
    }

    private void animateContent() {
        // Animate the receipt card to fade in with scale
        View receiptCard = findViewById(R.id.receiptCard);
        receiptCard.setAlpha(0f);
        receiptCard.setScaleX(0.9f);
        receiptCard.setScaleY(0.9f);
        receiptCard.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Animate the services card with a slight delay
        View servicesCard = findViewById(R.id.servicesCard);
        servicesCard.setAlpha(0f);
        servicesCard.setTranslationY(50f);
        servicesCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(200)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Animate the summary card with a slight delay
        View summaryCard = findViewById(R.id.summaryCard);
        summaryCard.setAlpha(0f);
        summaryCard.setTranslationY(50f);
        summaryCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(300)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Animate the buttons
        btnDone.setAlpha(0f);
        btnDone.setTranslationY(50f);
        btnDone.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(600)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        btnLeaveReview.setAlpha(0f);
        btnLeaveReview.setTranslationY(50f);
        btnLeaveReview.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(600)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle result from ReviewActivity
        if (requestCode == REQUEST_REVIEW_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                boolean reviewSubmitted = data.getBooleanExtra("REVIEW_SUBMITTED", false);
                if (reviewSubmitted) {
                    // Show a toast message that review was submitted successfully
                    Toast.makeText(this, "Thank you for your review!", Toast.LENGTH_SHORT).show();

                    // Disable the review button to prevent multiple reviews
                    btnLeaveReview.setEnabled(false);
                    btnLeaveReview.setAlpha(0.5f);
                    btnLeaveReview.setText("Review Submitted");
                }
            }
        }
    }
}