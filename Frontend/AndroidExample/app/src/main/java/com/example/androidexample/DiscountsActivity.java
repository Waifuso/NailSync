package com.example.androidexample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import com.example.androidUI.OfferAdapter;
import com.example.androidUI.PointsAnimationHelper;
import com.example.androidUI.RewardAdapter;
import com.example.androidUI.RewardsService;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity for rewards and offers screen
 */
public class DiscountsActivity extends AppCompatActivity {

    // Intent extras constants
    public static final String EXTRA_USER_POINTS = "userPoints";
    public static final String EXTRA_USER_ID = "userID";
    public static final String EXTRA_USERNAME = "username";

    // UI components
    private TextView currentRankValue;
    private TextView pointsValue;
    private TextView pointsToNextRank;
    private ProgressBar rankProgressBar;
    private RecyclerView rewardsRecyclerView;
    private RecyclerView offersRecyclerView;
    private ImageButton backButton;
    private TextView viewBenefitsLink;
    private TextView viewHistoryLink;
    private TextView viewAllRewards;
    private TextView viewAllOffers;
    private ProgressDialog progressDialog;

    // Adapters
    private RewardAdapter rewardAdapter;
    private OfferAdapter offerAdapter;

    // Data
    private List<RewardsService.Offer> offers = new ArrayList<>();
    private androidx.cardview.widget.CardView activeRewardCard;
    private TextView tvActiveRewardTitle;
    private com.google.android.material.button.MaterialButton btnClearReward;
    private boolean hasActiveReward = false;
    // Service
    private RewardsService rewardsService;

    // User ID and points
    private long userID = -1;
    private int userPoints = -1;

    private int lastRedeemedPoints = 0;
    private String rewardTitle = "";
    private String rewardImageUrl = "";

    // Handler for posting UI updates from background threads
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private RewardsService.UserRank userRank;
    private List<RewardsService.Reward> rewards = new ArrayList<>();

    private String username = "";
    private boolean pointsUpdated = false;

    private int updatedPoints = 0;

    // Activity result launcher for reward details
    private final ActivityResultLauncher<Intent> rewardDetailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                // Get the updated points from result intent
                                int updatedPoints = data.getIntExtra(EXTRA_USER_POINTS, userPoints);

                                // Store the reward title and image URL if available
                                rewardTitle = data.getStringExtra("REWARD_TITLE");
                                rewardImageUrl = data.getStringExtra("REWARD_IMAGE_URL");

                                // Log for debugging
                                if (rewardTitle != null) {
                                    Log.d("DiscountsActivity", "Received reward title: " + rewardTitle);
                                }
                                if (rewardImageUrl != null) {
                                    Log.d("DiscountsActivity", "Received reward image URL: " + rewardImageUrl);
                                }

