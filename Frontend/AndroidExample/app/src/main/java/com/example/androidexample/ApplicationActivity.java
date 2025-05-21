package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidUI.ImageSlideAdapter;
import com.example.androidUI.ReviewAdapter;
import com.example.androidUI.ReviewModel;
import com.example.androidUI.ServiceAdapter;
import com.example.androidUI.ServiceItem;
import com.example.androidUI.ServiceModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Application Activity - Applet that appears after the user logs in or signs up, this is the home screen of the app for the customer (employees and admins have separate home pages).
 * This activity will only appear when the user is signed into their account.
 * @author Jordan Nguyen (hieu2k@iastate.edu)
 */
public class ApplicationActivity extends AppCompatActivity {

    private ViewPager2 imageSlideshow;
    private TabLayout slideDotsTabLayout;
    private ImageSlideAdapter imageSlideAdapter;
    private List<ImageSlideAdapter.SlideItem> slideItems;
    private RecyclerView recyclerViewServices;
    private BottomNavigationView bottomNavView;

    // Reviews section components
    private RecyclerView recyclerViewReviews;
    private ReviewAdapter reviewAdapter;
    private List<ReviewModel> reviewList;
    private RequestQueue requestQueue;

    // Quick action boxes - vertically arranged
    private CardView boxBookAppointment;
    private CardView boxViewRewards;
    private CardView boxMyAppointments;
    private CardView boxTryAR;

    // UI elements
    private Button learnMoreButton;
    private TextView welcomeText;
    private TextView viewAllServices;

    // Chat button
    private FloatingActionButton chatbtn;

    // User information
    private long userID = -1;
    private String username, email, rewardTitle, rewardImageUrl;
    private int userPoints = -1;

