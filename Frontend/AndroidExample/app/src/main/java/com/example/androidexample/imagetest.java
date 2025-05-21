package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.R;

import org.json.JSONException;
import org.json.JSONObject;

public class imagetest extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String JSON_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/images/image/loader/pinkColor";

    private RequestQueue requestQueue;
    private ImageView imageView;
    private TextView statusTextView;
    private Button loadImageButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagetest);

        // Initialize views
        imageView = findViewById(R.id.imageView);
        statusTextView = findViewById(R.id.statusTextView);
        loadImageButton = findViewById(R.id.loadImageButton);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Set up button click listener
        loadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImage();
            }
        });
    }

    private void loadImage() {
        // Update UI to show loading state
        progressBar.setVisibility(View.VISIBLE);
        statusTextView.setText("Status: Fetching image URL...");
        loadImageButton.setEnabled(false);

        // Make request to get the JSON containing the actual image URL
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                JSON_URL,
                null, // No JSON body for this GET request
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Extract the image URL from the JSON response
                            String imageUrl = response.getString("message");
                            statusTextView.setText("Status: Loading image from URL...");
                            Log.d(TAG, "Extracted image URL: " + imageUrl);

                            // Now that we have the image URL, create a request to load the image
                            fetchImageFromUrl(imageUrl);

                        } catch (JSONException e) {
                            handleError("Error parsing JSON: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError("Failed to fetch image URL: " + error.getMessage());
                    }
                }
        );

        // Add the JSON request to the queue
        requestQueue.add(jsonObjectRequest);
    }

    private void fetchImageFromUrl(String imageUrl) {
        ImageRequest imageRequest = new ImageRequest(
                imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        // Set the bitmap to your ImageView
                        imageView.setImageBitmap(bitmap);
                        statusTextView.setText("Status: Image loaded successfully!");
                        progressBar.setVisibility(View.GONE);
                        loadImageButton.setEnabled(true);
                        Log.d(TAG, "Image loaded successfully");
                    }
                },
                0, // Max width (0 = no limit)
                0, // Max height (0 = no limit)
                ImageView.ScaleType.FIT_CENTER,
                Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError("Error loading image: " + error.getMessage());
                    }
                }
        );

        // Add the image request to the queue
        requestQueue.add(imageRequest);
    }

    private void handleError(String errorMessage) {
        Log.e(TAG, errorMessage);
        statusTextView.setText("Status: " + errorMessage);
        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
        loadImageButton.setEnabled(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Cancel all pending requests when activity stops
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}