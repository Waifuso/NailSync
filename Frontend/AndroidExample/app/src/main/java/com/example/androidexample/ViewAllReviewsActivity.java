package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidUI.ReviewAdapter;
import com.example.androidUI.ReviewModel;
import com.google.android.material.chip.Chip;
import com.example.androidUI.ReviewAdapter;
import com.example.androidUI.ReviewModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewAllReviewsActivity extends AppCompatActivity {

    private static final String TAG = "ViewAllReviewsActivity";
    private static final String REVIEWS_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/rating/get/allReviews";

    // UI components
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private ImageButton backButton;
    private TextView ratingAverage, reviewCount;
    private Chip filterAll, filter5star, filter4star, filter3star, filter2star, filter1star;

    // Data
    private List<ReviewModel> reviewList;
    private List<ReviewModel> filteredReviewList;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_reviews);

        // Initialize data lists
        reviewList = new ArrayList<>();
        filteredReviewList = new ArrayList<>();

        // Initialize UI components
        initializeViews();
        setupClickListeners();

        // Initialize RecyclerView
        setupRecyclerView();

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Fetch reviews
        fetchReviews();
    }

    private void initializeViews() {
        // Main views
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        backButton = findViewById(R.id.backButton);
        ratingAverage = findViewById(R.id.ratingAverage);
        reviewCount = findViewById(R.id.reviewCount);

        // Filter chips
        filterAll = findViewById(R.id.filterAll);
        filter5star = findViewById(R.id.filter5star);
        filter4star = findViewById(R.id.filter4star);
        filter3star = findViewById(R.id.filter3star);
        filter2star = findViewById(R.id.filter2star);
        filter1star = findViewById(R.id.filter1star);
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Filter chips
        filterAll.setOnClickListener(v -> applyFilter(0));
        filter5star.setOnClickListener(v -> applyFilter(5));
        filter4star.setOnClickListener(v -> applyFilter(4));
        filter3star.setOnClickListener(v -> applyFilter(3));
        filter2star.setOnClickListener(v -> applyFilter(2));
        filter1star.setOnClickListener(v -> applyFilter(1));
    }

    private void setupRecyclerView() {
        // Set layout manager
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create and set adapter
        reviewAdapter = new ReviewAdapter(this, filteredReviewList);
        reviewsRecyclerView.setAdapter(reviewAdapter);
    }

    private void fetchReviews() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                REVIEWS_URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            parseReviews(response);
                            updateStatistics();
                            applyFilter(0); // Show all reviews by default
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing reviews: " + e.getMessage());
                            Toast.makeText(ViewAllReviewsActivity.this,
                                    "Error parsing reviews", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching reviews: " + error.toString());
                        Toast.makeText(ViewAllReviewsActivity.this,
                                "Failed to load reviews", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add request to queue
        requestQueue.add(jsonArrayRequest);
    }

    private void parseReviews(JSONArray jsonArray) throws JSONException {
        reviewList.clear();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject reviewObject = jsonArray.getJSONObject(i);

            // Create a new ReviewModel
            ReviewModel review = new ReviewModel();

            // Parse rating object
            JSONObject ratingObject = reviewObject.getJSONObject("rating");
            ReviewModel.Rating rating = new ReviewModel.Rating();
            rating.setId(ratingObject.getInt("id"));
            rating.setStar(ratingObject.getInt("star"));
            rating.setComment(ratingObject.getString("comment"));
            review.setRating(rating);

            // Parse service names
            JSONArray serviceNamesArray = reviewObject.getJSONArray("serviceName");
            List<String> serviceNames = new ArrayList<>();
            for (int j = 0; j < serviceNamesArray.length(); j++) {
                serviceNames.add(serviceNamesArray.getString(j));
            }
            review.setServiceName(serviceNames);

            // Set employee and date
            review.setEmployee(reviewObject.getString("employee"));
            review.setDate(reviewObject.getString("date"));

            // Add to the list
            reviewList.add(review);
        }

        // Log the number of reviews loaded
        Log.d(TAG, "Loaded " + reviewList.size() + " reviews");
    }

    private void updateStatistics() {
        if (reviewList.isEmpty()) {
            ratingAverage.setText("0.0");
            reviewCount.setText("(0 reviews)");
            return;
        }

        // Calculate average rating
        float totalStars = 0;
        for (ReviewModel review : reviewList) {
            totalStars += review.getRating().getStar();
        }
        float average = totalStars / reviewList.size();

        // Format to one decimal place
        String averageText = String.format("%.1f", average);
        ratingAverage.setText(averageText);
        reviewCount.setText("(" + reviewList.size() + " reviews)");
    }

    private void applyFilter(int starFilter) {
        // Update chip UI
        updateFilterChipsState(starFilter);

        // Apply filter
        filteredReviewList.clear();
        if (starFilter == 0) {
            // Show all reviews
            filteredReviewList.addAll(reviewList);
        } else {
            // Filter by star rating
            for (ReviewModel review : reviewList) {
                if (review.getRating().getStar() == starFilter) {
                    filteredReviewList.add(review);
                }
            }
        }

        // Update adapter
        reviewAdapter.notifyDataSetChanged();

        // Show empty state if no reviews match filter
        if (filteredReviewList.isEmpty()) {
            Toast.makeText(this, "No reviews found with " + starFilter + " stars", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFilterChipsState(int selectedFilter) {
        // Reset all chips
        filterAll.setChipBackgroundColorResource(android.R.color.white);
        filterAll.setTextColor(getResources().getColor(android.R.color.black));

        filter5star.setChipBackgroundColorResource(android.R.color.white);
        filter5star.setTextColor(getResources().getColor(android.R.color.black));

        filter4star.setChipBackgroundColorResource(android.R.color.white);
        filter4star.setTextColor(getResources().getColor(android.R.color.black));

        filter3star.setChipBackgroundColorResource(android.R.color.white);
        filter3star.setTextColor(getResources().getColor(android.R.color.black));

        filter2star.setChipBackgroundColorResource(android.R.color.white);
        filter2star.setTextColor(getResources().getColor(android.R.color.black));

        filter1star.setChipBackgroundColorResource(android.R.color.white);
        filter1star.setTextColor(getResources().getColor(android.R.color.black));

        // Set selected chip
        int themeColor = 0xFFA47DAB; // Hex color code for #A47DAB

        switch (selectedFilter) {
            case 0:
                filterAll.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(themeColor));
                filterAll.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case 5:
                filter5star.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(themeColor));
                filter5star.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case 4:
                filter4star.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(themeColor));
                filter4star.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case 3:
                filter3star.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(themeColor));
                filter3star.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case 2:
                filter2star.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(themeColor));
                filter2star.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case 1:
                filter1star.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(themeColor));
                filter1star.setTextColor(getResources().getColor(android.R.color.white));
                break;
        }
    }
}