    // Auto-scrolling variables
    private Handler sliderHandler = new Handler();
    private int currentPage = 0;
    private static final int AUTO_SCROLL_DELAY = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);
        EdgeToEdge.enable(this);

        // Get userID and other information from intent
        getUserDataFromIntent();

        // Initialize UI components
        initializeViews();

        // Setup UI components
        setupToolbar();
        setupWelcomeMessage();
        setupImageSlideshow();
        setupReviewsSection(); // New reviews section instead of card slider
        setupActionBoxes();
        setupServicesRecyclerView();
        setupLearnMoreButton();
        setupViewAllServices();
        setupBottomNavigation();
        setupChatButton();
    }

    private void getUserDataFromIntent() {
        // Get basic user information with proper default values and logging
        userID = getIntent().getLongExtra("userID", -1);
        username = getIntent().getStringExtra("username");
        email = getIntent().getStringExtra("email");
        userPoints = getIntent().getIntExtra("userPoints", 0);
        boolean isEmployee = getIntent().getBooleanExtra("isEmployee", false);

        // Log all received data for debugging
        Log.d("ApplicationActivity", "Received userID: " + userID);
        Log.d("ApplicationActivity", "Received username: " + username);
        Log.d("ApplicationActivity", "Received email: " + email);
        Log.d("ApplicationActivity", "Received userPoints: " + userPoints);
        Log.d("ApplicationActivity", "Received isEmployee: " + isEmployee);

        // Handle reward info if present
        try {
            rewardTitle = getIntent().getStringExtra("REWARD_TITLE");
            rewardImageUrl = getIntent().getStringExtra("REWARD_IMAGE_URL");
            if (rewardTitle != null) {
                Log.d("ApplicationActivity", "Received reward title: " + rewardTitle);
            }
            if (rewardImageUrl != null) {
                Log.d("ApplicationActivity", "Received reward image URL: " + rewardImageUrl);
            }
        } catch (Exception e) {
            Log.d("ApplicationActivity", "No reward or discount info found: " + e.getMessage());
        }

        // Validate key data is present
        if (userID == -1) {
            Log.w("ApplicationActivity", "No valid userID received");
        }
        if (username == null || username.isEmpty()) {
            Log.w("ApplicationActivity", "No valid username received");
            username = "User"; // Provide default username
        }
    }

    private void initializeViews() {
        // Initialize main content views
        imageSlideshow = findViewById(R.id.imageSlideshow);
        slideDotsTabLayout = findViewById(R.id.slideDotsTabLayout);
        recyclerViewServices = findViewById(R.id.recyclerViewServices);

        // Initialize reviews recycler view
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);

        // Initialize quick action boxes
        boxBookAppointment = findViewById(R.id.boxBookAppointment);
        boxViewRewards = findViewById(R.id.boxViewRewards);
        boxMyAppointments = findViewById(R.id.boxMyAppointments);
        boxTryAR = findViewById(R.id.boxTryAR);

        // Initialize other UI elements
        learnMoreButton = findViewById(R.id.learnMoreButton);
        welcomeText = findViewById(R.id.welcomeText);
        viewAllServices = findViewById(R.id.viewAllServices);
        chatbtn = findViewById(R.id.chat_btn);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(""); // Empty title
        }
    }

    private void setupWelcomeMessage() {
        if (username != null && !username.isEmpty()) {
            welcomeText.setText("Hello, " + username + "!");
        } else {
            welcomeText.setText("Hello, User!");
        }
    }

    private void setupActionBoxes() {
        // Book Appointment Box
        boxBookAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(ApplicationActivity.this, SelectNailTechActivity.class);
            intent.putExtra("userID", userID);
            Log.d("ApplicationActivity", "Book Appointment box clicked, passing userID: " + userID);
            startActivity(intent);
        });

        // View Rewards Box
        boxViewRewards.setOnClickListener(v -> {
            Intent intent = new Intent(ApplicationActivity.this, DiscountsActivity.class);
            intent.putExtra("userID", userID);
            intent.putExtra("username", username);
            intent.putExtra("userPoints", userPoints);
            Log.d("ApplicationActivity", "View Rewards box clicked, passing userID: " + userID);
            discountsLauncher.launch(intent);
        });

        // My Appointments Box
        boxMyAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(ApplicationActivity.this, AllAppointmentsActivity.class);
            intent.putExtra("userID", userID);
            intent.putExtra("username", username);
            intent.putExtra("userPoints", userPoints);

            // Add active reward information to the intent if available
            if (rewardTitle != null && !rewardTitle.isEmpty()) {
                intent.putExtra("rewardTitle", rewardTitle);
                Log.d("ApplicationActivity", "Passing rewardTitle to AllAppointmentsActivity: " + rewardTitle);
            }
            if (rewardImageUrl != null && !rewardImageUrl.isEmpty()) {
                intent.putExtra("rewardImageUrl", rewardImageUrl);
                Log.d("ApplicationActivity", "Passing rewardImageUrl to AllAppointmentsActivity: " + rewardImageUrl);
            }

            Log.d("ApplicationActivity", "My Appointments box clicked, passing userID: " + userID);
            startActivity(intent);
        });

        // Try AR Feature Box
        boxTryAR.setOnClickListener(v -> {
            Intent intent = new Intent(ApplicationActivity.this, LaptopCameraActivity.class);
            intent.putExtra("userID", userID);
            Log.d("ApplicationActivity", "Try AR Feature box clicked, passing userID: " + userID);
            startActivity(intent);
        });
    }

    private void setupReviewsSection() {
        // Initialize the reviews list
        reviewList = new ArrayList<>();

        // Set up RecyclerView for reviews with limited items
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));

        // Create and set adapter
        reviewAdapter = new ReviewAdapter(this, reviewList);
        recyclerViewReviews.setAdapter(reviewAdapter);

        // Find or add "View All Reviews" button in your layout
        TextView viewAllReviews = findViewById(R.id.viewAllReviews);
        if (viewAllReviews != null) {
            viewAllReviews.setVisibility(View.VISIBLE);
            viewAllReviews.setOnClickListener(v -> {
                Intent intent = new Intent(ApplicationActivity.this, ViewAllReviewsActivity.class);
                startActivity(intent);
            });
        }

        // Fetch reviews with limit
        fetchReviews();
    }

    private void fetchReviews() {
        String url = "http://coms-3090-020.class.las.iastate.edu:8080/api/rating/get/allReviews";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            parseReviews(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ApplicationActivity.this,
                                    "Error parsing reviews", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ApplicationActivity", "Error fetching reviews: " + error.toString());
                        Toast.makeText(ApplicationActivity.this,
                                "Failed to load reviews", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add request to queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }

    // Method to parse reviews from JSON response
    private void parseReviews(JSONArray jsonArray) throws JSONException {
        reviewList.clear();

        // Only take the first 3 reviews
        int reviewsToShow = Math.min(jsonArray.length(), 3);

        for (int i = 0; i < reviewsToShow; i++) {
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

        // Update the adapter
        reviewAdapter.notifyDataSetChanged();

        // Log the number of reviews loaded
        Log.d("ApplicationActivity", "Loaded " + reviewList.size() + " of " +
                jsonArray.length() + " total reviews");
    }

    private void setupLearnMoreButton() {
        learnMoreButton.setOnClickListener(v -> {
            Intent intent = new Intent(ApplicationActivity.this, LearnMoreActivity.class);
            intent.putExtra("userID", userID);
            startActivity(intent);
        });
    }

    private void setupViewAllServices() {
        viewAllServices.setOnClickListener(v -> {
            Intent intent = new Intent(ApplicationActivity.this, ViewAllServices.class);
            intent.putExtra("userID", userID);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        bottomNavView = findViewById(R.id.bottom_nav);
        bottomNavView.setSelectedItemId(R.id.nav_home);
        bottomNavView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home screen
                return true;
            } else if (itemId == R.id.nav_appointments) {
                Intent intent = new Intent(ApplicationActivity.this, AllAppointmentsActivity.class);
                intent.putExtra("userID", userID);
                intent.putExtra("username", username);
                intent.putExtra("userPoints", userPoints);

                // Add active reward information to the intent
                if (rewardTitle != null && !rewardTitle.isEmpty()) {
                    intent.putExtra("rewardTitle", rewardTitle);
                    Log.d("ApplicationActivity", "Passing rewardTitle to AllAppointmentsActivity: " + rewardTitle);
                }

                if (rewardImageUrl != null && !rewardImageUrl.isEmpty()) {
                    intent.putExtra("rewardImageUrl", rewardImageUrl);
                    Log.d("ApplicationActivity", "Passing rewardImageUrl to AllAppointmentsActivity: " + rewardImageUrl);
                }

                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_explore) {
                Intent intent = new Intent(ApplicationActivity.this, LaptopCameraActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(ApplicationActivity.this, ViewProfileActivity.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
                return true;
            }
            else if(itemId == R.id.nav_messages) {


                // Navigate to ChatActivity
                Intent intent = new Intent(ApplicationActivity.this, ViewMessagesActivity.class);
                intent.putExtra("userID", userID);
                intent.putExtra("username", username);
                intent.putExtra("nailtech", "NailTechName");
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void setupChatButton() {
        chatbtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatBotActivity.class);
            intent.putExtra("userID", userID);
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }



    /**
     * A helper method that sets up the image slideshow banner seen on the home page.
     */
    private void setupImageSlideshow() {
        // Initialize slide items list
        slideItems = new ArrayList<>();

        // Add slide items with proper parameters
        slideItems.add(new ImageSlideAdapter.SlideItem(R.drawable.exploregallery, GalleryActivity.class, userID));
        slideItems.add(new ImageSlideAdapter.SlideItem(R.drawable.viewservices, ViewAllServices.class, userID));
        slideItems.add(new ImageSlideAdapter.SlideItem(R.drawable.viewspecialoffers, DiscountsActivity.class, userID));
        slideItems.add(new ImageSlideAdapter.SlideItem(R.drawable.nailbooking, SelectServiceActivity.class, userID));
        slideItems.add(new ImageSlideAdapter.SlideItem(R.drawable.discount, DiscountsActivity.class, userID));
        slideItems.add(new ImageSlideAdapter.SlideItem(R.drawable.viewnailtech, SelectBookingTimeActivity.class, userID));

        // Create and set adapter
        imageSlideAdapter = new ImageSlideAdapter(this, slideItems);
        imageSlideshow.setAdapter(imageSlideAdapter);

        // Connect TabLayout with ViewPager2 (dot indicators)
        new TabLayoutMediator(slideDotsTabLayout, imageSlideshow,
                (tab, position) -> {
                    // No text for tabs
                }).attach();

        // Set page change callback for auto-scroll control
        imageSlideshow.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;

                // Reset auto-scroll when user manually changes page
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, AUTO_SCROLL_DELAY);
            }
        });
    }

    /**
     * A helper method that sets up the services recycler view banner.
     */
    private void setupServicesRecyclerView() {
        // Set up horizontal layout
        recyclerViewServices.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Create services list
        List<ServiceModel> serviceItems = new ArrayList<>();

        // Regular Manicure
        serviceItems.add(new ServiceModel(
                1,  // id
                "Regular Manicure",  // title
                "$35.99",  // shortDescription (using price as short description)
                "A classic manicure that includes nail shaping, cuticle care, and polish application.",  // longDescription
                R.drawable.ic_manicure  // iconResourceId
        ));

        // Gel Manicure
        serviceItems.add(new ServiceModel(
                2,  // id
                "Gel Manicure",  // title
                "$45.99",  // shortDescription
                "Long-lasting gel polish that cures under UV light for a chip-free finish that lasts up to 2 weeks.",  // longDescription
                R.drawable.ic_gel  // iconResourceId
        ));

        // Acrylic Nails
        serviceItems.add(new ServiceModel(
                3,  // id
                "Acrylic Nails",  // title
                "$55.99",  // shortDescription
                "Artificial nail extensions created by mixing a liquid monomer and powder polymer for added length and strength.",  // longDescription
                R.drawable.ic_polish  // iconResourceId
        ));

        // Nail Art
        serviceItems.add(new ServiceModel(
                4,  // id
                "Nail Art",  // title
                "$25.99",  // shortDescription
                "Custom designs, patterns, and decorations applied to natural or artificial nails.",  // longDescription
                R.drawable.ic_extra  // iconResourceId
        ));

        // Pedicure
        serviceItems.add(new ServiceModel(
                5,  // id
                "Pedicure",  // title
                "$39.99",  // shortDescription
                "A foot treatment that includes soaking, exfoliation, nail trimming, and polish application.",  // longDescription
                R.drawable.ic_pedicure  // iconResourceId
        ));

        // Create and set adapter with a service selection listener
        ServiceAdapter serviceAdapter = new ServiceAdapter(this, serviceItems, new ServiceAdapter.OnServiceSelectedListener() {
            @Override
            public void onServiceSelected(int serviceId, boolean isSelected) {
                // Handle service selection here
                Log.d("ApplicationActivity", "Service " + serviceId + " selected: " + isSelected);

                // You might want to store selected services or update UI based on selection
                // For example:
                Toast.makeText(ApplicationActivity.this,
                        "Service " + serviceId + " " + (isSelected ? "selected" : "unselected"),
                        Toast.LENGTH_SHORT).show();
            }
        });

        recyclerViewServices.setAdapter(serviceAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restart auto-scroll for the slideshow
        sliderHandler.postDelayed(sliderRunnable, AUTO_SCROLL_DELAY);

        // Set the selected item to home when returning to this activity
        if (bottomNavView != null) {
            bottomNavView.setSelectedItemId(R.id.nav_home);
        }

        // Check for active rewards
        checkActiveRewards();

        // Refresh reviews data
        fetchReviews();
    }

    /**
     * Check if user has active rewards and update UI accordingly
     */
    private void checkActiveRewards() {
        if (rewardTitle != null && !rewardTitle.isEmpty()) {
            // User has an active reward
            Log.d("ApplicationActivity", "User has active reward: " + rewardTitle);
            // You could update UI to show active reward badge or indicator
        } else {
            // No active reward
            Log.d("ApplicationActivity", "No active reward");
        }
    }

    // Auto-scroll runnable for main slideshow
    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (imageSlideshow == null) return;

            if (currentPage == slideItems.size() - 1) {
                currentPage = 0;
            } else {
                currentPage++;
            }

            imageSlideshow.setCurrentItem(currentPage, true);
            sliderHandler.postDelayed(this, AUTO_SCROLL_DELAY);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        // Remove callbacks for slideshow
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    // Handle results from the DiscountsActivity
    private final ActivityResultLauncher<Intent> discountsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Log the result code
                        Log.d("ApplicationActivity", "Result code: " + result.getResultCode());

                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                processDiscountResult(data);
                            } else {
                                Log.d("ApplicationActivity", "Result data is null");
                            }
                        } else {
                            Log.d("ApplicationActivity", "Result was not OK, no data to process");
                        }
                    });

    private void processDiscountResult(Intent data) {
        // Log all extras for debugging
        Bundle extras = data.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Log.d("ApplicationActivity", "Extra key: " + key + ", value: " + extras.get(key));
            }
        }

        // Get updated points with detailed logging
        int oldPoints = userPoints;
        userPoints = data.getIntExtra(DiscountsActivity.EXTRA_USER_POINTS, userPoints);
        Log.d("ApplicationActivity", "Points before: " + oldPoints + ", Points after: " + userPoints);

        // Get reward title and image URL if available
        String rewardTitle = data.getStringExtra("REWARD_TITLE");
        String rewardImageUrl = data.getStringExtra("REWARD_IMAGE_URL");

        // Update reward info
        updateRewardInfo(rewardTitle, rewardImageUrl);
    }

    private void updateRewardInfo(String rewardTitle, String rewardImageUrl) {
        // Log whether values are present
        Log.d("ApplicationActivity", "Reward title present: " + rewardTitle);
        Log.d("ApplicationActivity", "Reward image URL present: " + rewardImageUrl);

        if (rewardTitle != null && !rewardTitle.isEmpty()) {
            this.rewardTitle = rewardTitle;
            Log.d("ApplicationActivity", "Received reward title: " + rewardTitle);
        } else {
            Log.d("ApplicationActivity", "No reward title received or empty string");
        }

        if (rewardImageUrl != null && !rewardImageUrl.isEmpty()) {
            this.rewardImageUrl = rewardImageUrl;
            Log.d("ApplicationActivity", "Received reward image URL: " + rewardImageUrl);
        } else {
            Log.d("ApplicationActivity", "No reward image URL received or empty string");
        }

        // Show a toast to indicate the user has selected a reward
        if (rewardTitle != null) {
            Toast.makeText(this, "Selected reward: " + rewardTitle,
                    Toast.LENGTH_SHORT).show();
            Log.d("ApplicationActivity", "Toast shown for reward: " + rewardTitle);
        }
    }
}