package com.example.androidexample;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidUI.RewardsService;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class RewardDetailActivity extends AppCompatActivity {

    // Result constants
    public static final String RESULT_POINTS_UPDATED = "points_updated";
    public static final String RESULT_REWARD_REDEEMED = "reward_redeemed";
    public static final String EXTRA_USER_POINTS = "userPoints";
    public static final String EXTRA_USER_ID = "userID";
    public static final String EXTRA_USERNAME = "username";

    // UI Components
    private ImageView rewardImage;
    private TextView pointsBadge;
    private TextView rewardDescription;
    private TextView termsConditions;
    private TextView pointsRequired;
    private TextView expiryDate;
    private TextView validLocations;
    private Button redeemButton;
    private ImageButton backButton;
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView userPointsDisplay;

    // Data
    private String rewardTitle;
    private String rewardDesc;
    private int rewardPoints;
    private String rewardExpiry;
    private String rewardImageUrl;
    private String existingRewardTitle = null;
    private int rewardImageResId = -1;
    private int userPoints;
    private boolean rewardRedeemed = false;
    private String username = "";
    private long userID = -1;

    // Service
    private RewardsService rewardsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_details);

        // Initialize service
        rewardsService = new RewardsService(this);

        // Get data from intent
        getIntentData();

        // Initialize views
        initializeViews();

        // Set up data
        setupData();

        // Set up click listeners
        setupClickListeners();

        // Check if user can redeem
        checkRedeemability();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishWithResult();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finishWithResult();
        return true;
    }

    /**
     * AsyncTask to load images from URLs
     */
    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private static final String TAG = "LoadImageTask";
        private final ImageView imageView;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String imageUrl = params[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                Log.d(TAG, "Image loaded successfully from: " + imageUrl);
            } catch (Exception e) {
                Log.e(TAG, "Error loading image from URL: " + imageUrl, e);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            } else {
                // Set placeholder if loading failed
                imageView.setImageResource(R.drawable.reward_placeholder);
            }
        }
    }
    private void updateUserPoints(long userId, int newPointsValue) {
        // Show loading
        View loadingView = findViewById(R.id.loadingView);
        if (loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
        }

        // Calculate the points being spent
        int pointsToRedeem = userPoints - newPointsValue;

        // Create the URL with userID as parameter
        String url = "http://coms-3090-020.class.las.iastate.edu:8080/api/coupon/send/" + userId + "/" + pointsToRedeem + "/SPRING25";

        // Add debug logging
        Log.d("RewardDetailActivity", "Redemption URL: " + url);
        Log.d("RewardDetailActivity", "Points being redeemed: " + pointsToRedeem);
        Log.d("RewardDetailActivity", "New points will be: " + newPointsValue);

        // Create the JSON body with the new points value
        JSONObject jsonBody = new JSONObject();
        try
        {
            jsonBody.put("userPoints", newPointsValue);
        }
        catch (JSONException e)
        {
            Log.e("RewardDetailActivity", "Error creating JSON body: " + e.getMessage());

            // Hide loading
            if (loadingView != null) {
                loadingView.setVisibility(View.GONE);
            }

            // Show error message
            Toast.makeText(this, "Error updating points: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the PUT request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT, // Changed from PUT to GET based on your URL structure
                url,
                null, // No request body needed for GET
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("RewardDetailActivity", "Response: " + response.toString());
                        try {
                            // Parse the response message
                            String message = response.getString("message");

                            if (message.contains("Successfully redeemed")) {
                                // Update local user points
                                userPoints = newPointsValue;

                                // Flag that a redemption occurred
                                rewardRedeemed = true;

                                // Update the points display
                                if (userPointsDisplay != null) {
                                    userPointsDisplay.setText(String.format("Your Points: %,d", userPoints));
                                }

                                // Show success message
                                Toast.makeText(RewardDetailActivity.this,
                                        message,
                                        Toast.LENGTH_SHORT).show();

                                // Disable redeem button
                                redeemButton.setEnabled(false);
                                redeemButton.setText("Redeemed");
                                redeemButton.setBackgroundResource(R.drawable.rounded_button_disabled);

                                // Hide loading
                                if (loadingView != null) {
                                    loadingView.setVisibility(View.GONE);
                                }

                                // Finish after delay to return to the DiscountsActivity with updated points
                                new Handler().postDelayed(() -> finishWithResult(), 2000);
                            } else {
                                // Show message from server
                                Toast.makeText(RewardDetailActivity.this,
                                        message,
                                        Toast.LENGTH_SHORT).show();

                                // Hide loading
                                if (loadingView != null) {
                                    loadingView.setVisibility(View.GONE);
                                }
                            }
                        } catch (JSONException e) {
                            Log.e("RewardDetailActivity", "Error parsing response: " + e.getMessage());

                            // Hide loading
                            if (loadingView != null) {
                                loadingView.setVisibility(View.GONE);
                            }

                            // Show error message
                            Toast.makeText(RewardDetailActivity.this,
                                    "Error processing server response",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("RewardDetailActivity", "Volley Error: " + error.toString());

                        // Hide loading
                        if (loadingView != null) {
                            loadingView.setVisibility(View.GONE);
                        }

                        // Get error message
                        String errorMessage = "Network error";
                        if (error.networkResponse != null) {
                            errorMessage += " (Code: " + error.networkResponse.statusCode + ")";
                        }

                        // Show error message
                        Toast.makeText(RewardDetailActivity.this,
                                "Failed to update points: " + errorMessage,
                                Toast.LENGTH_SHORT).show();

                        // FALLBACK: Update local points anyway for demo purposes
                        // In production, you would not do this fallback
                        if (isFinishing()) {
                            return; // Don't continue if activity is finishing
                        }

                        // For demo purposes, simulate success after server failure
                        new AlertDialog.Builder(RewardDetailActivity.this)
                                .setTitle("Server Error")
                                .setMessage("There was a problem communicating with the server. Would you like to continue with a simulated redemption?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    // Simulate successful redemption
                                    userPoints = userPoints - rewardPoints;
                                    rewardRedeemed = true;

                                    // Update UI
                                    if (userPointsDisplay != null) {
                                        userPointsDisplay.setText(String.format("Your Points: %,d", userPoints));
                                    }

                                    // Disable redeem button
                                    redeemButton.setEnabled(false);
                                    redeemButton.setText("Redeemed");
                                    redeemButton.setBackgroundResource(R.drawable.rounded_button_disabled);

                                    // Show success message
                                    Toast.makeText(RewardDetailActivity.this,
                                            "Simulated reward redemption successful!",
                                            Toast.LENGTH_SHORT).show();

                                    // Finish after delay
                                    new Handler().postDelayed(() -> finishWithResult(), 2000);
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Add any required headers (e.g., authentication)
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Set timeout for the request
        request.setRetryPolicy(new DefaultRetryPolicy(
                15000, // 15 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    private void finishWithResult() {
        // Set result with updated points
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_USER_POINTS, userPoints);

        // Always pass back the reward title and image URL
        resultIntent.putExtra("REWARD_TITLE", rewardTitle);
        if (rewardImageUrl != null && !rewardImageUrl.isEmpty()) {
            resultIntent.putExtra("REWARD_IMAGE_URL", rewardImageUrl);
        }

        // Add redemption info if reward was redeemed
        if (rewardRedeemed) {
            resultIntent.putExtra(RESULT_POINTS_UPDATED, true);
            resultIntent.putExtra(RESULT_REWARD_REDEEMED, rewardTitle);
        }

        // Log all the data being returned
        Log.d("RewardDetailActivity", "Finishing with result: userPoints=" + userPoints);
        Log.d("RewardDetailActivity", "Finishing with result: rewardTitle=" + rewardTitle);
        Log.d("RewardDetailActivity", "Finishing with result: rewardImageUrl=" + rewardImageUrl);
        Log.d("RewardDetailActivity", "Finishing with result: rewardRedeemed=" + rewardRedeemed);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void getIntentData() {
        if (getIntent() != null) {
            rewardTitle = getIntent().getStringExtra("REWARD_TITLE");
            rewardDesc = getIntent().getStringExtra("REWARD_DESCRIPTION");
            rewardPoints = getIntent().getIntExtra("REWARD_POINTS", 0);
            rewardExpiry = getIntent().getStringExtra("REWARD_EXPIRY");
            rewardImageUrl = getIntent().getStringExtra("REWARD_IMAGE_URL");
            rewardImageResId = getIntent().getIntExtra("REWARD_IMAGE_RES_ID", -1);
            userID = getIntent().getLongExtra(EXTRA_USER_ID, -1);
            username = getIntent().getStringExtra(EXTRA_USERNAME);
            userPoints = getIntent().getIntExtra(EXTRA_USER_POINTS, 0);

            // Check if user already has an active reward
            existingRewardTitle = getIntent().getStringExtra("EXISTING_REWARD");

            // Log for debugging
            if (existingRewardTitle != null && !existingRewardTitle.isEmpty()) {
                Log.d("RewardDetailActivity", "User has existing reward: " + existingRewardTitle);
            }
        }
    }

    private void initializeViews() {
        // Find views
        rewardImage = findViewById(R.id.rewardImage);
        pointsBadge = findViewById(R.id.pointsBadge);
        rewardDescription = findViewById(R.id.rewardDescription);
        termsConditions = findViewById(R.id.termsConditions);
        pointsRequired = findViewById(R.id.pointsRequired);
        expiryDate = findViewById(R.id.expiryDate);
        validLocations = findViewById(R.id.validLocations);
        redeemButton = findViewById(R.id.redeemButton);
        backButton = findViewById(R.id.backButton);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);

        // Add a text view to display user's current points
        userPointsDisplay = findViewById(R.id.userPointsDisplay);
        if (userPointsDisplay != null) {
            userPointsDisplay.setText(String.format("Your Points: %,d", userPoints));
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setupData() {
        // Set title in the collapsing toolbar
        collapsingToolbar.setTitle(rewardTitle);

        // Set points badge
        if (pointsBadge != null) {
            pointsBadge.setText(String.valueOf(rewardPoints));
        }

        // Set reward description
        if (rewardDescription != null && rewardDesc != null && !rewardDesc.isEmpty()) {
            rewardDescription.setText(rewardDesc);
        }

        // Set points required
        if (pointsRequired != null) {
            pointsRequired.setText(String.valueOf(rewardPoints) + " points");
        }

        // Set expiry date
        if (expiryDate != null && rewardExpiry != null && !rewardExpiry.isEmpty()) {
            expiryDate.setText("Valid until " + rewardExpiry);
        }

        // Set valid locations
        if (validLocations != null) {
            validLocations.setText("All Locations");
        }

        // Set terms and conditions
        if (termsConditions != null) {
            termsConditions.setText("This reward cannot be combined with other offers. One-time use only.");
        }

        // Load image - prioritize resource ID over URL
        // Load image - prioritize resource ID over URL
        if (rewardImageResId != -1) {
            // Use the resource ID passed from DiscountsActivity
            rewardImage.setImageResource(rewardImageResId);
            Log.d("RewardDetailActivity", "Loading image from resource ID: " + rewardImageResId);
        } else if (rewardImageUrl != null && !rewardImageUrl.isEmpty()) {
            // Show placeholder while loading
            rewardImage.setImageResource(R.drawable.reward_placeholder);

            // Load image using AsyncTask
            new LoadImageTask(rewardImage).execute(rewardImageUrl);
            Log.d("RewardDetailActivity", "Loading image from URL: " + rewardImageUrl);
        } else {
            // Load placeholder image
            rewardImage.setImageResource(R.drawable.reward_placeholder);
            Log.d("RewardDetailActivity", "Using placeholder image");
        }
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finishWithResult());

        // Redeem button
        redeemButton.setOnClickListener(v -> {
            // First check if there's an existing active reward
            if (existingRewardTitle != null && !existingRewardTitle.isEmpty()
                    && !existingRewardTitle.equals(rewardTitle)) {
                // Show warning about existing reward
                showExistingRewardWarningDialog();
            } else {
                // Normal flow - check if user can redeem reward
                if (canRedeem()) {
                    showRedeemConfirmationDialog();
                } else {
                    showNotEnoughPointsDialog();
                }
            }
        });
    }

    // Add this new method to show warning about existing reward
    private void showExistingRewardWarningDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Active Reward Exists")
                .setMessage("You already have an active reward: " + existingRewardTitle + ". Do you want to replace it with this new reward?")
                .setPositiveButton("Replace", (dialog, which) -> {
                    // User wants to replace existing reward
                    if (canRedeem()) {
                        showRedeemConfirmationDialog();
                    } else {
                        showNotEnoughPointsDialog();
                    }
                })
                .setNegativeButton("Keep Current", (dialog, which) -> {
                    // User wants to keep existing reward, so just go back
                    finishWithResult();
                })
                .show();
    }

    private boolean canRedeem() {
        // Check if user has enough points
        return userPoints >= rewardPoints;
    }

    private void checkRedeemability() {
        if (canRedeem()) {
            redeemButton.setEnabled(true);
            redeemButton.setText("Redeem Reward");
            redeemButton.setBackgroundResource(R.drawable.rounded_button);

            // Show how many points the user will have left after redemption
            if (userPointsDisplay != null) {
                int remainingPoints = userPoints - rewardPoints;
                userPointsDisplay.setText(String.format("Your Points: %,d (-%,d = %,d after redemption)",
                        userPoints, rewardPoints, remainingPoints));
            }
        } else {
            redeemButton.setEnabled(false);
            redeemButton.setText("Not Enough Points");
            redeemButton.setBackgroundResource(R.drawable.rounded_button_disabled);

            // Show how many more points the user needs
            if (userPointsDisplay != null) {
                int pointsNeeded = rewardPoints - userPoints;
                userPointsDisplay.setText(String.format("Your Points: %,d (need %,d more points)",
                        userPoints, pointsNeeded));
            }
        }
    }

    private void showRedeemConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Redeem Reward")
                .setMessage("Are you sure you want to redeem this reward for " + rewardPoints + " points?")
                .setPositiveButton("Redeem", (dialog, which) -> {
                    // Proceed with redemption
                    redeemReward();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showNotEnoughPointsDialog() {
        int pointsNeeded = rewardPoints - userPoints;

        new AlertDialog.Builder(this)
                .setTitle("Not Enough Points")
                .setMessage("You need " + pointsNeeded + " more points to redeem this reward. Continue earning points by booking services!")
                .setPositiveButton("OK", null)
                .show();
    }

    private void redeemReward() {
        // Calculate new points value after redemption
        int newPointsValue = userPoints - rewardPoints;

        // Make sure points don't go negative
        if (newPointsValue < 0)
        {
            newPointsValue = 0;
        }

        // Make the PUT request to update points on the server
        updateUserPoints(userID, newPointsValue);
    }


}