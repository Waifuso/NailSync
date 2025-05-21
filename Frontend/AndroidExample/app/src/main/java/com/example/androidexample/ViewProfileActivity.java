package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {

    private static final String TAG = "ViewProfileActivity";
    private static final String USER_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/users/";

    // UI Components
    private ImageButton backButton;
    private CircleImageView profileImage;
    private FloatingActionButton editPhotoButton;
    private TextView profileUsername, profileEmail, profilePhone, profileRank;
    private MaterialButton editProfileButton, logoutButton, deleteAccountButton;


    // User data
    private long userID;
    private String username, email, phone, role, createdAt;
    private String rankName;
    private int rankPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewprofile);

        // Initialize UI components
        initializeViews();
        setupClickListeners();

        // Get userID from SharedPreferences
        PrefManager prefManager = PrefManager.getInstance(this);
        userID = getIntent().getLongExtra("userID", -1);

        // Fetch user profile data
        fetchUserProfile();
    }

    private void initializeViews() {
        // Back button
        backButton = findViewById(R.id.backButton);

        // Profile section
        profileImage = findViewById(R.id.profile_image);
        editPhotoButton = findViewById(R.id.edit_photo_button);
        profileUsername = findViewById(R.id.profile_username);
        editProfileButton = findViewById(R.id.edit_profile_button);

        // Details section
        profileEmail = findViewById(R.id.profile_email);
        profilePhone = findViewById(R.id.profile_phone);
        profileRank = findViewById(R.id.profile_location); // Reusing the location TextView for rank

        // Actions section
        logoutButton = findViewById(R.id.profile_logout_button);
        deleteAccountButton = findViewById(R.id.profile_delete_button);

        // Bottom navigation if present

    }

    private void setupClickListeners() {
        // Back button click
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewProfileActivity.this, ApplicationActivity.class);
            intent.putExtra("userID", userID);
            intent.putExtra("username", username);
            intent.putExtra("email", email);
            intent.putExtra("phone", phone);
            intent.putExtra("rankName", rankName);
            intent.putExtra("rankPoints", rankPoints);
            intent.putExtra("role", role);
            intent.putExtra("createdAt", createdAt);
            startActivity(intent);
            finish(); // Close this activity
        });

        // Edit profile button click
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("userID", userID);
            intent.putExtra("username", username);
            intent.putExtra("email", email);
            intent.putExtra("phone", phone);
            intent.putExtra("rankName", rankName);
            intent.putExtra("rankPoints", rankPoints);
            intent.putExtra("role", role);
            intent.putExtra("createdAt", createdAt);
            startActivity(intent);
        });

        // Edit photo button click
        editPhotoButton.setOnClickListener(v -> {
            Toast.makeText(this, "Photo editing not implemented yet", Toast.LENGTH_SHORT).show();
            // Implement photo editing functionality
        });

        // Logout button click
        logoutButton.setOnClickListener(v -> {
            // Clear user data from SharedPreferences
            PrefManager prefManager = PrefManager.getInstance(this);
            prefManager.clear();

            // Navigate to login screen
            Intent intent = new Intent(ViewProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Delete account button click
        deleteAccountButton.setOnClickListener(v -> {
            // Show confirmation dialog before deleting
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Implement account deletion
                        deleteUserAccount();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from EditProfileActivity
        fetchUserProfile();
    }

    private void fetchUserProfile() {
        String url = USER_URL + userID;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            parseUserProfileResponse(response);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage());
                            Toast.makeText(ViewProfileActivity.this,
                                    "Error loading profile data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error: " + error.toString());
                        Toast.makeText(ViewProfileActivity.this,
                                "Error connecting to server",
                                Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Add any headers if needed (like authentication)
                return headers;
            }
        };

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void parseUserProfileResponse(JSONObject response) {
        try {
            // No need to get "object" - the response itself is the user object
            // JSONObject userObject = response.getJSONObject("object");

            // Extract user data directly from the response
            username = response.getString("userName");
            email = response.getString("email");

            try {
                createdAt = response.getString("joinedDAte");
            } catch (JSONException e) {
                createdAt = ""; // Default value if not present
            }

            // Get the profile object - this contains additional user data
            try {
                JSONObject profileObject = response.getJSONObject("profile");

                // Extract data from the profile object
                try {
                    phone = profileObject.getString("phone");
                } catch (JSONException e) {
                    phone = "Not set"; // Default value if not present
                }

                try {
                    rankName = profileObject.getString("ranking");
                } catch (JSONException e) {
                    rankName = "NONE"; // Default value if not present
                }

                try {
                    rankPoints = profileObject.getInt("totalPoints");
                } catch (JSONException e) {
                    rankPoints = 0; // Default value if not present
                }

            } catch (JSONException e) {
                // Handle case where profile object doesn't exist
                phone = "Not set";
                rankName = "NONE";
                rankPoints = 0;
                Log.e(TAG, "Profile object not found: " + e.getMessage());
            }

            // Default role value if not present elsewhere
            role = "User";

            // Update UI
            profileUsername.setText(username);
            profileEmail.setText(email);
            profilePhone.setText(phone);

            // Format rank display: "RankName (Points pts)"
            String rankDisplay = rankName + " (" + rankPoints + " pts)";
            profileRank.setText(rankDisplay);

            // Also update SharedPreferences
            PrefManager prefManager = PrefManager.getInstance(this);
            prefManager.setUsername(username);
            prefManager.setEmail(email);

            Log.d(TAG, "Profile data loaded successfully");

        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e.getMessage());
            Toast.makeText(this, "Error parsing profile data", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteUserAccount() {
        String url = USER_URL + userID;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(ViewProfileActivity.this,
                                "Account deleted successfully",
                                Toast.LENGTH_SHORT).show();

                        // Clear user data and go to login screen
                        PrefManager prefManager = PrefManager.getInstance(ViewProfileActivity.this);
                        prefManager.clear();

                        Intent intent = new Intent(ViewProfileActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error deleting account: " + error.toString());
                        Toast.makeText(ViewProfileActivity.this,
                                "Error deleting account",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }


}