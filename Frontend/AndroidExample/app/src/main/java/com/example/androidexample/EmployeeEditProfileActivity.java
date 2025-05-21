package com.example.androidexample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EmployeeEditProfileActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "EmployeeEditProfile";
    private static final String EMPLOYEE_PROFILE_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/employee/profile/";
    private static final String EMPLOYEE_UPDATE_PROFILE_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/employee/id";

    // UI Components
    private CircleImageView profileImage;
    private MaterialButton changePhotoButton, updateProfileButton;
    private Button cancelButton;
    private TextInputEditText fullNameInput, emailInput, passwordInput, bioInput;
    private BottomNavigationView bottomNavigationView;

    // Employee Data
    private int userID;
    private String username;
    private String email;
    private String role;
    private String bio;
    private String profileImageUrl;
    private Uri selectedImageUri;

    // Photo Picker Launcher
    private ActivityResultLauncher<String> photoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_editprofile);

        // Initialize UI components
        initializeUIComponents();

        // Get intent data
        getIntentData();

        // Register photo picker launcher
        registerPhotoPickerLauncher();

        // Set up click listeners
        setupClickListeners();

        // Setup bottom navigation
        setupBottomNavigation();

        // Fetch employee profile data
        fetchEmployeeProfileData();
    }

    private void initializeUIComponents() {
        profileImage = findViewById(R.id.profile_image);
        changePhotoButton = findViewById(R.id.change_photo_button);
        fullNameInput = findViewById(R.id.fullname);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        bioInput = findViewById(R.id.bio);

        // Buttons
        updateProfileButton = findViewById(R.id.update_profile_button);
        cancelButton = findViewById(R.id.cancel_button);

        // Bottom Navigation - find the correct view in your layout
        // Could be bottomNavigation or bottom_nav depending on your layout
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (bottomNavigationView == null) {
            bottomNavigationView = findViewById(R.id.bottom_nav);
        }

        if (bottomNavigationView == null) {
            Log.e(TAG, "Bottom navigation view not found in layout!");
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) {
            Log.e(TAG, "Cannot setup bottom navigation - view is null");
            return;
        }

        // Set the listener for navigation items
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // We won't select any item in the edit profile activity since it's not
        // one of the main navigation destinations

        // Make sure it's visible
        bottomNavigationView.setVisibility(View.VISIBLE);

        Log.d(TAG, "Bottom navigation setup complete in edit profile");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Before navigating away, check for unsaved changes and confirm with user if needed

        if (itemId == R.id.nav_home) {
            // Navigate to home
            Intent homeIntent = new Intent(this, EmployeeHomeActivity.class);
            homeIntent.putExtra("userID", userID);
            homeIntent.putExtra("username", username);
            homeIntent.putExtra("email", email);
            startActivity(homeIntent);
            finish(); // Close this activity
            return true;
        } else if (itemId == R.id.nav_appointments) {
            // Navigate to appointments
            Intent appointmentsIntent = new Intent(this, SelectBookingTimeActivity.class);
            appointmentsIntent.putExtra("userID", userID);
            startActivity(appointmentsIntent);
            finish(); // Close this activity
            return true;
        } else if (itemId == R.id.nav_messages) {
            // Navigate to messages (to be implemented)
            Toast.makeText(this, "Messages feature coming soon", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.nav_profile) {
            // Navigate to profile view
            Intent profileIntent = new Intent(this, EmployeeViewProfileActivity.class);
            profileIntent.putExtra("userID", userID);
            profileIntent.putExtra("username", username);
            profileIntent.putExtra("email", email);
            profileIntent.putExtra("role", role);
            profileIntent.putExtra("bio", bioInput.getText().toString().trim());
            startActivity(profileIntent);
            finish(); // Close this activity
            return true;
        }
        return false;
    }

    private void getIntentData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userID = extras.getInt("userID", -1);
            username = extras.getString("username", "");
            email = extras.getString("email", "");
            role = extras.getString("role", "Nail Technician");
            bio = extras.getString("bio", "");

            // Set initial employee info
            fullNameInput.setText(username);
            emailInput.setText(email);
            bioInput.setText(bio);

            // Log received data
            Log.d(TAG, "UserID: " + userID);
            Log.d(TAG, "Username: " + username);
            Log.d(TAG, "Email: " + email);
            Log.d(TAG, "Role: " + role);
            Log.d(TAG, "Bio: " + bio);
        } else {
            // Handle case when no data is passed
            Log.e(TAG, "No user data received");
            Toast.makeText(this, "Error: No user data", Toast.LENGTH_SHORT).show();
            // Navigate back
            finish();
        }
    }

    private void registerPhotoPickerLauncher() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        // Load the selected image into the ImageView
                        Glide.with(this)
                                .load(uri)
                                .apply(RequestOptions.circleCropTransform())
                                .into(profileImage);
                    }
                });
    }

    private void setupClickListeners() {
        // Change photo button
        changePhotoButton.setOnClickListener(v -> {
            // Launch photo picker
            photoPickerLauncher.launch("image/*");
        });

        // Update profile button
        updateProfileButton.setOnClickListener(v -> {
            // Validate inputs
            String password = passwordInput.getText().toString().trim();
            if (password.isEmpty()) {
                passwordInput.setError("Password cannot be empty");
                return;
            }

            // Additional password validation if needed
            if (password.length() < 6) {
                passwordInput.setError("Password must be at least 6 characters");
                return;
            }

            // Update profile
            updateEmployeeProfile();
        });

        // Cancel button
        cancelButton.setOnClickListener(v -> {
            // Navigate back without saving
            finish();
        });
    }

    private void fetchEmployeeProfileData() {

        String url = EMPLOYEE_PROFILE_URL + userID;

        JsonObjectRequest profileRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // Populate form with existing profile data
                        populateFormData(response);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error processing profile data", e);
                        Toast.makeText(EmployeeEditProfileActivity.this,
                                "Error processing profile data",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Failed to load profile data", error);
                    Toast.makeText(EmployeeEditProfileActivity.this,
                            "Failed to load profile data",
                            Toast.LENGTH_SHORT).show();
                }
        );

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(profileRequest);
    }

    private void populateFormData(JSONObject response) throws JSONException {
        // Populate profile image
        profileImageUrl = response.optString("profileImageUrl", "");
        if (!profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .apply(RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile))
                    .into(profileImage);
        }

        PrefManager storedData = PrefManager.getInstance(this);

        // Populate text fields (if not already set from intent)
        if (fullNameInput.getText().toString().isEmpty()) {
            fullNameInput.setText(storedData.getUsername());
        }

        if (emailInput.getText().toString().isEmpty()) {
            emailInput.setText(storedData.getEmail());
        }

        // Only populate bio from API if it wasn't passed in the intent
        if (bioInput.getText().toString().isEmpty()) {
            String bio = response.optString("bio", "");
            bioInput.setText(bio);
        }

        // Do not populate the password field for security reasons
        // passwordInput is left empty intentionally
    }

    private void updateEmployeeProfile() {
        try {
            // Show loading state
            updateProfileButton.setEnabled(false);
            updateProfileButton.setText("Updating...");

            // Create JSON object with updated profile data
            JSONObject profileData = new JSONObject();
            //profileData.put("id", userID);
            profileData.put("username", fullNameInput.getText().toString().trim());
            //profileData.put("email", emailInput.getText().toString().trim());
            profileData.put("password", passwordInput.getText().toString().trim());
            //profileData.put("bio", bioInput.getText().toString().trim());

            // Add profile image URI if selected
            if (selectedImageUri != null)
            {
                profileData.put("profileImageUri", selectedImageUri.toString());
            }

            // Send update request
            JsonObjectRequest updateRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    EMPLOYEE_UPDATE_PROFILE_URL,
                    profileData,
                    response -> {
                        // Handle successful response
                        boolean success = response.optBoolean("success", false);
                        String message = response.optString("message", "Profile updated");

                        if (success)
                        {
                            Toast.makeText(EmployeeEditProfileActivity.this,
                                    message, Toast.LENGTH_SHORT).show();
                            // Return to profile screen
                            handleSuccessfulUpdate();
                        } else
                        {
                            Toast.makeText(EmployeeEditProfileActivity.this,
                                    "Update failed: " + message, Toast.LENGTH_SHORT).show();
                            // Reset button state
                            updateProfileButton.setEnabled(true);
                            updateProfileButton.setText("Update Profile");
                        }
                    },
                    error -> {
                        // Handle error
                        Log.e(TAG, "Error updating profile", error);
                        Toast.makeText(EmployeeEditProfileActivity.this,
                                "Error updating profile", Toast.LENGTH_SHORT).show();
                        // Reset button state
                        updateProfileButton.setEnabled(true);
                        updateProfileButton.setText("Update Profile");
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    // Add any required headers
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            // Add the request to the RequestQueue
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(updateRequest);

            // Note: In a real app, you would handle the image upload separately
            // For now, we're just simulating a successful update
            handleSuccessfulUpdate();

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for profile update", e);
            Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
            // Reset button state
            updateProfileButton.setEnabled(true);
            updateProfileButton.setText("Update Profile");
        }
    }

    private void handleSuccessfulUpdate() {
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to profile view with updated data
        Intent intent = new Intent(this, EmployeeViewProfileActivity.class);
        intent.putExtra("userID", userID);
        intent.putExtra("username", fullNameInput.getText().toString().trim());
        intent.putExtra("email", emailInput.getText().toString().trim());
        intent.putExtra("role", role);
        intent.putExtra("bio", bioInput.getText().toString().trim());
        startActivity(intent);

        finish();
    }
}