                                // Check if points were updated through redemption
                                if (data.getBooleanExtra(RewardDetailActivity.RESULT_POINTS_UPDATED, false)) {
                                    // Get old points value before updating
                                    int oldPoints = userPoints;

                                    // Get redeemed reward name if available
                                    String redeemedReward = data.getStringExtra(
                                            RewardDetailActivity.RESULT_REWARD_REDEEMED);

                                    // Capture points spent for potential refund later
                                    lastRedeemedPoints = oldPoints - updatedPoints;
                                    Log.d("DiscountsActivity", "Stored lastRedeemedPoints: " + lastRedeemedPoints);

                                    // Update the points
                                    userPoints = updatedPoints;
                                    pointsUpdated = true;

                                    // Update active reward flag
                                    hasActiveReward = true;

                                    // Update rank with new points
                                    updateRankWithNewPoints();

                                    // Show visual feedback for points change
                                    if (oldPoints != userPoints) {
                                        showPointsUpdateFeedback(oldPoints, redeemedReward);
                                    }

                                    // Update active reward display
                                    checkAndDisplayActiveReward();
                                } else {
                                    // Just update points without animation
                                    userPoints = updatedPoints;
                                    pointsUpdated = true;
                                    updateRankWithNewPoints();

                                    // Update active reward display if there is one
                                    if (rewardTitle != null && !rewardTitle.isEmpty()) {
                                        checkAndDisplayActiveReward();
                                    }
                                }
                            }
                        }
                    });

    // Activity result launcher for other activities that might update points
    private final ActivityResultLauncher<Intent> otherActivitiesLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.hasExtra(EXTRA_USER_POINTS)) {
                                // Get the updated points
                                int updatedPoints = data.getIntExtra(EXTRA_USER_POINTS, userPoints);

                                // Update points if changed
                                if (updatedPoints != userPoints) {
                                    userPoints = updatedPoints;
                                    pointsUpdated = true;
                                    updateRankWithNewPoints();
                                }
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discounts);

        // Initialize services
        rewardsService = new RewardsService(this);

        // Get user ID and points from intent
        if (getIntent() != null) {
            userID = getIntent().getLongExtra("userID", -1);
            username = getIntent().getStringExtra("username");
            userPoints = getIntent().getIntExtra("userPoints", -1);
            rewardTitle = getIntent().getStringExtra("REWARD_TITLE");

            Log.d("DiscountsActivity", "Received userID: " + userID);
            Log.d("DiscountsActivity", "Received username: " + username);
            Log.d("DiscountsActivity", "Received userPoints: " + userPoints);
        }

        // Initialize the UI components
        initializeViews();

        activeRewardCard = findViewById(R.id.activeRewardCard);
        tvActiveRewardTitle = findViewById(R.id.tvActiveRewardTitle);
        btnClearReward = findViewById(R.id.btnClearReward);

        btnClearReward.setOnClickListener(v -> {
            clearActiveReward();
        });

        checkAndDisplayActiveReward();
        // Set up click listeners
        setupClickListeners();

        // Show loading indicator
        showLoading(true);

        // Load user data
        loadUserData();
    }
    /**
     * Check if the user has an active reward and display it
     */
    private void checkAndDisplayActiveReward() {
        // Check if rewardTitle is not empty
        if (rewardTitle != null && !rewardTitle.isEmpty()) {
            // Show active reward card
            activeRewardCard.setVisibility(View.VISIBLE);
            tvActiveRewardTitle.setText(rewardTitle);

            // If you want to show points, update the title to include points
            if (lastRedeemedPoints > 0) {
                tvActiveRewardTitle.setText(rewardTitle + " (" + lastRedeemedPoints + " points)");
            } else {
                tvActiveRewardTitle.setText(rewardTitle);
            }

            hasActiveReward = true;

            // Log for debugging
            Log.d("DiscountsActivity", "Displaying active reward: " + rewardTitle +
                    (lastRedeemedPoints > 0 ? " (cost: " + lastRedeemedPoints + " points)" : ""));
        } else {
            // Hide active reward card
            activeRewardCard.setVisibility(View.GONE);
            hasActiveReward = false;
            Log.d("DiscountsActivity", "No active reward to display");
        }
    }

    /**
     * Clear the currently active reward and refund points to the user
     */
    private void clearActiveReward() {
        // Log the points being refunded
        Log.d("DiscountsActivity", "Refunding " + lastRedeemedPoints + " points for cleared reward: " + rewardTitle);



        // Refund the points to the user if there are points to refund
        if (lastRedeemedPoints > 0) {
            // Show loading
            showLoading(true);

            int newPointsValue = userPoints + lastRedeemedPoints;

            // Create the URL - using the same URL as the reward redemption
            String url = "http://coms-3090-020.class.las.iastate.edu:8080/api/coupon/clear/" + userID +"/"+ newPointsValue + "/SPRING25";
            // Log the URL for debugging
            Log.d("DiscountsActivity", "Refund URL: " + url);
            Log.d("DiscountsActivity", "Points being refunded: " + lastRedeemedPoints);
            Log.d("DiscountsActivity", "New points will be: " + newPointsValue);

            // Make the API request
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("DiscountsActivity", "Refund Response: " + response.toString());

                            try {
                                // Parse the response message
                                String message = response.getString("message");

                                if (message.contains("Successfully")) {
                                    // Store original points for animation
                                    int oldPoints = userPoints;

                                    // Add the points back to the user's balance
                                    userPoints += lastRedeemedPoints;

                                    // Update points in UI
                                    if (pointsValue != null) {
                                        // Animate the points change
                                        PointsAnimationHelper.animatePointsChange(pointsValue, oldPoints, userPoints);

                                        // Highlight the updated points
                                        PointsAnimationHelper.highlightView(pointsValue);
                                    }

                                    // Update rank with new points
                                    updateRankWithNewPoints();

                                    // Show toast to inform user with message from server
                                    Toast.makeText(DiscountsActivity.this,
                                            lastRedeemedPoints + " points refunded - " + message,
                                            Toast.LENGTH_SHORT).show();

                                    // Flag that points were updated
                                    pointsUpdated = true;

                                    // Clear reward data
                                    rewardTitle = "";
                                    rewardImageUrl = "";
                                    hasActiveReward = false;
                                    lastRedeemedPoints = 0; // Reset last redeemed points

                                    // Hide the active reward card
                                    activeRewardCard.setVisibility(View.GONE);

                                    // Log for debugging
                                    Log.d("DiscountsActivity", "Active reward cleared successfully");
                                } else {
                                    // Show error message from server
                                    Toast.makeText(DiscountsActivity.this,
                                            "Error refunding points: " + message,
                                            Toast.LENGTH_SHORT).show();

                                    Log.e("DiscountsActivity", "Error refunding points: " + message);
                                }

                                // Hide loading
                                showLoading(false);

                            } catch (JSONException e) {
                                Log.e("DiscountsActivity", "Error parsing refund response: " + e.getMessage());

                                // Show error message
                                Toast.makeText(DiscountsActivity.this,
                                        "Error processing server response",
                                        Toast.LENGTH_SHORT).show();

                                // Hide loading
                                showLoading(false);

                                // Fallback: Still perform the local refund
                                performLocalRefund();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("DiscountsActivity", "Volley Error during refund: " + error.toString());

                            // Get error message
                            String errorMessage = "Network error";
                            if (error.networkResponse != null) {
                                errorMessage += " (Code: " + error.networkResponse.statusCode + ")";
                            }

                            // Show error message
                            Toast.makeText(DiscountsActivity.this,
                                    "Failed to refund points: " + errorMessage,
                                    Toast.LENGTH_SHORT).show();

                            // Hide loading
                            showLoading(false);

                            // Fallback: Still perform the local refund for demo purposes
                            // In production, you would handle this differently
                            new AlertDialog.Builder(DiscountsActivity.this)
                                    .setTitle("Server Error")
                                    .setMessage("There was a problem communicating with the server. Would you like to continue with a simulated refund?")
                                    .setPositiveButton("Yes", (dialog, which) -> {
                                        // Perform local refund
                                        performLocalRefund();
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }
                    });

            // Add the request to the RequestQueue
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
        } else {
            // No points to refund, just clear the reward
            rewardTitle = "";
            rewardImageUrl = "";
            hasActiveReward = false;

            // Hide the active reward card
            activeRewardCard.setVisibility(View.GONE);

            // Log for debugging
            Log.d("DiscountsActivity", "Active reward cleared (no points to refund)");
        }
    }

    // Helper method to perform local refund when server communication fails
    private void performLocalRefund() {
        // Store original points for animation
        int oldPoints = userPoints;

        // Add the points back to the user's balance
        userPoints += lastRedeemedPoints;

        // Update points in UI
        if (pointsValue != null) {
            // Animate the points change
            PointsAnimationHelper.animatePointsChange(pointsValue, oldPoints, userPoints);

            // Highlight the updated points
            PointsAnimationHelper.highlightView(pointsValue);
        }

        // Update rank with new points
        updateRankWithNewPoints();

        // Show toast to inform user
        Toast.makeText(DiscountsActivity.this,
                lastRedeemedPoints + " points refunded (locally)",
                Toast.LENGTH_SHORT).show();

        // Flag that points were updated
        pointsUpdated = true;

        // Clear reward data
        rewardTitle = "";
        rewardImageUrl = "";
        hasActiveReward = false;
        lastRedeemedPoints = 0; // Reset last redeemed points

        // Hide the active reward card
        activeRewardCard.setVisibility(View.GONE);

        // Log for debugging
        Log.d("DiscountsActivity", "Active reward cleared with local refund");
    }

    private void updateRankWithNewPoints() {
        // If we have user rank data, update it with the new points
        if (userRank != null) {
            userRank = new RewardsService.UserRank(
                    userRank.getRankName(),
                    userPoints,
                    userRank.getNextRankThreshold(),
                    userRank.getNextRankThreshold() - userPoints
            );

            // Update UI with the refreshed rank data
            updateRankUI();
        }
    }

    /**
     * Show visual feedback when points have been updated
     * @param oldPoints The previous points value
     * @param redeemedReward The name of the redeemed reward (if applicable)
     */
    private void showPointsUpdateFeedback(int oldPoints, String redeemedReward) {
        // Animate the points value changing
        PointsAnimationHelper.animatePointsChange(pointsValue, oldPoints, userPoints);

        // Highlight the points area
        PointsAnimationHelper.highlightView(findViewById(R.id.pointsValue));

        // Show a toast message about the redemption
        if (redeemedReward != null && !redeemedReward.isEmpty()) {
            int pointsRedeemed = oldPoints - userPoints;
            Toast.makeText(this,
                    redeemedReward + " redeemed for " + pointsRedeemed + " points!",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        // Initialize TextViews
        currentRankValue = findViewById(R.id.currentRankValue);
        pointsValue = findViewById(R.id.pointsValue);
        pointsToNextRank = findViewById(R.id.pointsToNextRank);
        viewBenefitsLink = findViewById(R.id.viewBenefitsLink);
        viewHistoryLink = findViewById(R.id.viewHistoryLink);
        viewAllRewards = findViewById(R.id.viewAllRewards);
        viewAllOffers = findViewById(R.id.viewAllOffers);

        // Initialize ProgressBar
        rankProgressBar = findViewById(R.id.rankProgressBar);

        // Initialize Buttons
        backButton = findViewById(R.id.backButton);

        // Initialize RecyclerViews and set up adapters
        setupRecyclerViews();
    }

    private void setupRecyclerViews() {
        // Rewards RecyclerView
        rewardsRecyclerView = findViewById(R.id.rewardsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);
        rewardsRecyclerView.setLayoutManager(layoutManager);

        // Create and set adapter with click listener
        rewardAdapter = new RewardAdapter(rewards, position -> {
            RewardsService.Reward selectedReward = rewards.get(position);
            showRewardDetails(selectedReward);
        });
        rewardsRecyclerView.setAdapter(rewardAdapter);

        // Offers RecyclerView
        offersRecyclerView = findViewById(R.id.offersRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        offersRecyclerView.setLayoutManager(gridLayoutManager);

        // Create and set adapter with click listener
        offerAdapter = new OfferAdapter(offers, position -> {
            RewardsService.Offer selectedOffer = offers.get(position);
            showOfferDetails(selectedOffer);
        });
        offersRecyclerView.setAdapter(offerAdapter);
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> {
            // Create a result intent to pass back data
            Intent resultIntent = new Intent();

            // Set the user points
            resultIntent.putExtra(EXTRA_USER_POINTS, userPoints);

            // Pass back reward title and image URL if available
            if (rewardTitle != null && !rewardTitle.isEmpty()) {
                resultIntent.putExtra("REWARD_TITLE", rewardTitle);
                Log.d("DiscountsActivity", "Passing back rewardTitle: " + rewardTitle);
            }

            if (rewardImageUrl != null && !rewardImageUrl.isEmpty()) {
                resultIntent.putExtra("REWARD_IMAGE_URL", rewardImageUrl);
                Log.d("DiscountsActivity", "Passing back rewardImageUrl: " + rewardImageUrl);
            }

            // Set result as OK so the launcher processes it
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // View benefits link
        viewBenefitsLink.setOnClickListener(v -> {
            // Navigate to rank benefits screen
            Intent intent = new Intent(this, RankBenefitsActivity.class);
            intent.putExtra(EXTRA_USER_ID, userID);
            intent.putExtra(EXTRA_USERNAME, username);
            intent.putExtra(EXTRA_USER_POINTS, userPoints);
            intent.putExtra("RANK_NAME", userRank.getRankName());
            otherActivitiesLauncher.launch(intent);
        });

        // View history link
        viewHistoryLink.setOnClickListener(v -> {
            // Navigate to points history screen
            Intent intent = new Intent(this, PointsHistoryActivity.class);
            intent.putExtra(EXTRA_USER_ID, userID);
            intent.putExtra(EXTRA_USERNAME, username);
            intent.putExtra(EXTRA_USER_POINTS, userPoints);
            otherActivitiesLauncher.launch(intent);
        });

        // View all rewards
        viewAllRewards.setOnClickListener(v -> {
            // Navigate to all rewards screen
            Intent intent = new Intent(this, AllRewardsActivity.class);
            intent.putExtra(EXTRA_USER_ID, userID);
            intent.putExtra(EXTRA_USERNAME, username);
            intent.putExtra(EXTRA_USER_POINTS, userPoints);
            otherActivitiesLauncher.launch(intent);
        });

        // View all offers
        viewAllOffers.setOnClickListener(v -> {
            // Navigate to all offers screen
            Intent intent = new Intent(this, AllOffersActivity.class);
            intent.putExtra(EXTRA_USER_ID, userID);
            intent.putExtra(EXTRA_USERNAME, username);
            intent.putExtra(EXTRA_USER_POINTS, userPoints);
            otherActivitiesLauncher.launch(intent);
        });
    }

    // Fixed loadUserData method for DiscountsActivity.java
    private void loadUserData() {
        // Show loading indicator
        showLoading(true);

        // Create the URL with userID as parameter
        String url = "http://coms-3090-020.class.las.iastate.edu:8080/api/payment/userRank&Points/" + userID;

        // Create the request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("DiscountsActivity", "Response: " + response.toString());

                try {
                    // Extract only the total points and ranking
                    int totalPoints = response.getInt("totalPoints");
                    String ranking = response.getString("ranking");

                    // Only update userPoints if it wasn't already set from the intent or is invalid
                    if (userPoints <= 0) {
                        Log.d("DiscountsActivity", "Using server points: " + totalPoints +
                                " (previous points invalid: " + userPoints + ")");
                        userPoints = totalPoints;
                    } else {
                        // Keep the points from intent but log both values
                        Log.d("DiscountsActivity", "Keeping points from intent: " + userPoints +
                                " instead of server points: " + totalPoints);
                    }

                    // Create a UserRank object with the ranking string
                    // Use the current userPoints value (either from intent or server)
                    int nextRankThreshold = userPoints + 500;
                    userRank = new RewardsService.UserRank(
                            ranking,
                            userPoints,
                            nextRankThreshold,
                            nextRankThreshold - userPoints
                    );

                    // Update the UI with the refreshed rank data
                    updateRankUI();

                    // After loading rank, load rewards and offers
                    loadRewardsAndOffers();

                } catch (JSONException e) {
                    Log.e("DiscountsActivity", "JSON parsing error: " + e.getMessage());
                    // Use mock data as fallback
                    //loadMockData();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("DiscountsActivity", "Volley Error: " + error.toString());
                mainHandler.post(() -> {
                    showLoading(false);
                    Toast.makeText(DiscountsActivity.this,
                            "Error loading user data: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    // Use mock data as fallback for demoing
                    //loadMockData();
                });
            }
        });

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void loadRewardsAndOffers() {
        // Load rewards
        rewardsService.getAvailableRewards(userID, new RewardsService.RewardsCallback() {
            @Override
            public void onSuccess(List<RewardsService.Reward> rewardsList) {
                mainHandler.post(() -> {
                    rewards.clear();
                    rewards.addAll(rewardsList);
                    rewardAdapter.notifyDataSetChanged();

                    // Check if everything is loaded
                    checkAllDataLoaded();
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    Toast.makeText(DiscountsActivity.this,
                            "Error loading rewards: " + errorMessage,
                            Toast.LENGTH_SHORT).show();

                    // Use mock rewards data as fallback
                    rewards.clear();
                    rewards.addAll(getSampleRewards());
                    rewardAdapter.notifyDataSetChanged();

                    // Check if everything is loaded
                    checkAllDataLoaded();
                });
            }
        });

        // Load offers
        rewardsService.getAvailableOffers(userID, new RewardsService.OffersCallback() {
            @Override
            public void onSuccess(List<RewardsService.Offer> offersList) {
                mainHandler.post(() -> {
                    offers.clear();
                    offers.addAll(offersList);
                    offerAdapter.notifyDataSetChanged();

                    // Check if everything is loaded
                    checkAllDataLoaded();
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    Toast.makeText(DiscountsActivity.this,
                            "Error loading offers: " + errorMessage,
                            Toast.LENGTH_SHORT).show();

                    // Use mock offers data as fallback
                    offers.clear();
                    offers.addAll(getSampleOffers());
                    offerAdapter.notifyDataSetChanged();

                    // Check if everything is loaded
                    checkAllDataLoaded();
                });
            }
        });
    }

    private void checkAllDataLoaded() {
        // If we have loaded both rewards and offers, hide the loading indicator
        if (!rewards.isEmpty() && !offers.isEmpty()) {
            showLoading(false);
        }
    }

    private void updateRankUI() {
        // Update UI with rank data
        currentRankValue.setText(userRank.getRankName());
        pointsValue.setText(String.format("%,d", userRank.getCurrentPoints()));
        pointsToNextRank.setText(String.format("%,d points to %s",
                userRank.getPointsToNextRank(),
                userRank.getNextRankName()));

        // Update progress bar
        rankProgressBar.setProgress(userRank.getProgressPercentage());
    }

    private void showRewardDetails(RewardsService.Reward reward) {
        // Navigate to reward details screen
//        Intent intent = new Intent(this, RewardDetailActivity.class);
//        intent.putExtra("REWARD_TITLE", reward.getTitle());
//        intent.putExtra("REWARD_DESCRIPTION", reward.getDescription());
//        intent.putExtra("REWARD_POINTS", reward.getPointsRequired());
//        intent.putExtra("REWARD_EXPIRY", reward.getExpiryDate());
//        intent.putExtra(EXTRA_USER_ID, userID);
//        intent.putExtra(EXTRA_USERNAME, username);
//        intent.putExtra(EXTRA_USER_POINTS, userPoints);
//
//        // Log values safely
//        Log.d("REWARD_TITLE", reward.getTitle());
//        Log.d("REWARD_DESCRIPTION", reward.getDescription());
//        Log.d("REWARD_POINTS", String.valueOf(reward.getPointsRequired()));
//        Log.d("REWARD_EXPIRY", reward.getExpiryDate());
//        Log.d(EXTRA_USER_ID, String.valueOf(userID));
//        Log.d(EXTRA_USERNAME, username != null ? username : "");
//        Log.d(EXTRA_USER_POINTS, String.valueOf(userPoints));
//
//        // Check if there's an image URL before adding it to intent and logging
//        String imageUrl = reward.getImageUrl();
//        if (imageUrl != null && !imageUrl.isEmpty()) {
//            intent.putExtra("REWARD_IMAGE_URL", imageUrl);
//            Log.d("REWARD_IMAGE_URL", imageUrl);
//        }
//
//        // Pass the image resource ID if available
//        if (reward.getImageResId() != -1) {
//            intent.putExtra("REWARD_IMAGE_RES_ID", reward.getImageResId());
//            Log.d("REWARD_IMAGE_RES_ID", String.valueOf(reward.getImageResId()));
//        }
//
//        // Launch with the activity result launcher to catch when we return
//        rewardDetailLauncher.launch(intent);

        if (rewardTitle != null && !rewardTitle.isEmpty()) {
            // User already has an active reward, show confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Active Reward Exists")
                    .setMessage("You already have an active reward: " + rewardTitle + ". Do you want to replace it with " + reward.getTitle() + "?")
                    .setPositiveButton("Replace", (dialog, which) -> {
                        // Clear current reward and proceed to new reward details
                        clearActiveReward();
                        proceedToRewardDetails(reward);
                    })
                    .setNegativeButton("Keep Current", null) // Do nothing if user wants to keep current reward
                    .show();
        } else {
            // No active reward, proceed normally
            proceedToRewardDetails(reward);
        }
    }

    // Support method to proceed to reward details
    private void proceedToRewardDetails(RewardsService.Reward reward) {
        // Navigate to reward details screen
        Intent intent = new Intent(this, RewardDetailActivity.class);
        intent.putExtra("REWARD_TITLE", reward.getTitle());
        intent.putExtra("REWARD_DESCRIPTION", reward.getDescription());
        intent.putExtra("REWARD_POINTS", reward.getPointsRequired());
        intent.putExtra("REWARD_EXPIRY", reward.getExpiryDate());
        intent.putExtra(EXTRA_USER_ID, userID);
        intent.putExtra(EXTRA_USERNAME, username);
        intent.putExtra(EXTRA_USER_POINTS, userPoints);

        // Log values safely
        Log.d("DiscountsActivity", "Reward title: " + reward.getTitle());
        Log.d("DiscountsActivity", "Reward description: " + reward.getDescription());
        Log.d("DiscountsActivity", "Reward points: " + reward.getPointsRequired());
        Log.d("DiscountsActivity", "Reward expiry: " + reward.getExpiryDate());
        Log.d("DiscountsActivity", "User ID: " + userID);
        Log.d("DiscountsActivity", "Username: " + (username != null ? username : ""));
        Log.d("DiscountsActivity", "User points: " + userPoints);

        // Check if there's an image URL before adding it to intent and logging
        String imageUrl = reward.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            intent.putExtra("REWARD_IMAGE_URL", imageUrl);
            Log.d("DiscountsActivity", "Reward image URL: " + imageUrl);
        }

        // Pass the image resource ID if available
        if (reward.getImageResId() != -1) {
            intent.putExtra("REWARD_IMAGE_RES_ID", reward.getImageResId());
            Log.d("DiscountsActivity", "Reward image resource ID: " + reward.getImageResId());
        }

        // Launch with the activity result launcher to catch when we return
        rewardDetailLauncher.launch(intent);
    }
    private void navigateToRewardDetails(RewardsService.Reward reward) {
        // Navigate to reward details screen
        Intent intent = new Intent(this, RewardDetailActivity.class);
        intent.putExtra("REWARD_TITLE", reward.getTitle());
        intent.putExtra("REWARD_DESCRIPTION", reward.getDescription());
        intent.putExtra("REWARD_POINTS", reward.getPointsRequired());
        intent.putExtra("REWARD_EXPIRY", reward.getExpiryDate());
        intent.putExtra(EXTRA_USER_ID, userID);
        intent.putExtra(EXTRA_USERNAME, username);
        intent.putExtra(EXTRA_USER_POINTS, userPoints);

        // Log values safely
        Log.d("REWARD_TITLE", reward.getTitle());
        Log.d("REWARD_DESCRIPTION", reward.getDescription());
        Log.d("REWARD_POINTS", String.valueOf(reward.getPointsRequired()));
        Log.d("REWARD_EXPIRY", reward.getExpiryDate());
        Log.d(EXTRA_USER_ID, String.valueOf(userID));
        Log.d(EXTRA_USERNAME, username != null ? username : "");
        Log.d(EXTRA_USER_POINTS, String.valueOf(userPoints));

        // Check if there's an image URL before adding it to intent and logging
        String imageUrl = reward.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            intent.putExtra("REWARD_IMAGE_URL", imageUrl);
            Log.d("REWARD_IMAGE_URL", imageUrl);
        }

        // Pass the image resource ID if available
        if (reward.getImageResId() != -1) {
            intent.putExtra("REWARD_IMAGE_RES_ID", reward.getImageResId());
            Log.d("REWARD_IMAGE_RES_ID", String.valueOf(reward.getImageResId()));
        }

        // Launch with the activity result launcher to catch when we return
        rewardDetailLauncher.launch(intent);
    }
    private void showOfferDetails(RewardsService.Offer offer) {
        // Navigate to offer details screen
        Intent intent = new Intent(this, OfferDetailsActivity.class);
        intent.putExtra("OFFER_TITLE", offer.getTitle());
        intent.putExtra("OFFER_DESCRIPTION", offer.getDescription());
        intent.putExtra("OFFER_DISCOUNT", offer.getDiscountText());
        intent.putExtra("OFFER_TAG", offer.getTagText());
        intent.putExtra("OFFER_VALIDITY", offer.getValidityPeriod());
        intent.putExtra(EXTRA_USER_ID, userID);
        intent.putExtra(EXTRA_USERNAME, username);
        intent.putExtra(EXTRA_USER_POINTS, userPoints);

        // Log values safely
        Log.d("OFFER_TITLE", offer.getTitle());
        Log.d("OFFER_DESCRIPTION", offer.getDescription());
        Log.d("OFFER_DISCOUNT", offer.getDiscountText());
        Log.d("OFFER_TAG", offer.getTagText());
        Log.d("OFFER_VALIDITY", offer.getValidityPeriod());
        Log.d(EXTRA_USER_ID, String.valueOf(userID));
        Log.d(EXTRA_USERNAME, username != null ? username : "");
        Log.d(EXTRA_USER_POINTS, String.valueOf(userPoints));

        // Check if there's an image URL before adding it to intent and logging
        String imageUrl = offer.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            intent.putExtra("OFFER_IMAGE_URL", imageUrl);
            Log.d("OFFER_IMAGE_URL", imageUrl);
        }

        // Pass the image resource ID if available
        if (offer.getImageResId() != -1) {
            intent.putExtra("OFFER_IMAGE_RES_ID", offer.getImageResId());
            Log.d("OFFER_IMAGE_RES_ID", String.valueOf(offer.getImageResId()));
        }

        otherActivitiesLauncher.launch(intent);
        finish();
    }

    private void showLoading(boolean show) {
        if (show) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Loading your rewards...");
                progressDialog.setCancelable(false);
            }
            progressDialog.show();
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private void loadMockData() {
        // Create sample rank data with current points
        // Only create with -1 if userPoints is still invalid
        int pointsToUse = userPoints > 0 ? userPoints : 1000; // Default to 1000 if userPoints is invalid
        userRank = new RewardsService.UserRank("Silver", pointsToUse, 4000, 4000 - pointsToUse);

        updateRankUI();

        // Create sample rewards and offers
        rewards.clear();
        rewards.addAll(getSampleRewards());
        rewardAdapter.notifyDataSetChanged();

        offers.clear();
        offers.addAll(getSampleOffers());
        offerAdapter.notifyDataSetChanged();

        // Hide loading indicator
        showLoading(false);
    }

    private List<RewardsService.Reward> getSampleRewards() {
        List<RewardsService.Reward> sampleRewards = new ArrayList<>();

        // Create a reward and set its image resource ID
        RewardsService.Reward r1 = new RewardsService.Reward(
                "Free Gel Polish Upgrade",
                "Valid on any service",
                250,
                "May 30");
        r1.setImageResId(R.drawable.reward_gel_polish);
        sampleRewards.add(r1);

        RewardsService.Reward r2 = new RewardsService.Reward(
                "Free Nail Art Design",
                "One accent nail",
                350,
                "June 15");
        r2.setImageResId(R.drawable.reward_free_nail);
        sampleRewards.add(r2);

        RewardsService.Reward r3 = new RewardsService.Reward(
                "15% Off Any Service",
                "Manicure or pedicure",
                500,
                "July 1");
        r3.setImageResId(R.drawable.reward_fifteen_off);
        sampleRewards.add(r3);

        RewardsService.Reward r4 = new RewardsService.Reward(
                "Free Hand Massage",
                "With any service",
                200,
                "Aug 10");
        r4.setImageResId(R.drawable.reward_earlybird);
        sampleRewards.add(r4);

        return sampleRewards;
    }

    private List<RewardsService.Offer> getSampleOffers() {
        List<RewardsService.Offer> sampleOffers = new ArrayList<>();

        // Create an offer and set its image resource ID
        RewardsService.Offer o1 = new RewardsService.Offer(
                "Weekday Special",
                "Valid on Wednesdays only",
                "15% OFF",
                "LIMITED",
                "Apr 1 - May 31");
        o1.setImageResId(R.drawable.offer_weekday_special);
        sampleOffers.add(o1);

        RewardsService.Offer o2 = new RewardsService.Offer(
                "First-Time Client",
                "New customers only",
                "20% OFF",
                "EXCLUSIVE",
                "Valid anytime");
        o2.setImageResId(R.drawable.stockplaceholderimage);
        sampleOffers.add(o2);

        RewardsService.Offer o3 = new RewardsService.Offer(
                "Birthday Month",
                "Valid during your birthday month",
                "FREE",
                "SPECIAL",
                "Your birthday month");
        o3.setImageResId(R.drawable.offer_metallic);
        sampleOffers.add(o3);

        RewardsService.Offer o4 = new RewardsService.Offer(
                "Refer a Friend",
                "When they book their first appointment",
                "10% OFF",
                "ONGOING",
                "Always available");
        o4.setImageResId(R.drawable.stockplaceholderimage);
        sampleOffers.add(o4);

        return sampleOffers;
    }
}