package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.androidUI.RewardAdapter;
import com.example.androidUI.RewardsService;

import java.util.ArrayList;
import java.util.List;

public class AllRewardsActivity extends AppCompatActivity {

    // UI components
    private TextView titleText;
    private TextView subtitleText;
    private ImageButton backButton;
    private RecyclerView rewardsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;

    // Adapter
    private RewardAdapter rewardAdapter;

    // Data
    private List<RewardsService.Reward> rewards = new ArrayList<>();

    // Service and helpers
    private RewardsService rewardsService;
    private SharedPreferencesHelper prefsHelper;

    // Handler for posting UI updates from background threads
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private long userID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_rewards);

        // Initialize service and helpers
        rewardsService = new RewardsService(this);
        prefsHelper = new SharedPreferencesHelper(this);

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Load rewards
        loadRewards();
    }

    private void initializeViews() {
        // Find views
        titleText = findViewById(R.id.title);
        subtitleText = findViewById(R.id.subtitle);
        backButton = findViewById(R.id.backButton);
        rewardsRecyclerView = findViewById(R.id.rewardsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyView = findViewById(R.id.emptyView);

        // Set up RecyclerView with grid layout (2 columns)
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rewardsRecyclerView.setLayoutManager(layoutManager);

        // Create and set adapter with click listener
        rewardAdapter = new RewardAdapter(rewards, position -> {
            RewardsService.Reward selectedReward = rewards.get(position);
            showRewardDetails(selectedReward);
        });
        rewardsRecyclerView.setAdapter(rewardAdapter);

        // Set up swipe refresh
        swipeRefreshLayout.setColorSchemeResources(R.color.purple);
        swipeRefreshLayout.setOnRefreshListener(this::loadRewards);
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());
    }

    private void loadRewards() {
        // Show refreshing indicator
        swipeRefreshLayout.setRefreshing(true);

        // Fetch rewards from service
        userID = getIntent().getLongExtra("userID", -1);
        rewardsService.getAvailableRewards(userID, new RewardsService.RewardsCallback() {
            @Override
            public void onSuccess(List<RewardsService.Reward> rewardsList) {
                mainHandler.post(() -> {
                    // Update data
                    rewards.clear();
                    rewards.addAll(rewardsList);

                    // Update UI
                    rewardAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);

                    // Show empty view if no rewards
                    updateEmptyViewVisibility();
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    // Show error message
                    Toast.makeText(AllRewardsActivity.this,
                            "Error loading rewards: " + errorMessage,
                            Toast.LENGTH_SHORT).show();

                    // Hide refreshing indicator
                    swipeRefreshLayout.setRefreshing(false);

                    // Show empty view
                    updateEmptyViewVisibility();
                });
            }
        });
    }

    private void updateEmptyViewVisibility() {
        if (rewards.isEmpty()) {
            rewardsRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            rewardsRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showRewardDetails(RewardsService.Reward reward) {
        // Navigate to reward details screen
        Intent intent = new Intent(this, RewardDetailActivity.class);
        intent.putExtra("REWARD_TITLE", reward.getTitle());
        intent.putExtra("REWARD_DESCRIPTION", reward.getDescription());
        intent.putExtra("REWARD_POINTS", reward.getPointsRequired());
        intent.putExtra("REWARD_EXPIRY", reward.getExpiryDate());
        intent.putExtra("REWARD_IMAGE_URL", reward.getImageUrl());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh rewards when returning to this screen (in case a reward was redeemed)
        loadRewards();
    }
}