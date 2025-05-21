package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class EmployeeViewProfileActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "EmployeeViewProfile";

    // UI Components
    private Toolbar toolbar;
    private CircleImageView profileImage;
    private FloatingActionButton editPhotoButton;
    private TextView profileUsername;
    private TextView profileRole;
    private TextView profileEmail;
    private TextView profileBio;
    private ChipGroup specializationChipGroup;
    private MaterialButton editProfileButton;
    private MaterialButton logoutButton;
    private MaterialButton deleteUserButton;
    private BottomNavigationView bottomNavigationView;

    // Employee Data
    private int userID;
    private String username;
    private String email;
    private String role;
    private String bio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_viewprofile);

        // Initialize UI components
        initializeUIComponents();

        // Get intent data
        getIntentData();

        // Populate profile data
        populateProfileData();

        // Setup click listeners
        setupClickListeners();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void initializeUIComponents() {
        // Toolbar
        toolbar = findViewById(R.id.top_toolbar);
        setSupportActionBar(toolbar);

        // Profile info
        profileImage = findViewById(R.id.profile_image);
        editPhotoButton = findViewById(R.id.edit_photo_button);
        profileUsername = findViewById(R.id.profile_username);
        profileRole = findViewById(R.id.profile_role);
        profileEmail = findViewById(R.id.profile_email);
        profileBio = findViewById(R.id.profile_bio);
        specializationChipGroup = findViewById(R.id.specialization_chip_group);

        // Buttons
        editProfileButton = findViewById(R.id.edit_profile_button);
        logoutButton = findViewById(R.id.profile_logout_button);
        deleteUserButton = findViewById(R.id.profile_delete_user_button);

        // Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_nav);

        // Make sure the bottom navigation is visible
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
            Log.d(TAG, "Bottom navigation view found and set to visible");
        } else {
            Log.e(TAG, "Bottom navigation view not found in layout");
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) {
            Log.e(TAG, "Cannot setup bottom navigation - view is null");
            return;
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Set profile as selected item since we're on the profile page
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        Log.d(TAG, "Bottom navigation setup complete with profile selected");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Log the navigation selection
        Log.d(TAG, "Navigation item selected: " + item.getTitle());

        if (itemId == R.id.nav_home) {
            // Navigate to home
            Intent homeIntent = new Intent(this, EmployeeHomeActivity.class);
            homeIntent.putExtra("userID", userID);
            homeIntent.putExtra("username", username);
            homeIntent.putExtra("email", email);
            startActivity(homeIntent);
            return true;
        } else if (itemId == R.id.nav_appointments) {
            // Navigate to appointments
            Intent appointmentsIntent = new Intent(this, SelectBookingTimeActivity.class);
            appointmentsIntent.putExtra("userID", userID);
            startActivity(appointmentsIntent);
            return true;
        } else if (itemId == R.id.nav_messages) {
            // Navigate to messages (to be implemented)
            Toast.makeText(this, "Messages feature coming soon", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.nav_profile) {
            // Already on profile, do nothing
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
            bio = extras.getString("bio", "I am a nail technician with a passion for nail art and design.");

            // Log received data
            Log.d(TAG, "UserID: " + userID);
            Log.d(TAG, "Username: " + username);
            Log.d(TAG, "Email: " + email);
            Log.d(TAG, "Role: " + role);
        } else {
            // Handle case when no data is passed
            Log.e(TAG, "No user data received");
            Toast.makeText(this, "Error: No user data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void populateProfileData() {
        // Set username and role
        profileUsername.setText(username);
        profileRole.setText(role);

        // Set email and bio
        profileEmail.setText(email);
        profileBio.setText(bio);

        // Set profile image if available
        //TODO: Add profile image
//        PrefManager prefManager = PrefManager.getInstance(this);
//        String profileImageUrl = prefManager.getProfileImageUrl();
//
//        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
//            Glide.with(this)
//                    .load(profileImageUrl)
//                    .apply(new RequestOptions()
//                            .placeholder(R.drawable.ic_profile)
//                            .error(R.drawable.ic_profile))
//                    .into(profileImage);
//        }
    }

    private void setupClickListeners() {
        // Edit photo button
        editPhotoButton.setOnClickListener(v -> {
            Toast.makeText(this, "Edit photo feature coming soon", Toast.LENGTH_SHORT).show();
        });

        // Edit profile button
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EmployeeEditProfileActivity.class);
            intent.putExtra("userID", userID);
            intent.putExtra("username", username);
            intent.putExtra("email", email);
            intent.putExtra("role", role);
            intent.putExtra("bio", bio);
            startActivity(intent);
        });

        // Logout button
        logoutButton.setOnClickListener(v -> {
            // Handle logout
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();

            // Clear preferences/session
            PrefManager prefManager = PrefManager.getInstance(this);
            prefManager.clear();

            // Navigate to login
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Delete user button
        deleteUserButton.setOnClickListener(v -> {
            // Show confirmation dialog
            Toast.makeText(this, "Delete account feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh profile data when returning to this screen
        populateProfileData();

        // Make sure profile tab is selected
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        }
    }
}