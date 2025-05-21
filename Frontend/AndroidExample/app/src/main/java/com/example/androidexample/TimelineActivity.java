package com.example.androidexample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidUI.TimelineAdapter;
import com.example.androidUI.TimelinePost;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimelineActivity extends AppCompatActivity {
    private static final String TAG = "TimelineActivity";

    // API endpoints
    private static final String BASE_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/notify";
    private static final String GET_POSTS_URL = BASE_URL + "/gallery";
    private static final String CREATE_POST_URL = BASE_URL + "/employee/upload";

    private RecyclerView recyclerView;
    private TimelineAdapter adapter;
    private List<TimelinePost> timelinePosts;
    private FloatingActionButton backBtn;
    private FloatingActionButton createPostFab;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    // Employee stuff
    private long userID;
    private String username;
    private String email;

    // Create post components
    private View createPostLayout;
    private EditText messageInput;
    private ImageView imagePreview;
    private FloatingActionButton submitPostFab;
    private FloatingActionButton selectImageFab;
    private Uri selectedImageUri = null;
    private Bitmap selectedImageBitmap = null;

    // Activity result launcher for image picking
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            selectedImageUri = imageUri;
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(), imageUri);
                            imagePreview.setImageBitmap(selectedImageBitmap);
                            imagePreview.setVisibility(View.VISIBLE);
                        } catch (IOException e) {
                            Log.e(TAG, "Error loading image: " + e.getMessage());
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_timeline);

        // Initialize views
        recyclerView = findViewById(R.id.timeline_recycler_view);
        backBtn = findViewById(R.id.timeline_back_btn);
        createPostFab = findViewById(R.id.create_post_fab);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        progressBar = findViewById(R.id.progress_bar);

        // Create post views
        createPostLayout = findViewById(R.id.create_post_layout);
        messageInput = findViewById(R.id.message_input);
        imagePreview = findViewById(R.id.image_preview);
        submitPostFab = findViewById(R.id.submit_post_fab);
        selectImageFab = findViewById(R.id.select_image_fab);

        // Initialize timeline posts list
        timelinePosts = new ArrayList<>();

        // Initialize adapter
        adapter = new TimelineAdapter(this, timelinePosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set up back button
        backBtn.setOnClickListener(v -> finish());

        // Set up create post FAB
        createPostFab.setOnClickListener(v -> showCreatePostLayout());

        // Set up submit post button
        submitPostFab.setOnClickListener(v -> submitPost());

        // Set up select image button
        selectImageFab.setOnClickListener(v -> openImagePicker());

        // Set up swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::fetchTimelinePosts);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userID = extras.getLong("userID", -1);
            username = extras.getString("username", "");
            email = extras.getString("email", "");

            // Log received data
            Log.d(TAG, "From Intent - id: " + userID);
            Log.d(TAG, "From Intent - Username: " + username);
            Log.d(TAG, "From Intent - Email: " + email);
        }

        // Fetch posts initially
        fetchTimelinePosts();
    }

    private void fetchTimelinePosts() {
        showLoading(true);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                GET_POSTS_URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        timelinePosts.clear();

                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject postObject = response.getJSONObject(i);

                                String id = postObject.optString("id", "");
                                String employeeName = postObject.optString("employeeName", "Unknown");
                                // Use "caption" instead of "message" to match backend API
                                String message = postObject.optString("caption", "");
                                String imageUrl = postObject.optString("imageURL", "");
                                String timestamp = postObject.optString("timestamp", "");

                                // Create post with image URL - Glide in the adapter will handle loading
                                TimelinePost post = new TimelinePost(id, employeeName, message, imageUrl, timestamp);
                                timelinePosts.add(post);
                            }

                            // Notify adapter to refresh the RecyclerView with new data
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "Loaded " + timelinePosts.size() + " posts");
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing posts: " + e.getMessage());
                            Toast.makeText(TimelineActivity.this,
                                    "Error loading posts", Toast.LENGTH_SHORT).show();
                        }

                        showLoading(false);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching posts: " + error.toString());
                        Toast.makeText(TimelineActivity.this,
                                "Network error. Couldn't load posts.", Toast.LENGTH_SHORT).show();
                        showLoading(false);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Add authorization headers if needed
                // headers.put("Authorization", "Bearer " + userToken);
                return headers;
            }
        };

        // Add request to queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }

    private void showCreatePostLayout() {
        createPostLayout.setVisibility(View.VISIBLE);
        createPostFab.hide();
    }

    private void hideCreatePostLayout() {
        createPostLayout.setVisibility(View.GONE);
        createPostFab.show();

        // Reset form
        messageInput.setText("");
        imagePreview.setImageBitmap(null);
        imagePreview.setVisibility(View.GONE);
        selectedImageUri = null;
        selectedImageBitmap = null;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void submitPost() {
        String caption = messageInput.getText().toString().trim(); // Changed name from message to caption

        if (caption.isEmpty() && selectedImageBitmap == null) {
            Toast.makeText(this, "Please add a caption or image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        showLoading(true);

        // If there's an image, upload with multipart request
        if (selectedImageBitmap != null) {
            uploadWithImage(caption);
        } else {
            // Text-only post
            uploadTextOnly(caption);
        }
    }

    private void uploadTextOnly(String caption) {
        // Base64-encoded 1x1 transparent PNG
        String base64Image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=";
        byte[] imageData = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);

        // Create MultipartRequest with the placeholder image
        MultipartRequest multipartRequest = new MultipartRequest(
                Request.Method.POST,
                CREATE_POST_URL,
                imageData, // now we're passing placeholder image data
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        handleSuccessfulUpload();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleUploadError(error);
                    }
                }
        );

        // Add text parameters
        Map<String, String> params = new HashMap<>();
        params.put("employeeName", username);
        params.put("caption", caption);
        multipartRequest.setParams(params);

        // Add request to queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(multipartRequest);
    }


    private void uploadWithImage(String caption) {
        // Convert bitmap to byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] imageData = byteArrayOutputStream.toByteArray();

        // Create MultipartRequest
        MultipartRequest multipartRequest = new MultipartRequest(
                Request.Method.POST,
                CREATE_POST_URL,
                imageData,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        handleSuccessfulUpload();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleUploadError(error);
                    }
                }
        );

        // Add caption and employee info as parameters
        Map<String, String> params = new HashMap<>();
        params.put("employeeName", username);
        params.put("caption", caption);
        multipartRequest.setParams(params);

        // Add request to queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(multipartRequest);
    }

    private void handleSuccessfulUpload() {
        showLoading(false);
        hideCreatePostLayout();

        // Show success message
        Snackbar.make(recyclerView, "Post uploaded successfully!", Snackbar.LENGTH_SHORT).show();

        // Refresh timeline to show new post
        fetchTimelinePosts();
    }

    private void handleUploadError(VolleyError error) {
        showLoading(false);

        // Get more specific error message if possible
        String errorMessage = "Network error. Couldn't upload post.";

        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null && networkResponse.data != null) {
            try {
                String errorData = new String(networkResponse.data);
                Log.e(TAG, "Error response: " + errorData);
                if (!errorData.isEmpty()) {
                    errorMessage = "Error: " + errorData;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing error response: " + e.getMessage());
            }
        }

        Log.e(TAG, "Error uploading post: " + error.toString());
        Toast.makeText(TimelineActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}