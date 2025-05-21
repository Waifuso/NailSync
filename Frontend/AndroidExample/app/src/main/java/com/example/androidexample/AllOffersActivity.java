package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.androidUI.OfferAdapter;
import com.example.androidUI.RewardsService;

import java.util.ArrayList;
import java.util.List;

public class AllOffersActivity extends AppCompatActivity {

    private TextView titleText;
    private TextView subtitleText;
    private ImageButton backButton;
    private RecyclerView offersRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;

    private OfferAdapter offerAdapter;
    private List<RewardsService.Offer> offers = new ArrayList<>();
    private RewardsService rewardsService;
    private SharedPreferencesHelper prefsHelper;
    private String username = "";
    private long userID = -1;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_offers);

        rewardsService = new RewardsService(this);
        prefsHelper = new SharedPreferencesHelper(this);

        initializeViews();
        setupClickListeners();
        loadOffers();
    }

    private void initializeViews() {
        titleText = findViewById(R.id.title);
        subtitleText = findViewById(R.id.subtitle);
        backButton = findViewById(R.id.backButton);
        offersRecyclerView = findViewById(R.id.offersRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyView = findViewById(R.id.emptyView);

        offersRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        offerAdapter = new OfferAdapter(offers, position -> {
            RewardsService.Offer selectedOffer = offers.get(position);
            showOfferDetails(selectedOffer);
        });
        offersRecyclerView.setAdapter(offerAdapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.purple);
        swipeRefreshLayout.setOnRefreshListener(this::loadOffers);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
    }

    private void loadOffers() {
        swipeRefreshLayout.setRefreshing(true);
        userID = getIntent().getLongExtra("userID", -1);
        username = getIntent().getStringExtra("username");

        rewardsService.getAvailableOffers(userID, new RewardsService.OffersCallback() {
            @Override
            public void onSuccess(List<RewardsService.Offer> offersList) {
                mainHandler.post(() -> {
                    offers.clear();
                    offers.addAll(offersList);
                    offerAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                    updateEmptyViewVisibility();
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    Toast.makeText(AllOffersActivity.this,
                            "Error loading offers: " + errorMessage,
                            Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    updateEmptyViewVisibility();
                });
            }
        });
    }

    private void updateEmptyViewVisibility() {
        if (offers.isEmpty()) {
            offersRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            offersRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showOfferDetails(RewardsService.Offer offer) {
        Intent intent = new Intent(this, OfferDetailsActivity.class);

        intent.putExtra("OFFER_TITLE", offer.getTitle());
        intent.putExtra("OFFER_DESCRIPTION", offer.getDescription());
        intent.putExtra("OFFER_DISCOUNT", offer.getDiscountText());
        intent.putExtra("OFFER_TAG", offer.getTagText());
        intent.putExtra("OFFER_VALIDITY", offer.getValidityPeriod());
        intent.putExtra("OFFER_IMAGE_URL", offer.getImageUrl());
        intent.putExtra("userID", userID);
        intent.putExtra("username", username);

        // Log values
        Log.d("OfferDetailsActivity", "Offer Title: " + offer.getTitle());
        Log.d("OfferDetailsActivity", "Offer Discount: " + offer.getDiscountText());
        Log.d("OfferDetailsActivity", "User ID: " + userID);
        Log.d("OfferDetailsActivity", "Username: " + username);

        startActivity(intent);
    }
}
