package com.example.androidexample;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ImageView logo;
    private Animation fadeIn;
    private Button login_button;
    private FloatingActionButton admin_button;
    private SwitchMaterial employeeLoginSwitch;
    private TextView signup_button, reset_button;
    private TextInputEditText username_field, password_field;
    private CardView container;
    private View gradient;
    private boolean logged_in = false; // make sure this changes by api call and cache!

    // passed value for userID
    // change in the future
    private long userID = 1;
    private TextView text;

    // SERVER STUFF
    private static final String SERVER_LOGINURL = "http://coms-3090-020.class.las.iastate.edu:8080/api/login/by-email?email=";
    private static final String SERVER_EMPLOYEE_LOGINURL = "http://coms-3090-020.class.las.iastate.edu:8080/api/login/employee";
    private static final String SERVER_USER_PROFILE_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/users/";

    protected static User currentUser;
    private boolean isEmployeeLogin = false, isAdminLogin = false;

    private boolean bypassLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);             // link to Main activity XML

        // Setup logo with initial properties
        logo = findViewById(R.id.app_logo_header);
        // Start with the logo higher up off-screen and completely visible
        logo.setTranslationY(-800f);
        // Start with a fade in animation
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(fadeIn);

        login_button = findViewById(R.id.main_nextbtn);
        signup_button = findViewById(R.id.signupbtn);
        reset_button = findViewById(R.id.password_reset_btn);
        container = findViewById(R.id.cardView);
        gradient = findViewById(R.id.topGradient);
        employeeLoginSwitch = findViewById(R.id.employee_login_switch);
        admin_button = findViewById(R.id.admin_btn);

        container.setVisibility(View.INVISIBLE);

        // set background to fullscreen
        ViewGroup.LayoutParams params = gradient.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        gradient.setLayoutParams(params);

        text = findViewById(R.id.main_headermsg);

        /* here, add logic for api call / request for login information to confirm user is logged in or check application cache. */

        if (!logged_in) {
            username_field = findViewById(R.id.email_entryfield);
            password_field = findViewById(R.id.login_password_field);

            // Check for when the user was logged in, if there is any extra information for the email
            if (this.getIntent().getExtras() != null) {
                username_field.setText(this.getIntent().getStringExtra("email"));
            }

            // Start logo animation sequence - simple slide down animation
            new Handler().postDelayed(() -> {
                // First animation: Move logo down to center of screen
                ObjectAnimator moveDown = ObjectAnimator.ofFloat(logo, "translationY", 850f);
                moveDown.setDuration(1500);
                moveDown.start();
            }, 500);

            // Second animation: Move logo to final position (above the login card)
            new Handler().postDelayed(() -> {
                ObjectAnimator moveToFinal = ObjectAnimator.ofFloat(logo, "translationY", 75f);
                moveToFinal.setDuration(1000);
                moveToFinal.start();

                // Set pivotY to the bottom of the view for upward shrinking
                gradient.setPivotY(gradient.getHeight());

                // Animate the height change
                ValueAnimator animator = ValueAnimator.ofInt(gradient.getHeight(), 850);
                animator.setDuration(1500);
                animator.addUpdateListener(valueAnimator -> {
                    params.height = (int) valueAnimator.getAnimatedValue();
                    gradient.setLayoutParams(params);
                });
                animator.start();
            }, 3000);

            // Final step: Fade in login form
            new Handler().postDelayed(() -> {
                container.setVisibility(View.VISIBLE);
                ObjectAnimator fadeInTitle = ObjectAnimator.ofFloat(findViewById(R.id.cardView), "alpha", 0f, 1f);
                fadeInTitle.setDuration(800);
                fadeInTitle.start();
            }, 4000);

            /* click listener on login button pressed */
            login_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean valid = false;
                    /* grab strings from user inputs */
                    String email = username_field.getText().toString();
                    String password = password_field.getText().toString();

                    // for bypassing login during development
                    if (bypassLogin) {
                        Intent intent;
                        if (isEmployeeLogin) {
                            intent = new Intent(MainActivity.this, EmployeeHomeActivity.class);
                        } else {
                            intent = new Intent(MainActivity.this, ApplicationActivity.class);
                        }

                        // Add consistent test data for development
                        intent.putExtra("userID", userID);
                        intent.putExtra("username", "Jacob");
                        intent.putExtra("email", "test@example.com");
                        intent.putExtra("isEmployee", isEmployeeLogin);
                        intent.putExtra("userPoints", 1000);

                        Log.d("Login", "Bypassing login with userID: " + userID);

                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e("Login error", "Intent was not initialized properly: " + e.getMessage());
                        }
                        return; // Return to prevent the rest of the code from executing
                    }

                    // Regular login validation and processing
                    if (!email.isEmpty() && !password.isEmpty()) {
                        valid = true;
                    }

                    if (!valid) {
                        new MaterialAlertDialogBuilder(MainActivity.this, R.style.RoundedRectangleXML)
                                .setTitle("Please enter your login info")
                                .setMessage("Neither field may be empty, please enter your username or password to continue.")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .show();
                    }

                    if (valid) {
                        String baseUrl = isEmployeeLogin ? SERVER_EMPLOYEE_LOGINURL : SERVER_LOGINURL;
                        String SERVER_REQUESTURL = baseUrl + email + "&password=" + password;
                        makeLoginRequest(SERVER_REQUESTURL, isEmployeeLogin);
                    }
                }
            });

            signup_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /* when signup button is pressed, use intent to switch to Signup Activity */
                    Intent intent = new Intent(MainActivity.this, SignupActivity.class);

                    String cached = username_field.getText().toString();
                    intent.putExtra("CACHED", cached);
                    startActivity(intent);  // go to SignupActivity
                }
            });

            reset_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /* when signup button is pressed, use intent to switch to Signup Activity */
                    Intent intent = new Intent(MainActivity.this, ResetActivity.class);

                    String cached = username_field.getText().toString();
                    intent.putExtra("CACHED", cached);
                    startActivity(intent);  // go to ResetActivity
                }
            });

            employeeLoginSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isEmployeeLogin = isChecked;
                    } else {
                        // fixed employee switch, it wasnt configured properly.
                        isEmployeeLogin = false;
                    }
                }
            });
        } else {
            Intent intent = new Intent(MainActivity.this, ApplicationActivity.class);
            startActivity(intent);
            finish();
        }

        // TODO: TEMPORARY ADMIN LOGIN, FIX LATER
        admin_button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // CHECK USER CREDENTIALS WITH SERVER INFO.
    private void makeLoginRequest(String url, boolean isEmployee) {
        if (!isEmployeeLogin) {
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null, // Pass null as the request body since it's a GET request
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("Volley Response", response.toString());

                            // attempt to parse information.
                            try {
                                String requestAnswer = response.getString("message");
                                long USER_ID;

                                // check the request answer.
                                if (requestAnswer.equals("successfully")) {
                                    logged_in = true;
                                    USER_ID = response.getLong("id");

                                    // Determine which activity to launch based on employee status
                                    Intent intent;
                                    if (isEmployee)

                                    // will take to employee activity, for now I just set to ApplicationActivity

                                    {
                                        intent = new Intent(MainActivity.this, EmployeeHomeActivity.class);
                                    } else {
                                        intent = new Intent(MainActivity.this, ApplicationActivity.class);
                                    }

                                    String USER_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/" +
                                            (isEmployee ? "employee/" : "users/") + USER_ID;
                                    getUser(USER_URL, isEmployee);

                                    try {
                                        userID = currentUser.getId();
                                        intent.putExtra("id", userID);
                                        intent.putExtra("username", currentUser.getUsername());
                                        intent.putExtra("email", currentUser.getEmail());
                                        intent.putExtra("isEmployee", isEmployee);

                                    } catch (Exception e) {
                                        Log.d("Failed to store data", "User information could not be obtained");
                                    }

                                    startActivity(intent);  // go to MainActivity with the key-value data
                                    finish();
                                } else {
                                    new MaterialAlertDialogBuilder(MainActivity.this, R.style.RoundedRectangleXML)
                                            .setTitle(isEmployee ? "Employee Login Failed" : "Login Failed")
                                            .setMessage("Your credentials couldn't be verified. Please check your email and password.")
                                            .setPositiveButton("OK", (dialog, which) -> {
                                                dialog.dismiss();
                                            })
                                            .show();
                                }
                            } catch (JSONException e) {
                                Log.e("Volley Error", "JSON Request Error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Volley Error", error.toString());
                            new MaterialAlertDialogBuilder(MainActivity.this, R.style.RoundedRectangleXML)
                                    .setTitle("Connection Error")
                                    .setMessage("Couldn't connect to the server. Please check your internet connection and try again.")
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        dialog.dismiss();
                                    })
                                    .show();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    return headers;
                }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    return params;
                }
            };
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
        }
        else if (isEmployeeLogin) {
            // Get the current values from form fields
            String username = username_field.getText().toString().trim(), password = password_field.getText().toString().trim();

            // Create JSON object for request body
            JSONObject postBody = new JSONObject();

            // try to add email to JSON object
            try {
                postBody.put("username", username);
                postBody.put("password", password);
            }
            catch (JSONException e)
            {
                Log.e("JSON Error", "Error creating JSON: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Error processing request", Toast.LENGTH_LONG).show();
                return;
            }

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                    Request.Method.POST,  // Use PUT if your API requires it for updates
                    SERVER_EMPLOYEE_LOGINURL,
                    postBody,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response)
                        {
                            Log.d("Volley Response", response.toString());
                            try {
                                if (response.getString("message").equals(username)) {
                                    // TODO: login
                                    Intent intent = new Intent(MainActivity.this, EmployeeHomeActivity.class);
                                    intent.putExtra("userID", response.getLong("id"));
                                    intent.putExtra("username", response.getString("message"));
                                    intent.putExtra("email", "employee@example.com");
                                    intent.putExtra("isEmployee", true);
                                    Log.d("Login", "Login ID" + response.getLong("id"));
                                    startActivity(intent);
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            Log.e("Volley Error", "Profile update failed: " + error.toString());
                            new MaterialAlertDialogBuilder(MainActivity.this, R.style.RoundedRectangleXML)
                                    .setTitle("Please try again")
                                    .setMessage("The password that you have entered is incorrect.")
                                    .setPositiveButton("OK", (dialog, which) -> { dialog.dismiss(); })
                                    // in the lambda defined in the function above, the dialog box will close, then start the fade in animation and set the back button to visible if the user changes their mind
                                    .show();
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    // Add auth token if needed
                    // headers.put("Authorization", "Bearer " + getAuthToken());
                    return headers;
                }
            };

            // Adding request to request queue
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq);
        }
    }


    // Helper method to show error dialogs
    private void showErrorDialog(String title, String message) {
        new MaterialAlertDialogBuilder(MainActivity.this, R.style.RoundedRectangleXML)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // NEW METHOD: Get user profile information using the userID
    private void fetchUserProfile(long userID, boolean isEmployee) {
        String profileUrl = SERVER_USER_PROFILE_URL + userID;
        Log.d("UserProfile", "Fetching profile from: " + profileUrl);

        JsonObjectRequest profileRequest = new JsonObjectRequest(
                Request.Method.GET,
                profileUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("UserProfile", "Response: " + response.toString());

                            // Try to extract username with multiple possible field names
                            String username = "";
                            try {
                                if (response.has("userName")) {
                                    username = response.getString("userName");
                                } else if (response.has("username")) {
                                    username = response.getString("username");
                                } else {
                                    // If neither field exists, use a default value
                                    Log.e("UserProfile", "Neither 'userName' nor 'username' field found");
                                    username = "User " + userID;
                                }
                            } catch (JSONException e) {
                                Log.e("UserProfile", "Error extracting username: " + e.getMessage());
                                username = "User " + userID; // Fallback value
                            }

                            // Try to extract email
                            String email = "";
                            try {
                                email = response.getString("email");
                            } catch (JSONException e) {
                                Log.e("UserProfile", "Error extracting email: " + e.getMessage());
                                // Continue without email
                            }

                            // Initialize profile data
                            int totalPoints = 0;
                            String firstName = "";
                            String lastName = "";
                            String phone = "";
                            String ranking = "";

                            // Try to extract profile data
                            try {
                                if (response.has("profile") && !response.isNull("profile")) {
                                    JSONObject profileObj = response.getJSONObject("profile");
                                    firstName = profileObj.optString("firstName", "");
                                    lastName = profileObj.optString("lastName", "");
                                    phone = profileObj.optString("phone", "");
                                    ranking = profileObj.optString("ranking", "");
                                    totalPoints = profileObj.optInt("totalPoints", 0);
                                }
                            } catch (JSONException e) {
                                Log.e("UserProfile", "Error extracting profile data: " + e.getMessage());
                                // Continue with default values
                            }

                            // Create user object regardless of any parsing issues
                            currentUser = new User(userID, username, email, "", isEmployee);
                            currentUser.setUserPoints(totalPoints);

                            // Create intent for the next activity
                            Intent intent;
                            if (isEmployee) {
                                intent = new Intent(MainActivity.this, EmployeeHomeActivity.class);
                            } else {
                                intent = new Intent(MainActivity.this, ApplicationActivity.class);
                            }

                            // Add all user data to the intent with consistent key names
                            intent.putExtra("userID", userID);
                            intent.putExtra("username", username);
                            intent.putExtra("email", email);
                            intent.putExtra("isEmployee", isEmployee);
                            intent.putExtra("userPoints", totalPoints);

                            // Add profile information if available
                            if (!firstName.isEmpty()) intent.putExtra("firstName", firstName);
                            if (!lastName.isEmpty()) intent.putExtra("lastName", lastName);
                            if (!phone.isEmpty()) intent.putExtra("phone", phone);
                            if (!ranking.isEmpty()) intent.putExtra("ranking", ranking);

                            Log.d("Login", "Login successful for userID: " + userID);
                            Log.d("Login", "Username: " + username);
                            Log.d("Login", "Email: " + email);
                            Log.d("Login", "Points: " + totalPoints);
                            Log.d("Login", "First Name: " + firstName);
                            Log.d("Login", "Last Name: " + lastName);

                            startActivity(intent);
                            finish();

                        } catch (Exception e) {
                            Log.e("UserProfile", "Critical error parsing profile data: " + e.toString());
                            e.printStackTrace();

                            // Create a basic user with minimal info and continue
                            fallbackLoginWithBasicInfo(userID, isEmployee);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("UserProfile", "Error getting user profile: " + error.toString());
                        error.printStackTrace();

                        Toast.makeText(MainActivity.this,
                                "Failed to retrieve user profile. Please check your connection.",
                                Toast.LENGTH_SHORT).show();

                        // Fall back to a default profile to allow login to proceed
                        fallbackLoginWithBasicInfo(userID, isEmployee);
                    }
                });

        // Set a longer timeout for profile request
        profileRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                20000, // 20 seconds timeout
                1, // No retries
                1 // No backoff multiplier
        ));

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(profileRequest);
    }

    // Helper method for fallback login with minimal information
    private void fallbackLoginWithBasicInfo(long userID, boolean isEmployee) {
        try {
            // Create a default user profile
            currentUser = new User(userID, "User", "", "", isEmployee);

            // Create intent for the next activity
            Intent intent;
            if (isEmployee) {
                intent = new Intent(MainActivity.this, EmployeeHomeActivity.class);
            } else {
                intent = new Intent(MainActivity.this, ApplicationActivity.class);
            }

            // Add minimal user data
            intent.putExtra("userID", userID);
            intent.putExtra("username", "User " + userID);
            intent.putExtra("isEmployee", isEmployee);
            intent.putExtra("userPoints", 0);

            Log.d("Login", "Proceeding with minimal user data due to profile fetch error");

            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e("UserProfile", "Critical failure in fallback login: " + e.toString());

            // Show a dialog since we can't proceed
            showErrorDialog("Login Error",
                    "Unable to complete login process. Please try again later.");
        }
    }

    // Original getUser method is no longer needed as it's replaced by fetchUserProfile
    private void getUser(String url, boolean isEmployee) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override

                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("UserProfile", "Response: " + response.toString());

                            // Extract basic user information using the correct key names from the server response
                            // Change "username" to "userName" to match the actual JSON response
                            String username = response.getString("userName"); // Changed from "username" to "userName"
                            String email = response.getString("email");

                            // Rest of your code remains the same
                            int totalPoints = 0;
                            String firstName = "";
                            String lastName = "";
                            String phone = "";
                            String ranking = "";

                            if (response.has("profile") && !response.isNull("profile")) {
                                JSONObject profileObj = response.getJSONObject("profile");
                                firstName = profileObj.optString("firstName", "");
                                lastName = profileObj.optString("lastName", "");
                                phone = profileObj.optString("phone", "");
                                ranking = profileObj.optString("ranking", "");
                                totalPoints = profileObj.optInt("totalPoints", 0);
                            }

                            // Update User object
                            currentUser = new User(userID, username, email, "", isEmployee);
                            currentUser.setUserPoints(totalPoints);

                            // Create intent for the next activity
                            Intent intent;
                            if (isEmployee) {
                                intent = new Intent(MainActivity.this, EmployeeHomeActivity.class);
                            } else {
                                intent = new Intent(MainActivity.this, ApplicationActivity.class);
                            }

                            // Add all user data to the intent with consistent key names
                            intent.putExtra("userID", userID);
                            intent.putExtra("username", username);
                            intent.putExtra("email", email);
                            intent.putExtra("isEmployee", isEmployee);
                            intent.putExtra("userPoints", totalPoints);

                            // Add profile information if available
                            if (!firstName.isEmpty()) intent.putExtra("firstName", firstName);
                            if (!lastName.isEmpty()) intent.putExtra("lastName", lastName);
                            if (!phone.isEmpty()) intent.putExtra("phone", phone);
                            if (!ranking.isEmpty()) intent.putExtra("ranking", ranking);

                            Log.d("Login", "Login successful for userID: " + userID);
                            Log.d("Login", "Username: " + username);
                            Log.d("Login", "Email: " + email);
                            Log.d("Login", "Points: " + totalPoints);
                            Log.d("Login", "First Name: " + firstName);
                            Log.d("Login", "Last Name: " + lastName);

                            startActivity(intent);
                            finish();

                        } catch (JSONException e) {
                            Log.e("Volley error", "Error parsing user data: " + e.toString());
                            Toast.makeText(MainActivity.this,
                                    "Error retrieving user data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", "Error getting user: " + error.toString());
                        Toast.makeText(MainActivity.this,
                                "Failed to retrieve user data",
                                Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}

class User {
    private long id;
    private String username, email, password;
    private boolean isEmployee;

    private int userPoints;

    public User(long id, String username, String email, String password, int userPoints) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.isEmployee = false;
    }

    public User(long id, String username, String email, String password, boolean isEmployee) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.isEmployee = isEmployee;
    }

    public int getUserPoints() {
        return userPoints;
    }
    public void setUserPoints(int userPoints) {
        this.userPoints = userPoints;
    }
    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmployee() {
        return isEmployee;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmployee(boolean isEmployee) {
        this.isEmployee = isEmployee;
    }

    @Override
    public String toString() {
        // password omitted.
        return "ID: " + id + ", Name: " + username + ", Email: " + email +
                (isEmployee ? " (Employee)" : "");
    }
}
