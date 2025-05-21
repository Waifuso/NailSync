package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidUI.ServiceAdapter;
import com.example.androidUI.ServiceItemDecoration;
import com.example.androidUI.ServiceModel;
import com.example.androidexample.R;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectServiceActivity extends AppCompatActivity implements ServiceAdapter.OnServiceSelectedListener {

    private RecyclerView servicesRecyclerView;
    private ServiceAdapter serviceAdapter;
    private List<ServiceModel> serviceList;

    private Button nextButton;
    private Set<Integer> selectedServices = new HashSet<>();
    private ImageButton backButton;

    private long userID = -1;

    // Technician data
    private long technicianId = -1;
    private String technicianName = "";
    private double technicianPrice = 0.0;
    private boolean isAnyTechnician = false;

    // Date data
    private String selectedDate = "";
    private String rewardTitle, rewardImageUrl;
    private ArrayList<String> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_service);

        // Get user ID from intent
        userID = getIntent().getLongExtra("userID", -1);
        Log.d("SelectServiceActivity", "userID: " + userID);

        try {
            rewardTitle = getIntent().getStringExtra("REWARD_TITLE");
            rewardImageUrl = getIntent().getStringExtra("REWARD_IMAGE_URL");

            Log.d("BookingIntent", "rewardTitle: " + rewardTitle);
            Log.d("BookingIntent", "rewardImageUrl: " + rewardImageUrl);
        } catch (Exception e) {
            Log.d("BookingIntent", "No reward or discount used");
        }

        // Get technician data from intent
        if (getIntent().hasExtra("ANY_TECHNICIAN")) {
            isAnyTechnician = getIntent().getBooleanExtra("ANY_TECHNICIAN", false);
        } else {
            technicianId = getIntent().getLongExtra("TECHNICIAN_ID", -1);
            technicianName = getIntent().getStringExtra("TECHNICIAN_NAME");
            technicianPrice = getIntent().getDoubleExtra("TECHNICIAN_PRICE", 0.0);
        }

        // Get date from intent
        selectedDate = getIntent().getStringExtra("SELECTED_DATE");

        mapMessages();  // Initialize the service descriptions
        initializeUI();
        setupServiceList();

        // Set the click listener for the next button
        nextButton.setOnClickListener(v -> {
            if (!selectedServices.isEmpty()) {
                // Convert Set to ArrayList
                ArrayList<Integer> selectedServicesList = new ArrayList<>(selectedServices);

                int[] selectedServicesArray = new int[selectedServicesList.size()];

                // Populate array with selected services
                for (int i = 0; i < selectedServicesList.size(); i++) {
                    selectedServicesArray[i] = selectedServicesList.get(i);
                }

                // Navigate to SelectBookingTimeActivity with all collected data
                Intent intent = new Intent(SelectServiceActivity.this, SelectBookingTimeActivity.class);
                intent.putExtra("SELECTED_SERVICES", selectedServicesArray);
                intent.putExtra("userID", userID);
                Log.d("SelectServiceActivity", "userID: " + userID);
                Log.d("SelectServiceActivity", "Selected Services: " + selectedServicesList);

                // Pass date info from previous screen
                intent.putExtra("SELECTED_DATE", selectedDate);

                // Pass reward info if available
                if (rewardTitle != null && rewardImageUrl != null) {
                    intent.putExtra("REWARD_TITLE", rewardTitle);
                    intent.putExtra("REWARD_IMAGE_URL", rewardImageUrl);
                    Log.d("SelectServiceActivity", "Passing reward title: " + rewardTitle);
                    Log.d("SelectServiceActivity", "Passing reward image URL: " + rewardImageUrl);
                }

                // Pass technician info
                if (isAnyTechnician) {
                    intent.putExtra("ANY_TECHNICIAN", true);
                } else {
                    intent.putExtra("TECHNICIAN_ID", technicianId);
                    intent.putExtra("TECHNICIAN_NAME", technicianName);
                    intent.putExtra("TECHNICIAN_PRICE", technicianPrice);

                    Log.d("SelectServiceActivity", "Technician ID: " + technicianId);
                    Log.d("SelectServiceActivity", "Technician Name: " + technicianName);
                    Log.d("SelectServiceActivity", "Technician Price: " + technicianPrice);
                }

                Log.d("SelectServiceActivity", "Selected Services: " + selectedServicesList);
                startActivity(intent);
            }
        });
    }

    /**
     * Initializes UI components from the layout.
     */
    private void initializeUI() {
        nextButton = findViewById(R.id.nextButton);
        nextButton.setEnabled(false);
        nextButton.setAlpha(0.5f);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Setup RecyclerView
        servicesRecyclerView = findViewById(R.id.servicesRecyclerView);
        servicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add spacing between items
        int verticalSpacing = getResources().getDimensionPixelSize(R.dimen.service_item_vertical_spacing);
        int horizontalSpacing = getResources().getDimensionPixelSize(R.dimen.service_item_horizontal_spacing);
        servicesRecyclerView.addItemDecoration(new ServiceItemDecoration(verticalSpacing, horizontalSpacing));
    }

    /**
     * Sets up the service list with data.
     */
    private void setupServiceList() {
        serviceList = new ArrayList<>();

        // Add services with their respective IDs, titles, and descriptions
        serviceList.add(new ServiceModel(1, "Polish Change", "Quick nail refresh", messages.get(0), R.drawable.ic_polish));
        serviceList.add(new ServiceModel(2, "Gel Builder", "Strong and flexible", messages.get(1), R.drawable.ic_gel));
        serviceList.add(new ServiceModel(3, "Solar Acrylic", "Durable and stylish", messages.get(2), R.drawable.ic_solar));
        serviceList.add(new ServiceModel(4, "Manicure", "Natural nail care", messages.get(3), R.drawable.ic_manicure));
        serviceList.add(new ServiceModel(5, "Pedicure", "Toe nail care", messages.get(4), R.drawable.ic_pedicure));
        serviceList.add(new ServiceModel(6, "Dipping Powder", "Long-lasting color", messages.get(5), R.drawable.ic_dipping));
        serviceList.add(new ServiceModel(7, "Extras", "Add-on services", messages.get(6), R.drawable.ic_extra));

        // Initialize and set the adapter
        serviceAdapter = new ServiceAdapter(this, serviceList, this);
        servicesRecyclerView.setAdapter(serviceAdapter);
    }

    @Override
    public void onServiceSelected(int serviceId, boolean isSelected) {
        if (isSelected) {
            // Simply add the service to selected services
            selectedServices.add(serviceId);
            updateNextButton();
        } else {
            // Remove the service from selected services
            selectedServices.remove(serviceId);
            updateNextButton();
        }
    }

    /**
     * Find a service by its ID.
     *
     * @param serviceId The service ID to find.
     * @return The ServiceModel if found, null otherwise.
     */
    private ServiceModel findServiceById(int serviceId) {
        for (ServiceModel service : serviceList) {
            if (service.getId() == serviceId) {
                return service;
            }
        }
        return null;
    }

    /**
     * Updates the next button's state based on selected services.
     */
    private void updateNextButton() {
        if (!selectedServices.isEmpty()) {
            nextButton.setEnabled(true);
            nextButton.setAlpha(1.0f);
        } else {
            nextButton.setEnabled(false);
            nextButton.setAlpha(0.5f);
        }
    }

    private void mapMessages() {
        // Add the service descriptions - index 0 corresponds to service ID 1
        messages.add("A quick refresh for your nails with a new coat of regular nail polish, perfect for a fast, polished look without extra treatments.");
        messages.add("A long-lasting enhancement using a gel formula that strengthens and shapes nails, providing a durable, natural-looking finish.");
        messages.add("A high-quality acrylic enhancement that gives nails strength and a natural pink-and-white appearance, with a UV-cured finish for longevity.");
        messages.add("A relaxing treatment that includes nail shaping, cuticle care, and a polish application, designed to keep your hands and nails looking their best.");
        messages.add("A soothing foot treatment that involves nail care, exfoliation, and hydration, leaving your feet feeling refreshed and looking flawless.");
        messages.add("A no-light, long-lasting nail treatment where colored powder is applied to nails, offering strength, shine, and vibrant color.");
        messages.add("Additional nail treatments or embellishments to enhance your manicure or pedicure experience.");
    }
}