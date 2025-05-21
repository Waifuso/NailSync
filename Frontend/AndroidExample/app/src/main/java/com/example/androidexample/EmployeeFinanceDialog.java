package com.example.androidexample;

import static android.content.ContentValues.TAG;
import static android.view.View.INVISIBLE;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Custom dialog for activating features via HTTP GET request
 */
public class EmployeeFinanceDialog extends Dialog {

    private TextView txtDialogTitle;
    private TextView txtDialogDescription;
    private TextView txtStatus;
    private Button btnCancel;
    private Button btnActivate;
    private LinearLayout layoutStatus;
    private ImageButton btnCloseDialog;

    private ProgressBar loadingBar;

    private long employeeID;
    private String apiUrl = "http://coms-3090-020.class.las.iastate.edu:8080/api/finance/get/daily/income?id=";
    // 1&localDate=2025-04-11

    public EmployeeFinanceDialog(@NonNull Context context, long employeeID) {
        super(context);
        this.employeeID = employeeID;
        this.apiUrl = apiUrl;

        // Initialize the dialog
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.income_dialog, null);
        setContentView(view);

        // Initialize UI components
        initializeViews();
        setupListeners();
    }

    /**
     * Initializes the UI components of the dialog
     */
    private void initializeViews() {
        txtDialogTitle = findViewById(R.id.txtDialogTitle);
        txtDialogDescription = findViewById(R.id.txtDialogDescription);
        txtStatus = findViewById(R.id.txtStatus);
        btnCancel = findViewById(R.id.btnCancel);
        btnActivate = findViewById(R.id.btnActivate);
        layoutStatus = findViewById(R.id.layoutStatus);
        btnCloseDialog = findViewById(R.id.btnCloseDialog);
        loadingBar = findViewById(R.id.progressBar);
    }

    /**
     * Sets up click listeners for the dialog buttons
     */
    private void setupListeners() {
        btnCloseDialog.setOnClickListener(v -> dismiss());

        btnCancel.setOnClickListener(v -> dismiss());

        btnActivate.setOnClickListener(v -> {
            // Show status section
            layoutStatus.setVisibility(View.VISIBLE);
            txtStatus.setText("Connecting to server...");

            // Disable activate button to prevent multiple clicks
            btnActivate.setEnabled(false);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            // Execute HTTP GET request
            new FetchDataTask().execute(apiUrl + employeeID + "&localDate=" + currentDate);
            Log.d(TAG, "url used: " + apiUrl + employeeID + "&localDate=" + currentDate);
        });
    }

    public EmployeeFinanceDialog customize(String title, String description) {
        if (title != null && !title.isEmpty()) {
            txtDialogTitle.setText(title);
        }

        if (description != null && !description.isEmpty()) {
            txtDialogDescription.setText(description);
        }

        return this;
    }

    private class FetchDataTask extends AsyncTask<String, Void, String> {
        private Exception exception = null;

        @Override
        protected void onPreExecute() {
            txtStatus.setText("Retrieving data...");
        }

        @Override
        protected String doInBackground(String... urls) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            StringBuilder response = new StringBuilder();

            try {
                URL url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            } catch (Exception e) {
                this.exception = e;
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (exception != null) {
                txtStatus.setText("Error: " + exception.getMessage());
                btnActivate.setEnabled(true);
                return;
            }

            processResponse(result);
        }
    }

    private void processResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);

            // Extract the message from the JSON object
            // {"object":"The old report is on its way"}
            String message = jsonResponse.optString("object", "No message provided");

            txtStatus.setText("Data received...");

            txtDialogDescription.setText(message);
            btnCancel.setVisibility(INVISIBLE);
            btnActivate.setVisibility(INVISIBLE);

            // Show notification
            Toast.makeText(getContext(), "Data received.", Toast.LENGTH_SHORT).show();

            loadingBar.postDelayed(() -> loadingBar.setVisibility(INVISIBLE) ,2000);

            // txtStatus.postDelayed(this::dismiss, 2000);

        } catch (JSONException e) {
            e.printStackTrace();
            txtStatus.setText("Error processing response: " + e.getMessage());
            btnActivate.setEnabled(true);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // No need to clean up connections as AsyncTask handles this
    }
}