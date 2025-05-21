package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectNailTechActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTechnicians;
    private ProgressBar progressBar;
    private CardView cardAnyTechnician;
    private Button btnBookAnyTechnician;
    private Toolbar toolbar;
    private BottomNavigationView bottomNav;

    private List<Technician> technicianList = new ArrayList<>();
    private TechnicianAdapter adapter;

    // Initialize as null instead of directly accessing intent
    private int[] receivedIntArray = null;

    // API endpoints
    private static final String BASE_URL = "https://your-api-endpoint.com/api";
    private static final String GET_TECHNICIANS = "/available-technicians";

    private long userID = -1;

    private String rewardTitle, rewardImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_nailtech);

        userID = getIntent().getLongExtra("userID", -1);

        Log.d("SelectNailTechActivity", "userID: " + userID);

        // if user uses a discount or reward
        try {
            rewardTitle = getIntent().getStringExtra("REWARD_TITLE");
            rewardImageUrl = getIntent().getStringExtra("REWARD_IMAGE_URL");

            // log for debugging
            Log.d("ApplicationActivity", "Received reward title: " + rewardTitle);
            Log.d("ApplicationActivity", "Received reward image URL: " + rewardImageUrl);
        }
        catch (Exception e)
        {
            Log.d("ApplicationActivity", "No reward or discount used");
            // do nothing
        }
        // Initialize receivedIntArray only if it's provided in the intent
        if (getIntent().hasExtra("SELECTED_SERVICES")) {
            receivedIntArray = getIntent().getIntArrayExtra("SELECTED_SERVICES");
            Log.d("SelectNailTechActivity", "Received Int Array: " + Arrays.toString(receivedIntArray));
        } else {
            // No services selected yet, this is normal at this point in the flow
            Log.d("SelectNailTechActivity", "No services selected yet");
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        fetchTechnicians();
    }

    private void initViews() {
        recyclerViewTechnicians = findViewById(R.id.recyclerViewTechnicians);
        progressBar = findViewById(R.id.progressBarTechnicians);
        cardAnyTechnician = findViewById(R.id.cardAnyTechnician);
        btnBookAnyTechnician = findViewById(R.id.btnBookAnyTechnician);
        toolbar = findViewById(R.id.toolbar);
        bottomNav = findViewById(R.id.bottom_nav);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupRecyclerView() {
        adapter = new TechnicianAdapter(technicianList);
        recyclerViewTechnicians.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTechnicians.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBookAnyTechnician.setOnClickListener(v -> {
            // Book with any available technician
            proceedToDateSelection(null); // null indicates "any technician"
        });
    }

    private void fetchTechnicians() {
        showLoading(true);

        // For testing - comment out when connecting to actual API
        //simulateTechniciansResponse();


        String url = "http://coms-3090-020.class.las.iastate.edu:8080/api/employee/info/name";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // Parse the response with the new structure
                        if (response.has("object") && !response.isNull("object")) {
                            // The "object" field is a JSONArray
                            JSONArray techniciansArray = response.getJSONArray("object");
                            technicianList.clear();

                            for (int i = 0; i < techniciansArray.length(); i++) {
                                JSONObject techObj = techniciansArray.getJSONObject(i);

                                Technician technician = new Technician();
                                technician.setId(techObj.getLong("id"));
                                technician.setName(techObj.getString("message"));

                                // Set default values since these fields are not in your JSON
                                technician.setSpecialty("Nail Technician");
                                technician.setExperience(0);
                                technician.setRating(5.0);
                                technician.setPrice(0); // Set price to 0 as requested
                                technician.setImageUrl("");
                                technician.setAvailable(true);

                                technicianList.add(technician);
                            }

                            // Update UI
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.e("SelectNailTechActivity", "Response doesn't contain 'object' array");
                            Toast.makeText(SelectNailTechActivity.this,
                                    "Invalid server response format", Toast.LENGTH_SHORT).show();
                        }

                        showLoading(false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("SelectNailTechActivity", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(SelectNailTechActivity.this,
                                "Error parsing technicians data", Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    }
                },
                error -> {
                    error.printStackTrace();
                    Log.e("SelectNailTechActivity", "Volley error: " + error.toString());
                    Toast.makeText(SelectNailTechActivity.this,
                            "Error loading technicians", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                // Add any authentication headers if needed
                return headers;
            }
        };

// Add the request to the request queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);

        }


    private void simulateTechniciansResponse() {
        // For testing only
        new Handler().postDelayed(() -> {
            technicianList.clear();

            try {
                // Create a sample JSON response that matches your desired structure
                JSONObject response = new JSONObject();
                JSONArray techArray = new JSONArray();

                JSONObject tech1 = new JSONObject();
                tech1.put("message", "jacobDang");
                tech1.put("id", 1);
                techArray.put(tech1);

                JSONObject tech2 = new JSONObject();
                tech2.put("message", "Bao");
                tech2.put("id", 2);
                techArray.put(tech2);

                JSONObject tech3 = new JSONObject();
                tech3.put("message", "Han");
                tech3.put("id", 3);
                techArray.put(tech3);

                response.put("object", techArray);

                // Now parse this response as if it came from the network
                parseJsonResponse(response);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("SelectNailTechActivity", "Error creating test JSON", e);
            }

            // Update UI
            adapter.notifyDataSetChanged();
            showLoading(false);
        }, 1000);
    }

    private void parseJsonResponse(JSONObject response) {
        try {
            // Parse the response according to your new structure
            JSONArray techniciansArray = response.getJSONArray("object");

            for (int i = 0; i < techniciansArray.length(); i++) {
                JSONObject techObj = techniciansArray.getJSONObject(i);

                Technician technician = new Technician();
                technician.setId(techObj.getLong("id"));
                technician.setName(techObj.getString("message")); // Use "message" for name
                technician.setSpecialty("Nail Technician"); // Default value
                technician.setExperience(0); // Default value
                technician.setRating(5.0); // Default value
                technician.setPrice(0); // Set price to 0 as requested
                technician.setImageUrl(""); // Default empty image URL
                technician.setAvailable(true); // Default to available

                technicianList.add(technician);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("SelectNailTechActivity", "Error parsing technicians data", e);
            // Toast.makeText(SelectNailTechActivity.this, "Error parsing technicians data", Toast.LENGTH_SHORT).show();
        }
    }
    private void proceedToDateSelection(Technician technician) {
        Intent intent = new Intent(this, SelectBookingDate.class);

        if (technician != null) {
            // Selected a specific technician
            intent.putExtra("TECHNICIAN_ID", technician.getId());
            intent.putExtra("TECHNICIAN_NAME", technician.getName());
            intent.putExtra("TECHNICIAN_PRICE", technician.getPrice());

            // if user uses a discount or reward
            if(rewardTitle != null && rewardImageUrl != null)
            {
                intent.putExtra("REWARD_TITLE", rewardTitle);
                intent.putExtra("REWARD_IMAGE_URL", rewardImageUrl);

                // log for debugging
                Log.d("SelectNailTechActivity", "Received reward title: " + rewardTitle);
                Log.d("SelectNailTechActivity", "Received reward image URL: " + rewardImageUrl);
            }

            Log.d("SelectNailTechActivity", "TECHNICIAN_ID: " + technician.getId());
            Log.d("SelectNailTechActivity", "TECHNICIAN_NAME: " + technician.getName());
            Log.d("SelectNailTechActivity", "TECHNICIAN_PRICE: " + technician.getPrice());
        } else {
            // Selected "any technician" option
            intent.putExtra("ANY_TECHNICIAN", true);
        }

        // Pass user ID
        intent.putExtra("userID", userID);

        startActivity(intent);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Technician Adapter
    private class TechnicianAdapter extends RecyclerView.Adapter<TechnicianAdapter.TechnicianViewHolder> {

        private List<Technician> technicians;

        public TechnicianAdapter(List<Technician> technicians) {
            this.technicians = technicians;
        }

        @NonNull
        @Override
        public TechnicianViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_technician, parent, false);
            return new TechnicianViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TechnicianViewHolder holder, int position) {
            Technician technician = technicians.get(position);

            // Set the technician name
            holder.tvTechnicianName.setText(technician.getName());

            // Set the price
            holder.tvPrice.setText("$" + (int)technician.getPrice());

            // Load image with Glide
            if (technician.getImageUrl() != null && !technician.getImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(technician.getImageUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(holder.imgTechnician);
            }

            // Set book button click listener
            holder.btnBookTechnician.setOnClickListener(v -> {
                // post data to server
                postData(technician);
                proceedToDateSelection(technician);
            });
        }

        @Override
        public int getItemCount() {
            return technicians.size();
        }

        class TechnicianViewHolder extends RecyclerView.ViewHolder {
            ImageView imgTechnician;
            TextView tvTechnicianName, tvPrice;
            Button btnBookTechnician;

            TechnicianViewHolder(@NonNull View itemView) {
                super(itemView);
                imgTechnician = itemView.findViewById(R.id.imgTechnician);
                tvTechnicianName = itemView.findViewById(R.id.tvTechnicianName);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                btnBookTechnician = itemView.findViewById(R.id.btnBookTechnician);
            }
        }

        private void postData(Technician technician) {
            // Create request params
            JSONObject body = new JSONObject();
            try {
                body.put("id", userID);
                // Only include serviceId if receivedIntArray is not null
                if (receivedIntArray != null) {
                    body.put("serviceId", receivedIntArray);
                }
                body.put("employeeId", technician.getId());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Make POST request
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    BASE_URL,
                    body,
                    response -> {
                        try {
                            boolean success = response.getBoolean("success");
                            String message = response.getString("message");

                            // Hide loading
                            showLoading(false);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            // Toast.makeText(SelectNailTechActivity.this, "Error selecting employee", Toast.LENGTH_SHORT).show();
                            showLoading(false);
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        //Toast.makeText(SelectNailTechActivity.this, "Error selecting employee", Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    // Add any authentication headers if needed
                    // headers.put("Authorization", "Bearer " + yourAuthToken);
                    return headers;
                }
            };

            // Add the request to the request queue
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
        }
    }

    // Technician Model
    public static class Technician {
        private long id;
        private String name;
        private String specialty;
        private int experience;
        private double rating;
        private double price;
        private String imageUrl;
        private boolean available;

        // Getters and Setters
        public long getId() { return id; }
        public void setId(long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getSpecialty() { return specialty; }
        public void setSpecialty(String specialty) { this.specialty = specialty; }

        public int getExperience() { return experience; }
        public void setExperience(int experience) { this.experience = experience; }

        public double getRating() { return rating; }
        public void setRating(double rating) { this.rating = rating; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }
}