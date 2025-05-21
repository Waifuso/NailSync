package com.example.androidexample;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    // UI elements
    private TextInputEditText usernameInput, emailInput, passwordInput, reenterPasswordInput, dobInput;
    private TextInputLayout usernameInputLayout, emailInputLayout, passwordInputLayout, reenterPasswordInputLayout;
    private MaterialButton signUpButton;
    private LinearLayout googleSignUpLayout;
    private TextView loginText;
    private ImageButton backButton;
    private ImageView logoImage;
    private SwitchMaterial employeeSignupSwitch;

    // Calendar for date of birth
    private Calendar calendar;
    private Date dateOfBirth;

    // Signup URLs
    private static final String CLIENT_SIGNUP_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/signup/reg/";
    private static final String EMPLOYEE_SIGNUP_URL = "https://a94bb9c8-d7e6-424a-a5f4-699db6365ac1.mock.pstmn.io/Post_Request/PostResponse";

    // Logging tag
    private static final String TAG = "SignupActivity";

    // FOR DEBUGGING ONLY TO BYPASS SENDING POST TO SERVER WHEN NOT UP, REMOVE WHEN TESTING SIGNUP APPLICATION
    private Boolean debugTrue = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Initialize calendar
        calendar = Calendar.getInstance();

        // Initialize UI components
        initializeSetup();

        // Set click listeners
        setupClickListeners();

        // Setup text watchers for real-time validation
        setupTextWatchers();

        // Animate the logo
        animateLogo();
    }

    private void initializeSetup() {
        // Initializing UI components
        usernameInput = findViewById(R.id.name);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        reenterPasswordInput = findViewById(R.id.reenter_password);
        dobInput = findViewById(R.id.dateOfBirth);

        // Input Layouts
        usernameInputLayout = findViewById(R.id.name_input_layout);
        emailInputLayout = findViewById(R.id.email_input_layout);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        reenterPasswordInputLayout = findViewById(R.id.reenter_password_input_layout);

        signUpButton = findViewById(R.id.signup_button);
        loginText = findViewById(R.id.loginIn);
        googleSignUpLayout = findViewById(R.id.google_signup);
        backButton = findViewById(R.id.backButton);
        logoImage = findViewById(R.id.logoImage);

        // Pre-fill email if passed from login screen
        if (getIntent().hasExtra("CACHED")) {
            String cachedEmail = getIntent().getStringExtra("CACHED");
            if (cachedEmail != null && !cachedEmail.isEmpty()) {
                emailInput.setText(cachedEmail);
            }
        }
    }

    private void setupTextWatchers() {
        // Username Validation
        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUsername(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Email Validation
        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Password Validation
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Confirm Password Validation
        reenterPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateUsername(String username) {
        username = username.trim();
        if (username.isEmpty()) {
            usernameInputLayout.setError("Username cannot be empty");
            return false;
        }
        if (username.length() < 3) {
            usernameInputLayout.setError("Username must be at least 3 characters");
            return false;
        }
        usernameInputLayout.setError(null);
        return true;
    }

    private boolean validateEmail(String email) {
        email = email.trim();
        Pattern emailPattern = Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        );
        if (email.isEmpty()) {
            emailInputLayout.setError("Email cannot be empty");
            return false;
        }
        if (!emailPattern.matcher(email).matches()) {
            emailInputLayout.setError("Invalid email format");
            return false;
        }
        emailInputLayout.setError(null);
        return true;
    }

    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            passwordInputLayout.setError("Password cannot be empty");
            return false;
        }
        if (password.length() < 8) {
            passwordInputLayout.setError("Password must be at least 8 characters");
            return false;
        }

        passwordInputLayout.setError(null);

        // Validate confirm password if it's not empty
        String confirmPassword = reenterPasswordInput.getText().toString();
        if (!confirmPassword.isEmpty()) {
            validateConfirmPassword(confirmPassword);
        }

        return true;
    }

    private boolean validateConfirmPassword(String confirmPassword) {
        String password = passwordInput.getText().toString();
        if (confirmPassword.isEmpty()) {
            reenterPasswordInputLayout.setError("Please confirm your password");
            return false;
        }
        if (!confirmPassword.equals(password)) {
            reenterPasswordInputLayout.setError("Passwords do not match");
            return false;
        }
        reenterPasswordInputLayout.setError(null);
        return true;
    }

    private void setupClickListeners() {
        // Back button click listener
        backButton.setOnClickListener(v -> finish());

        // Date of birth field click listener
        dobInput.setOnClickListener(v -> showDatePickerDialog());

        // Sign up button click listener
        signUpButton.setOnClickListener(v -> registerUser());

        // Login text click listener
        loginText.setOnClickListener(v -> finish());

        // Google sign up button click listener
        googleSignUpLayout.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, GoogleSUActivity.class);
            startActivity(intent);
        });
    }

    private void showDatePickerDialog() {
        // Set date picker to 18 years ago by default (minimum age)
        final Calendar defaultDate = Calendar.getInstance();
        defaultDate.add(Calendar.YEAR, -18);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateOfBirthField();
                },
                defaultDate.get(Calendar.YEAR),
                defaultDate.get(Calendar.MONTH),
                defaultDate.get(Calendar.DAY_OF_MONTH)
        );

        // Set maximum date to today (can't be born in the future)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Set minimum date to 100 years ago (reasonable age limit)
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private void updateDateOfBirthField() {
        dateOfBirth = calendar.getTime();
        String dateFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        dobInput.setText(sdf.format(dateOfBirth));
    }

    private void animateLogo() {
        // Position the logo higher off-screen and make it fully transparent initially
        logoImage.setY(-200f);
        logoImage.setAlpha(0f);
        logoImage.setVisibility(View.VISIBLE);

        // Animate translation and fade in effect
        logoImage.animate()
                .translationY(0f)  // Slide down to its final position
                .alpha(1f)         // Fade in to full opacity
                .setDuration(1000) // Reduced duration (1 second) for a faster animation
                .setStartDelay(100) // Reduced delay (0.1 seconds) to start almost immediately
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    private boolean validateInputs() {
        boolean isUsernameValid = validateUsername(usernameInput.getText().toString());
        boolean isEmailValid = validateEmail(emailInput.getText().toString());
        boolean isPasswordValid = validatePassword(passwordInput.getText().toString());
        boolean isConfirmPasswordValid = validateConfirmPassword(reenterPasswordInput.getText().toString());
        boolean isDateOfBirthValid = !dobInput.getText().toString().isEmpty();

        if (!isDateOfBirthValid) {
            dobInput.setError("Date of birth cannot be empty");
        }

        return isUsernameValid && isEmailValid && isPasswordValid &&
                isConfirmPasswordValid && isDateOfBirthValid;
    }

    private void registerUser() {
        // Validate all inputs before proceeding
        if (!validateInputs()) {
            Log.w(TAG, "Signup validation failed");
            return;
        }

        // Show loading state
        signUpButton.setEnabled(false);
        signUpButton.setText("Creating Account...");

        // Format date of birth to ISO format for API
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String formattedDob = apiDateFormat.format(dateOfBirth);

        // Determine signup URL based on employee switch
        String signupUrl = CLIENT_SIGNUP_URL;

        // Create JSON object for request body
        JSONObject postBody = new JSONObject();

        try
        {
            postBody.put("username", usernameInput.getText().toString().trim());
            postBody.put("email", emailInput.getText().toString().trim());
            postBody.put("dateOfBirth", formattedDob);
            postBody.put("password", passwordInput.getText().toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Error processing request", Toast.LENGTH_LONG).show();
            resetButton();
            return;
        }

        // Log signup attempt
        Log.i(TAG, "Attempting signup for: " + usernameInput.getText().toString());

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST,
                signupUrl,
                postBody,
                response -> {
                    Log.d(TAG, "User registered: " +response.toString());

                    // Using PrefManager to store the users data
                    // PrefManager is wrapper class for SharedPreferences
                    PrefManager storedData = PrefManager.getInstance(SignupActivity.this);
                    try
                    {
                        String message = response.getString("message");
                    }
                    catch (JSONException e)
                    {
                        Log.e(TAG, "Error processing server response", e);
                        resetButton();
                    }
                    try
                    {
                        String username = response.getString("username");
                        storedData.setUsername(username);
                        Log.d(TAG, "Username: " + username);
                    }
                    catch (JSONException e)
                    {
                        Log.e(TAG, "Error getting usernamee", e);
                        resetButton();
                    }
                    try
                    {
                        String email = response.getString("email");
                        storedData.setEmail(email);
                    }
                    catch (JSONException e)
                    {
                        Log.e(TAG, "Error getting email", e);
                        resetButton();
                    }
                    try
                    {
                        int userID = response.getInt("id");
                        storedData.setUserId(userID);
                        Log.d(TAG, "UserID: " + userID);
                    }
                    catch (JSONException e)
                    {
                        Log.e(TAG, "Error getting userID", e);
                        resetButton();
                    }

                    // Determine which activity to launch based on user type
                    Intent intent;
                    if (employeeSignupSwitch.isChecked())
                    {
                        intent = new Intent(SignupActivity.this, EmployeeHomeActivity.class);
                        intent.putExtra("userID", storedData.getUserId());
                        intent.putExtra("username", storedData.getUsername());
                        intent.putExtra("email", storedData.getEmail());

                    }
                    else
                    {
                        intent = new Intent(SignupActivity.this, ApplicationActivity.class);
                        intent.putExtra("userID", storedData.getUserId());
                        intent.putExtra("username", storedData.getUsername());
                        intent.putExtra("email", storedData.getEmail());
                    }

                    startActivity(intent);

                    // Close current activity
                    finish();
                },
                error -> {
                    // Log signup failure
                    Log.e(TAG, "Signup failed", error);

                    String errorMessage = "An error occurred";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try
                        {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            JSONObject data = new JSONObject(responseBody);
                            errorMessage = data.optString("message", "Signup failed");
                        }
                        catch (UnsupportedEncodingException | JSONException e)
                        {
                            Log.e(TAG, "Error parsing error response", e);
                        }
                    }

                    // Show error dialog
                    new AlertDialog.Builder(SignupActivity.this)
                            .setTitle("Signup Error")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK", (dialog, which) -> {
                                dialog.dismiss();
                                resetButton();
                            })
                            .show();
                }
        );

        // Add request to queue with a tag for potential cancellation
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq);
    }

    private void resetButton() {
        // Re-enable signup button and reset text
        signUpButton.setEnabled(true);
        signUpButton.setText("Sign Up");
    }
}