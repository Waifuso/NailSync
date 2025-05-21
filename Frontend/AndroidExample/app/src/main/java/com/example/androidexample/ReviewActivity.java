package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidUI.ReviewPagerAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    // Tag for logging
    private static final String TAG = "ReviewActivity";

    // UI Components
    private ViewPager2 viewPager;
    private MaterialButton btnBack;
    private MaterialButton btnNext;
    private View dot1, dot2, dot3;
    private ProgressBar progressBar;

    // Data
    private long appointmentId;
    private long employeeId;
    private long userId;
    private ArrayList<String> serviceNames;
    private int[] serviceIds;
    private String technicianName;
    private String appointmentDate;
    private String appointmentTime;
    private int duration;
    private boolean isPaid = false; // Payment status flag

    // Review data
    private float rating = 0;
    private List<String> selectedPositives = new ArrayList<>();
    private String reviewText = "";

    // Page adapter
    private ReviewPagerAdapter pagerAdapter;

    private ImageButton backButton;

    // Constants
    private static final int PAYMENT_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Initialize views
        initializeViews();

        // Extract intent data
        extractIntentData();

        // Setup payment required UI
        setupPaymentRequiredUI();

        // Check payment status
        checkPaymentStatus();

        // Setup ViewPager (this will only initialize review UI if payment is completed)
        setupViewPager();

        // Setup listeners
        setupListeners();
    }

    private void initializeViews() {
        // Initialize the back button
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Initialize navigation components
        viewPager = findViewById(R.id.viewPager);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        // Initialize dots
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        // Initialize progress bar
        progressBar = findViewById(R.id.progressBar);

        // Hide back button initially
        btnBack.setVisibility(View.INVISIBLE);
    }

    private void extractIntentData() {
        Intent intent = getIntent();

        appointmentId = intent.getLongExtra("appointmentId", -1);
        employeeId = intent.getLongExtra("employeeId", -1);
        userId = intent.getLongExtra("userId", -1);
        serviceNames = intent.getStringArrayListExtra("serviceNames");
        serviceIds = intent.getIntArrayExtra("serviceIds");
        technicianName = intent.getStringExtra("technicianName");
        appointmentDate = intent.getStringExtra("appointmentDate");
        appointmentTime = intent.getStringExtra("appointmentTime");
        duration = intent.getIntExtra("duration", 60);
        isPaid = intent.getBooleanExtra("isPaid", false); // Get payment status

        // Log received data for debugging
        Log.d(TAG, "Received data - appointmentId: " + appointmentId +
                ", employeeId: " + employeeId +
                ", userId: " + userId +
                ", serviceIds: " + (serviceIds != null ? serviceIds.length : 0) +
                " services, isPaid: " + isPaid);
    }

    private void setupPaymentRequiredUI() {
        // Get button reference
        Button btnGoToPayment = findViewById(R.id.btnGoToPayment);
        if (btnGoToPayment == null) {
            Log.e(TAG, "btnGoToPayment not found in layout");
            return;
        }

        // Set click listener
        btnGoToPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to launch payment activity
                Intent intent = new Intent(ReviewActivity.this, CompletePaymentActivity.class);

                // Pass appointment information
                intent.putExtra("appointmentId", appointmentId);
                intent.putExtra("userId", userId);
                intent.putExtra("employeeId", employeeId);
                intent.putExtra("technicianName", technicianName);
                intent.putExtra("appointmentDate", appointmentDate);
                intent.putExtra("appointmentTime", appointmentTime);

                // Pass service information
                intent.putExtra("serviceNames", serviceNames);
                intent.putExtra("serviceIds", serviceIds);

                // Start payment activity for result to know when payment is completed
                startActivityForResult(intent, PAYMENT_REQUEST_CODE);
            }
        });
    }

    private void checkPaymentStatus() {
        // If we didn't get payment status from intent, check from API
        if (!isPaid) {
            // Show progress while checking
            showProgress(true);

            // API endpoint to check payment status
            String url = "http://coms-3090-020.class.las.iastate.edu:8080/api/appointments/" + appointmentId;

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            showProgress(false);
                            try {
                                // Check if appointment is paid
                                isPaid = response.getBoolean("isPaid");
                                Log.d(TAG, "Payment status from API: " + isPaid);

                                // Update UI based on payment status
                                updateUIBasedOnPaymentStatus();

                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing payment status: " + e.getMessage());
                                // Default to false if can't determine
                                isPaid = false;
                                updateUIBasedOnPaymentStatus();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            showProgress(false);
                            Log.e(TAG, "Error checking payment status: " + error.toString());
                            // Default to false if can't determine
                            isPaid = false;
                            updateUIBasedOnPaymentStatus();
                        }
                    }
            );

            // Add request to queue
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
        } else {
            // If we already have payment status, update UI accordingly
            updateUIBasedOnPaymentStatus();
        }
    }

    private void updateUIBasedOnPaymentStatus() {
        // Get review UI elements
        View reviewControls = findViewById(R.id.reviewControls);
        View paymentRequired = findViewById(R.id.paymentRequiredMessage);

        if (reviewControls == null) {
            Log.e(TAG, "reviewControls not found in layout");
            return;
        }

        if (isPaid) {
            // Show review controls
            reviewControls.setVisibility(View.VISIBLE);

            // Hide payment required message if exists
            if (paymentRequired != null) {
                paymentRequired.setVisibility(View.GONE);
            }
        } else {
            // Hide review controls
            reviewControls.setVisibility(View.GONE);

            // Show payment required message if exists
            if (paymentRequired != null) {
                paymentRequired.setVisibility(View.VISIBLE);
            } else {
                // If payment message view doesn't exist, show a toast
                Toast.makeText(this,
                        "Payment required before submitting review",
                        Toast.LENGTH_LONG).show();

                // Finish activity after delay
                new android.os.Handler().postDelayed(
                        () -> finish(),
                        2000);
            }
        }
    }

    private void setupViewPager() {
        // Only proceed with setting up review flow if paid
        if (!isPaid) {
            return;
        }

        // Create adapter
        pagerAdapter = new ReviewPagerAdapter(this, technicianName, appointmentDate,
                appointmentTime, duration, serviceNames);

        // Set adapter
        viewPager.setAdapter(pagerAdapter);

        // Disable swiping
        viewPager.setUserInputEnabled(false);

        // Set page change listener
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                updateNavButtons(position);

                // Update data based on position
                if (position == 1) {
                    // Get rating from adapter before moving to page 2
                    rating = pagerAdapter.getRating();
                    Log.d(TAG, "Moving to page 2, rating: " + rating);
                } else if (position == 2) {
                    // Update data from stage 2 before moving to page 3
                    validateStage2(); // This captures the review text
                    updateStage3WithPreviousData();
                    Log.d(TAG, "Moving to page 3, review text: " + reviewText);
                }
            }
        });
    }

    private void updateDots(int position) {
        dot1.setBackground(getDrawable(position == 0 ? R.drawable.dot_active : R.drawable.dot_inactive));
        dot2.setBackground(getDrawable(position == 1 ? R.drawable.dot_active : R.drawable.dot_inactive));
        dot3.setBackground(getDrawable(position == 2 ? R.drawable.dot_active : R.drawable.dot_inactive));
    }

    private void updateNavButtons(int position) {
        btnBack.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);

        if (position == 2) {
            btnNext.setVisibility(View.GONE);
        } else {
            btnNext.setVisibility(View.VISIBLE);
            btnNext.setText(position == 1 ? "Review" : "Next");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            int currentPosition = viewPager.getCurrentItem();
            if (currentPosition > 0) {
                viewPager.setCurrentItem(currentPosition - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            int currentPosition = viewPager.getCurrentItem();

            if (currentPosition == 0) {
                // Validate first page
                if (validateStage1()) {
                    viewPager.setCurrentItem(1);
                }
            } else if (currentPosition == 1) {
                // Validate second page
                if (validateStage2()) {
                    viewPager.setCurrentItem(2);
                }
            }
        });
    }

    private boolean validateStage1() {
        // Get the current rating from the adapter
        rating = pagerAdapter.getRating();

        // Get selected positives from stage 1 view
        View stageView = pagerAdapter.getStageView(0);
        if (stageView == null) {
            Log.e(TAG, "Stage 1 view is null");
            return false;
        }

        ChipGroup chipGroup = stageView.findViewById(R.id.chipGroupWhatWentWell);
        if (chipGroup == null) {
            Log.e(TAG, "ChipGroup not found in stage 1");
            return false;
        }

        // Clear previous selections
        selectedPositives.clear();

        // Add currently selected chips
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedPositives.add(chip.getText().toString());
            }
        }

        // Check if rating is provided
        if (rating == 0) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            return false;
        }

        Log.d(TAG, "Stage 1 validated with rating: " + rating +
                " and " + selectedPositives.size() + " positives selected");
        return true;
    }

    private boolean validateStage2() {
        // Get review text from stage 2
        View stageView = pagerAdapter.getStageView(1);
        if (stageView == null) {
            Log.e(TAG, "Stage 2 view is null in validateStage2");
            return false;
        }

        TextInputEditText etReviewText = stageView.findViewById(R.id.etReviewText);
        if (etReviewText == null) {
            Log.e(TAG, "Review text field not found in stage 2");
            // Allow to proceed with empty review
            reviewText = "";
            return true;
        }

        // Get the text from the edit text
        reviewText = etReviewText.getText() != null ?
                etReviewText.getText().toString().trim() : "";

        // Check if review text exceeds character limit
        if (reviewText.length() > 80) {
            Toast.makeText(this, "Review must be 80 characters or less", Toast.LENGTH_SHORT).show();
            return false;
        }

        Log.d(TAG, "Stage 2 validated with review text: " + reviewText);
        return true;
    }

    private void submitReview() {
        // Show progress
        showProgress(true);

        try {
            // Choose one of these URL formats based on your API requirements:

            // Option 1: Using path parameters
            // URL encode the review text to make it URL-safe
            String encodedReviewText = java.net.URLEncoder.encode(reviewText, "UTF-8");
            String url = "http://coms-3090-020.class.las.iastate.edu:8080/api/rating/rate/"
                    + (int)rating  + "/" + encodedReviewText + "/" + appointmentId;
            // Log the URL for debugging
            Log.d(TAG, "Submitting review to URL: " + url);

            // Create JSON request body
            JSONObject requestBody = new JSONObject();
            // For Option 1 (path parameters), you can leave this empty
            // For Option 2 (JSON body), uncomment these:
            // requestBody.put("star", (int) rating);
            // requestBody.put("comment", reviewText);

            Log.d(TAG, "Request body: " + requestBody.toString());

            // Create Volley request
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            showProgress(false);
                            Log.d(TAG, "Received response: " + response.toString());

                            try {
                                // Get response message
                                String message = response.getString("message");
                                Toast.makeText(ReviewActivity.this, message, Toast.LENGTH_SHORT).show();

                                // Set result and finish
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("REVIEW_SUBMITTED", true);
                                resultIntent.putExtra("APPOINTMENT_ID", appointmentId);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing response: " + e.getMessage());

                                // Default success message
                                Toast.makeText(ReviewActivity.this,
                                        "Review submitted successfully!",
                                        Toast.LENGTH_SHORT).show();

                                // Set result and finish
                                setResult(RESULT_OK);
                                finish();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            showProgress(false);

                            // Log error details
                            Log.e(TAG, "Error submitting review: " + error.toString());
                            if (error.networkResponse != null) {
                                Log.e(TAG, "Status code: " + error.networkResponse.statusCode);
                                try {
                                    String responseBody = new String(error.networkResponse.data, "utf-8");
                                    Log.e(TAG, "Error response: " + responseBody);
                                } catch (Exception e) {
                                    Log.e(TAG, "Could not parse error response");
                                }
                            }

                            // Create error message
                            String errorMessage = "Failed to submit review";
                            if (error.networkResponse != null) {
                                errorMessage += " (Error " + error.networkResponse.statusCode + ")";
                            }

                            // Show error message
                            Toast.makeText(ReviewActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
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
                    30000, // 30 seconds timeout (increased from 10 seconds)
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            // Add the request to the RequestQueue
            Log.d(TAG, "Adding request to queue");
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);

        } catch (Exception e) {
            showProgress(false);
            Log.e(TAG, "Error in submitReview: " + e.getMessage(), e);
            Toast.makeText(this, "Error preparing review data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnNext.setEnabled(!show);
        btnBack.setEnabled(!show);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if this is the result from payment activity
        if (requestCode == PAYMENT_REQUEST_CODE) {
            // Check if payment was successful
            if (resultCode == RESULT_OK && data != null) {
                // Extract payment success from intent
                boolean paymentSuccess = data.getBooleanExtra("PAYMENT_SUCCESSFUL", false);

                if (paymentSuccess) {
                    // Update payment status
                    isPaid = true;

                    // Update UI based on new payment status
                    updateUIBasedOnPaymentStatus();

                    // Setup the ViewPager now that payment is completed
                    setupViewPager();
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void updateStage3WithPreviousData() {
        // Get the stage view from the adapter
        View stageView = pagerAdapter.getStageView(2);

        // Check if view is null before trying to access its elements
        if (stageView == null) {
            Log.e(TAG, "Stage 3 view is null in updateStage3WithPreviousData");
            return;
        }

        try {
            // Set technician name
            TextView tvTechnicianNameSummary = stageView.findViewById(R.id.tvTechnicianNameSummary);
            if (tvTechnicianNameSummary != null) {
                tvTechnicianNameSummary.setText(technicianName);
            }

            // Set services
            TextView tvServicesSummary = stageView.findViewById(R.id.tvServicesSummary);
            if (tvServicesSummary != null) {
                if (serviceNames != null && !serviceNames.isEmpty()) {
                    StringBuilder servicesText = new StringBuilder();
                    for (int i = 0; i < serviceNames.size(); i++) {
                        servicesText.append(serviceNames.get(i));
                        if (i < serviceNames.size() - 1) {
                            servicesText.append(", ");
                        }
                    }
                    tvServicesSummary.setText(servicesText.toString());
                } else {
                    tvServicesSummary.setText("No services listed");
                }
            }

            // Set rating
            RatingBar ratingBarSummary = stageView.findViewById(R.id.ratingBarSummary);
            if (ratingBarSummary != null) {
                ratingBarSummary.setRating(rating);
            }

            // Add selected positives as chips
            ChipGroup chipGroup = stageView.findViewById(R.id.chipGroupPositivesSummary);
            if (chipGroup != null) {
                chipGroup.removeAllViews();

                for (String positive : selectedPositives) {
                    Chip chip = new Chip(this);
                    chip.setText(positive);
                    chip.setChipBackgroundColorResource(R.color.light_purple);
                    chip.setTextColor(getResources().getColor(R.color.purple_primary));
                    chipGroup.addView(chip);
                }
            }

            // Set review text
            TextView tvReviewTextSummary = stageView.findViewById(R.id.tvReviewTextSummary);
            if (tvReviewTextSummary != null) {
                tvReviewTextSummary.setText(reviewText.isEmpty() ? "No additional comments." : reviewText);
            }

            // Set submit button listener
            MaterialButton btnSubmitReview = stageView.findViewById(R.id.btnSubmitReview);
            if (btnSubmitReview != null) {
                btnSubmitReview.setOnClickListener(v -> submitReview());
            } else {
                Log.e(TAG, "Submit Review button not found in stage 3");
            }

            // Set edit review listener
            TextView tvEditReview = stageView.findViewById(R.id.tvEditReview);
            if (tvEditReview != null) {
                tvEditReview.setOnClickListener(v -> viewPager.setCurrentItem(1));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in updateStage3WithPreviousData: " + e.getMessage());
        }
    }
}