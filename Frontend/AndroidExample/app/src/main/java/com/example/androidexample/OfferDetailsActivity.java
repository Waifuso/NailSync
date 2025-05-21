package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class OfferDetailsActivity extends AppCompatActivity {

    // UI Components
    private ImageView offerImage;
    private TextView offerTag;
    private TextView discountBadge;
    private TextView offerDescription;
    private TextView termsConditions;
    private TextView validityPeriod;
    private TextView discountValue;
    private TextView applicableServices;
    private TextView validLocations;
    private Button useOfferButton;
    private ImageButton backButton;
    private CollapsingToolbarLayout collapsingToolbar;

    // Data
    private String offerTitle;
    private String offerDesc;
    private String offerDiscount;
    private String offerTagText;
    private String offerValidity;
    private String offerImageUrl;
    private String username = "";
    private long userID = -1;

    private int offerImageResId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_details);

        // Get data from intent
        getIntentData();

        // Initialize views
        initializeViews();

        // Set up data
        setupData();

        // Set up click listeners
        setupClickListeners();
    }

    private void getIntentData() {
        if (getIntent() != null) {
            offerTitle = getIntent().getStringExtra("OFFER_TITLE");
            offerDesc = getIntent().getStringExtra("OFFER_DESCRIPTION");
            offerDiscount = getIntent().getStringExtra("OFFER_DISCOUNT");
            offerTagText = getIntent().getStringExtra("OFFER_TAG");
            offerValidity = getIntent().getStringExtra("OFFER_VALIDITY");
            offerImageUrl = getIntent().getStringExtra("OFFER_IMAGE_URL");
            offerImageResId = getIntent().getIntExtra("OFFER_IMAGE_RES_ID", -1);
            userID = getIntent().getLongExtra("userID", -1);
            username = getIntent().getStringExtra("username");
        }
    }

    private void initializeViews() {
        // Find views
        offerImage = findViewById(R.id.offerImage);
        offerTag = findViewById(R.id.offerTag);
        discountBadge = findViewById(R.id.discountBadge);
        offerDescription = findViewById(R.id.offerDescription);
        termsConditions = findViewById(R.id.termsConditions);
        validityPeriod = findViewById(R.id.validityPeriod);
        discountValue = findViewById(R.id.discountValue);
        applicableServices = findViewById(R.id.applicableServices);
        validLocations = findViewById(R.id.validLocations);
        useOfferButton = findViewById(R.id.useOfferButton);
        backButton = findViewById(R.id.backButton);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setupData() {
        // Set title
        collapsingToolbar.setTitle(offerTitle);

        // Set tag
        if (offerTag != null && !offerTagText.isEmpty()) {
            this.offerTag.setText(offerTagText);
        }

        // Set discount badge
        if (offerDiscount != null && !offerDiscount.isEmpty()) {
            discountBadge.setText(offerDiscount);
            discountValue.setText(offerDiscount.toLowerCase());
        }

        // Set offer description
        if (offerDesc != null && !offerDesc.isEmpty()) {
            // In a real app, you'd have a more detailed description in the API response
            String fullDescription = "Enjoy this special offer: " + offerDiscount + " " +
                    offerDesc + " Don't miss out on this limited-time promotion!";
            offerDescription.setText(fullDescription);
        }

        // Set terms & conditions
        // In a real app, this would come from your API

        // Set validity period
        if (offerValidity != null && !offerValidity.isEmpty()) {
            validityPeriod.setText(offerValidity);
        }

        // Set applicable services
        // This would normally come from the API
        applicableServices.setText("All Services");

        // Set valid locations
        // This would normally come from the API
        validLocations.setText("All Locations");

        // Load image - prioritize resource ID over URL
        if (offerImageResId != -1) {
            // Use the resource ID
            offerImage.setImageResource(offerImageResId);
            Log.d("OfferDetailsActivity", "Loading image from resource ID: " + offerImageResId);
        } else if (offerImageUrl != null && !offerImageUrl.isEmpty()) {
            // Use Glide to load from URL
            Glide.with(this)
                    .load(offerImageUrl)
                    .placeholder(R.drawable.offer_placeholder)
                    .error(R.drawable.offer_placeholder)
                    .into(offerImage);
            Log.d("OfferDetailsActivity", "Loading image from URL: " + offerImageUrl);
        } else {
            // Load placeholder image
            offerImage.setImageResource(R.drawable.offer_placeholder);
            Log.d("OfferDetailsActivity", "Using placeholder image");
        }

        // Customize tag background based on tag type
        customizeTagBackground();
    }

    private void customizeTagBackground() {
        // This is just an example - you might want to use different colors or styles
        // based on the tag text (LIMITED, EXCLUSIVE, etc.)
        if (offerTag != null) {
            switch (offerTagText) {
                case "LIMITED":
                    // Using the app's accent color for LIMITED tags
                    offerTag.setBackgroundResource(R.drawable.tag_background);
                    break;
                case "EXCLUSIVE":
                    // You could create a different drawable for EXCLUSIVE tags
                    // offerTag.setBackgroundResource(R.drawable.exclusive_tag_background);
                    break;
                case "SPECIAL":
                    // And another for SPECIAL tags
                    // offerTag.setBackgroundResource(R.drawable.special_tag_background);
                    break;
                default:
                    // Default tag background
                    offerTag.setBackgroundResource(R.drawable.tag_background);
                    break;
            }
        }
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Use offer button
        useOfferButton.setOnClickListener(v -> {
            // Navigate to booking screen with offer applied
            navigateToBooking();
        });
    }

    private void navigateToBooking() {
        // Create intent for booking screen with offer info
        Intent intent = new Intent(this, SelectNailTechActivity.class);
        intent.putExtra("OFFER_APPLIED", true);
        intent.putExtra("OFFER_TITLE", offerTitle);
        intent.putExtra("OFFER_DISCOUNT", offerDiscount);
        intent.putExtra("userID", userID);
        intent.putExtra("username", username);

        // Log values
        Log.d("OfferDetailsActivity", "Offer Title: " + offerTitle);
        Log.d("OfferDetailsActivity", "Offer Discount: " + offerDiscount);
        Log.d("OfferDetailsActivity", "User ID: " + userID);
        Log.d("OfferDetailsActivity", "Username: " + username);
        Log.d("OfferDetailsActivity", "Offer Applied: true");

        // Show confirmation toast
        Toast.makeText(this,
                "Offer applied! Continue to book your appointment.",
                Toast.LENGTH_SHORT).show();

        // Start booking activity
        startActivity(intent);
        finish();
    }
}