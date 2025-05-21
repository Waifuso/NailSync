package com.example.androidexample;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText usernameField, emailField, newPasswordField, confirmPasswordField, passwordField;
    private MaterialButton updateButton;
    private Button cancelButton;
    private long userID;

    private String username, email;
    private String originalUsername, originalEmail;
    private static final String UPDATE_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/users/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editprofile);

        // Initialize UI components
        usernameField = findViewById(R.id.username);
        emailField = findViewById(R.id.email);
        newPasswordField = findViewById(R.id.new_password);
        confirmPasswordField = findViewById(R.id.confirm_password);
        updateButton = findViewById(R.id.update_profile_button);
        cancelButton = findViewById(R.id.cancel_button);


        // Pre-fill fields with current data from SharedPreferences
        usernameField.setText(originalUsername);
        emailField.setText(originalEmail);



        // Get user data from intent
        if (getIntent().getExtras() != null) {
            userID = getIntent().getLongExtra("userID", -1);
            originalUsername = getIntent().getStringExtra("username");
            originalEmail = getIntent().getStringExtra("email");

            Log.d("EditProfileActivity", "userID: " + userID);

            // Pre-fill fields with current data
            usernameField.setText(originalUsername);
            emailField.setText(originalEmail);
        }

        // Retrieve user data from SharedPreferences

        PrefManager storedData = PrefManager.getInstance(this);

        if (userID == -1) {
            userID = storedData.getUserId();
        }
        if ((originalUsername == null) || (originalEmail == null)) {
            originalUsername = storedData.getUsername();
            originalEmail = storedData.getEmail();
        }

        // Set up button click listeners
        updateButton.setOnClickListener(v -> updateProfile());

        cancelButton.setOnClickListener(v -> finish());
    }

    private void updateProfile() {
        String newPassword = newPasswordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();
        String username = usernameField.getText().toString();
        String email = emailField.getText().toString();

        // Check if passwords match
        if (newPassword.isEmpty()) {
            newPasswordField.setError("Password cannot be empty");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordField.setError("Passwords do not match");
            return;
        }

        // Create a minimal JSON object with required fields
        // Add a timestamp to email to avoid duplication check
        JSONObject updateData = new JSONObject();
        try {
            // Add a timestamp to the email to make it unique
            long timestamp = System.currentTimeMillis();
            String modifiedEmail = originalEmail.split("@")[0] + "+" + timestamp + "@" + originalEmail.split("@")[1];

            updateData.put("userName", username);
            updateData.put("email", email); // Modified to avoid duplication check
            updateData.put("password", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send update request
        String url = UPDATE_URL + userID;
        Log.d("EditProfileActivity", "Sending update to: " + url);
        Log.d("EditProfileActivity", "Update data: " + updateData.toString());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                updateData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(EditProfileActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();

                        // Now that password is updated, restore the original email
                        restoreOriginalEmail();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError(error);
                    }
                });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void restoreOriginalEmail() {
        // Create JSON with original email
        JSONObject updateData = new JSONObject();
        try {
            updateData.put("userName", originalUsername);
            updateData.put("email", originalEmail);
            // Don't include password here
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send update request to restore original email
        String url = UPDATE_URL + userID;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                updateData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Email restored, now finish activity
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Still finish even if this fails
                        Log.e("EditProfileActivity", "Failed to restore email: " + error.toString());
                        finish();
                    }
                });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    private void handleError(VolleyError error) {
        String errorMessage = "An error occurred";
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                // getting the error message from backend
                String responseBody = new String(error.networkResponse.data, "UTF-8");
                Log.e("Volley Error", "Raw error response: " + responseBody);
                JSONObject data = new JSONObject(responseBody);
                // Try different possible error message keys
                if (data.has("message")) {
                    errorMessage = data.getString("message");
                } else if (data.has("error")) {
                    errorMessage = data.getString("error");
                }
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
        }

        Log.e("Volley Error", "Request failed: " + errorMessage);

        new AlertDialog.Builder(EditProfileActivity.this)
                .setTitle("Error")
                .setMessage(errorMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
