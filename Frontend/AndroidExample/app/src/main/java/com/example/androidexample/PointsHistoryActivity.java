package com.example.androidexample;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidUI.ItemAnimationHelper;
import com.example.androidUI.TransactionAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PointsHistoryActivity extends AppCompatActivity {

    // UI components
    private TextView totalPointsValue;
    private TextView earnedThisMonthValue;
    private TextView redeemedThisMonthValue;
    private ImageButton backButton;
    private Spinner filterSpinner;
    private RecyclerView transactionsRecyclerView;

    // Data
    private List<Transaction> transactions = new ArrayList<>();
    private TransactionAdapter adapter;

    // Filter values
    private static final String[] FILTER_OPTIONS = {
            "All Activity", "Points Earned", "Points Redeemed", "Last 30 Days", "Last 90 Days"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_history);

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Set up spinner
        setupFilterSpinner();

        // Set up recycler view
        setupRecyclerView();

        // Load transaction data
        loadTransactionData();

        // Update summary data
        updateSummaryData();
    }

    private void initializeViews() {
        totalPointsValue = findViewById(R.id.totalPointsValue);
        earnedThisMonthValue = findViewById(R.id.earnedThisMonthValue);
        redeemedThisMonthValue = findViewById(R.id.redeemedThisMonthValue);
        backButton = findViewById(R.id.backButton);
        filterSpinner = findViewById(R.id.filterSpinner);
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());
    }

    private void setupFilterSpinner() {
        // Create adapter for spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, FILTER_OPTIONS);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapter
        filterSpinner.setAdapter(spinnerAdapter);

        // Set selection listener
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Filter transactions based on selection
                filterTransactions(FILTER_OPTIONS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupRecyclerView() {
        // Set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        transactionsRecyclerView.setLayoutManager(layoutManager);

        // Create and set adapter
        adapter = new TransactionAdapter(transactions);
        transactionsRecyclerView.setAdapter(adapter);
    }

    private void loadTransactionData() {
        // Show loading indicator
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading your transaction history...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Get userID from intent
        long userId = getIntent().getLongExtra("userID", -1);

        // If no valid userID, fallback to sample data
        if (userId <= 0) {
            progressDialog.dismiss();
            useSampleTransactionData();
            return;
        }

        // Create the URL with userID as parameter
        String url = "http://coms-3090-020.class.las.iastate.edu:8080/api/users/" + userId + "/transactions";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Clear existing transactions
                            transactions.clear();

                            // Get transactions array from response
                            JSONArray transactionsArray = response.getJSONArray("transactions");

                            // Parse summary data
                            int totalPoints = response.getInt("totalPoints");
                            int earnedThisMonth = response.getInt("earnedThisMonth");
                            int redeemedThisMonth = response.getInt("redeemedThisMonth");

                            // Parse each transaction
                            for (int i = 0; i < transactionsArray.length(); i++) {
                                JSONObject transactionObj = transactionsArray.getJSONObject(i);

                                String title = transactionObj.getString("title");
                                String date = transactionObj.getString("date");
                                int pointsValue = transactionObj.getInt("pointsValue");
                                int type = pointsValue > 0 ? Transaction.TYPE_EARNED : Transaction.TYPE_REDEEMED;

                                Transaction transaction = new Transaction(title, date, pointsValue, type);
                                transactions.add(transaction);
                            }

                            // Update UI with summary data
                            totalPointsValue.setText(String.format("%,d Points", totalPoints));
                            earnedThisMonthValue.setText(String.format("+%d", earnedThisMonth));
                            redeemedThisMonthValue.setText(String.format("-%d", redeemedThisMonth));

                            // Notify adapter of data change
                            adapter.notifyDataSetChanged();

                            // Use animation to dismiss loading indicator
                            ItemAnimationHelper.animateViewHeight(transactionsRecyclerView, true, 500, () -> {
                                progressDialog.dismiss();
                            });

                        } catch (JSONException e) {
                            Log.e("Transaction Error", "JSON Parsing Error: " + e.getMessage());
                            progressDialog.dismiss();
                            useSampleTransactionData();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Transaction Error", "Network Error: " + error.toString());
                        progressDialog.dismiss();
                        useSampleTransactionData();

                        // Show error toast
                        Toast.makeText(getApplicationContext(),
                                "Couldn't connect to the server. Using sample data instead.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Set a timeout for the request
        request.setRetryPolicy(new DefaultRetryPolicy(
                5000, // 10 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    // Helper method to use sample data
    private void useSampleTransactionData() {
        // Use sample data
        transactions.clear();
        transactions.addAll(getSampleTransactions());

        // Update summary with sample data (would be calculated from transactions in a real app)
        int totalPoints = 3245;
        int earnedThisMonth = 450;
        int redeemedThisMonth = 200;

        // Update UI
        totalPointsValue.setText(String.format("%,d Points", totalPoints));
        earnedThisMonthValue.setText(String.format("+%d", earnedThisMonth));
        redeemedThisMonthValue.setText(String.format("-%d", redeemedThisMonth));

        // Notify adapter
        adapter.notifyDataSetChanged();

        // Use animation to show the recycler view
        ItemAnimationHelper.animateViewHeight(transactionsRecyclerView, true, 500, null);
    }

    private void updateSummaryData() {
        // Calculate summary values
        int totalPoints = 3245; // Would normally be calculated from transactions
        int earnedThisMonth = 450; // Would normally be calculated from transactions
        int redeemedThisMonth = 200; // Would normally be calculated from transactions

        // Update UI
        totalPointsValue.setText(String.format("%,d Points", totalPoints));
        earnedThisMonthValue.setText(String.format("+%d", earnedThisMonth));
        redeemedThisMonthValue.setText(String.format("-%d", redeemedThisMonth));
    }

    private void filterTransactions(String filterOption) {
        // Clear current list
        transactions.clear();

        // Get all transactions
        List<Transaction> allTransactions = getSampleTransactions();

        // Apply filter
        switch (filterOption) {
            case "All Activity":
                transactions.addAll(allTransactions);
                break;
            case "Points Earned":
                for (Transaction transaction : allTransactions) {
                    if (transaction.getPointsValue() > 0) {
                        transactions.add(transaction);
                    }
                }
                break;
            case "Points Redeemed":
                for (Transaction transaction : allTransactions) {
                    if (transaction.getPointsValue() < 0) {
                        transactions.add(transaction);
                    }
                }
                break;
            case "Last 30 Days":
                // In a real app, you would filter by date
                // For now, just add all transactions
                transactions.addAll(allTransactions);
                break;
            case "Last 90 Days":
                // In a real app, you would filter by date
                // For now, just add all transactions
                transactions.addAll(allTransactions);
                break;
        }

        // Notify adapter
        adapter.notifyDataSetChanged();
    }

    private List<Transaction> getSampleTransactions() {
        List<Transaction> sampleTransactions = new ArrayList<>();

        // Add some sample transactions
        sampleTransactions.add(new Transaction(
                "Service Booking",
                "Apr 2, 2025",
                150,
                Transaction.TYPE_EARNED));

        sampleTransactions.add(new Transaction(
                "Referral Bonus",
                "Mar 28, 2025",
                100,
                Transaction.TYPE_EARNED));

        sampleTransactions.add(new Transaction(
                "Free Gel Polish Upgrade",
                "Mar 25, 2025",
                -250,
                Transaction.TYPE_REDEEMED));

        sampleTransactions.add(new Transaction(
                "Birthday Bonus",
                "Mar 15, 2025",
                200,
                Transaction.TYPE_EARNED));

        sampleTransactions.add(new Transaction(
                "Service Booking",
                "Mar 10, 2025",
                150,
                Transaction.TYPE_EARNED));

        sampleTransactions.add(new Transaction(
                "15% Off Any Service",
                "Feb 28, 2025",
                -500,
                Transaction.TYPE_REDEEMED));

        sampleTransactions.add(new Transaction(
                "Service Booking",
                "Feb 20, 2025",
                150,
                Transaction.TYPE_EARNED));

        sampleTransactions.add(new Transaction(
                "Service Booking",
                "Feb 5, 2025",
                150,
                Transaction.TYPE_EARNED));

        sampleTransactions.add(new Transaction(
                "Signup Bonus",
                "Jan 15, 2025",
                500,
                Transaction.TYPE_EARNED));

        return sampleTransactions;
    }

    /**
     * Transaction model class
     */
    public static class Transaction {
        public static final int TYPE_EARNED = 1;
        public static final int TYPE_REDEEMED = 2;

        private String title;
        private String date;
        private int pointsValue;
        private int type;

        public Transaction(String title, String date, int pointsValue, int type) {
            this.title = title;
            this.date = date;
            this.pointsValue = pointsValue;
            this.type = type;
        }

        public String getTitle() { return title; }
        public String getDate() { return date; }
        public int getPointsValue() { return pointsValue; }
        public int getType() { return type; }

        public String getFormattedPointsValue() {
            return (pointsValue > 0) ? "+" + pointsValue : String.valueOf(pointsValue);
        }
    }
}