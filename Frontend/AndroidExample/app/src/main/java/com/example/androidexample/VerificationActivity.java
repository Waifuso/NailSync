package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Verifcation Activity - Applet that appears when the user attempts to do a controlled action in which the user must verify ownership of their account.
 * This activity will be used to verify the user's ownership of the account when changing the password or deleting the account.
 * @author Jordan Nguyen (hieu2k@iastate.edu)
 */
public class VerificationActivity extends AppCompatActivity {

    private Animation fadeIn, fadeOut;
    private EditText codeEntryField;
    private Button enter_btn, back_btn;

    private String VERIFICATION_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api/signup/enterCode";

    /**
     * Called when the activity is first created.
     *
     * Initializes the UI components and sets up any corresponding listeners for buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verification);

        codeEntryField = findViewById(R.id.verif_entryfield);
        enter_btn = findViewById(R.id.verif_enterbtn);
        back_btn = findViewById(R.id.verif_backbtn);

        if (this.getIntent().getExtras() != null) {
            // send message, giving the user the options to go back or continue
            new MaterialAlertDialogBuilder(this, R.style.RoundedRectangleXML)
                    .setTitle(this.getIntent().getStringExtra("header_message"))
                    .setMessage(this.getIntent().getStringExtra("body_message"))
                    .setPositiveButton("OK", (dialog, which) -> { dialog.dismiss();})
                    // in the lambda defined in the function above, the dialog box will close, then start the fade in animation and set the back button to visible if the user changes their mind
                    .setNegativeButton("Back", (dialog, which) -> VerificationActivity.this.finish()) // this will return the user to the login page.
                    .show();

            // basic lambda function to close the reset activity and return to the login screen when pressed.
            // back_button.setOnClickListener(v -> ResetActivity.this.finish());
        }

        enter_btn.setOnClickListener(v -> {
            if (codeEntryField.getText().length() != 0) {
                postRequest(VERIFICATION_URL);
            }
        });

        back_btn.setOnClickListener(v -> {
            finish();
        });
    }

    /**
     * A helper method that sends a post request using the user's email passed from intent and the code received from the email that was typed in the allocated field.
     * @param url The URL of the postRequest from backend.
     */
    private void postRequest(String url) {
        // Get the current values from form fields
        String email = this.getIntent().getStringExtra("email"), code = codeEntryField.getText().toString().trim();

        // Create JSON object for request body
        JSONObject postBody = new JSONObject();

        // try to add email to JSON object
        try {
            postBody.put("email", email);
            postBody.put("resetNums", code);
        }
        catch (JSONException e)
        {
            Log.e("JSON Error", "Error creating JSON: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Error processing request", Toast.LENGTH_LONG).show();
            return;
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST,  // Use PUT if your API requires it for updates
                url,
                postBody,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        Log.d("Volley Response", response.toString());
                        try {
                            // TODO: add functionality.
                            if (response.getString("message").equals("Great! Code is correct.")) {
                                // open password activity.
                                // ask backend to return the user object that the email belongs to.
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
                                new MaterialAlertDialogBuilder(VerificationActivity.this, R.style.RoundedRectangleXML)
                                        .setTitle("Please try again")
                                        .setMessage("The code that you have entered does not match the one we sent you.")
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