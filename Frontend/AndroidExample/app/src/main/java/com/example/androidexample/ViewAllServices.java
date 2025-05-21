package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidUI.ServiceAdapter;
import com.example.androidUI.ServiceModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewAllServices extends AppCompatActivity implements ServiceAdapter.OnServiceSelectedListener {

    private static final String TAG = "ViewAllServices";
    private static final String SERVICES_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/services";

    private RecyclerView servicesRecyclerView;
    private ServiceAdapter serviceAdapter;
    private List<ServiceModel> serviceList;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_services);

        // Initialize UI components
        servicesRecyclerView = findViewById(R.id.servicesRecyclerView);
        backButton = findViewById(R.id.backButton);

        // Setup RecyclerView
        serviceList = new ArrayList<>();
        serviceAdapter = new ServiceAdapter(this, serviceList, this);
        servicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        servicesRecyclerView.setAdapter(serviceAdapter);

        // Set click listeners
        backButton.setOnClickListener(v -> finish());

        // Load services
        fetchServicesFromApi();
    }

    @Override
    public void onServiceSelected(int serviceId, boolean isSelected) {
        // This method is required by the interface but we don't need
        // to do anything specific with it for just viewing services
    }

    private void fetchServicesFromApi() {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                SERVICES_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            parseServicesResponse(response.toString());
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage());
                            Toast.makeText(ViewAllServices.this,
                                    "Error loading services",
                                    Toast.LENGTH_SHORT).show();
                            // Fallback to sample data
                            loadSampleServices();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error: " + error.toString());
                        Toast.makeText(ViewAllServices.this,
                                "Error connecting to server",
                                Toast.LENGTH_SHORT).show();
                        // Fallback to sample data
                        loadSampleServices();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                return headers;
            }
        };

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void parseServicesResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray servicesArray = jsonObject.getJSONArray("object");

            for (int i = 0; i < servicesArray.length(); i++) {
                JSONObject serviceObject = servicesArray.getJSONObject(i);
                String name = serviceObject.getString("service_name");
                int price = serviceObject.getInt("price");
                int duration = serviceObject.getInt("duration");

                // Create descriptions
                String shortDescription = duration + " min • $" + price;
                String longDescription = "Price: $" + price + "\nDuration: " + duration + " minutes";

                // Get icon
                int iconResourceId = getIconResourceForService(name);

                // Create ServiceModel
                ServiceModel service = new ServiceModel(
                        i + 1,
                        name,
                        shortDescription,
                        longDescription,
                        iconResourceId
                );
                serviceList.add(service);
            }

            // Notify adapter
            serviceAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e.getMessage());
            loadSampleServices();
        }
    }

    private void loadSampleServices() {
        serviceList.clear();
        parseServicesResponse(getSampleResponse());
    }

    private int getIconResourceForService(String serviceName) {
        // This is a placeholder - replace with your actual icons
        return R.drawable.ic_extra;
    }

    private String getSampleResponse() {
        return "{\"object\":[{\"service_name\":\"Polish Change\",\"price\":100,\"duration\":45},{\"service_name\":\"Gel Builder\",\"price\":110,\"duration\":35},{\"service_name\":\"Solar Acrylic\",\"price\":150,\"duration\":45},{\"service_name\":\"Manicure\",\"price\":70,\"duration\":60},{\"service_name\":\"Pedicure\",\"price\":50,\"duration\":75},{\"service_name\":\"Dipping Powder\",\"price\":120,\"duration\":90},{\"service_name\":\"Extras\",\"price\":70,\"duration\":45}]}";
    }
}