package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class ResetActivity extends AppCompatActivity {

    private Button next_button, back_button;
    private EditText email_field;
    private Animation fadeIn, fadeOut;
    private LinearLayout code_container;
    private boolean emailentered = false;

    // SERVER STUFF
    private String SERVER_POSTEMAILURL = "http://coms-3090-020.class.las.iastate.edu:8080/api/signup/resetPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset);

        // this whole section is initializing ui elements.
        next_button = findViewById(R.id.reset_nextbtn);
        back_button = findViewById(R.id.reset_backbtn);
        email_field = findViewById(R.id.reset_email_entryfield);

        code_container = findViewById(R.id.code_container);

        // this is modifying and initializing visual settings
        back_button.setVisibility(View.INVISIBLE);
        code_container.setVisibility(View.GONE);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fadeOut.setDuration(500); // set the fadeout duration to half a second

        // send the password reset message, giving the user the options to go back or continue
        new MaterialAlertDialogBuilder(this, R.style.RoundedRectangleXML)
                .setTitle("Forgot your password?")
                .setMessage("No worries, just enter the information associated with your account and we'll send you instructions to reset your password.")
                .setPositiveButton("OK", (dialog, which) -> { dialog.dismiss(); back_button.startAnimation(fadeIn); back_button.setVisibility(View.VISIBLE); })
                // in the lambda defined in the function above, the dialog box will close, then start the fade in animation and set the back button to visible if the user changes their mind
                .setNegativeButton("Back", (dialog, which) -> ResetActivity.this.finish()) // this will return the user to the login page.
                .show();

        // next_button click listener lambda function, calls sendMessageDialog()
        /*
            basically this function but cutdown:
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    ... function code lol
                    }
                }
            referenced android tutorial found at https://developer.android.com/codelabs/basic-android-kotlin-compose-function-types-and-lambda.
            the tutorial is in kotlin, but was translated to java using online tutorials.
         */
        next_button.setOnClickListener(v -> {
            // TODO: add some logic that calls the server to check if there is an account associated with the provided email.

            if (email_field.getText().length() > 0) {
                emailentered = true;
            }
            else {
                sendDialog_NoEmail();
            }

            if (emailentered) {
                new MaterialAlertDialogBuilder(this, R.style.RoundedRectangleXML)
                        .setTitle("Check your email")
                        .setMessage("We've sent instructions to your email to recover your account.\n\nOnce you're ready, please press next again and enter the code you received to reset your password.")
                        .setPositiveButton("OK", (dialog, which) -> postRequest(SERVER_POSTEMAILURL))
                        .show();
                email_field.setClickable(false); // lock the element so the user cannot click the field
                email_field.setEnabled(false);  // lock the field so that the user cannot change the entered value
            }
        });

        // listener to check when the email is changed, meaning the phone number has to be re-entered. resets some of the flag logic so that the code doesnt ask for an input
        // that isnt available to the user.
        email_field.addTextChangedListener(new TextWatcher() {
            // shouldnt need to change
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // when the email is modified, the emailentered should be reset.
                emailentered = false; // reset the email entered flag so that you can check the email again to make sure that its valid.
            }

            // shouldnt need to change.
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // basic lambda function to close the reset activity and return to the login screen when pressed.
        back_button.setOnClickListener(v -> ResetActivity.this.finish());
    }

    // helper method to send message dialog. reduce code in onCreate().
    // made referencing documentation https://developer.android.com/reference/com/google/android/material/dialog/MaterialAlertDialogBuilder
    // and online tutorial @ https://www.geeksforgeeks.org/how-to-create-an-alert-dialog-box-in-android/
    private void sendDialog_NoEmail() {
        new MaterialAlertDialogBuilder(this, R.style.RoundedRectangleXML)
                .setTitle("Account Details")
                .setMessage("Please enter the email associated with your account.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void sendDialog_NoPhone() {
        new MaterialAlertDialogBuilder(this, R.style.RoundedRectangleXML)
                .setTitle("Verification")
                .setMessage("Please enter the phone number associated with this account.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void sendDialog_InvalidEmail() {
        new MaterialAlertDialogBuilder(this, R.style.RoundedRectangleXML)
                .setTitle("Email entered is already linked to an account.")
                .setMessage("Please check the email you entered for any typos.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void postRequest(String url) {
        // Get the current values from form fields
        String email = email_field.getText().toString().trim();

        // Create JSON object for request body
        JSONObject postBody = new JSONObject();

        // try to add email to JSON object
        try {
            postBody.put("email", email);
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

                        // create the dialog prompt where the user can enter their verification code via email. when that code is entered, the user can then reset their password.
                        // open verification activity.
                        Intent intent = new Intent(ResetActivity.this, VerificationActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("header_message", "Reset Password");
                        intent.putExtra("body_message", "To change your password, please enter the code we sent to your email to authenticate this process.");
                        startActivity(intent);

                        email_field.setClickable(true); // unlock the element so the user can click the field
                        email_field.setEnabled(true);  // unlock the field so that the user can change the entered value
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("Volley Error", "Profile update failed: " + error.toString());
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
